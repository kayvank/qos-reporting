package repository

import com.amazonaws.services.kinesis.AmazonKinesis
import com.typesafe.scalalogging.LazyLogging
import model.{DdSeries, KclPublishResult, KplRecord}

import scalaz.concurrent.Task
import utils.CustomExecutor._
import model.DomainProtocols._

object KinesisPublisher extends LazyLogging {
  def publish(data: DdSeries): AmazonKinesis => Task[KclPublishResult] = client => (for {
    r1 <- Task(data.asKplRecordList)
    r2 <- Task.gatherUnordered(
      r1.grouped(100).toList.map(x =>
        Task(client.putRecords(x.asBatch))))
    r3 <- Task(r2.map(r => r.getFailedRecordCount).foldLeft(0)(_ + _))
  } yield (KclPublishResult(r3, r1.size))
    ).handleWith {
    case e: Exception =>
      logger.error(s"failed publish to kinesis. payload=${data}")
      Task.fail(e)
  }
}
