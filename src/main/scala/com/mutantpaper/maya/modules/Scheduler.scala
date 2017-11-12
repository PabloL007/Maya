package com.mutantpaper.maya.modules

import java.util.UUID

import akka.actor.Props
import com.mutantpaper.maya.Messages.Operation
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

object Scheduler {
  def props(quartzExtension: QuartzSchedulerExtension): Props =
    Props(new Scheduler(quartzExtension))
}

/**
  * Scheduler module: Used for defining recurring tasks.
  *
  * @param quartzExtension A QuartzSchedulerExtension object
  */
class Scheduler(quartzExtension: QuartzSchedulerExtension) extends MModule {

  /**
    * Module method for simplified quartz scheduling.
    *
    * @param arguments Quartz cron expression :: Nil
    * @return A date in the form of a string, which indicates the first time the task will be performed
    */
  def schedule(arguments: List[String]): String = arguments match {
    case cron :: Nil =>
      val desc = s"Custom-${UUID.randomUUID()}"
      val op   = currOp.copy().next(cron).get
      quartzExtension.createSchedule(name = desc, cronExpression = cron)
      quartzExtension
        .schedule(name = desc, receiver = context.system.actorSelection(s"user/${op.current.module}"), msg = op)
        .toString
    case _ => "error"
  }

  val name    = "scheduler"
  val methods = Map("schedule" -> schedule _)

  var currOp: Operation = _

  // To prevent immediate execution of the task, the receive must be overridden
  def customReceive: Receive = {
    case op: Operation =>
      currOp = op
      invoke(op) match {
        case _ => log.debug(s"finished op ($op)")
      }
  }
  override def receive: Receive = customReceive orElse super[MModule].receive

  log.info(s"$name module started")
}
