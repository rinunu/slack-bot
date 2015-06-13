package nu.rinu.slackbot.core

import java.net.URI
import javax.websocket._

import com.google.api.client.http.{GenericUrl, HttpTransport}
import nu.rinu.slackbot.core.SlackClient._
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import rx.lang.scala.Observable
import rx.lang.scala.subjects.PublishSubject

object SlackClient {

  /**
   * すべてのメッセージに共通するもの
   */
  private case class BaseEventJson(`type`: Option[String], subtype: Option[String], reply_to: Option[Int]) {
    def eventType = `type`
  }

  private case class SimpleMessageEventJson(channel: String, user: String, text: String)

  private case class UserJson(
                               id: String,
                               name: String
                               )


  private case class ChannelJson(
                                  id: String,
                                  name: String
                                  )

  private case class RtmStartJson(
                                   url: String,
                                   ok: Boolean,
                                   self: UserJson,
                                   team: Any,
                                   users: Array[UserJson],
                                   channels: Array[ChannelJson]
                                   )

  /**
   * 送信用
   */
  private case class MessageJson(
                                  id: Int,
                                  channel: String,
                                  text: String,
                                  `type`: String = "message"
                                  )

  case class Channel(
                      id: String,
                      name: String
                      )

  case class User(
                   id: String,
                   name: String
                   )

  case class Message(
                      channel: Channel,
                      user: User,
                      text: String
                      )


}

/**
 * 発言をひたすら通知します
 */
@ClientEndpoint()
class SlackClient(httpTransport: HttpTransport, token: String) {
  private var userSession: Session = _

  private implicit val formats = DefaultFormats

  private val subject = PublishSubject[Message]()

  private var users: Array[User] = Array.empty
  private var userMap: Map[String, User] = Map.empty

  private var channels_ : Array[Channel] = Array.empty

  private var nextEventId = 1

  private var self_ : User = _

  connect()

  private def connect() {
    log("WS 接続情報を取得します")
    val requestFactory = httpTransport.createRequestFactory()
    val url = new GenericUrl("https://slack.com/api/rtm.start?token=" + token)

    val req = requestFactory.buildGetRequest(url)
    val res = req.execute()

    val wsUri = try {
      val resStr = res.parseAsString()
      log(resStr)

      val startRes = parseAs[RtmStartJson](resStr)
      if (!startRes.ok) {
        throw new RuntimeException("接続失敗")
      }
      log("WS 接続情報を取得しました")

      self_ = user(startRes.self)
      users = startRes.users.map(user)
      userMap = users.map(u => u.id -> u).toMap
      channels_ = startRes.channels.map(channel)

      new URI(startRes.url)
    } finally {
      res.disconnect()
    }

    log("WS 接続します")
    val container = ContainerProvider.getWebSocketContainer
    container.connectToServer(this, wsUri)
  }

  def self: User = self_

  /**
   * 発言のストリーム
   */
  def getMessages: Observable[Message] = {
    subject
  }

  @OnOpen
  def onOpen(userSession: Session): Unit = {
    this.userSession = userSession
  }

  @OnClose
  def onClose(userSession: Session, reason: CloseReason) {
    log("Slack との WS がクローズしました")
    connect()
  }

  private def parseAs[A: Manifest](s: String): A = {
    parse(s).extract[A]
  }

  @OnMessage
  def onMessage(message: String) {
    log("受信: " + message)

    val a = parseAs[BaseEventJson](message)
    (a.`type`, a.subtype) match {
      case (Some("message"), None) =>
        val messageJson = parseAs[SimpleMessageEventJson](message)

        // TODO ユーザが増えた場合の対応
        subject.onNext(Message(getChannelById(messageJson.channel),
          userMap(messageJson.user),
          messageJson.text))
      case _ =>
    }
  }

  def send(channel: Channel, text: String): Unit = {
    val msg = MessageJson(nextEventId, channel.id, text)
    nextEventId += 1
    val msgJson = Serialization.write(msg)
    log("send " + msgJson)
    userSession.getAsyncRemote.sendText(msgJson)
  }

  def channels: Array[Channel] = channels_

  def getChannelByName(name: String): Channel = {
    channels_.find(_.name == name).get
  }

  private def log(log: String) {
    println(log)
  }

  private def user(json: UserJson): User = {
    User(json.id, json.name)
  }

  private def channel(json: ChannelJson): Channel = {
    Channel(json.id, json.name)
  }

  private def getChannelById(id: String): Channel = {
    channels_.find(_.id == id).get
  }
}