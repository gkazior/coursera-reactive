/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package actorbintree

import akka.actor._
import scala.collection.immutable.Queue
import akka.event.LoggingReceive

object BinaryTreeSet {

  trait Operation {
    def requester: ActorRef
    def id: Int
    def elem: Int
  }

  trait OperationReply {
    def id: Int
  }

  /**
   * Request with identifier `id` to insert an element `elem` into the tree.
   * The actor at reference `requester` should be notified when this operation
   * is completed.
   */
  case class Insert(requester: ActorRef, id: Int, elem: Int) extends Operation

  /**
   * Request with identifier `id` to check whether an element `elem` is present
   * in the tree. The actor at reference `requester` should be notified when
   * this operation is completed.
   */
  case class Contains(requester: ActorRef, id: Int, elem: Int) extends Operation

  /**
   * Request with identifier `id` to remove the element `elem` from the tree.
   * The actor at reference `requester` should be notified when this operation
   * is completed.
   */
  case class Remove(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request to perform garbage collection*/
  case object GC
  case object GCFinished

  /**
   * Holds the answer to the Contains request with identifier `id`.
   * `result` is true if and only if the element is present in the tree.
   */
  case class ContainsResult(id: Int, result: Boolean) extends OperationReply

  /** Message to signal successful completion of an insert or remove operation. */
  case class OperationFinished(id: Int) extends OperationReply

}

class BinaryTreeSet extends Actor {
  import BinaryTreeSet._
  import BinaryTreeNode._

  def createRoot: ActorRef = context.actorOf(BinaryTreeNode.props(0, initiallyRemoved = true))

  var root = createRoot

  var pendingQueue = Queue.empty[Operation]
  var gcRequesters = Queue.empty[ActorRef]

  private def enqueueRequester(requester: ActorRef) = {
    gcRequesters = gcRequesters.enqueue(requester)
  }

  private def enqueueOperation(operation: Operation) = {
    pendingQueue = pendingQueue.enqueue(operation)
  }  

  def receive = normal

  /** Accepts `Operation` and `GC` messages. */
  val normal: Receive = LoggingReceive {
    case operation: Operation =>
      root ! operation
    case GC =>
      val newRoot = createRoot
      val requester = context.sender
      root ! CopyTo(treeNode = newRoot)
      enqueueRequester(requester)
      context.become(garbageCollecting(newRoot))
  }

  /**
   * Handles messages while garbage collection is performed.
   * `newRoot` is the root of the new binary tree where we want to copy
   * all non-removed elements into.
   */
  def garbageCollecting(newRoot: ActorRef): Receive = LoggingReceive {
    case GC => 
      val requester = context.sender
      enqueueRequester(requester) 
    case operation: Operation => 
      enqueueOperation(operation)
    case CopyFinished =>
      def processPendingOperations(rootToUpdate: ActorRef) { 
        pendingQueue foreach (rootToUpdate ! _)
        pendingQueue = Queue.empty[Operation]
      }
      def processGcRequesters {
        gcRequesters foreach (_ ! GCFinished)
        gcRequesters = Queue.empty[ActorRef]
      }
      root ! PoisonPill // Kill the old root
      root = newRoot // enable new root

      processPendingOperations(newRoot)
      processGcRequesters

      context.become(normal)
  }

}

object BinaryTreeNode {
  trait Position

  case object Left extends Position
  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)
  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode], elem, initiallyRemoved)
}

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {
  import BinaryTreeNode._
  import BinaryTreeSet._

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  // optional
  def receive = normal

  // optional
  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = LoggingReceive {
    case insert: Insert =>
      if (elem == insert.elem) {
        if (removed) removed = false
        insert.requester ! OperationFinished(insert.id)
      } else {
        val newElem = insert.elem
        val newPosition = if (newElem < elem) Left else Right
        val responsibleActor = subtrees.get(newPosition) match {
          case Some(actorRef) => actorRef
          case None =>
            val newActor = context.actorOf(props(newElem, initiallyRemoved = false), s"$newElem")
            subtrees += (newPosition -> newActor)
            newActor
        }
        responsibleActor ! insert
      }
    case remove: Remove =>
      if (elem == remove.elem) {
        removed = true
        remove.requester ! OperationFinished(remove.id)
      } else {
        val newPosition = if (remove.elem < elem) Left else Right
        subtrees.get(newPosition) match {
          case Some(actorRef) => actorRef ! remove
          case None           => remove.requester ! OperationFinished(remove.id)
        }
      }
    case contains: Contains =>
      if (elem == contains.elem) {
        contains.requester ! ContainsResult(contains.id, result = !removed)
      } else {
        val checkedElem = contains.elem
        val checkedPosition = if (checkedElem < elem) Left else Right

        subtrees.get(checkedPosition) match {
          case Some(actorRef) => actorRef ! contains // delegate the check
          case None           => contains.requester ! ContainsResult(contains.id, result = false) // or answer no
        }
      }
    case copyTo: CopyTo =>
      var expected = Set[ActorRef]()

      if (subtrees.get(Left).nonEmpty)  expected += subtrees.get(Left).get
      if (subtrees.get(Right).nonEmpty) expected += subtrees.get(Right).get

      // Notify sub-nodes
      expected foreach (_ ! copyTo)
      
      // when the item is removed then no need to send insert nor wait for confirmation
      if (!removed) {
        copyTo.treeNode ! Insert(requester = context.self, id = elem, elem = elem)
      }

      if (expected.isEmpty && removed) 
        context.parent ! CopyFinished
      else
        context.become(copying(expected, insertConfirmed = removed))

  }

  /**
   * `expected` is the set of ActorRefs whose replies we are waiting for,
   * `insertConfirmed` tracks whether the copy of this node to the new tree has been confirmed.
   */
  def copying(expected: Set[ActorRef], insertConfirmed: Boolean): Receive = LoggingReceive {
    case CopyFinished =>
      val newExpected = expected - context.sender
      if (newExpected.isEmpty && insertConfirmed) {
        context.parent ! CopyFinished
        context.become(normal)
      }
      else 
        context.become(copying(newExpected, insertConfirmed))

    case operationFinished: OperationFinished =>
      if (expected.isEmpty) {
        context.parent ! CopyFinished
        context.become(normal)
      }
      else 
        context.become(copying(expected, insertConfirmed = true))
  }
}

class BinaryTreeSetApp extends Actor {
  val tree = context.actorOf(Props[BinaryTreeSet], "APP")

  import BinaryTreeSet._


  tree ! Insert(context.self, 1, 100)
  tree ! Insert(context.self, 2, 101)
  tree ! Insert(context.self, 3, 102)
  tree ! Insert(context.self, 4, 100)

  tree ! Contains(context.self, 5, 100)
  tree ! Remove(context.self, 6, 100)

  tree ! Contains(context.self, 7, 100)
  tree ! Contains(context.self, 666, 105)

  // Empty bodies enables logging with LoggingReceive
  def receive() = LoggingReceive {
    case GCFinished =>
    case operationFinished: OperationFinished =>
    case containsResult: ContainsResult =>
      if (containsResult.id == 666) { // After the last 
        tree ! GC
        println(f"Wait for GC end")
        context.become(makeGc)
      } 
  }

  def makeGc() = LoggingReceive {
    case GCFinished =>
      println(f"GC end")
      tree ! Contains(context.self, 11, 101)
      tree ! Contains(context.self, 12, 102)
      tree ! Contains(context.self, 13, 100)
      context.become(receive)
  }
}