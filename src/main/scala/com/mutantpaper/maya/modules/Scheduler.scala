package com.mutantpaper.maya.modules

import java.util.UUID

import akka.actor.Props
import com.mutantpaper.maya.Messages.Operation
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension

object Scheduler {
  def props(quartzExtension: QuartzSchedulerExtension): Props = {
    Props(new Scheduler(quartzExtension))
  }
}

class Scheduler(quartzExtension: QuartzSchedulerExtension) extends MModule {
  def schedule(arguments: List[String]): String = arguments match {
    case cron :: Nil =>
      val desc = s"Custom-${UUID.randomUUID()}"
      val op = currOp.copy().next(cron).get
      quartzExtension.createSchedule(name = desc, cronExpression = cron)
      quartzExtension.schedule(name = desc, receiver = context.system.actorSelection(s"user/${op.current.module}"), msg = op).toString
  }

  val name = "scheduler"
  val methods = Map("schedule" -> schedule _)

  var currOp: Operation = _

  def customReceive: Receive = {
    case op: Operation =>
      currOp = op
      invoke(op) match {
        case _ => log.debug(s"finished op ($op)")
      }
  }

  override def receive = customReceive orElse super[MModule].receive

  log.info(s"$name module started")
}
