package model

import scala.concurrent.duration.FiniteDuration

case class Job(name: String, interval: FiniteDuration, timeout: FiniteDuration)
