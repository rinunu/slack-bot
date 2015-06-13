package nu.rinu.slackbot.util

import nu.rinu.slackbot.util.SimSimiClient.{ResponseJson, Response}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization
import rx.lang.scala.Observable

import scala.util.control.NonFatal
import scalaj.http.Http

object SimSimiClient {

  private case class ResponseJson(
    result: Int,
    response: String,
    id: Int,
    msg: String

  )

  case class Response(text: String)

}

class SimSimiClient(apiKey: String) {
  private implicit val formats = DefaultFormats

  def request(text: String, nickname: String): Observable[Response] = {
    val endpoint = "http://api.simsimi.com/request.p"

    Observable[Response] { s =>
      println(apiKey)
      val req = Http(endpoint).params(
        "key" -> apiKey,
        "text" -> text,
        "lc" -> "ja",
        "ft" -> "0" // filter
      ).timeout(2000, 2000)

      val res = req.asString
      if (res.isSuccess) {
        val resJson = Serialization.read[ResponseJson](res.body)
        println("受信 " + res.body)
        s.onNext(Response(resJson.response))
        s.onCompleted()
      } else {
        s.onError(new RuntimeException(s"error: code = ${res.code}, ${res.body}"))
      }
    }
  }
}
