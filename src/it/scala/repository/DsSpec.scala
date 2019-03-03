package repository


import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification
import doobie.imports._
import io.circe.Json
import model.QosQueryResult
import scala.concurrent.duration._
import scalaz._
import Scalaz._
import scalaz.concurrent.Task


class DSSpec extends Specification with LazyLogging {

  import svc.DisseminatorSvc._
  import Ds._

  "Data srouce specifications".title

  "connect to databse and run queries" >> {
    val v = 42.point[ConnectionIO].transact(hxa).run
    logger.info(s"DS.42 = $v")
    val program3 =
      for {
        a <- sql"select 42".query[Int].unique
        b <- sql"select now()".query[String].unique
      } yield (a, b)
    val p = program3.transact(hxa).run
    v == 42
  }

  "should readqueries from pipelindDB" >> {
    val dynamicQuery = "select query, metric_key from qos_queries"
    val q2 = sql"""${dynamicQuery}"""
    val qosQueries =
      (sql"""select query, metric_key from qos_queries"""
        .query[QosQueryResult]
        .list.transact(hxa))

    val qosQuery2 = q2.query[QosQueryResult]
      .list.transact(hxa)
    val qList = qosQueries.run
    qList.size > 0
  }
  "should read ALL the queries from pipelindDB" >> {
    val rawQuery = "select platform, qos_99 as metric from qos_spinner_events_10m_c_v;" // another table will provide the query
    import doobie.syntax._
    import cats._, cats.data._, cats.implicits._

    val b = Fragment.const(rawQuery)
    val computedTask = b.query[QosQueryResult].list.transact(hxa)
    val computed = computedTask.run
    computed.size > 0
  }
  "should convert queries to valid json" >> {
    import QosRepo._
    val json = quer4AllMetrics.run
    !json.toString.isEmpty
  }

  "should post qos-data to dd-agents" >> {
    import QosRepo._
    val computedTask = for {
      q <- qosMetricsGenerator
      j <- sink(q)

    } yield j
    val computed = computedTask.run
    !computed.toString.isEmpty
  }


}
