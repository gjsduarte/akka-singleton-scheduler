package scheduling

import scheduling.actors.Worker
import akka.actor._
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.Config
import scheduling.model.Job

object JobScheduler {
  val clusterKey = "task-scheduler"
}

class JobScheduler(config: Config)(jobs: Job*) {
  import JobScheduler._

  val system = ActorSystem(clusterKey, config)

  def initialize(): Unit = {
    AkkaManagement(system).start() // Akka Management hosts the HTTP routes used by bootstrap
    ClusterBootstrap(system).start() // Starting the bootstrap process needs to be done explicitly
    Cluster(system).registerOnMemberUp {
      val mediator = DistributedPubSub(system).mediator
      val worker = system.actorOf(Props(new Worker(jobs, mediator)))
      // Scheduler / Master as a singleton actor
      system.actorOf(
        ClusterSingletonManager.props(
          singletonProps = Props(new scheduling.actors.Scheduler(jobs, worker.path, mediator)),
          terminationMessage = PoisonPill,
          settings = ClusterSingletonManagerSettings(system)
        )
      )
    }
  }
}