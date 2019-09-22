import akka.actor._
import akka.pattern._
import utils.ActorUtils

import scala.concurrent.duration._

case class Job(name: String, interval: FiniteDuration, timeout: FiniteDuration)

object Program {

  private val jobs = Seq(
    Job("1SecJob", 1.second, 2.seconds),
    Job("10SecJob", 10.second, 2.seconds)
  )

  def main(args: Array[String]): Unit = {
    val system: ActorSystem = ActorSystem("task-scheduler")
    system.actorOf(Props(new Scheduler(jobs)))
  }
}

class Scheduler(jobs: Seq[Job]) extends Actor with ActorUtils {

  private val worker = context.system.actorOf(Props(new Worker))

  override def preStart(): Unit = jobs.foreach(schedule)

  def receive: Receive = {
    case job: Job =>
      log(s"Executing job ${job.name}")
      ask(worker, job)(job.timeout)
        .map(_ => log(s"Job ${job.name} executed successfully!"))
        .recover { case e: AskTimeoutException => log(s"Error executing job ${job.name}: $e") }
        .andThen { case _ => schedule(job) } // send another periodic tick after the specified delay
  }

  private def schedule(job: Job): Unit = {
    log(s"Scheduling job ${job.name}")
    context.system.scheduler.scheduleOnce(job.interval, self, job)
  }
}

class Worker extends Actor with ActorUtils {

  def receive: Receive = {
    case job:Job => withTimeout(job.timeout) {
      log(s"Executing job ${job.name}")
      sleep(job.timeout) // This is where we actually execute the task
      log(s"Finished executing job ${job.name}")
    }.pipeTo(sender)
  }
}
