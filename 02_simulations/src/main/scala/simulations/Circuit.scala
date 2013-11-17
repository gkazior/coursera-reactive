package simulations

import common._

class Wire {
  private var sigVal = false
  private var actions: List[Simulator#Action] = List()

  def getSignal: Boolean = sigVal

  def setSignal(s: Boolean) {
    if (s != sigVal) {
      sigVal = s
      actions.foreach(action => action())
    }
  }

  def addAction(a: Simulator#Action) {
    actions = a :: actions
    a()
  }
  override def toString() = {
    getSignal.toString
  }
}

abstract class CircuitSimulator extends Simulator {

  val InverterDelay: Int
  val AndGateDelay: Int
  val OrGateDelay: Int

  def probe(name: String, wire: Wire) {
    wire addAction {
      () =>
        afterDelay(0) {
          println(
            "  " + currentTime + ": " + name + " -> " + wire.getSignal)
        }
    }
  }

  def inverter(input: Wire, output: Wire) {
    def invertAction() {
      val inputSig = input.getSignal
      afterDelay(InverterDelay) { output.setSignal(!inputSig) }
    }
    input addAction invertAction
  }

  def andGate(a1: Wire, a2: Wire, output: Wire) {
    def andAction() {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(AndGateDelay) { output.setSignal(a1Sig & a2Sig) }
    }
    a1 addAction andAction
    a2 addAction andAction
  }

  //
  // to complete with orGates and demux...
  //

  def orGate(a1: Wire, a2: Wire, output: Wire) {
    def orAction() {
      val a1Sig = a1.getSignal
      val a2Sig = a2.getSignal
      afterDelay(OrGateDelay) { output.setSignal(a1Sig | a2Sig) }
    }
    a1 addAction orAction
    a2 addAction orAction
  }

  def orGate2(a1: Wire, a2: Wire, output: Wire) {
    val outputToInverter, in1inverter, in2inverter = new Wire
    inverter(a1, in1inverter)
    inverter(a2, in2inverter)
    andGate(in1inverter, in2inverter, outputToInverter)
    inverter(outputToInverter, output)
  }

  def andGate(a: List[Wire], output: Wire) {
    def helperAnd(input: Wire, list: List[Wire], output: Wire): Unit = {
      list match {
        case head :: Nil => andGate(input, head, output)
        case head :: tail => {
          val newWire = new Wire
          andGate(input, head, newWire)
          helperAnd(newWire, tail, output)
        }
      }
    }
    if (a.size < 2) throw new IllegalStateException("AndGate should have at least two inputs")

    helperAnd(a.head, a.tail, output)
  }

  def getAndWires(first: Wire, address: Int, pos: List[Wire], neg: List[Wire], acc: List[Wire]): List[Wire] = {
    pos match {
      case Nil => first :: acc.reverse
      case head :: tail => {
        val myAddress = address & 1
        val myWire = if (myAddress == 1) head else neg.head
        getAndWires(first, address >> 1, pos.tail, neg.tail, myWire :: acc)
      }
    }
  }

  // Well, my implementation should be recursive and base on halfAdder however I forgot to watch instruction and remember a bit how demux works 
  def demux(in: Wire, c: List[Wire], out: List[Wire]) {
    val addCnt = c.size // Count of address lines
    val negativeWires = Array.fill(addCnt) { new Wire }
    val positiveWires = c

    val zippedAdd = c.zipWithIndex // zipped address lines

    // wire between address line and inverter 
    zippedAdd.map((pair) => (inverter(pair._1, negativeWires(pair._2))))

    for ((positiveInput, idx) <- c.view.zipWithIndex) {
      inverter(positiveInput, negativeWires(idx))
    }

    val address = 0
    for ((output, address) <- out.view.zipWithIndex) {
      // Connect inverters to the first and gate
      zippedAdd.foreach((pair) => {
        andGate(getAndWires(in, address, positiveWires, negativeWires.view.toList, Nil), output)
      })
    }
  }
}

object Circuit extends CircuitSimulator {
  val InverterDelay = 1
  val AndGateDelay = 3
  val OrGateDelay = 5

  def andGateExample {
    val in1, in2, out = new Wire
    andGate(in1, in2, out)
    probe("in1", in1)
    probe("in2", in2)
    probe("out", out)
    in1.setSignal(false)
    in2.setSignal(false)
    run

    in1.setSignal(true)
    run

    in2.setSignal(true)
    run
  }

  //
  // to complete with orGateExample and demuxExample...
  //

  def orGateExample {
    val in1, in2, out = new Wire
    orGate(in1, in2, out)
    probe("in1", in1)
    probe("in2", in2)
    probe("out", out)
    in1.setSignal(false)
    in2.setSignal(false)
    run

    in1.setSignal(true)
    run

    in2.setSignal(true)
    run
  }

  def printWires(label: String, wires: List[Wire]) = {
    println(label + ": " + wires.mkString(" "))
  }

  def demux1Example {
    val in = new Wire
    val c = Array(new Wire)
    val out = Array(new Wire, new Wire)
    demux(in, c.toList, out.toList)
    probe("in1", in)
    probe("c[0]", c(0))

    probe("out[0]", out(0))
    probe("out[1]", out(1))

    in.setSignal(true)
    c(0).setSignal(false)
    run

    c(0).setSignal(true)
    run

    in.setSignal(false)
    run

    c(0).setSignal(true)
    run
  }

  def andListExample(inputNo: Int) {
    val in = Array.fill(inputNo) { new Wire }
    val out = new Wire

    // Set probes
    for (i <- 0 until in.size - 1) {probe(s"in[$i]", in(i))} 

    // set inputs to false
    in map (_.setSignal(false))

    probe("out", out)

    andGate(in.toList, out)

    run

    in.foreach((i) => {
      i.setSignal(true)
      run
    })

  }

  def demuxExample {
    val in = new Wire
    val c = Array(new Wire, new Wire)
    val out = Array(new Wire, new Wire, new Wire, new Wire)
    demux(in, c.toList, out.toList)
    probe("in1", in)
    for (i <- 0 until c.size - 1)   {(probe(s"c[$i]"  , c(i))) }
    for (i <- 0 until out.size - 1) {(probe(s"out[$i]", out(i))) }

    in.setSignal(true)
    c(0).setSignal(false)
    c(1).setSignal(false)
    run

    c(0).setSignal(true)
    run

    c(1).setSignal(true)
    run

    c(0).setSignal(false)
    run
    printWires("out:", out.toList)
    printWires("c:  ", c.toList)
  }

}

object CircuitMain extends App {
  // You can write tests either here, or better in the test class CircuitSuite.
  Circuit.andGateExample
}
