package nodescala

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.Random
import scala.language.postfixOps
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit._
import scala.language.implicitConversions

//http://www.srirangan.net/2013-01-controlling-flow-with-scala-futures
object FutureWs {
  val tryDivideByZeroAgain = future {
    Thread.sleep(1000)
    1 / 0
  } recover {
    case e: ArithmeticException => "Infinity"
  }                                               //> tryDivideByZeroAgain  : scala.concurrent.Future[Any] = scala.concurrent.impl
                                                  //| .Promise$DefaultPromise@707561aa

  tryDivideByZeroAgain onSuccess {
    case e => Console.println("onSuccess:" + e)
  }
  tryDivideByZeroAgain onFailure {
    case e => Console.println("onFailure:" + e)
  }

  // Or maybe future f1 must fallback to future f2?

  val f1 = future {
    Thread.sleep(500)
    1 / 0
  }                                               //> f1  : scala.concurrent.Future[Int] = scala.concurrent.impl.Promise$DefaultPr
                                                  //| omise@437a7f13

  val f2 = future {
    Thread.sleep(500)
    "Infinity"
  }                                               //> f2  : scala.concurrent.Future[String] = scala.concurrent.impl.Promise$Defaul
                                                  //| tPromise@156fb054

  f1 fallbackTo f2 onSuccess {
    case v => Console.println(v)
  }

  Console.println("Try dividing by zero, fallback to another future..")
                                                  //> Try dividing by zero, fallback to another future..

  Thread.sleep(2000)                              //> Infinity
                                                  //| onSuccess:Infinity

  // output
  // Try dividing by zero, fallback to another future..
  // Infinity

  // `promises` can be used to compose type safe futures

  val willYouMarryMe = promise[Boolean]           //> willYouMarryMe  : scala.concurrent.Promise[Boolean] = scala.concurrent.impl
                                                  //| .Promise$DefaultPromise@eb5a4f6

  willYouMarryMe.future onSuccess {
    case true => Console.println("Yes! :D")
    case false => Console.println("No :(")
  }

  willYouMarryMe.future onFailure {
    case no => Console.println(s"No :(, details $no" )
  }

  val isSheDecided = willYouMarryMe.isCompleted   //> isSheDecided  : Boolean = false
  val a = willYouMarryMe.completeWith(future {
    Console.println("I have to think for 1 second")
    Future.delay(5 seconds)
    Console.println("I have got bad news")
    false
  })                                              //> I have to think for 1 second
                                                  //| a  : nodescala.FutureWs.willYouMarryMe.type = scala.concurrent.impl.Promise
                                                  //| $DefaultPromise@eb5a4f6

  future {
    Console.println("I have to think for 2 seconds and the use a coin")
    Future.delay(1 seconds)
    Console.println("Here is your answer")

    if (new Random().nextBoolean())
      willYouMarryMe success true // try passing non boolean value here
    else
      willYouMarryMe failure new Exception
  }                                               //> res0: scala.concurrent.Future[scala.concurrent.Promise[Boolean]] = scala.co
                                                  //| ncurrent.impl.Promise$DefaultPromise@678aa6a7

  val working = Future.run() { ct =>
    Future {
      while (ct.nonCancelled) {
        Thread.sleep(1)
        println("working")
      }
      println("done")
    }
  }                                               //> I have to think for 2 seconds and the use a coin
                                                  //| working  : nodescala.Subscription = nodescala.package$CancellationTokenSour
                                                  //| ce$$anon$1@24c67c0f
  Future.delay(5 seconds) onSuccess {
    case _ => working.unsubscribe()
  }                                               //> working
                                                  //| working
                                                  //| working
                                                  //| working
                                                  //| working
                                                  //| working
                                                  //| working
                                                  //| Here is your answer
                                                  //| I have got bad news
                                                  //| No :(
                                                  //| working
                                                  //| working
                                                  //| working
                                                  //| working
                                                  //| working
                                                  //| working
}