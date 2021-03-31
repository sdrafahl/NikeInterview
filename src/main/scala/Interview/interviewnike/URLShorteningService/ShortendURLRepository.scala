package Interview.interviewnike

import java.net.URL
import cats.effect.IO
import cats.effect.concurrent.Ref

abstract class ShortendURLRepository[F[_]] {
  def getLong(short: URL): F[Option[URL]]
  def getShort(long: URL): F[Option[URL]]
  def write(short: URL, long: URL): F[Unit]
}

case class LongToShortMap(ref: Ref[IO, Map[URL, URL]])
case class ShortToLongMap(ref: Ref[IO, Map[URL, URL]])

object ShortendURLRepository {
  implicit def createIOShortendURLRepository(
    implicit longToShort: LongToShortMap,
    shortToLong: ShortToLongMap
  ) = new ShortendURLRepository[IO] {

    def getLong(short: URL) = readFromRefMap(short, shortToLong.ref)
    def getShort(long: URL) = readFromRefMap(long, longToShort.ref)
    def write(short: URL, long: URL) = {
      writeReferences(long, short, longToShort, shortToLong)
        .attempt
        .flatMap{
          case Left(_) => rollBackWrites(long, short, longToShort, shortToLong).map(_ => ())
          case Right(_) => IO.unit
        }
    }

    private def writeReferences(long: URL, short: URL, longToShort: LongToShortMap, shortToLong: ShortToLongMap) = {      
      for {
        _ <- longToShort.ref.update(m => m ++ Map(long -> short))
        _ <- shortToLong.ref.update(m => m ++ Map(short -> long))
      } yield ()
    }

    private def rollBackWrites(long: URL, short: URL, longToShort: LongToShortMap, shortToLong: ShortToLongMap) = {
      for {
        _ <- longToShort.ref.update(m => m.removed(long))
        _ <- shortToLong.ref.update(m => m.removed(short))
      } yield ()
    }

    private def readFromRefMap(key: URL ,ref: Ref[IO, Map[URL, URL]]) = for {
      mapping <- ref.get
      longURL = mapping.get(key)
    } yield longURL
  }
}
