package nu.rinu.slackbot

import nu.rinu.slackbot.addin.ShinchokuDodesuka
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

    // 1つめだけ特別なのつける
    if (i == 0) {
      new SimpleBot(token, List(
        new ShinchokuDodesuka(".*進捗.*".r, targetUser)
      ))
    } else {
      new SimpleBot(token)
    }
  }

  bots.head.messages.toBlocking.last
}
