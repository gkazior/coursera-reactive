package simulations

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CircuitSuite extends CircuitSimulator with FunSuite {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5

  type BB2B = (Boolean, Boolean) => Boolean
  type WWW2 = (Wire, Wire, Wire) => Unit

  val Booleans = List(false, true)

  def testGate(refFunction: BB2B, gate: WWW2) {
    val in1, in2, out = new Wire
    gate(in1, in2, out)

    def testTruth(i1: Boolean, i2: Boolean, o: Boolean) {
      in1.setSignal(i1)
      in2.setSignal(i2)
      run
      assert(out.getSignal === o, "truth table for |" + i1 + "|" + i2 + "| -> " + o)
    }

    for {
      i1 <- Booleans
      i2 <- Booleans
    } ({
      testTruth(i1, i2, refFunction(i1, i2))
    })

  }

  test("andGate example") {
    testGate(_ && _, andGate)
  }

  test("orGate2 example") {
    testGate(_ || _, orGate2)
  }

  test("orGate example") {
    testGate(_ || _, orGate)
  }

  test("demux example") {
    def assertAllFalseButOne(wires: Seq[Wire], butTrueLine: Int, comment: String) {
      val outputs = wires map (_.getSignal) toArray

      for { i <- 0 to outputs.size - 1 } ({
        assert(outputs(i) === (butTrueLine == i), comment + " " + i + " is not false")
      })
    }
    
    val in = new Wire
    val c = Array.fill(3) { new Wire }
    val out = Array.fill(8) { new Wire }

    demux(in, c.toList, out.toList)

    in.setSignal(false)

    // No matter what address lines are (c) when in is false then all out false
    for {
      add1 <- Booleans
      add2 <- Booleans
      add3 <- Booleans
    } ({
      c(0).setSignal(add1)
      c(1).setSignal(add2)
      c(2).setSignal(add3)
      run
      assertAllFalseButOne(out, -1, "In false")
    })

    // When in is true then all lines false except only one with the given address
    in.setSignal(true)
    for {
      add1 <- Booleans
      add2 <- Booleans
      add3 <- Booleans
    } ({
      c(0).setSignal(add1)
      c(1).setSignal(add2)
      c(2).setSignal(add3)
      run
      val trueLine: Int = (if (add1) 1 else 0) + (if (add2) 2 else 0) + (if (add3) 4 else 0)
      assertAllFalseButOne(out, trueLine, "In false")
    })

  }

}
