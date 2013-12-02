package nodescala

import scala.util._
import scala.concurrent._
import ExecutionContext.Implicits.global

object PromiseApp extends App {
  override def main(args: Array[String]) {
    def print(msg: String) = {
      System.out.println(msg)
    }
    val howManyItems = promise[Int]
    val thePromisedList = promise[List[String]]

    howManyItems.future.onSuccess {
      case itemNo => {
        print("Started the computation after getting the promised value")
        Thread.sleep(1000)
        print("Finished sleep")
        var res: List[String] = Nil
        (1 until itemNo) foreach {
          (i) => { res = s"item no: $i" :: res } 
        }
        thePromisedList success res
      }
    }

    howManyItems.future.onFailure {
      case t => {
        print("On failure")
        List(s"Failure: $t")
      }
    }

    future {
      Thread.sleep(1000)
      if (true) //new Random().nextBoolean())
        howManyItems success 10 // try passing non boolean value here
      else
        howManyItems failure new Exception
    }

    thePromisedList.future onFailure {
      case item => println(s"onFailure: Item is: $item")
    }

    thePromisedList.future onComplete {
      case Success(res) => for (item <- res) print(s"onComplete: Found the item: $item")
      case Failure(t)   => println("onComplete: An error has occured: " + t.getMessage)
    }

    print("Starting to wait for futures")
    Thread.sleep(10000)
    print("Exiting in a moment")

  }
}
object FutureApp extends App {
  override def main(args: Array[String]) {
    def print(msg: String) = {
      System.out.println(msg)
    }
    val fok: Future[List[String]] = future {
      print("Started the computation")
      Thread.sleep(1000)
      print("Finished")
      List("DONE", "Everything", "Fine")
    }

    fok onComplete {
      case item => println(s"Item is: $item")
    }

    fok onComplete {
      case Success(res) => for (item <- res) print(s"Found the item: $item")
      case Failure(t)   => println("An error has occured: " + t.getMessage)
    }

    print("Starting to wait for futures")
    Thread.sleep(10000)
    print("Exiting in a moment")

  }
}