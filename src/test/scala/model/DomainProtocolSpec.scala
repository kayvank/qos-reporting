package model

import java.nio.charset.StandardCharsets

import com.typesafe.scalalogging.LazyLogging
import org.specs2.mutable.Specification
import io.circe.syntax._
import io.circe._
import io.circe.parser._
import utils.EitherToDisjunction.fromEither2zDisjunction
import io.circe._, io.circe.parser._
import DomainProtocols._

class DomainProtocolSpec extends Specification with LazyLogging {
  "domain json conversions" title

  import DomainProtocols._

  val tstmp: Long = System.currentTimeMillis
  val data = DdSeries(series = List(
    DdMetric(
      metric = "metric-1",
      points = List((tstmp, 1.01), (tstmp, 2.02)),
      host = "some-host",
      tags = List("tag1", "tag2")),
    DdMetric(
      metric = "metric-2",
      points = List((tstmp + 1, 10.01), (tstmp + 1, 2.02)),
      host = "some-host",
      tags = List("tag1", "tag2")))
  )
  "convert a debSeries object Metric json protocol" >> {
    val computed = (data.series map (_.asMetric.asMetricString))

    logger.warn(s"---- computed(0) = ${computed(0)}")
    logger.warn(s"---- computed(1) = ${computed(1)}")
    val s0 =
      s"""
         |{"name":"metric-1","value":1.01,"timestamp":$tstmp}
        """.stripMargin
    val s1 =
      s"""
         |{"name":"metric-2","value":10.01,"timestamp":${tstmp + 1}}
      """.stripMargin

    computed.size === 2 &&
      parse(computed(0)).right.get === parse(s0).right.get && (
      parse(computed(1)).right.get === parse(s1).right.get)
  }

  "convert a debSeries bytebuffer" >> {

    val kplRecord = data.asKplRecordList

    kplRecord.foreach(x => logger.warn(s"===== kplRec = ${x}"))

    val buf = kplRecord.map(d => d.data.asMetric.toOption)
    logger.warn(s"metricString(0) = ${buf(0).get}")
    logger.warn(s"metricString(1) = ${buf(1).get}")
    buf.size === 2 &&
      buf(0).isDefined && buf(1).isDefined &&
      buf(0).get === Metric("metric-1", 1.01D, tstmp) &&
      buf(1).get === Metric("metric-2", 10.01D, tstmp + 1)
  }


}
