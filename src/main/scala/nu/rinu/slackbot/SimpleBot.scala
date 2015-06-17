package nu.rinu.slackbot

import java.net.URI
import java.util.Random

import nu.rinu.slackbot.addin.{Context, Handler}
import nu.rinu.slackbot.core.SlackRtmApi.{Channel, Message}
import nu.rinu.slackbot.core.SlackWebApi.Attachment
import nu.rinu.slackbot.core.{SlackRtmApi, SlackWebApi}
import nu.rinu.slackbot.util.SimSimiClient.Response
import nu.rinu.slackbot.util.Utils._
import nu.rinu.slackbot.util._
import org.quartz.{Job, JobExecutionContext}
import rx.lang.scala.Observable

import scala.util.control.NonFatal
import scala.util.matching.Regex


class SimpleBot(slackToken: String, additionalHandlers: Seq[Handler] = Seq.empty) {
  private val docomoApiKeyOption = getenvOption("DOCOMO_API_KEY")
  private val simsimiApiKeyOption = getenvOption("SIMSIMI_API_KEY")
  private val theCatApiKeyOption = getenvOption("THE_CAT_API_KEY")

  private val cseSearchEngineId = getenvOption("CSE_SEARCH_ENGINE_ID")
  private val cseApiKey = getenvOption("CSE_API_KEY")

  private val simsimiNgWords = Set("まっくす")

  private val rtmApi = new SlackRtmApi(slackToken)
  private val webApi = new SlackWebApi(slackToken)

  // 自分自身の発言は無視する(主に複数起動時)
  val messages = for {m <- rtmApi.getMessages if m.user != rtmApi.self} yield m

  private val random = new Random()

  private def log(a: Any) {
    println(a)
  }

  private type HandlerFn = Message => Boolean
  private var handlers = List.empty[HandlerFn]

  for {m <- messages} {
    handlers.find { f =>
      try {
        f(m)
      } catch {
        case NonFatal(e) =>
          log(e)
          false
      }
    }
  }

  /**
    */
  private def addHandler(f: HandlerFn): Unit = {
    handlers :+= f
  }

  /**
   * 指定した正規表現にマッチした時に f を実行する
   *
   * @param f 処理を行ったら true
   */
  private def addHandler(r: Regex)(f: HandlerFn): Unit = {
    val R = r
    addHandler { m =>
      m.text match {
        case R() => f(m)
        case _ => false
      }
    }
  }

  /**
   * チャンネルに発言します
   */
  private def say(channel: Channel, text: String): Unit = {
    // ちょっと間を空ける
    Thread.sleep(1000)
    rtmApi.send(channel, text)
  }

  /**
   * 画像を発言します
   */
  private def sayImage(channel: Channel, text: String, imageUri: URI): Unit = {
    webApi.chatPostMessage(
      channel,
      Some(text),
      List(Attachment("画像なの",
        image_url = Some(imageUri.toString)
      )),
      asUser = true
    )
  }

  /**
   * ボットの処理
   *
   * ここをいじってね
   */
  private def main(): Unit = {

    //    /**
    //     * 定期的に「進捗どうですか?」する
    //     */
    //    class ShinchokuDodesukaJob extends Job {
    //      override def execute(context: JobExecutionContext) {
    //        shinchokuDodesuka(targetUser)
    //      }
    //    }
    //
    //    Scheduler.add[ShinchokuDodesukaJob]("0 0 10-1/3 * * ?")

    // image search
    for {apiKey <- cseApiKey; searchEngineId <- cseSearchEngineId} {
      val customSearch = new GoogleCustomSearch(apiKey, searchEngineId)

      addHandler { m =>
        val textBody = m.text.replaceAll( """<@\w+>:?""", "")

        def searchOne(word: String): Unit = {
          for {res <- customSearch.searchImages(word)
          } {
            if (res.images.nonEmpty) {
              sayImage(m.channel, "探してきたの〜", randomly(res.images).uri)
            }
          }
        }

        def searchAll(word: String): Unit = {
          for {res <- customSearch.searchImages(word)
          } {
            val msg = "いっぱい探してきたの〜"
            webApi.chatPostMessage(
              m.channel,
              Some(msg),
              res.images.map(image => Attachment("画像なの",
                title = Some(image.title),
                title_link = Some(image.contextRui.toString),
                thumb_url = Some(image.thumbnailUri.toString))
              ),
              asUser = true)
          }
        }

        def search(word: String): Unit = {
          if (textBody.matches(".*(?:ぜんぶ|すべて|全て|all|list).*")) {
            searchAll(word)
          } else {
            searchOne(word)
          }
        }

        val Re = """(.*)(?:の?画像).*""".r
        val Re2 = ".*&lt;(.*?)&gt;.*".r

        textBody match {
          case Re(word) => search(word)
            true
          case Re2(word) => search(word)
            true
          case _ =>
            false
        }
      }
    }

    // neko search
    for {key <- theCatApiKeyOption} {
      val theCatApi = new TheCatApi(key)
      addHandler( """.*(?:ねこ|猫).*""".r) { m =>
        for {res <- theCatApi.request()} {
          sayImage(m.channel, s"ねこ探してきたのー", new URI(res.url))
        }
        true
      }
    }

    addHandler(".*まっくす.*".r) { m =>
      sayImage(m.channel, s"HAHAHA!", new URI("http://train.sleepnel.me/images/max.png"))
      true
    }

    addHandler(".*進捗(?:だめ|ダメ).*".r) { m =>
      if (m.text.contains(rtmApi.self.id)) {
        say(m.channel, s"@${m.user.name} ぴええええええ！")
        true
      } else {
        false
      }
    }

    // 優先度的にここに。そのうち全部 additionalHandler 化するの。
    for (h <- additionalHandlers) {
      addHandler { m =>
        h.apply(Context(rtmApi, webApi), m)
      }
    }

    // chat
    for {key <- simsimiApiKeyOption} {
      val simsimiApi = new SimSimiClient(key)

      def simsimi(m: Message): Observable[Response] = {
        val text = m.text.replaceAll( """<@\w+>""", " ").replaceAll(":", " ")

        simsimiApi.request(text, m.user.name).onErrorResumeNext(e => Observable.empty)
      }

      def hasNgWord(text: String) =
        simsimiNgWords.exists(ng => text.contains(ng))

      addHandler { m =>
        if (m.text.contains(rtmApi.self.id)) {
          for {res <- simsimi(m)} {
            say(m.channel, s"@${m.user.name} ${res.text}")
          }
          true
        }
        else {
          false
        }
      }
    }
  }

  main()
}
