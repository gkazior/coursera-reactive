package simulations

object SimulatorWs {
  val a = Array(1, 2, 3, 4, 5, 6)                 //> a  : Array[Int] = Array(1, 2, 3, 4, 5, 6)

  for (i <- 1 until 5) {
  	print(i + " ")                            //> 1 2 3 4 
  }

  (1 until 5) foreach ((i: Int) => print(s"$i ")) //> 1 2 3 4 
  (1 until 5) foreach ((i: Int) => print(i.toString + " "))
                                                  //> 1 2 3 4 

  class PosWire extends Wire {
    override def toString() = {
      "Pos"
    }
  }
  class NegWire extends Wire {
    override def toString() = {
      "Neg"
    }
  }

  //val rolek = 1 >> 1
  //4 & 1
  //Array.range(2, 4)
  //Circuit.andGateExample

  //Circuit.orGateExample

  val res = List("a", "b", "c").zipWithIndex.map(x => x._1.toString + "[" + x._2 + "]")
                                                  //> res  : List[String] = List(a[0], b[1], c[2])
  //val res2 = List("a", "b", "c").zipWithIndex.map((_._1.toString + "[" + _._2 + "]" ))

  //res.take(1)

  val pos = List.fill(3) { new PosWire }          //> pos  : List[simulations.SimulatorWs.PosWire] = List(Pos, Pos, Pos)
  val neg = List.fill(3) { new NegWire }          //> neg  : List[simulations.SimulatorWs.NegWire] = List(Neg, Neg, Neg)

  Circuit.getAndWires(new Wire, 4, pos, neg, Nil) //> res0: List[simulations.Wire] = List(false, Neg, Neg, Pos)

  // Circuit.demux1Example
  //Circuit.demuxExample
  // Circuit.andListExample(10)
}