package nu.rinu.slackbot.util

import java.net.URI

import nu.rinu.slackbot.util.Lgtm.{ResponseJson, Response}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization
import rx.lang.scala.Observable

import scalaj.http.Http


object Lgtm {

  private case class ResponseJson(
    imageUrl: String,
    markdown: String
  )

  case class Response(imageUri: URI, markdown: String)

}

class Lgtm {
  private implicit val formats = DefaultFormats

  val endpoint = "http://www.lgtm.in/g"

  def request(): Observable[Response] = {
    Observable[Response] { s =>
      val req = Http(endpoint).headers(
        "Content-Type" -> "application/json",
        "Accept" -> "application/json"
      ).timeout(5000, 5000)

      val res = req.asString
      if (res.isSuccess) {
        println("受信 " + res.body)
        val resJson = Serialization.read[ResponseJson](res.body)
        s.onNext(Response(new URI(resJson.imageUrl), resJson.markdown))
        s.onCompleted()
      } else {
        sys.error(s"error: code = ${res.code}, ${res.body}")
      }
    }
  }
}
