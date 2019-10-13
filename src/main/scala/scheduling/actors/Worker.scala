package scheduling.actors

import akka.actor._
import akka.cluster.pubsub.DistributedPubSubMediator.Put
import akka.pattern.pipe
import scheduling.model.actions.Execute
import scheduling.model.Job
import utils.ActorUtils

class Worker(jobs: Seq[Job], mediator: ActorRef) extends Actor with ActorUtils {

  // register to the path
  mediator ! Put(self)

  def receive: Receive = {
    case Execute(jobName) =>
      jobs
        .find(_.name == jobName)
        .fold {
          log(s"Job $jobName not found!")
        } { job =>
          withTimeout(job.timeout) { implicit token =>
            log(s"Executing job ${job.name}")
            job.execute()
              .map(_ => log(s"Job ${job.name} executed successfully!"))
              .recover { case error => log(s"Error executing job ${job.name}", error) }
          }.pipeTo(sender)
        }
  }
}
