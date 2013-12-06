package suggestions
package gui

import scala.language.postfixOps
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConverters._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Try, Success, Failure }
import rx.lang.scala.subscriptions._
import rx.lang.scala._
import rx.lang.scala.observables.BlockingObservable
import rx.lang.scala.subjects._
import observablex._
import search._

object WikipediaUtils {
  def sanitizer(input: String): String = {
    input.replace(" ", "_")
  }
}

trait WikipediaApi {

  /**
   * Returns a `Future` with a list of possible completions for a search `term`.
   */
  def wikipediaSuggestion(term: String): Future[List[String]]

  /**
   * Returns a `Future` with the contents of the Wikipedia page for the given search `term`.
   */
  def wikipediaPage(term: String): Future[String]

  /**
   * Returns an `Observable` with a list of possible completions for a search `term`.
   */
  def wikiSuggestResponseStream(term: String): Observable[List[String]] = ObservableEx(wikipediaSuggestion(term))

  /**
   * Returns an `Observable` with the contents of the Wikipedia page for the given search `term`.
   */
  def wikiPageResponseStream(term: String): Observable[String] = ObservableEx(wikipediaPage(term))

  implicit class StringObservableOps(obs: Observable[String]) {

    /**
     * Given a stream of search terms, returns a stream of search terms with spaces replaced by underscores.
     *
     * E.g. `"erik", "erik meijer", "martin` should become `"erik", "erik_meijer", "martin"`
     */
    def sanitized: Observable[String] = {
      obs.map(WikipediaUtils.sanitizer)
    }

  }

  
  implicit class ObservableOps[T](obs: Observable[T]) {

    /**
     * Given an observable that can possibly be completed with an error, returns a new observable
     * with the same values wrapped into `Success` and the potential error wrapped into `Failure`.
     *
     * E.g. `1, 2, 3, !Exception!` should become `Success(1), Success(2), Success(3), Failure(Exception), !TerminateStream!`
     */
    def recovered: Observable[Try[T]] = {
      Observable[Try[T]]((observer: Observer[Try[T]]) => {
        obs.subscribe(
          (t: T)         =>  observer.onNext(Success(t)),
          (e: Throwable) => {observer.onNext(Failure(e)); observer.onCompleted()} , 
          ()             =>  observer.onCompleted() )
      })
    }

    
    /**
     * Emits the events from the `obs` observable, until `totalSec` seconds have elapsed.
     *
     * After `totalSec` seconds, if `obs` is not yet completed, the result observable becomes completed.
     *
     * Note: uses the existing combinators on observables.
     */
    def timedOut(totalSec: Long): Observable[T] = {
      val subscription = BooleanSubscription()
      def delayThenUnsubscribe(): Future[Unit] = {
        future {
          Try(Await.ready(Promise().future, totalSec seconds))
          subscription.unsubscribe
        }
      }
      val timoutObservable = ObservableEx(delayThenUnsubscribe)
      Observable[T]((observer: Observer[T]) => {
        obs.subscribe(
          (t: T)         => if (subscription.isUnsubscribed) {observer.onCompleted} else observer.onNext(t),
          (e: Throwable) => observer.onError(e),
          ()             => observer.onCompleted() )
      })

    }

    /**
     * Given a stream of events `obs` and a method `requestMethod` to map a request `T` into
     * a stream of responses `S`, returns a stream of all the responses wrapped into a `Try`.
     * The elements of the response stream should reflect the order of their corresponding events in `obs`.
     *
     * E.g. given a request stream:
     *
     * 1, 2, 3, 4, 5
     *
     * And a request method:
     *
     * num => if (num != 4) Observable.just(num) else Observable.error(new Exception)
     *
     * We should, for example, get:
     *
     * Success(1), Success(2), Success(3), Failure(new Exception), Success(5)
     *
     *
     * Similarly:
     *
     * Observable(1, 2, 3).concatRecovered(num => Observable(num, num, num))
     *
     * should return:
     *
     * Observable(Success(1), Succeess(1), Succeess(1), Succeess(2), Succeess(2), Succeess(2), Succeess(3), Succeess(3), Succeess(3))
     * 
     *  
     */
    def concatRecovered[S](requestMethod: T => Observable[S]): Observable[Try[S]] = {
      // I wanted to compose all using recovered and concat but got java.lang.IllegalStateException: Can not set subscription more than once
      // So finally I end with so complicated solution
      Observable[Try[S]]((observer: Observer[Try[S]]) => {
        obs.recovered.subscribe(
          (tryT: Try[T]) => {
            tryT match {
              case Success(t) => {
                val tryRequest = Try(requestMethod(t))
                tryRequest match {
                  case Success(r) => {
                    val innerList = r.recovered.toBlockingObservable.toList
                    innerList.foreach((innerItem: Try[S]) => observer.onNext(innerItem))
                  }
                  case Failure(tryRequestFailure) => observer.onNext(Failure(tryRequestFailure))
                }
              }
              case Failure(t) => observer.onNext(Failure(t))
            }

          },
          (e: Throwable) => { observer.onNext(Failure(e)) },
          () => observer.onCompleted())
      })
    } //concatRecovered
    
  } // ObservableOps

}

