package nodescala

import scala.async.Async.{ async, await }
import scala.concurrent._
import scala.util._
import ExecutionContext.Implicits.global
import scala.util.Random
import scala.language.postfixOps
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit._
import scala.language.implicitConversions

object FutureToolsWs {
  def testDelay() {
    val p = Promise[String]
    def delay(t: Duration): Future[Unit] = {
      future {
        Thread.sleep(t.toMillis)
      }
    }
    println("Go. Wygraj w zawodzie")

    val f1: Future[Unit] = delay(200 milliseconds) andThen {
      case _ => {
        println("Hello after delay")
        p success "Done"
      }
    }
    println("After future!")

    // Wait for other threads
    Await.result(p.future, 5 seconds)
    println("Exit!")
  }

  def testCancellationToken() {
    val cts = CancellationTokenSource()
    val ct = cts.cancellationToken
    val p = Promise[String]()
    println("Starting to wait ...")

    p.future onSuccess {
      case value => println(f"The future is ready with [$value]")
    }

    async {
      while (ct.nonCancelled) {
        // do work
      }

      p.success("done")
    }
    println("Trying to unsunscribe ...")

    cts.unsubscribe()

    println(f"Now the cancelled is ${ct.isCancelled}")
    if (Await.result(p.future, 1 second) == "done") {
      println(f"Well done. Result of future is ${p.isCompleted} ")
    } else {
      println(f"Problem here. Result of future is ${p.isCompleted}")
    }

  }

  def testAny() {
    val p = Promise[String]

    val l2 = List(Future { 1 }, Future { 2 }, Future { throw new Exception })
    val l1 = List(Future { 1 }, Future { 2 }, Future { 3 })
    println("Starting the any!")
    Future.any(l2) onComplete {
      case Success(v) => { println(f"Success $v"); p.success("done") }
      case Failure(t) => { println(f"failure $t"); p.success("failed") }
    }
    val result = Await.result(p.future, 1 seconds)
    println(f"any finished! $result")
  }

  def all[T](fs: List[Future[T]]): Future[List[T]] = {
    val successful = Promise[List[T]]()
    successful.success(Nil)
    fs.foldRight(successful.future) { //
      (f, acc) => for { x <- f; xs <- acc } yield x :: xs
    }
  }
  def testAll() {
    val p = Promise[String]

    val l2 = List(Future { 1 }, Future { 2 }, Future { throw new Exception })
    val l1 = List(Future { 1 }, Future { 2 }, Future { 3 })
    println("Starting the any!")
    all(l1) onComplete {
      case Success(v) => { println(f"Success $v"); p.success("done") }
      case Failure(t) => { println(f"failure $t"); p.success("failed") }
    }
    val result = Await.result(p.future, 1 seconds)
    println(f"any finished! $result")
  }

  def testCancel() {
    val p = Promise[String]

    val l1 = List(Future { 1 }, Future { 2 }, Future { 3 }, Future { Thread.sleep(50000) })
    println("Starting the any!")

    all(l1) onComplete {
      case Success(v) => { println(f"Success $v"); p.success("done") }
      case Failure(t) => { println(f"failure $t"); p.success("failed") }
    }

    val result = Try(Await.result(p.future, 1 seconds))
    println(f"any finished! $result")
  } //

  def testTools() {
    val p = Promise[String]

    val l1 = List(Future { 1 }, Future { 2 }, Future { 3 }, Future { Thread.sleep(50000) })
    println("Starting the any!")

    //val futureToWait = p.future
    val futureToWait = l1(1).continue({
      case Success(value) => val myValue = "$value_gk_2"; println(s"The value: $myValue"); myValue
    }).continue({
      case Success(value) => Thread.sleep(1000); p success s"SEC[$value]"
    })

    val result = Try(Await.result(futureToWait, 100 milliseconds))
    println(f"any finished! $result")
  } //

  //testCancel

  //testAll
  //testAny

  //testDelay
  //testCancellationToken
  //testTools
}