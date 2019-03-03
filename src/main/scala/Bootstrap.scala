import api.StatusApi
import com.typesafe.scalalogging.LazyLogging
import kamon.Kamon
import model.SinkResults
import org.http4s.server._
import blaze.BlazeBuilder
import scalaz._,Scalaz._
import scalaz.concurrent.Task
import scala.concurrent.duration._
import scalaz.stream.time.awakeEvery
import scalaz.stream._

object Bootstrap extends ServerApp with LazyLogging {

  case class ProgramStatus(u: Unit, s: Server)

  import repository.QosRepo._
  import svc.DisseminatorSvc._
  import utils._
  import CustomExecutor._
  import Global._

  Kamon.start()

  val quartz = cfgVevo.getInt("quartz.data.push.seconds")

  val qosDatadogTask: Task[SinkResults] = for {
    q <- qosMetricsGenerator
    j <- sink(q)
    _ <- Task(utils.Monitor.qosKinesisCounter.increment(j.kclPublishResult.totalRecords))
    _ <- Task(utils.Monitor.qosDdCounter.increment())
    _ <- Task(logger.info(s" sinkResult=${j}"))
  } yield j

  val d: Duration = quartz seconds
  val program = for {
    _ <- awakeEvery(d)
    q <- Process.eval(qosDatadogTask)
  } yield (q)

  case class RunRes(s: Server, u: Unit, m: Unit)

  def server(args: List[String]): Task[Server] = {

    val serverTask = BlazeBuilder.bindHttp(
      port = cfgVevo.getInt("server.port"),
      host = "0.0.0.0")
      .mountService(StatusApi.service, "/status").start

    val p = ApplicativeConcurrent.T.apply2(
      (Task.fork(program.run)(customExecutor)),
      (Task.fork(serverTask)(ec))
    )(ProgramStatus(_, _))
    p.map(_.s)
  }
}
