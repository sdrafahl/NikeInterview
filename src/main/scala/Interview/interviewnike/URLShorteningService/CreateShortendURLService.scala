package Interview.interviewnike

import java.net.URL
import cats.effect.IO
import scala.util.Try
import scala.util.Failure
import scala.util.Success

abstract class CreateShortendURLService[F[_]] {
  def createShortedURL(long: URL): F[URL]
}

object CreateShortendURLService {
  implicit def createIOCreateShortendURLService(implicit shortendRepo: ShortendURLRepository[IO], getBaseURL: GetBaseURL[IO]) = new CreateShortendURLService[IO] {
    def createShortedURL(long: URL): IO[URL] = for {
      baseURL <- getBaseURL.get
      maybeShort <- shortendRepo.getLong(long)
      shortUrl <- maybeShort match {
        case Some(short) => IO.pure(short)
        case None => {
          val hash = long.toExternalForm().hashCode()
          val newShort = baseURL.toExternalForm() + hash
          Try(new URL(newShort)) match {
            case Failure(_) => IO.raiseError(new Throwable(s"There was an error parsing the new URL: ${newShort}"))
            case Success(shortUrlToWrite) => shortendRepo.write(shortUrlToWrite, long).map(_ => shortUrlToWrite)
          }
        }
      }
    } yield shortUrl
  }
}
