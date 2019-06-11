name := "FastPreBERT"

version := "0.1"

scalaVersion := "2.12.7"

resolvers += "releases" at "http://oss.sonatype.org/content/repositories/releases"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies += "org.platanios" % "tensorflow_2.12" % "0.4.1" classifier "linux-cpu-x86_64"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.5"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.3")
