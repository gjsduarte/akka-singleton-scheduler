package actors

import akka.actor.{Actor, ActorRef}
import akka.cluster.pubsub.DistributedPubSubMediator.Put
import akka.pattern.pipe
import model.Job
import utils.ActorUtils

class Worker(mediator: ActorRef) extends Actor with ActorUtils {

  // register to the path
  mediator ! Put(self)

  def receive: Receive = {
    case job:Job => withTimeout(job.timeout) {
      log(s"Executing job ${job.name}")
      sleep(job.timeout) // This is where we actually execute the task
      log(s"Finished executing job ${job.name}")
    }.pipeTo(sender)
  }
}
