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
      assert(out.getSignal === o, s"truth table for |$i1|$i2| -> $o")
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

  // It should be very easy to write a generator and test demux of random size witj generator and ScalaCheck
  test("demux example") {
    def assertAllFalseButOne(wires: Seq[Wire], butTrueLine: Int, comment: String) {
      val outputs = wires.reverse.map((_.getSignal)).toArray

      for { i <- 0 to outputs.size - 1 } ({
        assert(outputs(i) === (butTrueLine == i), s"$comment. Error on: $i ")
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
      assertAllFalseButOne(out, -1, "All lines false when in false")
    })

    // When in is true then all lines false except only one with the given address
    in.setSignal(true)
    for {
      add0 <- Booleans
      add1 <- Booleans
      add2 <- Booleans
    } ({
      c(0).setSignal(add0)
      c(1).setSignal(add1)
      c(2).setSignal(add2)
      run
      val trueLine: Int = (if (add0) 4 else 0) + (if (add1) 2 else 0) + (if (add2) 1 else 0)
      assertAllFalseButOne(out, trueLine, s"Line $trueLine should be true and no other")
    })

  }

  test("demux with 2 controls test") {
    val in, c0, c1, out1, out2,  out3, out4  = new Wire
    demux(in, List(c1, c0), List(out4, out3, out2, out1))
    in.setSignal(true)
    c0.setSignal(false)
    c1.setSignal(true)
    run
    
    assert(out1.getSignal === false, "out1 signal")
    assert(out2.getSignal === false, "out2 signal")
    assert(out3.getSignal === true, "out3 signal")
    assert(out4.getSignal === false, "out4 signal")
  }
  
  test("demux medium") {
    val in, c0, c1, o0, o1, o2, o3 = new Wire
    val c = c1 :: c0 :: Nil
    val o = o3 :: o2 :: o1 :: o0 :: Nil
    demux(in, c, o)

    run
    assert(o0.getSignal === false, "1.1")
    assert(o1.getSignal === false, "1.2")
    assert(o2.getSignal === false, "1.3")
    assert(o3.getSignal === false, "1.4")

    in.setSignal(true)
    run
    assert(o2.getSignal === false, "2.3")
    assert(o1.getSignal === false, "2.2")
    assert(o0.getSignal === true , "2.1") // Here is the failure
    assert(o3.getSignal === false, "2.4")
    
    in.setSignal(true)
    c0.setSignal(true)
    run
    assert(o0.getSignal === false, "3.1")
    assert(o1.getSignal === true , "3.2")
    assert(o2.getSignal === false, "3.3")
    assert(o3.getSignal === false, "3.4")
  }
  
  test("demux large") {
    val in, c0, c1, c2, c3, o0, o1, o2, o3, o4, o5, o6, o7, o8, o9, o10, o11, o12, o13, o14, o15 = new Wire
    val c = c3 :: c2 :: c1 :: c0 :: Nil
    val o = o15 :: o14 :: o13 :: o12 :: o11 :: o10 :: o9 :: o8 :: o7 :: o6 :: o5 :: o4 :: o3 :: o2 :: o1 :: o0 :: Nil
    demux(in, c, o)

    run
    assert(o0.getSignal === false, 0)
    assert(o1.getSignal === false, 1)
    assert(o2.getSignal === false, 2)
    assert(o3.getSignal === false, 3)
    assert(o4.getSignal === false, "o4")
	assert(o5.getSignal === false, "o5")
	assert(o6.getSignal === false, "o6")
	assert(o7.getSignal === false, "o7")
	assert(o8.getSignal === false, "o8")
	assert(o9.getSignal === false, "o9")
	assert(o10.getSignal === false, "o10")
	assert(o11.getSignal === false, "o11")
	assert(o12.getSignal === false, "o12")
	assert(o13.getSignal === false, "o13")
	assert(o14.getSignal === false, "o14")
	assert(o15.getSignal === false, "o15")

    in.setSignal(true)
    run
    assert(o0.getSignal === true, 0)
    assert(o1.getSignal === false, 1)
    assert(o2.getSignal === false, 2)
    assert(o3.getSignal === false, 3)
    assert(o4.getSignal === false, "o4")
    assert(o5.getSignal === false, "o5")
	assert(o6.getSignal === false, "o6")
	assert(o7.getSignal === false, "o7")
	assert(o8.getSignal === false, "o8")
	assert(o9.getSignal === false, "o9")
	assert(o10.getSignal === false, "o10")
	assert(o11.getSignal === false, "o11")
	assert(o12.getSignal === false, "o12")
	assert(o13.getSignal === false, "o13")
	assert(o14.getSignal === false, "o14")
	assert(o15.getSignal === false, "o15")
    
    in.setSignal(true)
    c0.setSignal(true)
    c3.setSignal(true)
    run
    assert(o0.getSignal === false, 0)
    assert(o1.getSignal === false, 1)
    assert(o2.getSignal === false, 2)
    assert(o3.getSignal === false, 3)
    assert(o4.getSignal === false, "o4")
	assert(o5.getSignal === false, "o5")
	assert(o6.getSignal === false, "o6")
	assert(o7.getSignal === false, "o7")
	assert(o8.getSignal === false, "o8")
	assert(o9.getSignal === true, "o9")
	assert(o10.getSignal === false, "o10")
	assert(o11.getSignal === false, "o11")
	assert(o12.getSignal === false, "o12")
	assert(o13.getSignal === false, "o13")
	assert(o14.getSignal === false, "o14")
	assert(o15.getSignal === false, "o15")
  }
}
