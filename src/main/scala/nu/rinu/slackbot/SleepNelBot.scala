package nu.rinu.slackbot

import java.util.Random

import com.google.api.client.http.javanet.NetHttpTransport
import nu.rinu.slackbot.util.SlackClient.Message
import nu.rinu.slackbot.util.{DialogueApi, Scheduler, SlackClient}
import org.quartz._

import scala.util.matching.Regex


object SleepNelBot extends App {
  private val httpTransport = new NetHttpTransport

  def getenvOption(name: String): Option[String] = {
    Option(System.getenv(name))
  }

  def getenv(name: String): String = {
    getenvOption(name).getOrElse {
      throw new RuntimeException(s"環境変数 $name を設定してね")
    }
  }


  val docomoApiKeyOption = getenvOption("DOCOMO_API_KEY")
  val slackToken = getenv("SLACK_TOKEN")

  def addShutdownHook() {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run(): Unit = {
        println("shutdown")
        Scheduler.shutdown()
      }
    }))
  }

  /**
   * 指定した正規表現にマッチした時に f を実行する
   */
  def addHandler(r: Regex)(f: Message => Any): Unit = {
    val R = r
    for {m <- messages} {
      m.text match {
        case R() => f(m)
        case _ =>
      }
    }
  }

  /**
   * 指定した正規表現にマッチした時に f を実行する
   */
  def addHandler(f: Message => Any): Unit = {
    for {m <- messages} {
      f(m)
    }
  }

  val client = new SlackClient(httpTransport, slackToken)
  // 自分自身の発言は無視する(主に複数起動時)
  val messages = for {m <- client.getMessages if m.user != client.self} yield m

  val random = new Random()

  /**
   * ボットの処理
   *
   * ここをいじってね
   */
  def main(): Unit = {
    addShutdownHook()

    val targetUser = "takashima"
    //    val defaultChannel = client.getChannelByName("test2")
    val defaultChannel = client.getChannelByName("general")

    /**
     * 「進捗どうですか?」する
     */
    def shinchokuDodesuka(user: String): Unit = {
      val urls = Seq(
        "http://img2.finalfantasyxiv.com/accimg/01/00/0100ad42c5b1eb63e34023b2673dbd1575a71cc2.jpg",
        "http://40.media.tumblr.com/c47cbed880e821146fb67a6cf1d9993d/tumblr_mqpqr1e07H1sckns5o1_1280.jpg",
        "http://blog-imgs-64.fc2.com/a/k/a/akami1028/tumblr_mrxbyrqiN01sckns5o1_r1_500.jpg",
        "http://37.media.tumblr.com/724b165a61e0d310e1715bc4cb74c538/tumblr_mxzj2hPEE01ro6w1ho1_500.jpg",
        "http://img.gifmagazine.net/gifmagazine/images/129827/original.gif",
        "http://41.media.tumblr.com/e38911a2b6c4782a4bd0ccd33511bd63/tumblr_n86la8y54X1sckns5o1_500.png",
        "http://36.media.tumblr.com/c79d6b59dc4ee10595564c8aafc2225e/tumblr_mrkrngeogp1sckns5o1_500.png",
        "http://40.media.tumblr.com/bcdb555421139b93fcf516bc9ac7fff9/tumblr_n9zdk1Btkx1sckns5o1_1280.jpg"
      )
      val url = urls(random.nextInt(urls.size)) + "?" + random.nextInt(1000)
      client.send(defaultChannel, s"@$user 進捗どうですか?\n$url")
    }

    /**
     * 定期的に「進捗どうですか?」する
     */
    class ShinchokuDodesukaJob extends Job {
      override def execute(context: JobExecutionContext) {
        shinchokuDodesuka(targetUser)
      }
    }

    Scheduler.add[ShinchokuDodesukaJob]("0 0 10-1/3 * * ?")

    addHandler(".*進捗.*".r) { m =>
      shinchokuDodesuka(targetUser)
    }

    addHandler(".*まっくす.*".r) { m =>
      client.send(m.channel, s"まっくすまっくす！")
    }

    for {key <- docomoApiKeyOption} {
      val dialogueApi = new DialogueApi(key)
      addHandler { m =>
        if (m.text.contains(client.self.id)) {
          for {res <- dialogueApi.dialogue(m.text, m.user.name)} {
            client.send(m.channel, s"@${m.user.name} ${res.text}")
          }
        }
      }
    }

    val ver = "1"
    client.send(defaultChannel, s"もどりました〜")

    messages.toBlocking.last
  }

  main()
}
