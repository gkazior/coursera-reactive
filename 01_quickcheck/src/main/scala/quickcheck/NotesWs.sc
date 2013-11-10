package quickcheck

object NotesWs {
  val f1: String => String = {case "ping" => "pong"}
                                                  //> f1  : String => String = <function1>
  val pf1: PartialFunction[String,String] = {case "ping" => "pong"}
                                                  //> pf1  : PartialFunction[String,String] = <function1>
  val f1_ping = f1("ping")                        //> f1_ping  : String = pong
  // val f1_pong = f1("pong")    // Match error here
  
  val pf1_ping = pf1("ping")                      //> pf1_ping  : String = pong
  val pf1_pong = pf1.isDefinedAt("pong")          //> pf1_pong  : Boolean = false
  
  val pf1_lifted = pf1 lift ("dupa")              //> pf1_lifted  : Option[String] = None
  
  
  
  trait Generator[+T] {
    self =>
  	def generate: T
  	
  	def map[S](f: T=>S): Generator[S] = new Generator[S] {
  		def generate = f(self.generate)
  	}
  	def flatMap[S](f: T=>Generator[S]): Generator[S] = new Generator[S] {
  		def generate = f(self.generate).generate
  	}
  }
     
  val integers = new Generator[Int] {
    val rand = new java.util.Random
    def generate = rand.nextInt()
  }                                               //> integers  : quickcheck.NotesWs.Generator[Int]{val rand: java.util.Random} = 
                                                  //| quickcheck.NotesWs$$anonfun$main$1$$anon$3@7d5e8834
  val booleans2 = new Generator[Boolean] {
  	def generate = integers.generate > 0
  }                                               //> booleans2  : quickcheck.NotesWs.Generator[Boolean] = quickcheck.NotesWs$$ano
                                                  //| nfun$main$1$$anon$4@1ccfa5c1
  
  val booleans = for (x <- integers) yield x > 0  //> booleans  : quickcheck.NotesWs.Generator[Boolean] = quickcheck.NotesWs$$anon
                                                  //| fun$main$1$Generator$1$$anon$1@2c52d188
  
  
  def pairs[T, U](t: Generator[T], u: Generator[U]) = for {
     x <- t
     y <- u
  } yield (x,y)                                   //> pairs: [T, U](t: quickcheck.NotesWs.Generator[T], u: quickcheck.NotesWs.Gen
                                                  //| erator[U])quickcheck.NotesWs.Generator[(T, U)]
  
  val pairs2 = new Generator[(Int, Int)] {
  	def generate = (integers.generate,integers.generate)
  }                                               //> pairs2  : quickcheck.NotesWs.Generator[(Int, Int)] = quickcheck.NotesWs$$an
                                                  //| onfun$main$1$$anon$5@409c8a10
  def single[T](x:T): Generator[T] = new Generator[T] {
  	def generate = x
  }                                               //> single: [T](x: T)quickcheck.NotesWs.Generator[T]

  def choose(lo: Int, hi: Int): Generator[Int] = for (x <- integers) yield lo + x.abs % (hi - lo)
                                                  //> choose: (lo: Int, hi: Int)quickcheck.NotesWs.Generator[Int]

  def oneOf[T](xs: T*): Generator[T] = for (idx <- choose(0, xs.length)) yield xs(idx)
                                                  //> oneOf: [T](xs: T*)quickcheck.NotesWs.Generator[T]

  def emptyLists = single(Nil)                    //> emptyLists: => quickcheck.NotesWs.Generator[scala.collection.immutable.Nil.
                                                  //| type]
  def nonEmptyLists = for {
  	head <- integers
  	tail <- lists
  } yield head :: tail                            //> nonEmptyLists: => quickcheck.NotesWs.Generator[List[Int]]
  
  def lists: Generator[List[Int]] = for {
  	isEmpty <- booleans
  	list <- if (isEmpty) emptyLists else nonEmptyLists
  } yield list                                    //> lists: => quickcheck.NotesWs.Generator[List[Int]]
 
  
  val myRandomInt = integers.generate             //> myRandomInt  : Int = -1959593725
  val myRandomBoolean = booleans.generate         //> myRandomBoolean  : Boolean = false
  val oneOfThem = oneOf(1,2,3,4,5) generate       //> oneOfThem  : Int = 4
  
  val myList = lists  generate                    //> myList  : List[Int] = List()
  
  
  val res = List(1,2,3,4) map ((x:Int) => 2*x)    //> res  : List[Int] = List(2, 4, 6, 8)
  val resFlat = List(1,2,3,4) flatMap ((x:Int) => List(2*x))
                                                  //> resFlat  : List[Int] = List(2, 4, 6, 8)
  val leftUnitLaw = Some(4) flatMap ((x : Int) => Some(x * 2))
                                                  //> leftUnitLaw  : Option[Int] = Some(8)
  
  val rightUnitLawSome = Some(4) flatMap ((x: Int) => Option(x))
                                                  //> rightUnitLawSome  : Option[Int] = Some(4)
  val rightUnitLawNone = None flatMap ((x: Int) => Option(x))
                                                  //> rightUnitLawNone  : Option[Int] = None

   
}