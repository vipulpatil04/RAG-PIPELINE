ThisBuild / scalaVersion := "3.5.1"
libraryDependencies ++= Seq(
  // Retrieval index (pure JVM, cross-platform)
  "org.apache.lucene" % "lucene-core" % "9.10.0",
  "org.apache.lucene" % "lucene-analysis-common" % "9.10.0",
  // PDF extraction + HTTP + JSON
  "org.apache.pdfbox" % "pdfbox" % "2.0.31",
  "com.softwaremill.sttp.client3" %% "core"  % "3.9.5",
  "com.softwaremill.sttp.client3" %% "circe" % "3.9.5",
  "io.circe" %% "circe-generic" % "0.14.9",
  "io.circe" %% "circe-parser"  % "0.14.9",
  "ch.qos.logback" % "logback-classic" % "1.5.6"
)
// Optional FAISS CPU via JNI (Linux-only, CPU-only)
// libraryDependencies += "com.criteo.jfaiss" % "jfaiss-cpu" % "1.7.0-1"