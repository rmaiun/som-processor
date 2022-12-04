ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.rmaiun"
ThisBuild / organizationName := "somprocessor"

resolvers += "confluent" at "https://packages.confluent.io/maven/"

lazy val root = (project in file("."))
  .settings(
    name := "som-processor",
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-core"         % "2.9.0",
      "org.typelevel"         %% "cats-effect"       % "3.4.1",
      "com.github.fd4s"       %% "fs2-kafka"         % "2.5.0",
      "com.github.fd4s"       %% "fs2-kafka-vulcan"  % "2.5.0",
      "com.github.fd4s"       %% "vulcan-enumeratum" % "1.8.3",
      "com.github.fd4s"       %% "vulcan-generic"    % "1.8.3",
      "dev.profunktor"        %% "fs2-rabbit"        % "4.1.1",
      "ch.qos.logback"         % "logback-classic"   % "1.2.3",
      "org.typelevel"         %% "log4cats-slf4j"    % "2.2.0",
      "com.github.pureconfig" %% "pureconfig"        % "0.14.0",
      "io.circe"              %% "circe-generic"     % "0.14.2",
      "io.circe"              %% "circe-parser"      % "0.14.2"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val formatAll = taskKey[Unit]("Run scala formatter for all projects")

formatAll := {
  (root / Compile / scalafmt).value
  (root / Test / scalafmt).value
}
