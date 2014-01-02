package kvstore

import akka.actor.Props
import akka.actor.Actor
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.event.LoggingReceive
import scala.language.postfixOps
import kvstore.Persistence.Persist
import akka.actor.Terminated

object Replicator {
  // Replicate messages
  case class Replicate(key: String, valueOption: Option[String], id: Long)
  case class Replicated(key: String, id: Long)

  // Initial reply messages
  // 1. Replicator waits for Replay to initialize itself. It collects all changes for processing
  // 2. InitialReplay is sent by primary to persistence. 
  // 3. Persistence sends all persisted to replicator in Replay
  // 4. Replicator replicates all persisted (as Replicate) to replica
  // 5. When all persisted are acknowledged then replicator may return to its normal processing 
  //case class InitialReplay(replayToReplicator: ActorRef) // Sent when new replica is attached
  //case class Replay(persists: List[Persist])

  // Snapshot messages (by replica)
  case class Snapshot(key: String, valueOption: Option[String], seq: Long)
  case class SnapshotAck(key: String, seq: Long)

  def props(replica: ActorRef): Props = Props(new Replicator(replica))
}

class Replicator(val replica: ActorRef) extends Actor {
  import Replicator._
  import Replica._
  import context.dispatcher

  case class ReplicateRequests(actorAck: ActorRef, message: Replicate) 

  // map from sequence number to pair of sender and request
  var acks = Map.empty[Long, ReplicateRequests]
  var keys = Map.empty[String, Long] // From Key to Seq
  // a sequence of not-yet-sent snapshots (you can disregard this if not implementing batching)
  var pending = Vector.empty[Snapshot]

  object SendSnapshots

  val cancellable = context.system.scheduler.schedule(0 milliseconds,
    100 milliseconds,
    context.self,
    SendSnapshots)

  context.watch(replica)
  
  override def postStop() {
    cancellable.cancel
  }

  private def debug(message: String) {
    //println("Replicator: " + message)
  }

  private def handleNewReplicateRequest(sender: ActorRef, replicate: Replicate) {
    val snapshot = Snapshot(replicate.key, replicate.valueOption, replicate.id)
    val reqOption = acks.get(snapshot.seq)
    reqOption match {
      case None =>
        val req = ReplicateRequests(sender, replicate)
        val replicatedKey = keys.get(req.message.key)
        
        if (replicatedKey.nonEmpty) {
          acks = acks - replicatedKey.get // Forget ack for old value when I have new value for this
        }
        acks = acks + (snapshot.seq -> req)
        keys = keys + (snapshot.key -> snapshot.seq)
        pending = pending :+ snapshot

      case Some(req) =>
        debug("Possibly a duplicate request")
    }
  }

  private def markHandled(seq: Long) {
    pending = pending filter (_.seq != seq)
  }


  def receive: Receive = normalWork

  def normalWork: Receive = {
    case replicate: Replicate =>
      handleNewReplicateRequest(sender, replicate)

    case SnapshotAck(key, seq) =>
      acks.get(seq) match {
        case None =>
          debug("Already sent") // Already sent?

        case Some(replicateRequest) =>
          markHandled(replicateRequest.message.id)
          replicateRequest.actorAck ! Replicated(replicateRequest.message.key, replicateRequest.message.id)
      }

    case Terminated(_) =>
      context.stop(self)
      
    case SendSnapshots =>
      val snapshotsToSend = pending

      snapshotsToSend foreach ((snapshot) => {
        replica ! snapshot
      })
  }

}
