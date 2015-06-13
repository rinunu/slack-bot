package nu.rinu.slackbot.core

import java.net.URI

import nu.rinu.slackbot.core.SlackClient.Channel
import nu.rinu.slackbot.core.SlackWebApi.{Attachment, ResponseJson}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization

import scalaj.http.Http

object SlackWebApi {

  case class ResponseJson()

  case class Response()

  case class Field(
    title: String,
    value: String,
    short: Boolean = false
  )

  /**
   * https://api.slack.com/docs/attachments
   */
  case class Attachment(
    fallback: String,

    color: Option[String] = None,

    pretext: Option[String] = None,

    author_name: Option[String] = None,
    author_link: Option[String] = None,
    author_icon: Option[String] = None,

    title: Option[String] = None,
    title_link: Option[String] = None,

    text: Option[String] = None,

    fields: Seq[Field] = Seq.empty,

    image_url: Option[String] = None,
    thumb_url: Option[String] = None
  )

}

/**
 * Slack API Methods を呼び出します
 *
 * https://api.slack.com/methods
 */
class SlackWebApi(token: String) {
  private implicit val formats = DefaultFormats

  val endpoint = new URI("https://slack.com/api/")

  def chatPostMessage(channel: Channel,
    attachments: Seq[Attachment]
  ) {
    chatPostMessage(channel, None, attachments)
  }

  def chatPostMessage(channel: Channel,
    text: String
  ) {
    chatPostMessage(channel, Some(text), List.empty)
  }

  def chatPostMessage(channel: Channel,
    textOption: Option[String],
    attachments: Seq[Attachment],
    asUser: Boolean = false
  ) {

    val req = Http(endpoint.resolve("chat.postMessage").toString)
      .params(
        "token" -> token,
        "channel" -> channel.id,
        "attachments" -> Serialization.write(attachments),
        "as_user" -> asUser.toString
      )
      .params(
        textOption.map("text" -> _).toSeq
      )

    req.timeout(2000, 2000)

    val res = req.asString
    if (res.isSuccess) {
//      println("受信 " + res.body)
      val resJson = Serialization.read[ResponseJson](res.body)
    } else {
      sys.error(s"error: code = ${res.code}, ${res.body}")
    }
  }
}
