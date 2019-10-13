import com.typesafe.config._
import jobs.SleepJob
import scheduling.JobScheduler

import scala.concurrent.duration._

// Nodes
object Node1 extends Program(1) with App
object Node2 extends Program(2) with App

abstract class Program(nodeNr: Int) {

  new JobScheduler(configure(nodeNr))(
    new SleepJob("1SecJob", 1.second, 2.seconds),
    new SleepJob("10SecJob", 10.second, 2.seconds)
  ).initialize()

  private def configure(nr: Int): Config = {
    ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.hostname = "127.0.0.$nr"
      akka.management.http.hostname = "127.0.0.$nr"
    """).withFallback(ConfigFactory.load())
  }
}