package com.mutantpaper.maya

import akka.actor.{ActorRef, ActorSystem}
import com.mutantpaper.maya.modules._
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Main extends App {
  implicit val system = ActorSystem("Maya")
  implicit val materializer = ActorMaterializer()

  val scheduler = QuartzSchedulerExtension(system)

  val modules: Map[String, ActorRef] = Map(
    "core" -> system.actorOf(Core.props(), "core"),
    "rest" -> system.actorOf(Rest.props(), "rest"),
    "ssh" -> system.actorOf(Ssh.props(), "ssh")
  )

  val bindingFuture = Http().bindAndHandle(Routes.getRoute(modules("core")), "localhost", 8080)
}
