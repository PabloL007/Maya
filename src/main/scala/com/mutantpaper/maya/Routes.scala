package com.mutantpaper.maya

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import com.mutantpaper.maya.Messages.MetaData
import com.mutantpaper.maya.modules.Core.Request
import akka.pattern.ask

import scala.concurrent.duration._

object Routes {
  implicit val timeout: Timeout = 5 seconds

  def getRoute(core: ActorRef) =
    pathPrefix("api") {
      pathEndOrSingleSlash {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Welcome to the Maya api!</h1>"))
      } ~
      path("operations") {
        post {
          decodeRequest {
            // unmarshal with in-scope unmarshaller
            entity(as[String]) { msg =>
              complete((core ? Request(MetaData("anonymous", None, 1L), msg)).mapTo[String])
            }
          }
        }
      }
    }
}
