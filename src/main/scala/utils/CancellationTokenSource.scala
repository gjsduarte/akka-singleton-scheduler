package utils

class CancellationTokenSource extends CancellationToken {

  def cancel(): Unit = {
    cancelled = true
  }
}
