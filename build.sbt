import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

val akkaVersion = "2.5.25"

lazy val `akka-singleton-scheduler` = project
  .in(file("."))
  .enablePlugins(MultiJvmPlugin)
  .settings(multiJvmSettings: _*)
  .settings(
    scalaVersion := "2.12.7",
    /* dependencies */
    libraryDependencies ++= Seq(
      // -- Akka Cluster Bootstrap --
      // https://developer.lightbend.com/docs/akka-management/current/bootstrap/
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.0",
      // -- Akka --
      "com.typesafe.akka" %% "akka-actor"              % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster"            % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-tools"      % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery"          % akkaVersion,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % Test,
      // -- ScalaTest --
      "org.scalatest"     %% "scalatest"               % "3.0.7"     % Test
    )
  )
  .configs(MultiJvm)
