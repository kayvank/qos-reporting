package utils

import java.util.concurrent.Executors
import com.typesafe.scalalogging.LazyLogging
import scalaz.concurrent.Strategy

sealed trait CustomExecutor

final object CustomExecutor extends CustomExecutor with LazyLogging {

  import Global._

  final val numberOfThreads = 4

  final val ec: java.util.concurrent.ExecutorService =
    Executors.newSingleThreadExecutor()

  final val customExecutor: java.util.concurrent.ExecutorService =
    Executors.newFixedThreadPool(numberOfThreads)

  implicit final val sc = new java.util.concurrent.ScheduledThreadPoolExecutor(1)

  def execute(runnable: Runnable) = {
    customExecutor.submit(runnable)
  }

  def reportFailure(t: Throwable) =
    logger.error(t.getMessage)


  implicit val naive: Strategy = new Strategy {
    def apply[A](a: => A) = {
      import java.util.concurrent.Callable
      val thread = java.util.concurrent.Executors.newSingleThreadExecutor
      val fut = thread.submit(new Callable[A] {
        def call = a
      })
      thread.shutdown()
      () => fut.get
    }
  }

}
