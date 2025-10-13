ThisBuild / scalaVersion := "3.5.1"

libraryDependencies ++= Seq(
  // HTTP + JSON
  "com.softwaremill.sttp.client3" %% "core"  % "3.9.5",
  "com.softwaremill.sttp.client3" %% "circe" % "3.9.5",
  "io.circe" %% "circe-generic" % "0.14.9",
  "io.circe" %% "circe-parser"  % "0.14.9",

  // Lucene (vectors + analyzer)
  "org.apache.lucene" % "lucene-core" % "9.10.0",
  "org.apache.lucene" % "lucene-analysis-common" % "9.10.0",

  // PDF text
  "org.apache.pdfbox" % "pdfbox" % "2.0.31",

  // logging (optional)
  "ch.qos.logback" % "logback-classic" % "1.5.6"
)
