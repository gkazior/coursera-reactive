package example

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * This class implements a ScalaTest test suite for the methods in object
 * `Lists` that need to be implemented as part of this assignment. A test
 * suite is simply a collection of individual tests for some specific
 * component of a program.
 *
 * A test suite is created by defining a class which extends the type
 * `org.scalatest.FunSuite`. When running ScalaTest, it will automatically
 * find this class and execute all of its tests.
 *
 * Adding the `@RunWith` annotation enables the test suite to be executed
 * inside eclipse using the built-in JUnit test runner.
 *
 * You have two options for running this test suite:
 *
 * - Start the sbt console and run the "test" command
 * - Right-click this file in eclipse and chose "Run As" - "JUnit Test"
 */
@RunWith(classOf[JUnitRunner])
class ListsSuite extends FunSuite {

  /**
   * Tests are written using the `test` operator which takes two arguments:
   *
   * - A description of the test. This description has to be unique, no two
   *   tests can have the same description.
   * - The test body, a piece of Scala code that implements the test
   *
   * The most common way to implement a test body is using the method `assert`
   * which tests that its argument evaluates to `true`. So one of the simplest
   * successful tests is the following:
   */
  test("one plus one is two")(assert(1 + 1 === 2))

  /**
   * In Scala, it is allowed to pass an argument to a method using the block
   * syntax, i.e. `{ argument }` instead of parentheses `(argument)`.
   *
   * This allows tests to be written in a more readable manner:
   */
  test("one plus two is three") {
    assert(1 + 2 === 3)
  }

  /**
   * One problem with the previous (failing) test is that ScalaTest will
   * only tell you that a test failed, but it will not tell you what was
   * the reason for the failure. The output looks like this:
   *
   * {{{
   *    [info] - one plus one is three? *** FAILED ***
   * }}}
   *
   * This situation can be improved by using a special equality operator
   * `===` instead of `==` (this is only possible in ScalaTest). So if you
   * run the next test, ScalaTest will show the following output:
   *
   * {{{
   *    [info] - details why one plus one is not three *** FAILED ***
   *    [info]   2 did not equal 3 (ListsSuite.scala:67)
   * }}}
   *
   * We recommend to always use the `===` equality operator when writing tests.
   */
  test("two plus one is three") {
    assert(2 + 1 === 3)
  }

  /**
   * In order to test the exceptional behavior of a methods, ScalaTest offers
   * the `intercept` operation.
   *
   * In the following example, we test the fact that the method `intNotZero`
   * throws an `IllegalArgumentException` if its argument is `0`.
   */
  test("intNotZero throws an exception if its argument is 0") {
    intercept[IllegalArgumentException] {
      intNotZero(0)
    }
  }

  def intNotZero(x: Int): Int = {
    if (x == 0) throw new IllegalArgumentException("zero is not allowed")
    else x
  }

  /**
   * Now we finally write some tests for the list functions that have to be
   * implemented for this assignment. We fist import all members of the
   * `List` object.
   */
  import Lists._

  test("sum of empty list") {
      // well possible sum of empty list is 0 but SQL (in Oracle at least) tells null, count is other story - for empty list the 0 value is ok  
      assert(sum(List()) === 0)
  }
  test("sum of a few numbers") {
    assert(sum(List(1, 2, 0)) === 3)
  }
  test("sum of negative and positive") {
    assert(sum(List(-5, 5)) === 0)
  }
  test("long list sum") {
    assert(sum(List(-5, 5, 1, 1, 1, 1, 2, 2)) === 8)
  }
  test("max of a few numbers") {
    assert(max(List(3, 7, 2)) === 7)
  }
  test("max throws an exception for empty list") {
    intercept[NoSuchElementException] {
      max(List())
    }
  }
  test("max for one element") {
    assert(max(List(3)) === 3)
  }
  test("max for two element") {
    assert(max(List(3, 5)) === 5)
  }
  test("max for more element") {
    assert(max(List(3, 5, 1, -10, 3, 1, 0, 0, 5, 3, -1, 0)) === 5)
  }
  test("max at the beginning") {
    assert(max(List(13, 5, 1, -10, 3, 1, 0, 0, 5, 3, -1, 0)) === 13)
  }
  test("max at the end") {
    assert(max(List(3, 5, 1, -10, 3, 1, 0, 0, 5, 3, -1, 10)) === 10)
  }
  test("max is negative") {
    assert(max(List(-10, -9, -8, -7)) === -7)
  }
  test("max with zero and negatives") {
    assert(max(List(0, -5)) === 0)
  }
  test("max with zero and negatives - with different order") {
    assert(max(List(-5, 0)) === 0)
  }
}
