package svc

import com.typesafe.scalalogging.LazyLogging
import repository.DataDog._
import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import model._
import repository.{Kcl, KinesisPublisher}

import KinesisPublisher._

object DisseminatorSvc extends LazyLogging {

  import utils.ApplicativeConcurrent._

  val sink: DdSeries => Task[SinkResults] = req => {
    T.apply2(
      Task.fork(post2dd(req)),
      Task.fork(publish(req)(Kcl.client))
    )(SinkResults(_, _))
  }
}
