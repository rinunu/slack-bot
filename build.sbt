scalaVersion := "2.11.6"

enablePlugins(JavaAppPackaging)

enablePlugins(UniversalPlugin)

topLevelDirectory := None

organization := "nu.rinu"

name := "slack_bot"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "io.reactivex" %% "rxscala" % "0.24.1"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.0"


libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"

libraryDependencies += "com.google.guava" % "guava" % "18.0"

libraryDependencies += "org.json4s" %% "json4s-native" % "3.2.11"

libraryDependencies += "javax.websocket" % "javax.websocket-client-api" % "1.1"

libraryDependencies += "org.glassfish.tyrus" % "tyrus-client" % "1.10"

libraryDependencies += "org.glassfish.tyrus" % "tyrus-container-grizzly-server" % "1.10"

libraryDependencies += "org.quartz-scheduler" % "quartz" % "2.2.1"

libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "1.1.4"

libraryDependencies += "com.google.http-client" % "google-http-client" % "1.20.0"

// compile 'io.reactivex:rxjava-async-util:0.21.0'


// libraryDependencies += "com.google.http-client" % "google-http-client-jackson2" % "1.20.0"

// libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.5.1"

// libraryDependencies += "org.jsoup" % "jsoup" % "1.7.2"
