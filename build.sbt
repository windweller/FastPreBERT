name := "FastPreBERT"

version := "0.1"

scalaVersion := "2.12.7"

resolvers += "releases" at "http://oss.sonatype.org/content/repositories/releases"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

libraryDependencies += "org.platanios" % "tensorflow_2.12" % "0.4.1" classifier "linux-cpu-x86_64"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.5"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.5"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.9"

libraryDependencies += "commons-io" % "commons-io" % "2.6"

libraryDependencies += "me.tongfei" % "progressbar" % "0.7.4"

//libraryDependencies += "com.xxxnell" %% "flip" % "0.0.4"
//
//libraryDependencies  ++= Seq(
//  // Last stable release
//  "org.scalanlp" %% "breeze" % "0.13.2",
//
//  // Native libraries are not included by default. add this if you want them (as of 0.7)
//  // Native libraries greatly improve performance, but increase jar sizes.
//  // It also packages various blas implementations, which have licenses that may or may not
//  // be compatible with the Apache License. No GPL code, as best I know.
//  "org.scalanlp" %% "breeze-natives" % "0.13.2",
//
//  // The visualization library is distributed separately as well.
//  // It depends on LGPL code
//  "org.scalanlp" %% "breeze-viz" % "0.13.2"
//)

addSbtPlugin("com.artima.supersafe" % "sbtplugin" % "1.1.3")
