package Interview.interviewnike

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.scalatest.funspec.AnyFunSpec
import java.net.URL
import fs2.text
import scala.util.Try
import io.circe.generic.auto._
import io.circe.syntax._
import Interview.interviewnike.InterviewnikeRoutes.ShortenedURLRequest
import URLEncodersDecoders._
import cats.effect.concurrent.Ref
import Interview.interviewnike.InterviewnikeRoutes.FullURLRequest

class ShortenedURLServiceSpec extends AnyFunSpec {
  val testURL = new URL("https://www.google.com")

  implicit val testLongToShort = LongToShortMap((Ref.of[IO, Map[URL, URL]](Map[URL, URL]()).unsafeRunSync())) 
  implicit val testShortToLong = ShortToLongMap((Ref.of[IO, Map[URL, URL]](Map[URL, URL]()).unsafeRunSync()))

  describe("Create-Shortened-URL") {
    it("Should return a shortened URL given a URL and give back the original URL") {
      val returnedShortenedURL = createShortenedURL(testURL)
      val parsedUrl = Try(new URL(returnedShortenedURL))
      assert(parsedUrl.isSuccess)
     
      val short = parsedUrl.get
      val originalURL = getFullURL(short)
      assert(originalURL.equals(testURL.toExternalForm()))
    }
    it("Should return the correct message when asking for a short url that does not exist"){
      val expectedStatusCode = 400
      val statusCode = getFullURLWithoutBody(new URL("https://www.facebook.com")).status.code
      assert(statusCode == expectedStatusCode)
    }
  }

  private def createShortenedURL(url:URL) = {
    val uri = Uri.fromString("/Create-Shortened-URL").toOption.get
    val requestBody = ShortenedURLRequest(url)
    val request = Request[IO](Method.POST, uri).withEntity(requestBody.asJson.toString())

    InterviewnikeRoutes
      .urlShortentingRoutes[IO]
      .orNotFound(request)
      .unsafeRunSync()
      .body
      .through(text.utf8Decode)
      .compile
      .toList
      .unsafeRunSync()
      .head
  }

  private def getFullURL(url: URL) = {
    val uri = Uri.fromString("/Get-Full-URL").toOption.get
    val requestBody = FullURLRequest(url)
    val request = Request[IO](Method.GET, uri).withEntity(requestBody.asJson.toString())

    InterviewnikeRoutes
      .urlShortentingRoutes[IO]
      .orNotFound(request)
      .unsafeRunSync()
      .body
      .through(text.utf8Decode)
      .compile
      .toList
      .unsafeRunSync()
      .head
  }

  private def getFullURLWithoutBody(url: URL) = {
    val uri = Uri.fromString("/Get-Full-URL").toOption.get
    val requestBody = FullURLRequest(url)
    val request = Request[IO](Method.GET, uri).withEntity(requestBody.asJson.toString())

    InterviewnikeRoutes
      .urlShortentingRoutes[IO]
      .orNotFound(request)
      .unsafeRunSync()
  }
}
