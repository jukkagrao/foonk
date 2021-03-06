name := "foonk"

version := "0.0.6-SNAPSHOT"

scalaVersion := "2.12.8"

val akkaHttpVersion = "10.1.7"
val scalaTestVersion = "3.0.5"
val swaggerAkkaVersion = "1.0.0"


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.github.swagger-akka-http" %% "swagger-akka-http" % swaggerAkkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.github.pureconfig" %% "pureconfig" % "0.10.1",
  "com.ibm.icu" % "icu4j" % "59.1",
  "org.scalactic" %% "scalactic" % scalaTestVersion % "test",
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
)

libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.0"

mainClass in Compile := Some("com.jukkagrao.foonk.Foonk")

mainClass in assembly := Some("com.jukkagrao.foonk.Foonk")


assemblyJarName in assembly := "foonk.jar"

fork in run := true
cancelable in Global := true