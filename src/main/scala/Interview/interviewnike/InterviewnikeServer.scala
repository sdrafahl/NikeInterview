package Interview.interviewnike

import cats.effect.{ConcurrentEffect, Timer}
import fs2.Stream
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import scala.concurrent.ExecutionContext.global

object InterviewnikeServer {

  def stream[F[_]: ConcurrentEffect:
      CreateShortendURLService:
      GetFullURLFromShortService
  ](
    implicit T: Timer[F]
  ): Stream[F, Nothing] = {
    val httpApp = (
      InterviewnikeRoutes.urlShortentingRoutes[F]
    ).orNotFound
    val finalHttpApp = Logger.httpApp(true, true)(httpApp)
    for {
      exitCode <- BlazeServerBuilder[F](global)
      .bindHttp(8081, "0.0.0.0")
      .withHttpApp(finalHttpApp)
      .serve
    } yield exitCode
  }.drain
}
