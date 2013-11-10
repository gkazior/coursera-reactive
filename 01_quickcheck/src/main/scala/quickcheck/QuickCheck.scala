package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {
  /**
   *  Returns true when the heap is sorted
   *  It checks if after deleteMin the min was the least element
   */
  private def heapIsSorted(h: H, currentMin: Int): Boolean = {
    if (isEmpty(h)) true
    else {
      val m = findMin(h)
      if (m < currentMin) false
      else heapIsSorted(deleteMin(h), m)
    }
  }

  /**
   *  Returns true when heaps are the same
   *  When heaps are the same is returns the same values for findMin
   */
  private def heapsAreSortedSame(h1: H, h2: H): Boolean = {
    (isEmpty(h1), isEmpty(h2)) match {
      case (false, false) => {
        val m1 = findMin(h1)
        val m2 = findMin(h2)
        if (m1 != m2) false
        else heapsAreSortedSame(deleteMin(h1), deleteMin(h2))
      }
      case (true, true) => true // true when both list are empty
      case (_, _)       => false // when one list is empty but not the second
    }
  }

  // Well, comment is more complicated then the code. Scala is simply great! 
  property("min1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }

  // inserted value of findMin does not change the findMin
  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }

  // Given any heap, you should get a sorted sequence of elements 
  // when continually finding and deleting minima. (Hint: recursion and helper functions are your friends.)
  // Fixes Bogus 5
  property("checkSorted") = forAll { (h: H) =>
    heapIsSorted(h, Int.MinValue)
  }

  // Finding a minimum of the melding of any two heaps should return a minimum of one or the other.
  property("meldIsSorted") = forAll { (h1: H, h2: H) =>
    heapIsSorted(meld(h1, h2), Int.MinValue)
  }

  // Finding a minimum of the melding of any two heaps should return a minimum of one or the other.
  property("minOfMeld") = forAll { (h1: H, h2: H) =>
    val m1 = findMin(h1)
    val m2 = findMin(h2)
    findMin(meld(h1, h2)) == math.min(m1, m2)
  }

  // Very funny! The property is fine for test! 
  // insert2
  property("findMinOkCheckInsert") = forAll { (h: H, i: Int) =>
    val m = if (isEmpty(h)) i else findMin(h)
    findMin(insert(i, h)) == math.min(m, i)
  }

  property("alwaysSortedAfterInsert") = forAll { (h: H, i: Int) =>
    heapIsSorted(insert(i, h), Int.MinValue)
  }

  property("alwaysSortedAfterMeld") = forAll { (h1: H, h2: H) =>
    heapIsSorted(meld(h1, h2), Int.MinValue)
  }

  /**
   * Associativity of insert operation. Checking with findMin.
   */
  property("associativityInsertWithFindMin") = forAll { (h: H, i: Int, j: Int) =>
    findMin(insert(j, insert(i, h))) ==
    findMin(insert(i, insert(j, h)))
  }
   /**
   * Associativity of insert operation. Checking with comparison.
   */
  property("associativityInsertWithComparison") = forAll { (h: H, i: Int, j: Int) =>
    heapsAreSortedSame(insert(j, insert(i, h))
                      ,insert(i, insert(j, h)))
  }
  /**
   * Associativity of meld operation. Checking with findMin.
   */
  property("associativityMeldWithFindMin") = forAll { (h1: H, h2: H, h3: H) =>
    findMin(meld(h3, meld(h2, h1))) ==
    findMin(meld(h2, meld(h3, h1)))
  }
  /**
   * Associativity of meld operation. Checking with comparison.
   */
  property("associativityMeldWithComparison") = forAll { (h1: H, h2: H, h3: H) =>
    heapsAreSortedSame(meld(h3, meld(h2, h1))
                      ,meld(h2, meld(h3, h1)))
  }

  // The generator for heap
  lazy val genHeap: Gen[H] = for {
    i <- arbitrary[Int]
    h <- oneOf(value(empty), genHeap)
  } yield insert(i, h)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

}
