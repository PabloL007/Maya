package com.mutantpaper.maya

import com.mutantpaper.maya.Messages._
import org.scalatest._
import org.scalatest.Matchers._

class MessagesSpec extends WordSpec {

  "The Messages object" should {
    "be able to convert a string to a call object" in {
      Call.fromString("core.learn($0)") shouldEqual Call("core", "learn", List("$0"))
    }
  }
}
