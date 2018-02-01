name := "foonk"

version := "0.0.4-SNAPSHOT"

scalaVersion := "2.12.4"

val akkaHttpVersion = "10.1.0-RC1"
val akkaVersion = "2.5.9"
val scalaTestVersion = "3.0.1"
val swaggerAkkaVersion = "0.12.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.github.swagger-akka-http" %% "swagger-akka-http" % swaggerAkkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.github.pureconfig" %% "pureconfig" % "0.8.0",
  "com.ibm.icu" % "icu4j" % "59.1",
  "org.scalactic" %% "scalactic" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
)


mainClass in Compile := Some("com.jukkagrao.foonk.Foonk")

mainClass in assembly := Some("com.jukkagrao.foonk.Foonk")

assemblyJarName in assembly := "foonk.jar"

fork in run := true
cancelable in Global := true