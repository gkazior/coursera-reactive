package kvstore

import scala.concurrent.duration.DurationInt
import Persistence._
import Replicator._
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.PoisonPill
import akka.actor.Props
import akka.actor.Terminated
import kvstore.Arbiter._
import scala.language.postfixOps
import akka.actor.Stash
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy

object Replica {
  sealed trait Operation {
    def key: String
    def id: Long
  }
  case class Insert(key: String, value: String, id: Long) extends Operation
  case class Remove(key: String, id: Long) extends Operation
  case class Get(key: String, id: Long) extends Operation

  sealed trait OperationReply
  case class OperationAck(id: Long) extends OperationReply
  case class OperationFailed(id: Long) extends OperationReply
  case class GetResult(key: String, valueOption: Option[String], id: Long) extends OperationReply

  object RetryPersist

  object Test {
    object KillPersistence
    object KillReplicas
  }

  def props(arbiter: ActorRef, persistenceProps: Props): Props = Props(new Replica(arbiter, persistenceProps))
}

trait RetryMessage {
  private var retriedTimes: Int = 0
  val retryEnabled: Boolean
  /**
   * MaximumFailureCount is a limit for persistence actor. When any message is retried MaximumFailureCount or more than primary fails with stop
   *  Hard to say why but persistence does not generate Terminated message even if I watch it!
   */
  private val MaximumFailureCount = 10
  def retryFails(): Boolean = {
    if (retryEnabled) {
      retriedTimes += 1
      retriedTimes >= MaximumFailureCount
    } else
      return false
  }
}

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor {
  import Replica._
  import Replicator._
  import Persistence._
  import context.dispatcher

  var kv = Map.empty[String, String]
  // a map from secondary replicas to replicators
  var secondaries = Map.empty[ActorRef, ActorRef]
  // the current set of replicators
  var replicators = Set.empty[ActorRef]

  /**
   * ackActor - to whom the ack or failure is sent
   * ackMessage - acknowledge message (in the case of success). May be OperationAck or SnapshotAck
   * failMessage - failure message. May be OperationFailed or None
   *
   */
  case class RequestToProcess(key: String, valueOption: Option[String], id: Long, ackActor: ActorRef, ackMessage: Any, failMessage: Any, rollbackValue: Option[String], withTimeout: Boolean, var persisted: Boolean = false, var replicatorsToAck: Set[ActorRef] = Set.empty[ActorRef]) extends RetryMessage {
    val retryEnabled = withTimeout
    def markHandled() {
      requestToAck = requestToAck - id
    }
    def setReplicatorsToAck(replicators: Set[ActorRef]) {
      replicatorsToAck = replicators
    }
    def registerReplicator(replicator: ActorRef) {
      replicatorsToAck += replicator
    }
    def unregisterReplicator(replicator: ActorRef) {
      replicatorsToAck -= replicator
    }
    def sendFailedMessage() {
      if (failMessage != None)
        ackActor ! failMessage
    }
    def sendAckMessage() {
      if (ackMessage != None)
        ackActor ! ackMessage
    }
  }

  // Request waiting to be acknowledge from requestId to RequestToProcess 
  var requestToAck = Map.empty[Long, RequestToProcess]

  arbiter ! Join // Send join to arbiter who will send me JoinedPrimary or JoinedSecondary

  def receive = {
    case JoinedPrimary =>
      context.become(leader)
    case JoinedSecondary =>
      context.become(replica)
  }

  // Here is cancellable which sends RetryPersist
  val cancellable = context.system.scheduler.schedule(0 milliseconds,
    100 milliseconds,
    context.self,
    RetryPersist)

  override def postStop() {
    cancellable.cancel
  }

  private def makeNewPersistence() = {
    debug("making new persistence")
    val a = context.actorOf(persistenceProps, "persistence")
    var id: Long = 0

    kv foreach ((item) => { a ! Persist(item._1, Some(item._2), id); id += 1 })
    context.watch(a) // experiments with Termination
    a
  }

  var persistence: ActorRef = makeNewPersistence
  override val supervisorStrategy = OneForOneStrategy() {
    case _: Exception => SupervisorStrategy.Restart
  }
  private def debug(message: String) {
    //println("Replica   : " + message)
  }

  private def sendAcksIfReady() {
    val rqs = requestToAck
    rqs foreach ((item) => sendAcksIfReady(item._2))
    //rqs mapValues (sendAcksIfReady(_)) // TODO: Why it fails!
  }

  private def sendAcksIfReady(rq: RequestToProcess) {
    debug(s"sendAcksIfReady $rq")
    if (rq.persisted && rq.replicatorsToAck.isEmpty) {
      debug(s"sendAcksIfReady OK $rq")
      rq.markHandled
      rq.sendAckMessage
    }
  }

  private def replicateRequest(rq: RequestToProcess) {
    rq.setReplicatorsToAck(replicators)
    replicators foreach (_ ! Replicate(rq.key, rq.valueOption, rq.id))
  }

  private def persistRequest(key: String, valueOption: Option[String], id: Long, ackActor: ActorRef, ackMessage: Any, failMessage: Any, withTimeout: Boolean = true) {
    requestToAck = requestToAck + (id -> RequestToProcess(key, valueOption, id, ackActor, ackMessage, failMessage, kv.get(key), withTimeout = withTimeout))
    persistence ! Persist(key, valueOption, id)
  }

  // If the primary determines that this update has failed, it will then replicate the previous state for that key to all replicas, awaiting confirmation without a timeout.
  private def rollbackCurrentChange(rq: RequestToProcess) {
    // None for ack/fail response because we have already have answered failed and now we are trying to recover the previous state
    persistRequest(rq.key, rq.rollbackValue, rq.id, sender, None, None, withTimeout = false)
  }

  private def handleRetry() {
    // Retry not only because of persist!
    // When there is no answer from replicas the request may fail too, however the message is not send again
    val rqs = requestToAck
    rqs foreach ((item) => {
      val (id, rq) = item
      debug(f"Handling retry for ${rq}!")

      if (!rq.persisted) {
        persistence ! Persist(rq.key, rq.valueOption, rq.id)
      }

      if (rq.retryFails) {
        rq.markHandled
        rq.sendFailedMessage
        rollbackCurrentChange(rq)
      }
    })
  }

  // All that stuff for two lines below ;-)
  private def updateStore(key: String, valueOption: Option[String]) {
    valueOption match {
      case None        => kv = kv - key
      case Some(value) => kv = kv + (key -> value)
    }
  }

  private def handlePersistedOrReplicated(sender: ActorRef, id: Long, persisted: Boolean, replicated: Boolean) = {
    requestToAck.get(id) match {
      case None =>
        debug(s"No message to ack $id. Already acknovledged?") // Message duplicate?

      case Some(rq) =>
        assert(!(persisted && replicated))

        if (persisted) {
          updateStore(rq.key, rq.valueOption)
          rq.persisted = true
          replicateRequest(rq)
        }
        if (replicated)
          rq.unregisterReplicator(sender)

        sendAcksIfReady(rq)
    }
  }

  private def handleTerminatedPersistence(sender: ActorRef) = {
    persistence = makeNewPersistence
  }

  private def registerReplica(newReplica: ActorRef) = {
    val newReplicator: ActorRef = context.actorOf(Replicator.props(newReplica))
    context.watch(newReplicator)

    secondaries = secondaries + (newReplica -> newReplicator)
    replicators = replicators + newReplicator
    debug(s"================= Added new $newReplica replicator: $newReplicator")

    requestToAck foreach { _._2.registerReplicator(newReplicator) }
    var id: Long = 0
    kv foreach ((item) => { newReplicator ! Replicate(item._1, Some(item._2), id); id += 1 })
    //persistence ! InitialReplay(replayToReplicator = newReplicator)
    handleRetry
  }

  private def unregisterReplica(replica: ActorRef, killReplicator: Boolean) = {

    val replicatorOption = secondaries.get(replica)

    if (replicatorOption.nonEmpty) {
      val replicator = replicatorOption.get

      debug(s"Removing replicator for $replicator")
      requestToAck foreach (_._2.unregisterReplicator(replicator))
      replicators = replicators - replicator
      if (killReplicator) {
        context.unwatch(replicator)
        replicator ! PoisonPill
      }
    } else {
      assert(false, "The map has to contain the replicator!")
    }
    secondaries = secondaries - replica
    sendAcksIfReady
  }

  private def forgetDiedReplicator(replicator: ActorRef) = {
    debug(s"Forget died replicator $replicator")
    // Linear search in a hash table (however the table is small and the event rare)
    val replicaEntry = secondaries find (_._2 == replicator)
    if (replicaEntry.nonEmpty) {
      unregisterReplica(replicaEntry.get._1, killReplicator = false)
    } else {
      assert(false, "The map has to contain the replica!")
    }
  }

  val leader: Receive = {
    case Persisted(key, id)     => handlePersistedOrReplicated(sender, id, true, false)
    case Replicated(key, id)    => handlePersistedOrReplicated(sender, id, false, true)
    case RetryPersist           => handleRetry()
    case Insert(key, value, id) => persistRequest(key, Some(value), id, sender, OperationAck(id), OperationFailed(id))
    case Remove(key, id)        => persistRequest(key, None, id, sender, OperationAck(id), OperationFailed(id))
    case Get(key, id)           => sender ! GetResult(key, kv.get(key), id)
    case Terminated(actor) if actor == persistence =>
      handleTerminatedPersistence(actor)
    case Terminated(actor) =>
      forgetDiedReplicator(actor)
    case Test.KillPersistence =>
      persistence ! PoisonPill
    case Test.KillReplicas =>
      secondaries.keySet foreach (_ ! PoisonPill)
    case Replicas(replicas: Set[ActorRef]) =>
      val currentReplicas = secondaries.keySet
      val replicasToRemove = currentReplicas -- replicas - context.self
      val newReplicas = replicas -- currentReplicas - context.self

      replicasToRemove foreach (unregisterReplica(_, killReplicator = true))
      newReplicas foreach (registerReplica(_))

    // no acknowledge to arbiter is required
  }

  var expectedSeq: Option[Long] = None

  val replica: Receive = {
    case Persisted(key, id) => handlePersistedOrReplicated(sender, id, true, false)
    case RetryPersist       => handleRetry()

    case Get(key, id) =>
      sender ! GetResult(key, kv.get(key), id)

    case Snapshot(key, valueOption, seq) =>
      val expectedValue = expectedSeq.getOrElse(0L)

      math.signum(seq - expectedValue) match {
        case 0 => // exactly as expected   
          debug(s"Sending ack for ($key, $seq)")
          updateStore(key, valueOption)
          persistRequest(key, valueOption, seq, sender, SnapshotAck(key, seq), None)
          expectedSeq = Some(expectedValue + 1)
        //unstashAll
        case -1 => // old message
          sender ! SnapshotAck(key, seq)
        case 1 => // too fresh - do nothing  
        //stash for to new messages breaks case 4 which I believe is not fine      
      }

    //    case Terminated(actor) if actor == persistence =>
    //      handleTerminatedPersistence(actor)
    //      debug(s"================= TERMINATED in replica for $actor")

    case o: Operation =>
      OperationFailed(o.id)
  }

}
