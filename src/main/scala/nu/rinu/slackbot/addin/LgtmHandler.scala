package nu.rinu.slackbot.addin

import nu.rinu.slackbot.core.SlackRtmApi.Message
import nu.rinu.slackbot.core.SlackWebApi.Attachment
import nu.rinu.slackbot.util.Lgtm

import scala.util.matching.Regex

/**
  */
class LgtmHandler(trigger: Regex) extends Handler {
  val lgtm = new Lgtm()

  override def apply(context: Context, message: Message): Boolean = {
    val Trigger = trigger
    message.text match {
      case Trigger() =>
        for {res <- lgtm.request()} {
          context.slackWebApi.chatPostMessage(
            message.channel,
            "lgtm too!",
            Attachment("画像なの",
              image_url = Some(res.imageUri.toString)),
            Attachment("markdown なの",
              text = Some(res.markdown))
          )
        }
        true
    }
  }
}
