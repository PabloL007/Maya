package com.mutantpaper.maya.modules

import java.util.UUID
import java.util.regex.Pattern

import akka.actor.Props
import com.mutantpaper.maya.Messages._
import com.mutantpaper.maya.MSDLParser

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

object Core {
  def props(): Props =
    Props(new Core())

  /**
    * Function used for parsing commands from incoming messages.
    *
    * @param skills Immutable copy of the list of skills
    * @param meta Metadata for the request
    * @param input Raw message
    * @return An operation object if a command could be extracted
    */
  def interpret(skills: List[Skill], meta: MetaData, input: String): Option[Operation] =
    skills.filter(s => input.matches(s.regex)) match {
      case head :: _ =>
        val matcher = Pattern.compile(head.regex).matcher(input)
        matcher.matches()
        Some(
          Operation(
            UUID.randomUUID(),
            meta,
            (for (i <- 1 to matcher.groupCount) yield matcher.group(i)).toList,
            List.empty[String],
            head.procedure
          )
        )
      case Nil => None
    }

  case class Request(meta: MetaData, input: String)
}

/**
  * Core module: Language processor, used to store and interpret skills and process commands that make use of them.
  */
class Core extends MModule {
  import Core._

  /**
    * Module method for parsing Maya SDL (Skill Definition Language) into actual skill objects.
    *
    * @param arguments MSDL string :: Nil
    * @return The new skill's id
    */
  def learn(arguments: List[String]): Future[String] = {
    log.info(arguments.toString())
    /*
    val procedure = arguments.head.split(" -> ").map { call =>
      Call.fromString(call)
    }
    val id = UUID.randomUUID()
    skills += Skill(id, procedure.head.module, procedure.head.arguments.head, procedure.tail.toList)
    Future.successful(id.toString)
    */
    MSDLParser(arguments.head) match {
      case Right(skill::tail) =>
        log.info(skill.toString)
        skills += Skill(skill.id, skill.source, skill.regex, skill.procedure.tail)
        Future.successful(skill.id.toString)
      case Left(error) =>
        Future.failed(new Exception(error.toString))
    }
  }

  val name    = "core"
  val methods = Map("learn" -> learn _)

  // Maya at this point needs to at least have a learn skill
  var skills: ListBuffer[Skill] = ListBuffer(
    Skill(
      UUID.fromString("06a6ca4f-d1f6-4542-96d5-aaad4ba6c6d7"),
      "rest",
      "learn \\{\\{(.+)\\}\\}",
      List(Call("core", "learn", List("$0")))
    )
  )

  // Needs to be able to handle Requests in addition to operations
  def customReceive: Receive = {
    case Request(meta, input) =>
      interpret(skills.toList, meta, input) match {
        case Some(operation) =>
          sender() ! "started"
          context.system.actorSelection(s"user/${operation.current.module}") ! operation
        case None =>
          sender() ! "error"
          log.warning("No operation could be extracted")
      }
  }
  override def receive: Receive = customReceive orElse super[MModule].receive

  log.info(s"$name module started")
}
