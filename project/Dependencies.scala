import sbt._
import Keys._

object Dependencies {

  val akkaVersion = "2.4.0"

  val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % akkaVersion
  val akkaCluster =  "com.typesafe.akka" %% "akka-cluster" % akkaVersion
  val akkaClusterMetrics = "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion
}