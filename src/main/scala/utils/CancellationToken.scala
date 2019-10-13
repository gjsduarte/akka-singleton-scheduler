package utils

import scala.concurrent.CancellationException

abstract class CancellationToken {

  protected var cancelled = false

  def isCancelled: Boolean = cancelled

  def throwIfCancelled(): Unit = if (isCancelled) throw new CancellationException

}

object CancellationToken {
  object None extends CancellationToken
}