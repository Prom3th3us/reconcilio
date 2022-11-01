import http.client.HttpClient
import sttp.client3._
import sttp.client3.HttpClientAsyncBackend
import zio.IO
import config._
import zio.config.typesafe._
import zio.config.magnolia.Descriptor._
import zio.config.magnolia._

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import zio._
import zio.stream._

import java.net.URI
import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}

object MainApp extends ZIOAppDefault {
  def elapsed: IO[String, String] => UIO[Duration] = io => {
    def now = Instant.now()
    val before = now
    io.exitCode.map(_ => Duration.between(before, now))
  }
  def isError: IO[String, String] => UIO[Boolean] = io => {
    io.exitCode.map {
      case ExitCode.failure => true
      case ExitCode.success => false
    }
  }
  case class Result(duration: Duration, isError: Boolean, timestamp: Instant)
  def pepe(config: Config, client: HttpClient): UIO[Result] = {
    val e = client.GET( URI.create(config.uri))
    for {
      duration <- elapsed(e)
      isError <- isError(e)
      timestamp = Instant.now()
    } yield Result(duration, isError, timestamp)
  }

  object Readside {
    case class Second private(instant: Instant) extends AnyVal
    object Second {
      def apply2(instant: Instant) =  Second(instant.truncatedTo(ChronoUnit.SECONDS))
    }
    case class Proyection(requestsPerSecond: Map[Second, Long], errorsPerSecond: Map[Second, Long], latency: Double = 0.0) {
      def addRequestAt(result: Result) = {
        val key = Second.apply2(result.timestamp)
        Proyection(
          requestsPerSecond = requestsPerSecond.+(key -> (requestsPerSecond.getOrElse(key, 0L) + 1L)),
          errorsPerSecond = if (result.isError) errorsPerSecond.+(key -> (errorsPerSecond.getOrElse(key, 0L) + 1L)) else errorsPerSecond,
          latency = if (latency == 0.0) result.duration.toNanos else (latency + result.duration.toNanos) / 2,
        )
      }
    }
    object Proyection { def empty = Proyection(Map.empty, Map.empty) }

    case class Summary(
                        maxRequestsPerSecondReached: Long,
                        averageRequestsPerSecond: Double,
                        averageErrorsPerSecond: Double,
                        averageLatency: Double,
                      ) {
      override def toString: String =
        s"""
           |Maximum Requests per Second: ${maxRequestsPerSecondReached}
           |Average Requests per Second: ${averageRequestsPerSecond}
           |Average Errors per Second: ${averageErrorsPerSecond}
           |Average latency: ${averageRequestsPerSecond}""".stripMargin
    }
    def summary: Proyection => Summary = proyection =>
      Summary(
        maxRequestsPerSecondReached = proyection.requestsPerSecond.values.max,
        averageRequestsPerSecond = {
          val n = proyection.requestsPerSecond.values.size.toDouble
          val sum = proyection.requestsPerSecond.values.sum.toDouble
          sum / n
        },
        averageErrorsPerSecond = {
          val n = proyection.errorsPerSecond.values.size.toDouble
          val sum = proyection.errorsPerSecond.values.sum.toDouble
          sum / n
        },
        averageLatency = proyection.latency
      )
  }

    def run =
    (for {
      config <- ZIO.service[Config]
      client <- ZIO.service[HttpClient]
      readsideProyection <- Ref.make(Readside.Proyection.empty)
      running <- zio.stream.ZStream.repeatWithSchedule(
        (), Schedule.spaced(Duration.ofMillis(1))
      ).mapZIO{ _ =>
        pepe(config, client)
      }.
      runForeach{ result => for {
              state <- readsideProyection.get
              _ <- readsideProyection.set(
                state.addRequestAt(result)
              )
            } yield ()
            }
            .forkScoped.interruptible
      _ <- Console.readLine(prompt = "Press any key to stop \n")
      _ <- running.interrupt
      readsideState <- readsideProyection.get
      _ <- Console.printLine(
        Readside
          .summary(readsideState)
      )
    } yield ()
    )
    .provide(
      Config.layer,
      HttpClient.layer,
      Scope.default
    )
}
