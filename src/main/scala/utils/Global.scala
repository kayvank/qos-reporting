package utils

import com.typesafe.config.ConfigFactory

object Global {

  val cfg = ConfigFactory.load
  val cfgVevo = cfg.getConfig("vevo")
}
