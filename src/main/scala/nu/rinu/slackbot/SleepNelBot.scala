package nu.rinu.slackbot

import java.util.Random

import com.google.api.client.http.javanet.NetHttpTransport
import nu.rinu.slackbot.util.Scheduler
import nu.rinu.slackbot.util.SimSimiClient.Response
import nu.rinu.slackbot.util.SlackClient.{User, Channel, Message}
import nu.rinu.slackbot.util._
import org.quartz.{JobExecutionContext, Job}
import rx.lang.scala.Observable

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

  val slackToken = getenv("SLACK_TOKEN")
  val docomoApiKeyOption = getenvOption("DOCOMO_API_KEY")
  val simsimiApiKeyOption = getenvOption("SIMSIMI_API_KEY")
  val theCatApiKeyOption = getenvOption("THE_CAT_API_KEY")
  val env = getenvOption("ENV").getOrElse("develop")

  val simsimiNgWords = Set("まっくす")

  def addShutdownHook() {
    Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      def run(): Unit = {
        println("shutdown")
        Scheduler.shutdown()
      }
    }))
  }

  val client = new SlackClient(httpTransport, slackToken)
  // 自分自身の発言は無視する(主に複数起動時)
  val messages = for {m <- client.getMessages if m.user != client.self} yield m
  messages.onErrorResumeNext(a => Observable.empty)

  val random = new Random()

  type Handler = Message => Boolean
  var handlers = List.empty[Handler]
  for {m <- messages} {
    handlers.find(f => f(m))
  }

  /**
    */
  def addHandler(f: Handler): Unit = {
    handlers :+= f
  }

  /**
   * 指定した正規表現にマッチした時に f を実行する
   *
   * @param f 処理を行ったら true
   */
  def addHandler(r: Regex)(f: Handler): Unit = {
    val R = r
    addHandler { m =>
      m.text match {
        case R() => f(m)
        case _ => false
      }
    }
  }


  /**
   * ボットの処理
   *
   * ここをいじってね
   */
  def main(): Unit = {
    addShutdownHook()

    val targetUser = "takashima"

    val defaultChannel = if (env == "production") {
      client.getChannelByName("general")
    } else {
      client.getChannelByName("test2")
    }

    def say(channel: Channel, text: String): Unit = {
      // ちょっと間を空ける
      Thread.sleep(1000)
      client.send(channel, text)
    }

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

      say(defaultChannel, s"@$user 進捗どうですか?\n$url")
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
      true
    }

    addHandler(".*まっくす.*".r) { m =>
      say(m.channel, s"まっくすまっくす！")
      true
    }

    for {key <- theCatApiKeyOption} {
      val theCatApi = new TheCatApi(key)
      addHandler( """.*(?:ねこ|猫).*""".r) { m =>
        for {res <- theCatApi.request()} {
          say(m.channel, s"ねこ探してきたのー\n${res.url}")
        }
        true
      }
    }

    for {key <- simsimiApiKeyOption} {
      val simsimiApi = new SimSimiClient(key)

      def simsimi(m: Message): Observable[Response] = {
        val text = m.text.replaceAll( """<@\w+>""", " ").replaceAll(":", " ")

        simsimiApi.request(text, m.user.name).onErrorResumeNext(e => Observable.empty)
      }

      def hasNgWord(text: String) =
        simsimiNgWords.exists(ng => text.contains(ng))

      addHandler { m =>
        if (m.text.contains(client.self.id)) {
          for {res <- simsimi(m)} {
            say(m.channel, s"@${m.user.name} ${res.text}")
          }
          true
        }
        // ごくまれーに反応する
        else if (random.nextInt(10) == 0 && !hasNgWord(m.text)) {
          for {res <- simsimi(m)} {
            say(m.channel, s"@${m.user.name} ${res.text}")
          }
          true
        }
        false
      }
    }

    client.send(defaultChannel, s"もどりました〜")

    messages.toBlocking.last
  }

  main()
}
