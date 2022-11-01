
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.8.3"

lazy val ZioVersion = "2.0.2"
lazy val ZioConfigVersion = "3.0.2"
libraryDependencies += "dev.zio" %% "zio-streams" % ZioVersion
libraryDependencies += "dev.zio" %% "zio-config" % ZioConfigVersion
// https://mvnrepository.com/artifact/dev.zio/zio-config-magnolia
libraryDependencies += "dev.zio" %% "zio-config-magnolia" % ZioConfigVersion
// https://mvnrepository.com/artifact/dev.zio/zio-config-typesafe
libraryDependencies += "dev.zio" %% "zio-config-typesafe" % ZioConfigVersion
