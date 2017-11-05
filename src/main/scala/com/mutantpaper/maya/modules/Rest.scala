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

/**
  * Rest module: Used for interacting with rest APIs.
  */
class Rest extends MModule {
  // It is recommended to use a different dispatcher for performing blocking operations
  implicit val blockingDispatcher              = context.system.dispatchers.lookup("blocking-dispatcher")
  implicit val materializer: ActorMaterializer = ActorMaterializer()(context.system)

  /**
    * Module method for performing http GET operations
    *
    * @param arguments Complete url :: Nil
    * @return The data retrieved from the endpoint or the word error if the number of arguments was incorrect
    */
  def get(arguments: List[String]): String = arguments match {
    case url :: Nil =>
      Await.result(Http()(context.system).singleRequest(HttpRequest(uri = url)), 10 seconds).entity.toString
    case _ => "error"
  }

  val name    = "rest"
  val methods = Map("get" -> get _)

  log.info(s"$name module started")
}
