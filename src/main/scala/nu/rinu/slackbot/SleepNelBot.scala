package nu.rinu.slackbot

import nu.rinu.slackbot.util.Scheduler
import nu.rinu.slackbot.util.Utils._


object SleepNelBot extends App {
  // TODO messages.toBlocking.last

  private def addShutdownHook() {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run(): Unit = {
        println("shutdown")
        Scheduler.shutdown()
      }
    }))
  }

  addShutdownHook()

  for {i <- 0 to 10
       token <- getenvOption("SLACK_TOKEN" + i)
  } {
    println(s"start bot $i")
    new SimpleBot(token)
  }
}
