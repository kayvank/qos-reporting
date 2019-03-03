package repository

import com.typesafe.scalalogging.LazyLogging
import io.circe.Json
import io.circe.syntax._
import model.{DdMetric, DdSeries}
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.Await

class KplSpecs extends Specification with LazyLogging {

  import KinesisPublisher._
  import KplConfig._

  "KPL specifications" >> {
    val data = DdSeries(List(
      DdMetric(
        metric = "some-metric",
        points = List((1, 1.01), (2, 2.02)),
        host = "some-host",
        tags = List("tag1", "tag2")),
      DdMetric(
        metric = "some-metric",
        points = List((1, 1.01), (2, 2.02)),
        host = "some-host",
        tags = List("tag1", "tag2")))
    )

    val jsonLoad: Json = Map("qos-test-metric" -> 1).asJson
    val computed = publish(data)(Kcl.client).run

    logger.warn(s"======SequenceNumber=${computed}")
    computed ==0

  }

}
