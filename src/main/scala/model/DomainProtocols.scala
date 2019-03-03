package model

import java.nio.charset.StandardCharsets

import io.circe._
import io.circe.parser._
import io.circe.generic.semiauto._
import repository.DataDog
import io.circe.syntax._
import scalaz._
import Scalaz._
import scala.util.{Failure, Success, Try}
import java.nio.ByteBuffer
import utils.EitherToDisjunction._
import collection.JavaConverters._
import com.amazonaws.services.kinesis.model.{PutRecordsRequest, PutRecordsRequestEntry}

object DomainProtocols {

  implicit val ddMetricDecoder: Decoder[DdMetric] = deriveDecoder
  implicit val ddMetricEncoder: Encoder[DdMetric] = deriveEncoder

  implicit val ddSeriesDecoder: Decoder[DdSeries] = deriveDecoder
  implicit val ddSeriesEncoder: Encoder[DdSeries] = deriveEncoder

  implicit val metricDecoder: Decoder[Metric] = deriveDecoder
  implicit val metricEncoder: Encoder[Metric] = deriveEncoder

  implicit class MetricProtocol(ddMetric: DdMetric) {
    def asMetric: Metric =
      Metric(
        name = ddMetric.metric,
        value = ddMetric.points.headOption.map(_._2).getOrElse(0D),
        timestamp = ddMetric.points.headOption.map(_._1).getOrElse(0L)
      )
  }

  implicit class BatchInsert(kplRecords: List[KplRecord]) {
    def asBatch: PutRecordsRequest = {
      val putrecsList = kplRecords.foldLeft(List[PutRecordsRequestEntry]())((z,r) =>
        (new PutRecordsRequestEntry).withData(r.data).withPartitionKey(r.partitionKey) :: z)
      (new PutRecordsRequest()).withStreamName(kplRecords.head.streamName).withRecords(putrecsList.asJava)
    }
  }

  implicit class MetricStringProtocol(metric: Metric) {
    def asMetricString: String = metric.asJson.noSpaces
  }


  implicit class KPLlProtocol(buf: String) {
    def asKplRecord: KplRecord = {
      KplRecord(data = ByteBuffer.wrap(buf.getBytes))
    }
  }

  implicit class DdSeriesKplProtocol(ddSeries: DdSeries) {
    def asKplRecordList = {
       ddSeries.series map (_.asMetric.asMetricString.asKplRecord)
    }
  }

  implicit class DdMetricProtocol(ddResult: DdQueryResult) {
    def asDdMetrics: List[DdMetric] = {
      ddResult.metric.map(r => DdMetric(
        metric = r._1,
        points = List((System.currentTimeMillis / 1000, string2double(r._2))),
        host = DataDog.host,
        tags = DataDog.tags
      ))
    }

    def string2double(s: String) = {
      Try(s.toDouble).toOption.getOrElse(0D)
    }
  }

  /**
    * this class is used in unit tests only for round-trip protocol conversions
    * @param buf
    */

  implicit class ByteBufToMetric(buf: ByteBuffer) {
    def asMetric: \/[Exception, Metric] = {
      val str = Try(new java.lang.String(
        (buf.array()), StandardCharsets.UTF_8)) match {
        case Success(s) => s.right
        case Failure(e) => new Exception(e).left
      }
      for {
        s <- str
        j <- fromEither2zDisjunction(parse(s))
        m <- fromEither2zDisjunction(j.as[Metric])
      } yield (m)

    }
  }

}
