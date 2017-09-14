name := "CsvTask"

version := "1.0"

scalaVersion := "2.12.3"

import scala.language.postfixOps

libraryDependencies ++= {
  object V {
    val specsVersion = "3.8.9"
    val akkaVersion = "2.5.4"
  }

  import V._

  val testing = Seq(
    "org.specs2" %% "specs2-core" % specsVersion,
    "org.specs2" %% "specs2-mock" % specsVersion
  ) map {
    _ % "test"
  }

  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.0.10"

  testing ++ Seq(akkaStream, akkaActor, akkaHttp)

}
