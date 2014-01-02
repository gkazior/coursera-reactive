package kvstore

object NotepadWs {
  var v = Vector.empty[Long]                      //> v  : scala.collection.immutable.Vector[Long] = Vector()
  v = v :+ 1L
  v = v :+ 2L
  v = v :+ 3L
  println(v)                                      //> Vector(1, 2, 3)
  println("DUPA")                                 //> DUPA
  v = v.filter(_ != 2L)
  println(v)                                      //> Vector(1, 3)
  v(1)                                            //> res0: Long = 3
  v(0)                                            //> res1: Long = 1
  math.signum(5-2)                                //> res2: Int = 1
  
  val m = Map(("test" -> "best"), ("skok" -> "bok"), ("skoka" -> "bok"))
                                                  //> m  : scala.collection.immutable.Map[String,String] = Map(test -> best, skok 
                                                  //| -> bok, skoka -> bok)
  
  
  m.get("test")                                   //> res3: Option[String] = Some(best)
  
  m foreach ( (pair) => println(f"$pair._1----$pair._2"))
                                                  //> (test,best)._1----(test,best)._2
                                                  //| (skok,bok)._1----(skok,bok)._2
                                                  //| (skoka,bok)._1----(skoka,bok)._2
  m mapValues ( (x) => println(f"MappedValue: $x"))
                                                  //> MappedValue: best
                                                  //| MappedValue: bok
                                                  //| MappedValue: bok
                                                  //| res4: scala.collection.immutable.Map[String,Unit] = Map(test -> (), skok -> 
                                                  //| (), skoka -> ())
  
  val x: Any = 2                                  //> x  : Any = 2
  if (x != None) println("YES ok") else println("NO")
                                                  //> YES ok
                                                  
  (1 until 3).map((x) =>println(s"Got $x"))       //> Got 1
                                                  //| Got 2
                                                  //| res5: scala.collection.immutable.IndexedSeq[Unit] = Vector((), ())
}