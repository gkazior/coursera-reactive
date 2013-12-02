package nodescala

import scala.language.postfixOps
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit._
import scala.language.implicitConversions

object PostfixWs {
  // instantiation
  //val d1 = Duration(100, MILLISECONDS ) // from Long and TimeUnit
  val d2 = Duration(100, "millis") // from Long and String
  val d3 = 100 millis // implicitly from Long, Int or Double
  val d4 = Duration("1.2 ms") // from String
  val d5 = 20 seconds // implicitly from Long, Int or Double

  // pattern matching
  //val Duration(length, unit) = 5 millis

  sealed case class TheUnit(pow: Int)
  val minutes = TheUnit(60) //> minutes  : nodescala.PostfixWs.TheUnit = TheUnit(60)

  class MyDuration(seconds: Long) {
    def getMinutes() = { seconds * 60 }
    def getSeconds() = { seconds }
  }

  implicit class MyDecoratedDuration(d: MyDuration) {
    def getWords() = {
      if (d.getMinutes / 60 > 1) "hours"
      else if (d.getMinutes / 2 > 1) "minutes"
      else if (d.getSeconds / 30 > 1) "minute"
      else "seconds"
    }
  }

  object MyDuration {
    implicit def apply(name: String): MyDuration = {
      name match {
        case "hour" => new MyDuration(60 * 60)
        case "min"  => new MyDuration(60)
      }
    }
    implicit def apply(value: Int, unit: TheUnit): MyDuration = {
      new MyDuration(value * unit.pow)
    }
  }

  val timePeriod: MyDuration = MyDuration(23, minutes)
  //> timePeriod  : nodescala.PostfixWs.MyDuration = nodescala.PostfixWs$$anonfun
  //| $main$1$MyDuration$2@3a5f299d

  val timePeriod2: MyDuration = "min" //> timePeriod2  : nodescala.PostfixWs.MyDuration = nodescala.PostfixWs$$anonfu
  //| n$main$1$MyDuration$2@3da5205b
  val s1 = timePeriod2 getSeconds //> s1  : Long = 60
  val s2 = timePeriod2 getMinutes //> s2  : Long = 3600
  val s3 = timePeriod getSeconds //> s3  : Long = 1380

  timePeriod.getWords //> res0: String = hours
}