package suggestions

import language.postfixOps
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Try, Success, Failure }
import rx.lang.scala._
import org.scalatest._
import gui._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class WikipediaApiTest extends FunSuite {

  object mockApi extends WikipediaApi {
    def wikipediaSuggestion(term: String) = Future {
      if (term.head.isLetter) {
        for (suffix <- List(" (Computer Scientist)", " (Footballer)")) yield term + suffix
      } else {
        List(term)
      }
    }
    def wikipediaPage(term: String) = Future {
      "Title: " + term
    }
  }

  import mockApi._

  test("WikipediaApi should make the stream valid using sanitized") {
    val notvalid = Observable("erik", "erik meijer", "martin")
    val valid = notvalid.sanitized

    var count = 0
    var completed = false

    val sub = valid.subscribe(
      term => {
        assert(term.forall(_ != ' '))
        count += 1
      },
      t => assert(false, s"stream error $t"),
      () => completed = true)
    assert(completed && count == 3, "completed: " + completed + ", event count: " + count)
  }

  test("WikipediaApi should correctly use concatRecovered") {
    val requests = Observable(1, 2, 3)
    val remoteComputation = (n: Int) => Observable(0 to n)
    val responses = requests concatRecovered remoteComputation
    val sum = responses.foldLeft(0) { (acc, tn) =>
      tn match {
        case Success(n) => acc + n
        case Failure(t) => throw t
      }
    }
    var total = -1
    val sub = sum.subscribe {
      s => total = s
    }
    assert(total == (1 + 1 + 2 + 1 + 2 + 3), s"Sum: $total")
  }

  test("Sanitizer tests") {
    def t(argument: String, expected: String) {
      assert(WikipediaUtils.sanitizer(argument) === expected, expected)
    }
    // `"erik", "erik meijer", "martin` should become `"erik", "erik_meijer", "martin"`
    t("erik", "erik")
    t("erik meijer", "erik_meijer")
    t("martin", "martin")

    t(" erik van _ gomes ", "_erik_van___gomes_")
  }

  import scala.collection._

  test("recovered simple") {
    val observed1 = mutable.Buffer[Try[Int]]()
    val o = ObservableOps(Observable(1, 2, 3)).recovered
    o.subscribe { observed1 += _ }
    assert(observed1 === Seq(Success(1), Success(2), Success(3)), observed1)
  }

  test("recovered error") {
    val exc = new RuntimeException("fake!")
    val observed1 = mutable.Buffer[Try[Int]]()
    val o = ObservableOps(Observable(1, 2, 3, 4, 5).map(o => if (o == 3) throw exc else o)).recovered
    o.subscribe { observed1 += _ }
    // No next after failure. Strange, but ok
    assert(observed1 === Seq(Success(1), Success(2), Failure(exc)), observed1)
  }
  
  test("recovered error [2]") {
    val exc = new RuntimeException("fake!")
    val observed1 = mutable.Buffer[Try[Int]]()
    val o = ObservableOps(Observable(1, 2) ++ Observable(exc) ++ Observable(4, 5)).recovered
    o.subscribe { observed1 += _ }
    // No next after failure. Strange, but ok
    assert(observed1 === Seq(Success(1), Success(2), Failure(exc)), observed1)
  }  

  test("recovered error [4]") {
    val baseO = Observable.interval(0.25.second).timedOut(1)
    val exc = new RuntimeException("exc!")
    val remoteComputation = (num: Long) => if (num != 2) num else throw exc
    
    val observed1 = mutable.Buffer[Try[Long]]()
    val o = ObservableOps(baseO.map(remoteComputation)).recovered
    o.subscribe { observed1 += _ }
    val blockingSeq = o.toBlockingObservable.toList.toSeq
    // No next after failure. Strange, but ok
    assert(observed1 === Seq(Success(0), Success(1), Failure(exc)), observed1)
    assert(observed1 === blockingSeq, observed1)
  }  
  
  test("timedOut simple") {
    val observed1 = mutable.Buffer[Int]()
    val observed2 = mutable.Buffer[Int]()
    val o = ObservableOps(Observable(1, 2, 3)).timedOut(3)

    o.subscribe { observed1 += _ }
    o.subscribe { observed2 += _ }

    assert(observed1 == Seq(1, 2, 3), observed1)
    assert(observed2 == Seq(1, 2, 3), observed2)
  }

  
  // Quite slow since uses delay 
  test("timedOut ticks") {
    val observed1 = mutable.Buffer[Long]()
    val observed2 = mutable.Buffer[Long]()
    val o = ObservableOps(Observable.interval(190 milliseconds)).timedOut(1)

    o.subscribe { observed1 += _ }
    o.subscribe { observed2 += _ }

    Thread.sleep(2000)

    assert(observed1 === Seq(0, 1, 2, 3, 4), observed1)
    assert(observed2 === Seq(0, 1, 2, 3, 4), observed2)
  }
  
  test("concatRecovered basics") {
    val observed1 = mutable.Buffer[Try[Int]]()

    //val o = ObservableOps(Observable(1, 2, 3, 4, 5)).concatRecovered(num => if (num != 4) Observable.just(num) else Observable.error(new Exception))
    val o = Observable(1, 2, 3).concatRecovered(num => Observable(num, num))
    o.subscribe { observed1 += _ }

    assert(observed1 === Seq(Success(1), Success(1), Success(2), Success(2), Success(3), Success(3)), observed1)

  }
  test("concatRecovered error") {
    val exc = new RuntimeException("fake!")

    val observed1 = mutable.Buffer[Try[Int]]()

    val o = ObservableOps(Observable(1, 2, 3, 4)).concatRecovered(num => if (num != 3) Observable(num, num) else Observable(exc))
    o.subscribe { observed1 += _ }

    assert(observed1 === Seq(Success(1), Success(1), Success(2), Success(2), Failure(exc), Success(4), Success(4)), observed1)

  }

  test("concatRecovered error in outer") {
    val exc = new RuntimeException("exc!")
    val excOuter = new RuntimeException("excOuter!")

    val observed1 = mutable.Buffer[Try[Int]]()

    val o = ObservableOps(
        Observable(1, 2, 3, 4, 5).map(x => if (x == 4) throw excOuter else x)).concatRecovered(num => if (num != 2) Observable(num, num) else Observable(exc))
    o.subscribe { observed1 += _ }

    assert(observed1 === Seq(Success(1), Success(1), Failure(exc), Success(3), Success(3), Failure(excOuter)), observed1)
  }
  
  test("test recovered by Matt Kowalczyk") {
    val requests = Observable(3, 2, 1)
    val comp = requests.map(i => i / (i - 1))
    val theList = comp.recovered.map(_.isFailure).toBlockingObservable.toList
    assert(theList === List(false, false, true))
  }
  
  test("concatRecovered error in outer tick") {
    val exc = new RuntimeException("exc!")
    val excOuter = new RuntimeException("excOuter!")

    val observed1 = mutable.Buffer[Try[Long]]()

    val o = ObservableOps(Observable.interval(0.23.second).timedOut(1).map(x => if (x == 3) throw excOuter else x)).concatRecovered(num => if (num != 1) Observable(num, num) else Observable(exc))
    o.subscribe { observed1 += _ }

    val blockingSeq = o.toBlockingObservable.toList.toSeq

    assert(observed1 === Seq(Success(0), Success(0), Failure(exc), Success(2), Success(2), Failure(excOuter)), observed1)
    assert(blockingSeq === observed1, blockingSeq)
  }

  test("Correctly compose the streams that have errors using concatRecovered") {
    val requests = Observable.interval(0.23.second).timedOut(1)
    val exception = new Exception("test")
    val remoteComputation = (num: Long) => if (num != 2) Observable(num) else Observable(exception)
    val responses = requests.concatRecovered(remoteComputation)
    val actual = responses.toBlockingObservable.toList
    val expected = List(Success(0), Success(1), Failure(exception), Success(3))
    assert(actual === expected, s"actual : $actual is not as expected : $expected")
  }
  
}