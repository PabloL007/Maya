package com.mutantpaper.maya

import java.util.UUID
import com.mutantpaper.maya.Messages._

import scala.util.parsing.combinator.RegexParsers

object MSDLParser extends RegexParsers {
  val test: String = """
               core.in("post note with "title (.+) and content (.+)")
                 -> rest.post("https://api.pushbullet.com/v2/pushes","application/json","{body:$1,title:$0,type:note}","Access-Token:x.xxxxxxxxxxxxxxx");

               core.in("write (.+) in (.+)")
                 -> rest.get("$0")
                 -> ssh.execute("pi", "echo '$res0' > test/$user_$1.txt");
             """.stripMargin

  sealed trait MSDLToken
  case class IDENTIFIER(str: String) extends MSDLToken
  case class LITERAL(str: String) extends MSDLToken
  case object DOT extends MSDLToken
  case object COMMA extends MSDLToken
  case object ARROW extends MSDLToken
  case object SEMICOLON extends MSDLToken
  case object LEFT_PARENTHESIS extends MSDLToken
  case object RIGHT_PARENTHESIS extends MSDLToken


  def identifier: Parser[IDENTIFIER] = {
    "[a-zA-Z_][a-zA-Z0-9_]*".r ^^ { str => IDENTIFIER(str) }
  }

  def literal: Parser[LITERAL] = {
    """"[^"]*"""".r ^^ { str =>
      val content = str.substring(1, str.length - 1)
      LITERAL(content)
    }
  }

  def dot = "." ^^ (_ => DOT)
  def comma = "," ^^ (_ => COMMA)
  def arrow = "->" ^^ (_ => ARROW)
  def semicolon = ";" ^^ (_ => SEMICOLON)
  def leftparenthesis = "(" ^^ (_ => LEFT_PARENTHESIS)
  def rightparenthesis = ")" ^^ (_ => RIGHT_PARENTHESIS)

  def arguments: Parser[List[String]] = literal ~ opt(comma ~ arguments) ^^ {
    case lit ~ None => lit.str :: Nil
    case lit ~ Some(COMMA ~ b) => lit.str :: b
  }
  def call: Parser[Call] = identifier ~ dot ~ identifier ~ leftparenthesis ~ arguments ~ rightparenthesis ^^ {
    case i1 ~ DOT ~ i2 ~ LEFT_PARENTHESIS ~ args ~ RIGHT_PARENTHESIS => Call(i1.str, i2.str, args)
  }
  def skill: Parser[Skill] = call ~ opt(arrow ~ skill) ^^ {
    case c ~ None => Skill(UUID.randomUUID(), "", "", c :: Nil)
    case c ~ Some(ARROW ~ s) => Skill(UUID.randomUUID(), "", c.arguments.head, c :: s.procedure)
  }
  def skillset: Parser[List[Skill]] = skill ~ semicolon ~ opt(skillset) ^^ {
    case s ~ SEMICOLON ~ None => s :: Nil
    case s ~ SEMICOLON ~ Some(b) => s :: b
  }

  trait MSDLCompilationError
  case class MSDLLexerError(location: Location,msg: String) extends MSDLCompilationError

  case class Location(line: Int, column: Int) {
    override def toString = s"$line:$column"
  }

  def apply(code: String): Either[MSDLLexerError, List[Skill]] = {
    parse(skillset, code) match {
      case NoSuccess(msg, next) => Left(MSDLLexerError(Location(next.pos.line, next.pos.column), msg))
      case Success(result, next) => Right(result)
    }
  }
}