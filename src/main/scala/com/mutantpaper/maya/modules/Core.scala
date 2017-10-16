package com.mutantpaper.maya.modules

import java.util.UUID
import java.util.regex.Pattern

import akka.actor.Props
import com.mutantpaper.maya.Messages._

import scala.collection.mutable.ListBuffer

object Core {
  def props(): Props = {
    Props(new Core())
  }

  def interpret(skills: List[Skill], meta: MetaData, input: String): Option[Operation] = {
    skills.filter(s => input.matches(s.regex)) match {
      case head :: rest =>
        val matcher = Pattern.compile(head.regex).matcher(input)
        matcher.matches()
        Some(Operation(UUID.randomUUID(), meta, (for(i <- 1 to matcher.groupCount) yield matcher.group(i)).toList, List.empty[String], head.procedure))
      case Nil => None
    }
  }

  case class Request(meta: MetaData, input: String)
}
class Core extends MModule {
  import Core._

  def learn(string: List[String]): String = {
    log.info(string.toString())
    val procedure = string.head.split(" -> ").map{
      call => Call.fromString(call)
    }
    skills += Skill(UUID.randomUUID(), procedure.head.module, procedure.head.arguments.head, procedure.tail.toList)
    "done"
  }

  val name = "core"
  val methods = Map("learn" -> learn _)

  var skills: ListBuffer[Skill] = ListBuffer(Skill(UUID.fromString("06a6ca4f-d1f6-4542-96d5-aaad4ba6c6d7"), "rest", "learn \\{\\{(.+)\\}\\}", List(Call("core", "learn", List("$0")))))

  def customReceive: Receive = {
    case Request(meta, input) => interpret(skills.toList, meta, input) match {
      case Some(operation) =>
        sender() ! "started"
        context.system.actorSelection("user/" + operation.current.module) ! operation
      case None =>
        sender() ! "error"
        log.warning("No operation could be extracted")
    }
  }

  override def receive = customReceive orElse super[MModule].receive

  log.info(s"$name module started")
}
