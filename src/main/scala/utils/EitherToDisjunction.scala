package utils

import scala.util.Either
import scalaz.{-\/, \/-}


object EitherToDisjunction {
  implicit def fromEither2zDisjunction[A, B](_either: Either[A, B]) =
    _either match {
      case Right(b) => \/-(b)
      case Left(a) => -\/(a)
    }
}

