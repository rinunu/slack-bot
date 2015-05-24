package nu.rinu.slackbot.util

import com.google.api.client.http.{GenericUrl, HttpTransport}
import nu.rinu.slackbot.util.DialogueApi.{ResponseJson, Response, RequestJson}
import org.json4s.DefaultFormats
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import rx.lang.scala.Observable
import scalaj.http._


object DialogueApi {

  private case class RequestJson(
    utt: String,
    context: Option[String] = None,
    nickname: Option[String] = None, // 10文字以下
    nickname_y: Option[String] = None,
    sex: Option[String] = None,
    bloodtype: Option[String] = None,
    birthdateY: Option[String] = None,
    birthdateM: Option[String] = None,
    birthdateD: Option[String] = None,
    age: Option[String] = None,
    constellations: Option[String] = None,
    place: Option[String] = None,
    mode: Option[String] = None,
    t: Option[String] = None)


  private case class ResponseJson(
    utt: String,
    yomi: String,
    mode: String,
    da: String,
    context: String
  )

  case class Response(text: String)

}

/**
 * https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_name=dialogue&p_name=api_1#tag01
 */
class DialogueApi(apiKey: String) {
  private implicit val formats = DefaultFormats
  private var userContextMap: Map[String, String] = Map.empty

  private val endpoint = "https://api.apigw.smt.docomo.ne.jp/dialogue/v1/dialogue?APIKEY=" + apiKey

  def dialogue(text: String, nickname: String): Observable[Response] = {
    Observable[Response] { s =>
      val body = RequestJson(
        utt = text,
        nickname = Some(nickname),
        context = userContextMap.get(nickname),
        mode = Some("srtr") 
      )
      val bodyJson = Serialization.write(body)
      println(bodyJson)

      val req = Http(endpoint).postData(bodyJson)
        .header("Content-Type", "application/json")

      val res = req.asString
      if (res.isSuccess) {
        val resJson = Serialization.read[ResponseJson](res.body)
        println("受信 " + res.body)
        userContextMap += (nickname -> resJson.context)
        s.onNext(Response(resJson.utt))
        s.onCompleted()
      } else {
        s.onError(new RuntimeException(s"error: code = ${res.code}, ${res.body}"))
      }
    }
  }
}
