package repository

import model._
import doobie.imports._
import scalaz._, Scalaz._
import doobie.util.fragment.Fragment
import scalaz.concurrent.Task
import utils.CustomExecutor._

/**
  * Desc: persistence access for qos objects
  */
object QosRepo {
lazy val q = 
  sql"""select query, metric_key from qos_queries"""
    .query[QosQuery]
    .list
  def qosQueryBank: Task[List[QosQuery]] = q.transact(Ds.hxa)

  def qosQueryResultList(q: QosQuery): Task[DdQueryResult] =
    Fragment.const(q.query)
      .query[QosQueryResult]
      .list
      .transact(Ds.hxa) map (DdQueryResult(q.metric_key, _))

  def qosListQueryResultList(qs: List[QosQuery]): Task[List[DdQueryResult]] = {
    Task.gatherUnordered(qs.map(qosQueryResultList(_)))
  }

  import DomainProtocols._

  def quer4AllMetrics: Task[List[DdQueryResult]] = {
    for {
      q <- q.transact(Ds.hxa)
      q2 <- qosListQueryResultList(q)
    } yield (q2)
  }

  def qosMetricsGenerator: Task[DdSeries] = {
    for {
      q <- quer4AllMetrics
      q2 = q.flatMap(x => x.asDdMetrics)
    } yield (DdSeries(q2))
  }
}
