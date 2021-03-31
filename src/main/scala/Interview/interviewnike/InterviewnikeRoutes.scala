package Interview.interviewnike

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.generic.auto._
import java.net.URL
import fs2.text
import io.circe.fs2._
import io.circe.Decoder
import org.http4s.Request
import URLEncodersDecoders._

object InterviewnikeRoutes {

  case class ShortenedURLRequest(urlToShorten: URL)
  case class FullURLRequest(shortURL: URL)

  def urlShortentingRoutes[F[_]: Sync:
      CreateShortendURLService:
      GetFullURLFromShortService
  ] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    HttpRoutes.of[F] {
      case req @ POST -> Root / "Create-Shortened-URL" => {
        responseCode(
          parseRequest[F, ShortenedURLRequest](req, {request =>
            implicitly[CreateShortendURLService[F]].createShortedURL(request.urlToShorten).map(url => url.toExternalForm())
          })
        )
      }
      case req @ GET -> Root / "Get-Full-URL" => {
        responseCode(
          parseRequest[F, FullURLRequest](req, {request =>
            implicitly[GetFullURLFromShortService[F]].getFullURL(request.shortURL).map(url => url.toExternalForm())
          })
        )
      }
    }
  }

  private def parseRequest[F[_]: Sync, A](request: Request[F], cb: A => F[String])(implicit d: Decoder[A]) = {
    request
      .body
      .through(text.utf8Decode)
      .through(stringStreamParser)      
      .through(decoder[F, A])
      .compile
      .toList
      .map(a => a.headOption.map(req => cb(req)))
      .flatMap{
        case None => Sync[F].raiseError[String](new Throwable("No request body found"))
        case Some(processed) => processed
      }
  }

  private def responseCode[F[_]: Sync](a: F[String]) = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    a.attempt
      .flatMap{
        case Left(_) => Sync[F].defer(BadRequest())
        case Right(shortUrl) => Ok(shortUrl.toString())
      }
  }
}
