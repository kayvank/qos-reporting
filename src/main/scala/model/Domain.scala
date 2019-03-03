package model

import java.net.InetAddress
import java.nio.ByteBuffer

import com.amazonaws.transform.SimpleTypeCborUnmarshallers.ByteBufferCborUnmarshaller
import io.circe.Json
import utils.Global.cfgVevo

sealed trait Domain

case class QosQuery(
  query: String,
  metric_key: String
) extends Domain

case class QosQueryResult(
  platform: Option[String],
  metric: Option[String]
) extends Domain

case class DdQueryResult(
  metric_key: String,
  qosQueryResult: List[QosQueryResult]
) {
  val metric: List[(String, String)] =
    qosQueryResult.map(x => (s"${metric_key}.${x.platform.getOrElse("")}",
      x.metric.getOrElse("0.0")))
}

case class DdTags(tags: List[String] =
List(cfgVevo.getString("datadog.statsd.tag.env"),
  cfgVevo.getString("datadog.statsd.tag.app")),
  host: String = InetAddress.getLocalHost.getHostName
)

case class DataDogResponse(
  status: String
)

case class DdMetric(
  metric: String,
  points: List[(Long, Double)],
  host: String,
  tags: List[String]
)

case class DdSeries(
  series: List[DdMetric]
)

case class Metric(
  name: String,
  value: Double,
  timestamp: Long
)

case class KplRecord(
  data: ByteBuffer,
  partitionKey: String = System.currentTimeMillis.toString,
  streamName: String = cfgVevo.getString("kinesis.stream.qos.name")
)

case class SinkResults(
  ddResult: Json,
  kclPublishResult: KclPublishResult
)

case class KclPublishResult(
  errors: Int,
  totalRecords: Int
)