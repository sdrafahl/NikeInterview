package Interview.interviewnike

import java.net.URL
import cats.effect.IO

abstract class GetFullURLFromShortService[F[_]] {
  def getFullURL(shortURL: URL): F[URL]
}

object GetFullURLFromShortService {
  implicit def createIOGetFullURLFromShortService(implicit shortendRepo: ShortendURLRepository[IO]) = new GetFullURLFromShortService[IO] {
    def getFullURL(shortURL: URL): IO[URL] = {
      shortendRepo.getLong(shortURL).flatMap{
        case None => IO.raiseError(new Throwable("That URL does not correspond to a longer URL"))
        case Some(url) => IO.pure(url)
      }
    }
  }
}
