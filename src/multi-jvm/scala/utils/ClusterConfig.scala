package utils

import akka.actor.ActorSystem
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config._

//noinspection TypeAnnotation
abstract class ClusterConfig extends MultiNodeConfig {

  protected val nodes: Seq[RoleName]

  protected def baseConfig: Config = ConfigFactory.parseString(s"""
    akka {
      actor.provider = cluster
      loglevel = "OFF"
      coordinated-shutdown {
        exit-jvm = off
        run-by-jvm-shutdown-hook = on
      }
      discovery {
        method = config
        config.services.local-cluster.endpoints = [
          ${nodes.map(node => s"""{
            host = localhost
            port = ${port(node)}
          }""").mkString(",")}
        ]
      }
      management {
        cluster.bootstrap.contact-point-discovery {
          service-name = "local-cluster"
          port-name = management
        }
        http.hostname = localhost
      }
    }""")

  protected def create(nr: Int): RoleName = {
    val node = role(nr.toString)
    nodeConfig(node) {
      ConfigFactory.parseString(s"akka.management.http.port = ${port(node)}")
    }
    node
  }

  protected def port(node: RoleName): String = {
    s"855${node.name}"
  }

  def initialize(config: Config): ActorSystem
}