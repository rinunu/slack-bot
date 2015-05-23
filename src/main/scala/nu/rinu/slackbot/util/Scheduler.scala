package nu.rinu.slackbot.util

import org.quartz.{Job, TriggerBuilder, CronScheduleBuilder, JobBuilder}
import org.quartz.impl.StdSchedulerFactory
import scala.reflect._

import scala.reflect.ClassTag

object Scheduler {
  private var nextId = 1

  private val scheduler = StdSchedulerFactory.getDefaultScheduler
  scheduler.start()

  def add[JOB <: Job : ClassTag](cronExpr: String) {
    // define the job and tie it to our HelloJob class
    val job = JobBuilder.newJob(classTag[JOB].runtimeClass.asInstanceOf[Class[Job]])
      .withIdentity("job" + nextId, "group1")
      .build()

    nextId += 1

    val trigger = TriggerBuilder.newTrigger()
      .withIdentity("trigger" + nextId, "group1")
      .withSchedule(CronScheduleBuilder.cronSchedule(cronExpr))
      .build()

    nextId += 1

    scheduler.scheduleJob(job, trigger)
  }

  def shutdown(): Unit = {
    scheduler.shutdown()
  }
}
