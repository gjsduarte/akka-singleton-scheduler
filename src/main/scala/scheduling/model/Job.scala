package scheduling.model

import utils.CancellationToken

import scala.concurrent._
import scala.concurrent.duration._

abstract class Job(val name: String, val interval: FiniteDuration, val timeout: FiniteDuration) {

  def execute()(implicit executor: ExecutionContext, token: CancellationToken): Future[_]
}