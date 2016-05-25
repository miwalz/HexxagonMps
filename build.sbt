name := "Hexxagon"

version := "1.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(

  // logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3",

  // testing
  "org.specs2" %% "specs2-core" % "3.6.5" % "test",
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",

  // gui
  "org.scala-lang" % "scala-swing" % "2.11+",
  "org.scalafx" %% "scalafx" % "8.0.60-R9"

)

scalacOptions in Test ++= Seq("-Yrangepos")