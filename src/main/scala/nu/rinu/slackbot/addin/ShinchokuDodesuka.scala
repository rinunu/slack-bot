package nu.rinu.slackbot.addin

import java.net.URI

import nu.rinu.slackbot.core.SlackRtmApi.{Channel, Message}
import nu.rinu.slackbot.core.SlackWebApi.Attachment
import nu.rinu.slackbot.util.Utils._

import scala.util.matching.Regex

/**
 * 「進捗どうですか?」する
 */
class ShinchokuDodesuka(trigger: Regex, targetUser: String) extends Handler {
  def shinchokuDodesuka(context: Context, channel: Channel): Unit = {
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

    context.slackWebApi.chatPostMessage(
      channel,
      s"@$targetUser 進捗どうですか?",
      Attachment("画像なの", image_url = Some(new URI(randomly(urls)).toString))
    )
  }

  override def apply(context: Context, message: Message): Boolean = {
    val Trigger = trigger
    message.text match {
      case Trigger() =>
        shinchokuDodesuka(context, message.channel)
        true
      case _ =>
        false
    }
  }
}
