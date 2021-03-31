package Interview.interviewnike

import java.net.URL
import cats.effect.IO

abstract class GetBaseURL[F[_]] {
  def get: F[URL]
}

object GetBaseURL {
  implicit object IOGetBaseURL extends GetBaseURL[IO] {
    def get = IO(new URL("https://baseurl/"))
  }
}
