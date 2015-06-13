package nu.rinu.slackbot.util

import java.util.{Date, Calendar}

/**
 * 雑多な便利メソッド
 */
object Utils {
  def getenvOption(name: String): Option[String] = {
    Option(System.getenv(name))
  }

  def getenv(name: String): String = {
    getenvOption(name).getOrElse {
      throw new RuntimeException(s"環境変数 $name を設定してね")
    }
  }

  def getCurrentHours: Int = {
    val cal = Calendar.getInstance()
    cal.setTime(new Date)
    cal.get(Calendar.HOUR)
  }
}
