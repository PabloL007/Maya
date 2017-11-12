package com.mutantpaper.maya

import akka.actor.{ ActorRef, ActorSystem }
import com.mutantpaper.maya.modules._
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object Main extends App {
  implicit val system: ActorSystem             = ActorSystem("Maya")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  val conf                                     = ConfigFactory.load()

  val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(system)

  val modules: Map[String, ActorRef] = Map(
    "core"      -> system.actorOf(Core.props(), "core"),
    "rest"      -> system.actorOf(Rest.props(), "rest"),
    "ssh"       -> system.actorOf(Ssh.props(), "ssh"),
    "scheduler" -> system.actorOf(Scheduler.props(scheduler), "scheduler")
  )

  val host = conf.getString("maya.host")
  val port = conf.getInt("maya.port")

  val bindingFuture = Http().bindAndHandle(Routes.getRoute(modules("core")), host, port)
}
