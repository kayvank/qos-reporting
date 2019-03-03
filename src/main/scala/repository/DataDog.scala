package repository

import java.net.InetAddress
import com.typesafe.scalalogging.LazyLogging
import io.circe.{Decoder, Encoder, Json}
import model.DdSeries
import org.http4s.client.blaze.SimpleHttp1Client
import org.http4s.{Method, ParseResult, Request, Uri}
import utils.Global.cfgVevo
import io.circe.syntax._
import scalaz.concurrent.Task
import model.DomainProtocols._

object DataDog extends LazyLogging {

  val apiKey: String =
    cfgVevo.getString("datadog.apiKey")
  val urlString: String =
    cfgVevo.getString("datadog.url")
  val tags: List[String] = List(
    cfgVevo.getString("datadog.statsd.tag.env"),
    cfgVevo.getString("datadog.statsd.tag.app"))
  val host: String = InetAddress.getLocalHost.getHostName
  val uri: ParseResult[Uri] =
    Uri.fromString(urlString) map (_.withQueryParam("api_key", apiKey)
      )
  val url = uri.toOption.get

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]) = org.http4s.circe.jsonOf[A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]) = org.http4s.circe.jsonEncoderOf[A]

  lazy val httpClient = SimpleHttp1Client()

  def post2dd(req: DdSeries): Task[Json] =
    httpClient.expect[Json](Request(Method.POST, url).withBody(req.asJson))

}
