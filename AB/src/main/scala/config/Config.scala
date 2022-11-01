package config

import zio._
import zio.config._
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

case class Config(uri: String, amount: Int)

object Config {

  import sttp.client3._
  import sttp.client3.HttpClientAsyncBackend
  
  val layer: ZLayer[Any, ReadError[String], Config] =
    ZLayer {
      read {
        descriptor[Config].from(
          TypesafeConfigSource.fromResourcePath
            .at(PropertyTreePath.$("HttpServerConfig"))
        )
      }
    }
}