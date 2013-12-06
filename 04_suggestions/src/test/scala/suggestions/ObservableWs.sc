package suggestions

import scala.collection._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Try, Success, Failure }
import scala.swing.event.Event
import scala.swing.Reactions.Reaction
import org.scalatest._
import scala.async.Async.{ async, await }
import scala.concurrent._
import scala.util._
import ExecutionContext.Implicits.global
import scala.util.Random
import scala.language.postfixOps
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit._
import rx.lang.scala._
import rx.lang.scala.subjects._
import rx.lang.scala.Observable
import rx.lang.scala.subscriptions.Subscription
import rx.lang.scala.Observer
import observablex._
import gui._

object ObservableWs {
  val myOnNext = (x: Int) => { println(s"The value is: $x") }
                                                  //> myOnNext  : Int => Unit = <function1>
  val myOnNextLong = (x: Long) => { println(s"The value is: $x") }
                                                  //> myOnNextLong  : Long => Unit = <function1>

  def never(): Observable[Nothing] = Observable[Nothing]((observer: Observer[Nothing]) => {
    Subscription {}
  })                                              //> never: ()rx.lang.scala.Observable[Nothing]

  def unlimitedConstant[T](value: T): Observable[T] = Observable[T]((observer: Observer[T]) => {
    observer.onNext(value)
    Subscription {}
  })                                              //> unlimitedConstant: [T](value: T)rx.lang.scala.Observable[T]

  var myCounter = 1                               //> myCounter  : Int = 1
  def incObservable(): Observable[Int] = Observable[Int]((observer: Observer[Int]) => {

    if (myCounter >= 100) observer.onCompleted
    else {
      myCounter += 1
      observer.onNext(myCounter)
    }
    Subscription {
    }
  })                                              //> incObservable: ()rx.lang.scala.Observable[Int]

  def testTicks() {
    val ticks: Observable[Long] = Observable.interval(1 seconds)
    val evens: Observable[Long] = ticks.filter(s => s % 2 == 0)
    val bufs: Observable[Seq[Long]] = ticks.buffer(2, 1)
    val s = bufs.subscribe(b => println(b))
    readLine()
    s.unsubscribe
  }                                               //> testTicks: ()Unit

  def testRange() {
    val o: Observable[Long] = Observable(1, 2, 3, 10)
    val s = o.subscribe(b => println(b))
  }                                               //> testRange: ()Unit

  def testMap() {
    val p = Promise[String]
    val f = Future { 5 }
    val f2 = f.map(_ + 15)
    val f3 = f2.map((value) => {
      val result = s"I have received $value"
      p.success(result)
      result
    })
    println(Await.result(p.future, 100 milliseconds))
  }                                               //> testMap: ()Unit

  def testReplayChannel() {
    val channel = ReplaySubject[Int]
    val a = channel.subscribe(x => println(s"a: $x"))
    val b = channel.subscribe(x => println(s"b: $x"))
    channel.onNext(42)
    a.unsubscribe()
    channel.onNext(4711)
    channel.onCompleted()
    val c = channel.subscribe(x => println(s"c: $x"))
    val a2 = channel.subscribe(x => println(s"a2: $x"))
    channel.onNext(13)
  }                                               //> testReplayChannel: ()Unit
  def testMyObservables() {
    val o1 = unlimitedConstant(5).subscribe(x => println(s"The value is: $x"))

    val o2 = Observable(1, 2, 3, 4, 5)
    o2.subscribe(x => println(s"The value is: $x"))
    o2 filter (_ % 2 == 0) subscribe (x => println(s"The value is: $x"))
    o2 map (_ * 2) subscribe (myOnNext(_))

  }                                               //> testMyObservables: ()Unit

  def delay(millis: Int, result: Int): Future[Int] = {
    future {
      Try(Await.ready(Promise().future, 10 milliseconds))
      result
    }
  }                                               //> delay: (millis: Int, result: Int)scala.concurrent.Future[Int]
  def testFutureObservable() {
    val f1: Future[Int] = future { 123 }
    val f2 = delay(3, 4711)

    f2.onComplete {
      case value => println(s"Found the $value from f2")
    }

    f1.onSuccess {
      case value => println(s"Found the $value from f1")
    }
    val o = ObservableEx(f1)
    o.subscribe(myOnNext(_))

  }                                               //> testFutureObservable: ()Unit
  def testObservableInterval() {
    val o = Observable.interval(1 second)

    val s = o.subscribe(myOnNextLong(_))

    readLine()

    s.unsubscribe
  }                                               //> testObservableInterval: ()Unit

  /*
  TODO: Fix the function: Not a member even if it is an implicit class
  def testRecovered() {
    val o = Observable(1, 2, 3).concatRecovered(num => Observable(num, num))
    o.subscribe(x => println(s"The cr: $x")) //
  }
  */

  
  //testTicks
  //testFutureObservable
  //testMap
  //testRange
  //testReplayChannel
}