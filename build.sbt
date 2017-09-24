name := "foonk"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.3"

val akkaHttpVersion = "10.0.10"
val scalaTestVersion = "3.0.1"
val swaggerAkkaVersion = "0.11.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.github.swagger-akka-http" %% "swagger-akka-http" % swaggerAkkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "org.scalactic" %% "scalactic" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

mainClass in Compile := Some("com.jukkagrao.foonk.Foonk")

mainClass in assembly := Some("com.jukkagrao.foonk.Foonk")
assemblyJarName in assembly := "foonk.jar"

cancelable in Global := true