package api

import java.util.NoSuchElementException
import javax.security.sasl.AuthenticationException
import io.circe.{Decoder, Encoder}
import org.http4s._
import org.http4s.dsl._

import scalaz._
import scalaz.concurrent.Task

trait BaseApi {

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  implicit def eitherToResponse[A](e: Throwable \/ A)(implicit encoder: Encoder[A]): Task[Response] =
    e.fold(l => InternalServerError(l.getMessage), r => Ok(r))


  implicit def eitherTaskToResponse[A](e: Task[Throwable \/ A])(implicit encoder: Encoder[A]): Task[Response] =
    for {
      t <- e
      x <- t.fold(l => throwbe2response(l), r => Ok(r))
    } yield (x)

  val throwbe2response: Throwable => Task[Response]  = t => t match {
    case e: AuthenticationException => Unauthorized(Challenge("", "", Map()))
    case e: IllegalArgumentException => BadRequest (e.getMessage)
    case e: NoSuchElementException => NotFound (e.getMessage)
    case e: Throwable => InternalServerError(e.getMessage)
  }
}
