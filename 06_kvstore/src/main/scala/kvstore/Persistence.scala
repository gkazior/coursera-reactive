package kvstore

import akka.actor.{ Props, Actor }
import scala.util.Random
import java.util.concurrent.atomic.AtomicInteger
import akka.actor.ActorRef
import kvstore.Replicator.Snapshot
import kvstore.Replicator.SnapshotAck


object Persistence {
  case class Persist(key: String, valueOption: Option[String], id: Long)
  case class Persisted(key: String, id: Long)
   
  class PersistenceException extends Exception("Persistence failure")

  def props(flaky: Boolean): Props = Props(classOf[Persistence], flaky)
}

class Persistence(flaky: Boolean) extends Actor {
  import Persistence._
  var log = List.empty[Persist]
  var replayPersistSender: Option[ActorRef] = None
  
 
  def receive = {
   
    case item@Persist(key, valueOption, id) =>
      if (!flaky || Random.nextBoolean()) {
        log = item :: log
        sender ! Persisted(key, id)
      } else {
        //context.stop(self)
        throw new PersistenceException
      }
  }

}
