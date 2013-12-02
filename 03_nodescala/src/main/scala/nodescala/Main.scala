package nodescala

import scala.language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.async.Async.{ async, await }
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit._
import scala.language.implicitConversions

object Main {

  def main(args: Array[String]) {
    // TO IMPLEMENT
    // 1. instantiate the server at 8191, relative path "/test",
    //    and have the response return headers of the request
    val myServer = new NodeScala.Default(8191)
    val myServerSubscription = myServer.createListener("/test").start

    // TO IMPLEMENT
    // 2. create a future that expects some user input `x`
    //    and continues with a `"You entered... " + x` message
    val userInterrupted: Future[String] = {
      val p = Promise[String]
      future {
        p success "You entered... " + readLine
      }
      p.future
    }
    // TO IMPLEMENT
    // 3. create a future that completes after 20 seconds
    //    and continues with a `"Server timeout!"` message
    val timeOut: Future[String] = {
      val p = Promise[String]
      val duration: Duration = Duration("20 sec") // 20 seconds
      println("Starting the delay ...")
      Future.delay(duration) onSuccess {
        case _ => {
          println("Sending the timeout message...")
          p success "Server timeout"
        }
      }
      p.future
    }

    // TO IMPLEMENT
    // 4. create a future that completes when either 10 seconds elapse
    //    or the user enters some text and presses ENTER
    val terminationRequested: Future[String] = {
      Future.any(List(timeOut, userInterrupted))
    }

    // TO IMPLEMENT
    // 5. unsubscribe from the server
    terminationRequested onSuccess {
      case msg => {
        println("Time to close the server")
        myServerSubscription.unsubscribe
      }
    }
  }

}