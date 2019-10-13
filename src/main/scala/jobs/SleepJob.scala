package jobs

import scheduling.model.Job
import utils.CancellationToken

import scala.concurrent._
import scala.concurrent.duration.FiniteDuration

class SleepJob(name: String, interval: FiniteDuration, timeout: FiniteDuration, executionTime: FiniteDuration)
  extends Job(name, interval, timeout) {

  def this(name: String, interval: FiniteDuration, timeout: FiniteDuration) = {
    this(name, interval, timeout, timeout)
  }

  override def execute()(implicit executor: ExecutionContext, token: CancellationToken): Future[_] = Future {
    val startedAt = System.nanoTime()
    def elapsed = System.nanoTime() - startedAt
    while (elapsed <= executionTime.toNanos) {
      token.throwIfCancelled()
      Thread.sleep(10)
    }
  }
}
