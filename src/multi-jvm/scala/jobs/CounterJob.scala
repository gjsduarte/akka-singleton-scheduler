package jobs

import scheduling.model.Job
import utils.CancellationToken

import scala.concurrent._
import scala.concurrent.duration._

class CounterJob(name: String, interval: FiniteDuration, timeout: FiniteDuration)
  extends Job(name, interval, timeout) {

  var counter = 0

  def execute()(implicit e: ExecutionContext, t: CancellationToken) = Future(counter += 1)
}