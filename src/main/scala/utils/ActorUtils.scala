package utils

import java.time.LocalDateTime._

import akka.actor.Actor
import akka.pattern.after

import scala.concurrent._
import scala.concurrent.duration.FiniteDuration

trait ActorUtils { this: Actor =>

  implicit protected val executor: ExecutionContext = context.dispatcher

  protected def log(msg: String): Unit = {
    println(Console.GREEN + format(msg))
  }

  protected def log(msg: String, exception: Throwable): Unit = {
    println(Console.RED + format(msg) + s": ${exception.getClass.getSimpleName}")
  }

  protected def withTimeout[T](timeout: FiniteDuration)(future: CancellationToken => Future[T]): Future[T] = {
    val token = new CancellationTokenSource()
    Future.firstCompletedOf { Seq(
      future(token), // The real action
      after(timeout, context.system.scheduler) { // Timeout with cancellation token
        token.cancel()
        Future.failed(new TimeoutException("Task execution timeout"))
      })
    }
  }

  private def format(msg: String) = s"$now: [${getClass.getSimpleName}] $msg"
}