package quickcheck


object notes2Ws {
  val h1 = new QuickCheckHeap with Bogus3BinomialHeap
                                                  //> h1  : quickcheck.QuickCheckHeap with quickcheck.Bogus3BinomialHeap = Prop
                                                              
  val sample = h1.genHeap.sample                  //> sample  : Option[quickcheck.notes2Ws.h1.H] = Some(List(Node(-1155962054,0,Li
                                                  //| st())))
}