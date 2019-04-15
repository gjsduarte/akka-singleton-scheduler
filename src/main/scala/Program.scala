import akka.actor._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Job(name: String, interval: FiniteDuration, timeout: FiniteDuration)

object Program {

  private val jobs = Seq(
    Job("1SecJob", 1.second, 2.seconds),
    Job("10SecJob", 10.second, 2.seconds)
  )

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("task-scheduler")
    jobs.foreach(schedule)
  }

  private def schedule(job: Job)(implicit system: ActorSystem): Unit = {
    system.scheduler.scheduleOnce(job.interval) {
      println(s"Executing job ${job.name}")
      schedule(job)
    }
  }

}