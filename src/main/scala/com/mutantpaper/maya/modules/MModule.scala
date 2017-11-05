package com.mutantpaper.maya.modules

import akka.actor.{ Actor, ActorLogging }
import com.mutantpaper.maya.Messages.{ Call, Operation }

/**
  * Maya module trait, combines all the common logic modules should have.
  */
trait MModule extends Actor with ActorLogging {
  val name: String
  val methods: Map[String, (List[String]) => String]

  /**
    * Method for dynamically calling a module's methods by name.
    *
    * @param operation Operation object from which the current call can be extracted
    * @return The next operation
    */
  def invoke(operation: Operation): Option[Operation] = {
    val Call(_, method, _) = operation.current
    operation.next(
      methods(method)(operation.current.getArguments(operation.arguments, operation.results, operation.meta))
    )
  }

  def receive: Receive = {
    case op: Operation =>
      invoke(op) match {
        case Some(nextOp) => context.system.actorSelection(s"user/${nextOp.current.module}") ! nextOp
        case None         => log.debug(s"finished op ($op)")
      }

    case msg =>
      log.warning(s"$name module received an unexpected message ($msg)")
  }

  override def postStop(): Unit =
    log.info(s"$name module stopped")
}
