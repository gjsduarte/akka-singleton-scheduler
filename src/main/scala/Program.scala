import akka.actor._
import akka.cluster.singleton._
import akka.cluster.Cluster
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.{Config, ConfigFactory}
import model.Job

import scala.concurrent.duration._

// Nodes
object Node1 extends Program(1) with App
object Node2 extends Program(2) with App

abstract class Program(nodeNr: Int) {

  private val jobs = Seq(
    Job("1SecJob", 1.second, 2.seconds),
    Job("10SecJob", 10.second, 2.seconds)
  )

  val system = ActorSystem("task-scheduler", configure(nodeNr))
  AkkaManagement(system).start() // Akka Management hosts the HTTP routes used by bootstrap
  ClusterBootstrap(system).start() // Starting the bootstrap process needs to be done explicitly
  Cluster(system).registerOnMemberUp {
    // Scheduler / Master as a singleton actor
    system.actorOf(
      ClusterSingletonManager.props(
        singletonProps = Props(new actors.Scheduler(jobs)),
        terminationMessage = PoisonPill,
        settings = ClusterSingletonManagerSettings(system)
      )
    )
  }

  private def configure(nr: Int): Config = {
    ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.hostname = "127.0.0.$nr"
      akka.management.http.hostname = "127.0.0.$nr"
    """).withFallback(ConfigFactory.load())
  }
}

