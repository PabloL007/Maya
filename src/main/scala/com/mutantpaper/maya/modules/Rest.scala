package com.mutantpaper.maya.modules

import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.Future
import scala.util.matching.Regex

object Rest {
  val headerRegex: Regex = """(.+):(.+)""".r

  def props(): Props =
    Props(new Rest())

  def stringList2HeaderList(strings: List[String]): List[RawHeader] = strings.flatMap {
    case headerRegex(key, value) => Some(RawHeader(key, value))
    case _                       => None
  }

  def string2ContentType(string: String): ContentType =
    ContentType.parse(string).right.getOrElse(ContentTypes.`text/plain(UTF-8)`)
}

/**
  * Rest module: Used for interacting with rest APIs.
  */
class Rest extends MModule {
  import Rest._
  implicit val materializer: ActorMaterializer = ActorMaterializer()(context.system)

  /**
    * Module method for performing http GET requests
    *
    * @param arguments Complete url :: Headers with format key:value :: Nil
    * @return The data retrieved from the endpoint or an exception if the number of arguments was incorrect
    */
  def get(arguments: List[String]): Future[String] = arguments match {
    case url :: Nil =>
      Http()(context.system).singleRequest(HttpRequest(uri = url)).map(_.entity.toString)
    case url :: headers =>
      Http()(context.system)
        .singleRequest(HttpRequest(uri = url, headers = stringList2HeaderList(headers)))
        .map(_.entity.toString)
    case _ =>
      Future.failed(
        new Exception(s"Wrong number of arguments for get method, found ${arguments.size} needed 1 or more")
      )
  }

  /**
    * Module method for performing http POST requests
    *
    * @param arguments Complete url :: Content type :: Body of the request :: Headers with format key:value :: Nil
    * @return The response from the endpoint or an exception if the number of arguments was incorrect
    */
  def post(arguments: List[String]): Future[String] = arguments match {
    case url :: contentType :: body :: Nil =>
      val request = HttpRequest(method = HttpMethods.POST,
                                uri = url,
                                entity = HttpEntity(string2ContentType(contentType), ByteString(body)))
      Http()(context.system).singleRequest(request).map(_.entity.toString)
    case url :: contentType :: body :: headers =>
      val request =
        HttpRequest(method = HttpMethods.POST,
                    uri = url,
                    entity = HttpEntity(string2ContentType(contentType), ByteString(body)),
                    headers = stringList2HeaderList(headers))
      Http()(context.system).singleRequest(request).map(_.entity.toString)
    case _ =>
      Future.failed(
        new Exception(s"Wrong number of arguments for post method, found ${arguments.size} needed 3 or more")
      )
  }

  val name    = "rest"
  val methods = Map("get" -> get _, "post" -> post _)

  log.info(s"$name module started")
}
