package simulations

object SimulatorWs {
  val a = Array(1, 2, 3, 4, 5, 6)                 //> a  : Array[Int] = Array(1, 2, 3, 4, 5, 6)

  val l = List(("a", 1), ("b", 2), ("c", 3))      //> l  : List[(String, Int)] = List((a,1), (b,2), (c,3))

  l.permutations.take(1)                          //> res0: Iterator[List[(String, Int)]] = non-empty iterator
  l.permutations.take(1)                          //> res1: Iterator[List[(String, Int)]] = non-empty iterator
  l.permutations.take(1)                          //> res2: Iterator[List[(String, Int)]] = non-empty iterator

  l(1)                                            //> res3: (String, Int) = (b,2)

  for (i <- 1 until 5) {
    print(i + " ")                                //> 1 2 3 4 
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

  //Circuit.andGateExample

  //Circuit.orGateExample

  val res = List("a", "b", "c").zipWithIndex.map(x => s"${x._1}[${x._2}]")
                                                  //> res  : List[String] = List(a[0], b[1], c[2])
  //val res2 = List("a", "b", "c").zipWithIndex.map((_._1.toString + "[" + _._2 + "]" ))

  //res.take(1)

  val pos = List.fill(3) { new PosWire }          //> pos  : List[simulations.SimulatorWs.PosWire] = List(Pos, Pos, Pos)
  val neg = List.fill(3) { new NegWire }          //> neg  : List[simulations.SimulatorWs.NegWire] = List(Neg, Neg, Neg)

  //Circuit.getAndWires(new Wire, 4, pos, neg, Nil)

  // Circuit.demux1Example
  // Circuit.demuxExample
  // Circuit.andListExample(10)
  import math.random

  def randomBelow(i: Int) = (random * i).toInt    //> randomBelow: (i: Int)Int

  def getRandomItem(list: List[(Int, Int)], predicate: ((Int, Int)) => Boolean): Option[(Int, Int)] = {
    if (list.isEmpty) None
    else {
      val randomItem = randomBelow(list.size)
      val randomAddress = list(randomItem)
      if (predicate(randomAddress)) {
        Some(randomAddress)
      } else {
             getRandomItem(list.take(randomItem - 1) ++ list.takeRight(list.size - 1 - randomItem), predicate)
     }
    }
  }                                               //> getRandomItem: (list: List[(Int, Int)], predicate: ((Int, Int)) => Boolean)
                                                  //| Option[(Int, Int)]
  val pred_t = (pair: (Int, Int)) => true         //> pred_t  : ((Int, Int)) => Boolean = <function1>
  val pred_1 = (pair: (Int, Int)) => pair._1 == 1 //> pred_1  : ((Int, Int)) => Boolean = <function1>
  val pred_3 = (pair: (Int, Int)) => pair._1 == 3 //> pred_3  : ((Int, Int)) => Boolean = <function1>

  val l2 = List((2,2), (7,4), (5,1), (1,5))       //> l2  : List[(Int, Int)] = List((2,2), (7,4), (5,1), (1,5))

  getRandomItem(l2, pred_t)                       //> res4: Option[(Int, Int)] = Some((7,4))
  getRandomItem(l2, pred_t)                       //> res5: Option[(Int, Int)] = Some((2,2))
  getRandomItem(l2, pred_t)                       //> res6: Option[(Int, Int)] = Some((7,4))
  getRandomItem(l2, pred_t)                       //> res7: Option[(Int, Int)] = Some((2,2))
  getRandomItem(l2, pred_1)                       //> res8: Option[(Int, Int)] = Some((1,5))
  getRandomItem(l2, pred_3)                       //> res9: Option[(Int, Int)] = None
 
  
  l2.take(1)                                      //> res10: List[(Int, Int)] = List((2,2))
  l2.takeRight(l2.size-1-1)                       //> res11: List[(Int, Int)] = List((5,1), (1,5))
  

}