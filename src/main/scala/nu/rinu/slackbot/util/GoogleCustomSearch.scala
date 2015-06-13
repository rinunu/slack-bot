package nu.rinu.slackbot.util

import java.net.URI

import nu.rinu.slackbot.util.GoogleCustomSearch.{Image, ResponseJson, Response}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization
import rx.lang.scala.Observable

import scalaj.http.Http

object GoogleCustomSearch {

  private case class ImageJson(
    contextLink: String,
    thumbnailLink: String
  )

  private case class ItemJson(
    title: String,
    link: String,
    image: Option[ImageJson]
  )

  private case class ResponseJson(
    items: List[ItemJson]
  )

  case class Image(title: String, uri: URI, thumbnailUri: URI, contextRui: URI)

  case class Response(images: Seq[Image])

}

/**
  */
class GoogleCustomSearch(apiKey: String, searchEngineId: String) {
  private implicit val formats = DefaultFormats

  val endpoint = "https://www.googleapis.com/customsearch/v1"

  def searchImages(word: String): Observable[Response] = {
    Observable[Response] { s =>
      val req = Http(endpoint).params(
        "key" -> apiKey,
        "cx" -> searchEngineId,
        "q" -> word,
        "searchType" -> "image"
      ).timeout(5000, 5000)

      val res = req.asString
      if (res.isSuccess) {
        //        println("受信 " + res.body)
        val resJson = Serialization.read[ResponseJson](res.body)
        s.onNext(Response(resJson.items.map(item =>
          Image(
            item.title,
            new URI(item.link),
            item.image.map(img => new URI(img.thumbnailLink)).get,
            item.image.map(img => new URI(img.contextLink)).get
          ))))
        s.onCompleted()
      } else {
        sys.error(s"error: code = ${res.code}, ${res.body}")
      }
    }
  }
}
