package nu.rinu.slackbot.util

import nu.rinu.slackbot.util.TheCatApi.Response
import org.json4s.DefaultFormats
import rx.lang.scala.Observable

import scalaj.http.{HttpOptions, Http}

object TheCatApi {

  case class Response(
    url: String
  )

}

/**
 * http://thecatapi.com/docs.html
 */
class TheCatApi(apiKey: String) {
  private implicit val formats = DefaultFormats

  def request(): Observable[Response] = {
    val endpoint = "http://thecatapi.com/api/images/get"

    Observable[Response] { s =>

      val req = Http(endpoint).params(
        "api_key" -> apiKey
      )

      val res = req.asString
      if (res.isRedirect) {
        s.onNext(Response(res.location.get))
        s.onCompleted()
      } else {
        s.onError(new RuntimeException(s"error: code = ${res.code}, ${res.body}"))
      }
    }
  }

}
