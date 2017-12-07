package com.mutantpaper.maya

import java.util.UUID
import java.util.regex.Matcher

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

object Messages {
  case class MetaData(user: String, channel: Option[String], timestamp: Long)
  case class Call(module: String, method: String, arguments: List[String]) {
    def getArguments(input: List[String], results: List[String], meta: MetaData): List[String] = {
      var str = arguments.mkString("---")
      input.zipWithIndex.foreach {
        case (item, index) => str = str.replaceAll("\\$" + index, Matcher.quoteReplacement(item))
      }
      results.reverse.zipWithIndex.foreach {
        case (result, index) => str = str.replaceAll("\\$res" + index, Matcher.quoteReplacement(result))
      }
      str = str.replaceAll("\\$user", Matcher.quoteReplacement(meta.user))
      str.split("---").toList
    }
  }
  object Call {
    val regex: Regex = """(\w+)\.(\w+)\((.*)\)""".r
    def splitArgs(string: String): List[String] = {
      var buffer: String          = ""
      val res: ListBuffer[String] = ListBuffer.empty[String]
      var incomplete: Int         = 0

      string.foreach {
        case '{' =>
          incomplete += 1
          buffer += "{"
        case '}' =>
          incomplete -= 1
          buffer += "}"
        case ',' if incomplete != 0 =>
          buffer += ","
        case ',' if incomplete == 0 =>
          res.append(buffer)
          buffer = ""
        case c: Char =>
          buffer += c
      }
      res.append(buffer)
      res.toList
    }
    def fromString(str: String): Call = str match {
      case regex(module, method, arguments) => Call(module, method, splitArgs(arguments))
    }
  }
  case class Skill(id: UUID, source: String, regex: String, procedure: List[Call])
  case class Operation(id: UUID, meta: MetaData, arguments: List[String], results: List[String], todo: List[Call]) {
    def next(newResult: String): Option[Operation] = todo match {
      case _ :: Nil  => None
      case _ :: tail => Some(Operation(id, meta, arguments, newResult :: results, tail))
    }
    def current: Call = todo.head
  }
}
