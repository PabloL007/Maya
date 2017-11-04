package com.mutantpaper.maya.modules

import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration._

object Rest {
  def props(): Props =
    Props(new Rest())
}

class Rest extends MModule {
  implicit val blockingDispatcher = context.system.dispatchers.lookup("blocking-dispatcher")
  implicit val materializer       = ActorMaterializer()(context.system)

  def get(arguments: List[String]): String = arguments match {
    case url :: Nil =>
      Await.result(Http()(context.system).singleRequest(HttpRequest(uri = url)), 10 seconds).entity.toString
    case _ => "error"
  }

  val name    = "rest"
  val methods = Map("get" -> get _)

  log.info(s"$name module started")
}
