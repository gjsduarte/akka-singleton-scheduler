package scheduling

import akka.actor.ActorSystem
import jobs.CounterJob
import org.scalatest._
import scheduling.JobSchedulerSpecs._
import utils._

import scala.concurrent.duration._

// One concrete test class per node
class JobSchedulerSpecsMultiJvmNode1 extends JobSchedulerSpecs
class JobSchedulerSpecsMultiJvmNode2 extends JobSchedulerSpecs

object JobSchedulerSpecs {

  //noinspection TypeAnnotation
  object Config extends ClusterConfig {

    // Initialize Test Objects
    var scheduler: JobScheduler = _
    val job = new CounterJob("test job", 500.millis, 1.second)

    // Initialize Nodes
    val node1 = create(1)
    val node2 = create(2)
    val nodes = Seq(node1, node2)

    // Initialize Configuration
    commonConfig(baseConfig)

    def initialize(config: com.typesafe.config.Config): ActorSystem = {
      scheduler = new JobScheduler(config)(job)
      scheduler.system
    }
  }
}

abstract class JobSchedulerSpecs
  extends ClusterSpec(Config)
    with WordSpecLike
    with Matchers {

  import Config._

  "JobScheduler" should {

    "initialize in all nodes" in {
      scheduler.initialize()
      enterBarrier("all-nodes-initialized")
    }

    "have all nodes up" in within(10.seconds) {
      assertMembersUp(node1, node2)
      enterBarrier("all-nodes-up")
    }

    "execute a job in all nodes" in within(5.seconds) {
      assertJobExecuted()
      enterBarrier("all-nodes-executing")
    }

    "keep oldest node up when newest node leaves" in within(30.seconds) {
      runOn(node1) {
        testConductor.exit(node2, 0).await
      }

      enterBarrier("newest-node-exit")

      runOn(node1) {
        assertMembersLeft(node2)
        assertMembersUnreachable()
        assertMembersUp(node1)
      }
    }

    "continue executing job in oldest node after newest node left" in within(5.seconds) {
      runOn(node1) {
        assertJobExecuted()
      }
    }
  }

  def assertJobExecuted(): Unit = {
    // Fetch the current task execution value
    val current = job.counter
    // Wait for the execution counter to increase
    awaitCond(job.counter > current)
  }
}