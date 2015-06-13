package nu.rinu.slackbot.addin

import nu.rinu.slackbot.core.SlackRtmApi.Message
import nu.rinu.slackbot.core.{SlackRtmApi, SlackWebApi}

case class Context(
  slackRtmApi: SlackRtmApi,
  slackWebApi: SlackWebApi
)

/**
  */
trait Handler extends ((Context, Message) => Boolean) {
}
