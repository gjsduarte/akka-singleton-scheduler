package utils

import java.time.LocalDateTime._

import akka.actor.Actor
import akka.pattern.after

import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.concurrent.duration.FiniteDuration

import scala.util.Random

trait ActorUtils { this: Actor =>

  implicit protected val executionContext: ExecutionContext = context.dispatcher

  private val random = new Random()

  protected def log(msg: String): Unit = {
    println(s"$now: [${getClass.getSimpleName}] $msg")
  }

  protected def sleep(duration: FiniteDuration): Unit = {
    val millis = duration.toMillis.toInt
    val sleep = random.nextInt(millis) // + (millis / 2) // <-- This will make it sleep 50% longer
    Thread.sleep(sleep)
  }

  protected def withTimeout[T](timeout: FiniteDuration)(action: => T): Future[T] =
    withTimeoutF(timeout)(Future(action))

  protected def withTimeoutF[T](timeout: FiniteDuration)(future: => Future[T]): Future[T] = {
    Future.firstCompletedOf(Seq(future, after(timeout, context.system.scheduler) {
      Future.failed(new TimeoutException("Task execution timeout"))
    }))
  }

}
