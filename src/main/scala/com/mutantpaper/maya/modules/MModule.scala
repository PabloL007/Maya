package com.mutantpaper.maya.modules

import akka.actor.{ Actor, ActorLogging }
import com.mutantpaper.maya.Messages.{ Call, Operation }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

/**
  * Maya module trait, combines all the common logic modules should have.
  */
trait MModule extends Actor with ActorLogging {
  val name: String
  val methods: Map[String, (List[String]) => Future[String]]

  // It is recommended to use a different dispatcher for performing blocking operations
  implicit val blockingDispatcher = context.system.dispatchers.lookup("blocking-dispatcher")

  /**
    * Method for dynamically calling a module's methods by name.
    *
    * @param operation Operation object from which the current call can be extracted
    * @return The next operation
    */
  def invoke(operation: Operation): Future[String] = {
    val Call(_, method, _) = operation.current
    methods(method)(operation.current.getArguments(operation.arguments, operation.results, operation.meta))
  }

  def receive: Receive = {
    case op: Operation =>
      invoke(op) onComplete {
        case Success(result) =>
          op.next(result) match {
            case Some(nextOp) => context.system.actorSelection(s"user/${nextOp.current.module}") ! nextOp
            case None         => log.debug(s"The operation ($op) finished successfully")
          }
        case Failure(ex) => log.error(ex, s"The operation ($op) failed with error: ${ex.getMessage}")
      }

    case msg =>
      log.warning(s"$name module received an unexpected message ($msg)")
  }

  override def postStop(): Unit =
    log.info(s"$name module stopped")
}
