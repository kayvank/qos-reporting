package api

import io.circe.Json
import io.circe.parser.parse
import scalaz._
import Scalaz._
import org.http4s._
import org.http4s.circe._
import io.circe.syntax._
import org.http4s.dsl._
import utils.CustomExecutor._
import scala.util.Try
import scalaz.concurrent.Task

object StatusApi extends BaseApi {
  import repository.Ds._
  val na = "na"
  val gocdPipelineCounter = Try(System.getProperty("build_number")).toOption.getOrElse(na)
  val bs = Map("gocdPipelineCounter" -> (
    (gocdPipelineCounter == null) ? na | gocdPipelineCounter)).asJson

  lazy val bInfo = parse(info.BuildInfo.toJson).right.getOrElse(Json.Null).deepMerge(bs)
  val bInfoT = Task(bInfo)

  def apply(): HttpService = service

  val service = HttpService {
    case req@GET -> Root =>
      Ok(bInfoT)

    case request@GET -> Root / "db" =>
      Ok(Task(bInfo.deepMerge(
               Map("pipelinedb-database-isUp" -> connectionStatus).asJson)))
  }.mapK(Task.fork(_)(ec))

}
