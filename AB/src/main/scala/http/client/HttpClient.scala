package http.client

import sttp.capabilities
import sttp.client3.basicRequest
import sttp.client3._
import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

import java.net.URI
import scala.concurrent.Future

trait HttpClient {
  def GET(url: URI): IO[String,String]
  def POST(url: URI, body: String): IO[String,String]
}

object HttpClient {
  val layer: ZLayer[Any, Nothing, HttpClient] =
    ZLayer {
      ZIO.succeed {
        new HttpClient {
          val backend: SttpBackend[Future, capabilities.WebSockets] = HttpClientFutureBackend()
          override def GET(url: URI): IO[String, String] = {
            val request = basicRequest.get(sttp.model.Uri.apply(url))
            ZIO.fromFuture(ec => request.send(backend))
            .flatMap{(response: Response[Either[String, String]]) =>
              //println(s"Received body ${response.body}")
              response.body match {
                case Left(value) => ZIO.fail(new Throwable(value))
                case Right(value) => ZIO.succeed(value)
              }
            }.mapErrorCause(_.map(_.getMessage))
          }
          override def POST(url: URI, body: String): IO[String, String] = {
            val request = basicRequest.post(sttp.model.Uri.apply(url)).body(body)
            ZIO.fromFuture(ec => request.send(backend))
              .flatMap((response: Response[Either[String, String]]) =>
                response.body match {
                  case Left(value) => ZIO.fail(new Throwable(value))
                  case Right(value) => ZIO.succeed(value)
                })
              .mapErrorCause(_.map(_.getMessage))
          }
        }
      }
    }
}
