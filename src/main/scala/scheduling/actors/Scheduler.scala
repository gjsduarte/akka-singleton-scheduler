package scheduling.actors

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.Send
import akka.pattern.ask
import scheduling.model.Job
import scheduling.model.actions.{Execute, Schedule}
import utils.ActorUtils

class Scheduler(jobs: Seq[Job], path: ActorPath, mediator: ActorRef) extends Actor with ActorUtils {

  override def preStart(): Unit = {
    log("Scheduler elected!")
    jobs.foreach(schedule)
  }

  // override postRestart so we don't call preStart and schedule a new message
  override def postRestart(reason: Throwable): Unit = {}

  def receive: Receive = {
    case Schedule(jobName) =>
      jobs
        .find(_.name == jobName)
        .fold {
          log(s"Job $jobName not found!")
        } { job =>
          log(s"Executing job ${job.name}")
          ask(mediator, Send(path.toStringWithoutAddress, Execute(jobName), localAffinity = false))(job.timeout)
            .map(_ => log(s"Job ${job.name} executed successfully!"))
            .recover { case error: Throwable => log(s"Error executing job ${job.name}", error) }
            .andThen { case _ => schedule(job) } // send another periodic tick after the specified delay
        }
  }

  private def schedule(job: Job): Unit = {
    log(s"Scheduling job ${job.name}")
    context.system.scheduler.scheduleOnce(job.interval, self, Schedule(job.name))
  }
}
