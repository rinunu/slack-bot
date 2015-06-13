package nu.rinu.slackbot

import nu.rinu.slackbot.util.Scheduler
import nu.rinu.slackbot.util.Utils._


object SleepNelBot extends App {
  private val Production = "production"

  private val env = getenvOption("ENV").getOrElse("develop")

  private def addShutdownHook() {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run(): Unit = {
        println("shutdown")
        Scheduler.shutdown()
      }
    }))
  }

  addShutdownHook()

  private val targetUser = if (env == Production) {
    "takashima"
  } else {
    "rinu"
  }

  val bots = for {i <- 0 to 10
                  token <- getenvOption("SLACK_TOKEN" + i)} yield {
    println(s"start bot $i")
    new SimpleBot(token, targetUser)
  }

  bots.head.messages.toBlocking.last
}
