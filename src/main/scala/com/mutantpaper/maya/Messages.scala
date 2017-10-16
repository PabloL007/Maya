package com.mutantpaper.maya

import java.util.UUID

import scala.util.matching.Regex

object Messages {
  case class MetaData(user: String, channel: Option[String], timestamp: Long)
  case class Call(module: String, method:String, arguments: List[String]){
    def getArguments(input: List[String]) = {
      var str = arguments.mkString("---")
      input.zipWithIndex.foreach {
        case (item, index) => str = str.replaceAll("\\$" + index, item)
      }
      str.split("---").toList
    }
  }
  object Call{
    val regex: Regex = """(.+)\.(.+)\((.*)\)""".r
    def fromString(str: String): Call = str match {
      case regex(module, method, arguments) => Call(module, method, arguments.split(',').toList)
    }
  }
  case class Skill(id: UUID, source: String, regex: String, procedure: List[Call])
  case class Operation(id: UUID, meta: MetaData, arguments: List[String], results: List[String], todo: List[Call]){
    def next(newResult: String): Option[Operation] = todo match {
      case head :: Nil => None
      case _ :: tail => Some(Operation(id, meta, arguments, newResult :: results, tail))
    }
    def current: Call = todo.head
  }
}
