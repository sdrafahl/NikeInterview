package Interview.interviewnike

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.concurrent.Ref
import cats.implicits._
import java.net.URL

object Main extends IOApp {
  def run(args: List[String]) = {
        (Ref.of[IO, Map[URL, URL]](Map[URL, URL]()), Ref.of[IO, Map[URL, URL]](Map[URL, URL]()))
        .bisequence
        .flatMap{f =>
          implicit val a = LongToShortMap(f._1)
          implicit val b = ShortToLongMap(f._2)
          InterviewnikeServer.stream[IO].compile.drain.as(ExitCode.Success)
        }      
  }    
}
