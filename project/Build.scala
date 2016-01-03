import sbt._
import Keys._
import Dependencies._

object ApplicationBuild extends Build {
  override lazy val settings = super.settings ++ Seq(
    version := "1.0",
    scalaVersion := "2.11.7",
    libraryDependencies ++= Seq(akka, akkaRemote, akkaCluster, akkaClusterMetrics),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )
}