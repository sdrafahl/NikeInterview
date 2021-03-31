package Interview.interviewnike

import io.circe.{ Decoder, Encoder }
import scala.util.Try
import java.net.URL

object URLEncodersDecoders {
  implicit val urlEncoder: Encoder[URL] = Encoder.encodeString.contramap(_.toExternalForm())
  implicit val urlDecoder: Decoder[URL] = Decoder.decodeString.emapTry{s => Try(new URL(s))}
}
