package suggestions

import scala.collection._
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Try, Success, Failure }
import scala.swing.event.Event
import scala.swing.Reactions.Reaction
import rx.lang.scala._
import org.scalatest._
import gui._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SwingApiTest extends FunSuite {

  object swingApi extends SwingApi {
    class ValueChanged(val textField: TextField) extends Event

    object ValueChanged {
      def unapply(x: Event) = x match {
        case vc: ValueChanged => Some(vc.textField)
        case _                => None
      }
    }

    class ButtonClicked(val source: Button) extends Event

    object ButtonClicked {
      def unapply(x: Event) = x match {
        case bc: ButtonClicked => Some(bc.source)
        case _                 => None
      }
    }

    class Component {
      private val subscriptions = mutable.Set[Reaction]()
      def subscribe(r: Reaction) {
        subscriptions add r
      }
      def unsubscribe(r: Reaction) {
        subscriptions remove r
      }
      def publish(e: Event) {
        for (r <- subscriptions) r(e)
      }
    }

    class TextField extends Component {
      private var _text = ""
      def text = _text
      def text_=(t: String) {
        _text = t
        publish(new ValueChanged(this))
      }
    }

    class Button extends Component {
      def click() {
        publish(new ButtonClicked(this))
      }
    }
  }

  import swingApi._
  import observablex._

  test("SwingApi should emit text field values to the observable") {
    val textField = new swingApi.TextField
    val values = textField.textValues

    val observed = mutable.Buffer[String]()
    val sub = values subscribe {
      observed += _
    }

    // write some text now
    textField.text = "T"
    textField.text = "Tu"
    textField.text = "Tur"
    textField.text = "Turi"
    textField.text = "Turin"
    textField.text = "Turing"

    assert(observed == Seq("T", "Tu", "Tur", "Turi", "Turin", "Turing"), observed)
  }

  test("SwingApi should unsubscribe") {
    val textField = new swingApi.TextField
    val values = textField.textValues

    val observed = mutable.Buffer[String]()
    val sub = values subscribe {
      observed += _
    }

    // write some text now
    textField.text = "T"
    textField.text = "Tu"
    textField.text = "Tur"
    textField.text = "Turi"
    sub.unsubscribe

    textField.text = "Turin"
    textField.text = "Turing"

    assert(observed == Seq("T", "Tu", "Tur", "Turi"), observed)
  }

  test("SwingApi should work for many subscribers") {
    val textField = new swingApi.TextField
    val values = textField.textValues

    val observed = mutable.Buffer[String]()
    val observed2 = mutable.Buffer[String]()
    val sub = values subscribe {
      observed += _
    }

    // write some text now
    textField.text = "T"
    textField.text = "Tu"

    val sub2 = values subscribe {
      observed2 += _
    }

    textField.text = "Tur"
    textField.text = "Turi"

    sub.unsubscribe

    textField.text = "Turin"

    sub2.unsubscribe

    textField.text = "Turing"

    assert(observed == Seq("T", "Tu", "Tur", "Turi"), observed)
    assert(observed2 == Seq("Tur", "Turi", "Turin"), observed2)
  }

  import scala.concurrent._
  import scala.language.postfixOps
  import scala.concurrent.duration._

  test("ObservableEx basics") {
    val observed1 = mutable.Buffer[Int]()
    val observed2 = mutable.Buffer[Int]()
    val myOnNext1 = (x: Int) => { observed1 += x }
    val myOnNext2 = (x: Int) => { observed2 += x }

    val f = future { 4711 }
    val o = ObservableEx(f)
    
    val s1 = o.subscribe(myOnNext1(_))
    Await.ready(f, 100 milliseconds)

    s1.unsubscribe

    val s2 = o.subscribe(myOnNext2(_))

    assert(observed1 === Seq(4711), observed1)
    assert(observed2 === Seq(4711), observed2)
  }

  test("ObservableEx errors") {
    val exception = new RuntimeException("Ble")
    val observed1 = mutable.Buffer[String]()

    val f = future { throw exception }
    val o = ObservableEx(f)
    
    val s1 = o.subscribe((x: Int) => { fail }, (t: Throwable) => {observed1 += t.getMessage()}, () => {})
    
    Try(Await.ready(f, 100 milliseconds))
    
    s1.unsubscribe

    assert(observed1 === Seq("Ble"), observed1)
  }

  test("ObservableShortSeq simple test") {
    val observed1 = mutable.Buffer[Int]()
    val myOnNext1 = (x: Int) => { observed1 += x }

    val o = ObservableShortSeq(List(1,2,3))
    val s1 = o.subscribe(myOnNext1(_))
    s1.unsubscribe

    assert(observed1 === Seq(1, 2, 3), observed1)
  }
}