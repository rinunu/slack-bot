package nu.rinu.slackbot.util

import java.util.{Random, Date, Calendar}

/**
 * 雑多な便利メソッド
 */
object Utils {
  private val random = new Random()

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

  /**
   * 指定した要素からランダムに 1 つ選択して返します
   */
  def randomly[A](seq: Seq[A]): A = {
    seq(random.nextInt(seq.size))
  }
}
