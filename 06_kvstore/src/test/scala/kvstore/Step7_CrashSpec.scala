package kvstore

import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import Arbiter._
import Replicator._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Ignore
import akka.actor.PoisonPill
import kvstore.Persistence.Persisted
import kvstore.Persistence.Persist

@RunWith(classOf[JUnitRunner])
class Step7_CrashSpec extends TestKit(ActorSystem("Step7CrashSpec"))
  with FunSuite
  with BeforeAndAfterAll
  with ShouldMatchers
  with ImplicitSender
  with Tools
  with FlakySpec {

  override def afterAll(): Unit = {
    system.shutdown()
  }

 /*
 
  test("case1: Persistence dies in primary") {
    val arbiter = TestProbe()
    val persistence = TestProbe()
    val primary = system.actorOf(Replica.props(arbiter.ref, probeProps(persistence)), "case1-primary")
    val user = session(primary)

    arbiter.expectMsg(Join)
    arbiter.send(primary, JoinedPrimary)

    val ack1 = user.set("k1", "v1")
    val persistId = persistence.expectMsgPF() {
      case Persist("k1", Some("v1"), id) => id
    }

    // Persistence does not answer because it is dead!
    // !!! persistence.reply(Persisted("k1", persistId))

    primary ! Replica.Test.KillPersistence
    //expectTerminated(persistence.testActor)

    val persistId2 = persistence.expectMsgPF() {
      case Persist("k1", Some("v1"), id) => id
    }
    persistence.reply(Persisted("k1", persistId))

    user.waitAck(ack1)

  }
*/
  test("case2: Replicas die") {
    val arbiter = TestProbe()
    val primary = system.actorOf(Replica.props(arbiter.ref, Persistence.props(flaky = flakySpec)), "case2-primary")
    val user = session(primary)
    val secondary = TestProbe()

    arbiter.expectMsg(Join)
    arbiter.send(primary, JoinedPrimary)

    arbiter.send(primary, Replicas(Set(primary, secondary.testActor)))

    val ack1 = user.set("k1", "v1")
    secondary.expectMsg(Snapshot("k1", Some("v1"), 0L))
    secondary.reply(SnapshotAck("k1", 0L))
    user.waitAck(ack1)

    val ack2 = user.set("k1", "v2")
    secondary.expectMsg(Snapshot("k1", Some("v2"), 1L))
    // Killing the replica works the same as sending new replica
    //secondary.ref ! PoisonPill
    primary ! Replica.Test.KillReplicas

    //arbiter.send(primary, Replicas(Set(primary)))
    user.waitAck(ack2)
  }

  test("case3: Primary must start replication to new replicas (after persistence dies)") {
    val arbiter = TestProbe()
    val primary = system.actorOf(Replica.props(arbiter.ref, Persistence.props(flaky = true)), "case3-primary")
    val user = session(primary)
    val secondary = TestProbe()

    arbiter.expectMsg(Join)
    arbiter.send(primary, JoinedPrimary)

    user.setAcked("k1", "v1")

    arbiter.send(primary, Replicas(Set(primary, secondary.ref)))


    secondary.expectMsg(Snapshot("k1", Some("v1"), 0L))
    secondary.reply(SnapshotAck("k1", 0L))

    //primary ! Replica.Test.KillPersistence

    val ack1 = user.set("k1", "v2")
    secondary.expectMsg(Snapshot("k1", Some("v2"), 1L))
    secondary.reply(SnapshotAck("k1", 1L))
    user.waitAck(ack1)

    val ack2 = user.remove("k1")
    secondary.expectMsg(Snapshot("k1", None, 2L))
    secondary.reply(SnapshotAck("k1", 2L))
    user.waitAck(ack2)
  }

}