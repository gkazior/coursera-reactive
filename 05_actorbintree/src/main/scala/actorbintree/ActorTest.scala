package actorbintree

import scala.concurrent.duration.DurationInt
import scala.math.BigInt.int2bigInt

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Restart
import akka.actor.SupervisorStrategy.Stop
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import akka.event.LoggingReceive


object BankAccount {
  case class Deposit(amount: BigInt) {
    require(amount > 0)
  }
  case class Withdraw(amount: BigInt) {
    require(amount > 0)
  }
  case object Done
  case object Failed
}

object BankAccountBackup {
  object Backup
  def props(counter: Int): Props = Props(new BankAccountBackup(counter))
}

class BankAccountBackup(counter: Int) extends Actor {
  import BankAccountBackup._
  def receive = {
    case Backup => if (counter < 10) throw new IllegalStateException("BUM")
  }

}

class BankAccount extends Actor {
  import BankAccount._
  import BankAccountBackup._

  var balance = BigInt(0)

  var backupIdCounter = 0
  def nextId: Int = {
    backupIdCounter += 1
    backupIdCounter
  }

  var backupActor = makeBackupActor
  
  def makeBackupActor(): ActorRef = {
    val a = context.actorOf(props(nextId), s"backup$backupIdCounter")
    context.watch(a)
    a
  }
  def debug(message: String) {
    println("MALONE: " + message)
  }
  
  override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 1, withinTimeRange = 500 millisecond) {
    case _: IllegalStateException =>
       debug(" Got IllegalStateException")
       Stop 
    // Restart // Resume // Stop // Escalate //Restart
    //    case _: NullPointerException     => Resume
    //    case _: IllegalArgumentException => Stop
    //    case _: Exception                => Stop // Escalate
    case _                       => 
      debug("MALONE: _")
      Restart
  }

  def sendBackup = {
    (1 until 2).map((x) => backupActor ! Backup)    
  }
  
  def receive = {
    case Deposit(amount) =>
      sendBackup
      balance += amount
      sender ! Done

    case Withdraw(amount) if amount <= balance =>
      balance -= amount
      sender ! Done

    case Terminated(actor) =>
      debug(s"======================== Terminated: $actor")
      backupActor = makeBackupActor
      sendBackup

    case _ => sender ! Failed
  }
}

object WireTransfer {
  case class Transfer(from: ActorRef, to: ActorRef, amount: BigInt)
  case object Done
  case object Failed
}

class WireTransfer extends Actor {
  import WireTransfer._

  def receive = {
    case Transfer(from, to, amount) =>
      from ! BankAccount.Withdraw(amount)
      context.become(awaitWithdraw(to, amount, sender))
  }

  def awaitWithdraw(to: ActorRef, amount: BigInt, client: ActorRef): Receive = {
    case BankAccount.Done =>
      to ! BankAccount.Deposit(amount)
      context.become(awaitDeposit(client))
    case BankAccount.Failed =>
      client ! Failed
      context.stop(self)
  }
  def awaitDeposit(client: ActorRef): Receive = {
    case BankAccount.Done =>
      client ! Done
      context.stop(self)
  }
}

class ActorTest extends Actor {
  val accountA = context.actorOf(Props[BankAccount], "accountA")
  val accountB = context.actorOf(Props[BankAccount], "accountB")

  accountA ! BankAccount.Deposit(100)
  accountB ! BankAccount.Deposit(100)

  def receive() = LoggingReceive {
    case BankAccount.Done => transfer(50)
  }

  def transfer(amount: BigInt): Unit = {
    val transaction = context.actorOf(Props[WireTransfer], "transfer")
    transaction ! WireTransfer.Transfer(accountA, accountB, amount)
    context.become(LoggingReceive {
      case WireTransfer.Done =>
        println(s"Transfer done for $amount")
        context.stop(self)
      case WireTransfer.Failed =>
        println("failed!")
        context.stop(self)
    })
  }
}