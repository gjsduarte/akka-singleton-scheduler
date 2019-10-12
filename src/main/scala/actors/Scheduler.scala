package actors

import akka.actor.{Actor, Props}
import akka.pattern.{ask, AskTimeoutException}
import model.Job
import utils.ActorUtils

class Scheduler(jobs: Seq[Job]) extends Actor with ActorUtils {

  private val worker = context.system.actorOf(Props(new Worker))

  override def preStart(): Unit = jobs.foreach(schedule)

  // override postRestart so we don't call preStart and schedule a new message
  override def postRestart(reason: Throwable): Unit = {}

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
