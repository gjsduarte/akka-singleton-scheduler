package actors

import akka.actor.Actor
import akka.pattern.pipe
import model.Job
import utils.ActorUtils

class Worker extends Actor with ActorUtils {

  def receive: Receive = {
    case job:Job => withTimeout(job.timeout) {
      log(s"Executing job ${job.name}")
      sleep(job.timeout) // This is where we actually execute the task
      log(s"Finished executing job ${job.name}")
    }.pipeTo(sender)
  }
}
