// *****************************************************************************
// Projects
// *****************************************************************************

lazy val Maya =
  project
    .in(file("."))
    .enablePlugins(JavaAppPackaging, AshScriptPlugin)
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akka,
        library.akkaHttp,
        library.akkaSl4j,
        library.janalyseSsh,
        library.json4sNative,
        library.parserCombinators,
        library.quartz,
        library.logbackClassic,
        library.scalaCheck % Test,
        library.scalaTest  % Test
      )
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {
    object Version {
      val akka = "2.4.20"
      val akkaHttp = "10.0.10"
      val logbackClassic = "1.2.3"
      val scalaCheck = "1.13.5"
      val janalyseSsh = "0.10.3"
      val json4sNative = "3.5.3"
      val parserCombinators = "1.0.6"
      val quartz = "1.6.0-akka-2.4.x"
      val scalaTest  = "3.0.3"
    }
    val akka = "com.typesafe.akka" %% "akka-actor" % Version.akka
    val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
    val akkaSl4j =  "com.typesafe.akka" %% "akka-slf4j" % Version.akka
    val janalyseSsh = "fr.janalyse" %% "janalyse-ssh" % Version.janalyseSsh
    val json4sNative = "org.json4s" %% "json4s-native" % Version.json4sNative
    val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logbackClassic
    val parserCombinators = "org.scala-lang.modules" %% "scala-parser-combinators" % Version.parserCombinators
    val quartz = "com.enragedginger" %% "akka-quartz-scheduler" % Version.quartz
    val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck
    val scalaTest  = "org.scalatest"  %% "scalatest"  % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++ dockerSettings

lazy val commonSettings =
  Seq(
    version := "0.2.0-SNAPSHOT",
    scalaVersion := "2.11.11",
    organization := "com.mutantpaper",
    organizationName := "Pablo Ley",
    startYear := Some(2017),
    scalacOptions ++= Seq(
      "-target:jvm-1.8",
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-unused",
      "-Ywarn-unused-import",
      "-language:_"
    ),
    unmanagedSourceDirectories.in(Compile) := Seq(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Seq(scalaSource.in(Test).value),
    shellPrompt in ThisBuild := { state =>
      val project = Project.extract(state).currentRef.project
      s"[$project]> "
    }
  )

lazy val dockerSettings =
  Seq(
    dockerBaseImage := "openjdk:8-jre-alpine"
  )