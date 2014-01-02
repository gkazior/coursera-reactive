package kvstore

import akka.testkit.{ TestProbe, TestKit, ImplicitSender }
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import akka.actor.ActorSystem
import scala.concurrent.duration._
import kvstore.Arbiter.{ JoinedSecondary, Join }
import kvstore.Persistence.{ Persisted, Persist }
import scala.util.Random
import scala.util.control.NonFatal
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class Step2_SecondarySpec extends TestKit(ActorSystem("Step2SecondarySpec"))
  with FunSuite
  with BeforeAndAfterAll
  with ShouldMatchers
  with ImplicitSender
  with Tools
  with FlakySpec {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("case1: Secondary (in isolation) should properly register itself to the provided Arbiter") {
    val arbiter = TestProbe()
    val secondary = system.actorOf(Replica.props(arbiter.ref, Persistence.props(flaky = flakySpec)), "case1-secondary")

    arbiter.expectMsg(Join)
  }

  def case2(flaky: Boolean, name: String) {
    import Replicator._

    val arbiter = TestProbe()
    val replicator = TestProbe()
    val secondary = system.actorOf(Replica.props(arbiter.ref, Persistence.props(flaky = flaky)), "case2-secondary_" + name)
    val client = session(secondary)

    arbiter.expectMsg(Join)
    arbiter.send(secondary, JoinedSecondary)

    client.get("k1") should be === None

    replicator.send(secondary, Snapshot("k1", None, 0L))
    replicator.expectMsg(SnapshotAck("k1", 0L))
    client.get("k1") should be === None

    replicator.send(secondary, Snapshot("k1", Some("v1"), 1L))
    replicator.expectMsg(SnapshotAck("k1", 1L))
    client.get("k1") should be === Some("v1")

    replicator.send(secondary, Snapshot("k1", None, 2L))
    replicator.expectMsg(SnapshotAck("k1", 2L))
    client.get("k1") should be === None
  }
  def case3(flaky: Boolean, name: String) {
    import Replicator._

    val arbiter = TestProbe()
    val replicator = TestProbe()
    val secondary = system.actorOf(Replica.props(arbiter.ref, Persistence.props(flaky = flaky)), "case3-secondary_" + name)
    val client = session(secondary)

    arbiter.expectMsg(Join)
    arbiter.send(secondary, JoinedSecondary)

    client.get("k1") should be === None

    replicator.send(secondary, Snapshot("k1", Some("v1"), 0L))
    replicator.expectMsg(SnapshotAck("k1", 0L))
    client.get("k1") should be === Some("v1")

    replicator.send(secondary, Snapshot("k1", None, 0L))
    replicator.expectMsg(SnapshotAck("k1", 0L))
    client.get("k1") should be === Some("v1")

    replicator.send(secondary, Snapshot("k1", Some("v2"), 1L))
    replicator.expectMsg(SnapshotAck("k1", 1L))
    client.get("k1") should be === Some("v2")

    replicator.send(secondary, Snapshot("k1", None, 0L))
    replicator.expectMsg(SnapshotAck("k1", 0L))
    client.get("k1") should be === Some("v2")
  }
  def case4(flaky: Boolean, name: String) {
    import Replicator._

    val arbiter = TestProbe()
    val replicator = TestProbe()
    val secondary = system.actorOf(Replica.props(arbiter.ref, Persistence.props(flaky = flaky)), "case4-secondary_" + name)
    val client = session(secondary)

    arbiter.expectMsg(Join)
    arbiter.send(secondary, JoinedSecondary)

    client.get("k1") should be === None

    replicator.send(secondary, Snapshot("k1", Some("v1"), 1L))
    replicator.expectNoMsg(300.milliseconds)
    client.get("k1") should be === None

    replicator.send(secondary, Snapshot("k1", Some("v2"), 0L))
    replicator.expectMsg(SnapshotAck("k1", 0L))
    client.get("k1") should be === Some("v2")

  }
  
  test("case2: Secondary (in isolation) must handle Snapshots") {
    case2(false, "case2")
  }
  
  test("case2+flaky", flakyTag) {
    case2(flakySpec, "case2+flaky")
  }

  test("case3: Secondary should drop and immediately ack snapshots with older sequence numbers") {
    case3(false, "case3")
  }
  test("case3+flaky", flakyTag) {
    case3(flakySpec, "case3+flaky")
  }

  test("case4: Secondary should drop snapshots with future sequence numbers") {
    case4(false, "case4")
  }

  test("case4+flaky", flakyTag) {
    case4(flakySpec, "case4+flaky")
  }
}