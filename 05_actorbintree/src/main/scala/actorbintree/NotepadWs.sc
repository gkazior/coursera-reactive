package actorbintree

import scala.collection.immutable.Queue

object NotepadWs {
  var map = Map[String, Int]()                    //> map  : scala.collection.immutable.Map[String,Int] = Map()
  
  map += ("koko" -> 1)
  
  val emptyOption    = map.get("kokon")           //> emptyOption  : Option[Int] = None
  val nonEmptyOption = map.get("koko")            //> nonEmptyOption  : Option[Int] = Some(1)

  emptyOption map {x => println(s"Found: $x")}    //> res0: Option[Unit] = None
  nonEmptyOption map {x => println(s"Found: $x")} //> Found: 1
                                                  //| res1: Option[Unit] = Some(())
  for (value <- nonEmptyOption) yield value       //> res2: Option[Int] = Some(1)
  
  val queue = Queue.empty[String]                 //> queue  : scala.collection.immutable.Queue[String] = Queue()
  
  val q2 = queue.enqueue("test").enqueue("ttest2")//> q2  : scala.collection.immutable.Queue[String] = Queue(test, ttest2)
  
  q2 foreach (println(_))                         //> test
                                                  //| ttest2
                                                  
                                                  
  val points = 65.0/84                            //> points  : Double = 0.7738095238095238
}