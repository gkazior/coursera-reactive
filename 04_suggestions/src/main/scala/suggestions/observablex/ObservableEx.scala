package suggestions
package observablex

import scala.concurrent.{ Future, ExecutionContext }
import scala.util._
import scala.util.Success
import scala.util.Failure
import java.lang.Throwable
import rx.lang.scala.Observable
import rx.lang.scala.Scheduler
import rx.lang.scala._
import rx.lang.scala.subjects.ReplaySubject
import rx.lang.scala.subscriptions.Subscription
import rx.lang.scala.subscriptions.BooleanSubscription

object ObservableEx {
  private def helper[T](f: Future[T], execContext: ExecutionContext)(observer: Observer[T]): Subscription = {
    val s = ReplaySubject[T]
    f.onComplete({
      case Success(value) => s.onNext(value); s.onCompleted
      case Failure(t)     => s.onError(t)
    })(execContext)
    s.subscribe(observer)
  }
  /**
   * Returns an observable stream of values produced by the given future.
   * If the future fails, the observable will fail as well.
   *
   * @param f future whose values end up in the resulting observable
   * @return an observable completed after producing the value of the future, or with an exception
   */
  def apply[T](f: Future[T])(implicit execContext: ExecutionContext): Observable[T] = Observable[T](helper[T](f, execContext)(_))
}

object ObservableShortSeq {
  private def helper[T](shortSeq: Seq[T])(observer: Observer[T]): Subscription = {
    val s = ReplaySubject[T]
    shortSeq.foreach (s.onNext(_))
    s.onCompleted
    s.subscribe(observer)
  }
  /**
   * Returns an observable stream of values produced by the given future.
   * If the future fails, the observable will fail as well.
   *
   * @param f future whose values end up in the resulting observable
   * @return an observable completed after producing the value of the future, or with an exception
   */
  def apply[T](shortSeq: Seq[T]): Observable[T] = Observable[T]((o: Observer[T]) => helper[T](shortSeq)(o))
}