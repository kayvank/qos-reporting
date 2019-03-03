package utils

import kamon.Kamon

object Monitor {

  val qosKinesisCounter =
    Kamon.metrics.counter("qos-published-to-kinesis")
  val qosDdCounter =
    Kamon.metrics.counter("qos-datadog")

}
