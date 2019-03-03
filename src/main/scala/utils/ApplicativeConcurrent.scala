package utils

import scalaz.{Applicative, Nondeterminism}
import scalaz.concurrent.Task


object ApplicativeConcurrent {


  val T = new Applicative[Task] {
    def point[A](a: => A) = Task.now(a)
    def ap[A,B](a: => Task[A])(f: => Task[A => B]): Task[B] = apply2(f,a)(_(_))
    override def apply2[A,B,C](a: => Task[A], b: => Task[B])(f: (A,B) => C): Task[C] =
      Nondeterminism[Task].mapBoth(a, b)(f)
  }
}
