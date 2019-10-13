package utils

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorSystem, Address}
import akka.cluster._
import akka.remote.testconductor.RoleName
import akka.remote.testkit.MultiNodeSpec
import com.typesafe.config.Config
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.collection.immutable.SortedSet
import scala.language.implicitConversions

abstract class ClusterSpec(config: ClusterConfig)
  extends MultiNodeSpec(config, config.initialize)
    with Suite
    with BeforeAndAfterAll {

  private val cachedAddresses = new ConcurrentHashMap[RoleName, Address]()

  protected val cluster: Cluster = Cluster(system)

  override def initialParticipants: Int = roles.size

  implicit def address(role: RoleName): Address = {
    cachedAddresses.get(role) match {
      case null =>
        val address = node(role).address
        cachedAddresses.put(role, address)
        address
      case address => address
    }
  }

  override def beforeAll(): Unit = multiNodeSpecBeforeAll()

  override def afterAll(): Unit = multiNodeSpecAfterAll()

  def members: SortedSet[Member] = cluster.state.members

  def assertMembersUnreachable(address: Address*): Unit = address.foreach { addr =>
    if (address.isEmpty) {
      awaitCond(cluster.state.unreachable.isEmpty)
    } else {
      awaitCond(cluster.state.unreachable.exists(_.address == addr))
    }
  }

  def assertMembersUp(address: Address*): Unit = address.foreach { addr =>
    awaitCond(members.exists(member => member.address == addr && member.status == MemberStatus.Up))
  }

  def assertMembersLeft(address: Address*): Unit = address.foreach { addr =>
    awaitCond(!members.exists(member => member.address == addr))
  }
}
