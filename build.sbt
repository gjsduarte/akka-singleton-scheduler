/* scala versions and options */
scalaVersion := "2.12.7"

val akka = "2.5.25"

/* dependencies */
libraryDependencies ++= Seq(
  // -- Akka --
  "com.typesafe.akka" %% "akka-actor"         % akka,
  "com.typesafe.akka" %% "akka-cluster"       % akka,
  "com.typesafe.akka" %% "akka-cluster-tools" % akka,
  "com.typesafe.akka" %% "akka-discovery"     % akka,
  // -- Akka Cluster Bootstrap --
  // https://developer.lightbend.com/docs/akka-management/current/bootstrap/
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.0"
)