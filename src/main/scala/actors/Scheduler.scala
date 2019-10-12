package actors

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.Send
import akka.pattern.{ask, AskTimeoutException}
import model.Job
import utils.ActorUtils

class Scheduler(jobs: Seq[Job], path: ActorPath, mediator: ActorRef) extends Actor with ActorUtils {

  override def preStart(): Unit = jobs.foreach(schedule)

  // override postRestart so we don't call preStart and schedule a new message
  override def postRestart(reason: Throwable): Unit = {}

  def receive: Receive = {
    case job: Job =>
      log(s"Executing job ${job.name}")
      ask(mediator, Send(path.toStringWithoutAddress, job, localAffinity = false))(job.timeout)
        .map(_ => log(s"Job ${job.name} executed successfully!"))
        .recover { case e: AskTimeoutException => log(s"Error executing job ${job.name}: $e") }
        .andThen { case _ => schedule(job) } // send another periodic tick after the specified delay
  }

  private def schedule(job: Job): Unit = {
    log(s"Scheduling job ${job.name}")
    context.system.scheduler.scheduleOnce(job.interval, self, job)
  }
}
