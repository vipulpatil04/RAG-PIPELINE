package app

import util.{Pdfs, Chunker}
import llm.Ollama
import index.LuceneIndex
import java.nio.file.{Paths, Path, Files}
import java.nio.charset.StandardCharsets
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** Build a Lucene index from PDFs: extract → chunk → embed → index. */
object BuildLuceneApp:
  final case class Opts(dir: Option[Path], pdfs: Vector[Path], index: Path, model: String, maxChars: Int, overlap: Int)
  def main(args: Array[String]): Unit =
    val opts = parse(args).getOrElse {
      println(
        """Usage:
          |  runMain app.BuildLuceneApp --index <dir> --model <ollama-model> [--dir <pdf-folder>]
          |  # or explicit files:
          |  runMain app.BuildLuceneApp --index <dir> --model <ollama-model> file1.pdf file2.pdf ...
          |  Options: [--max-chars N] [--overlap N]
          |""".stripMargin)
      sys.exit(1)
    }

    val allPdfs =
      (opts.pdfs ++ opts.dir.toVector.flatMap(scanPdfs)).distinct
        .filter(p => Files.exists(p))

    if allPdfs.isEmpty then
      Console.err.println("No PDFs found."); sys.exit(2)

    val client = new Ollama()
    Files.createDirectories(opts.index)

    allPdfs.foreach { pdf =>
      val base = stripExt(pdf.getFileName.toString)
      println(s"→ $base")
      Pdfs.readText(pdf).fold(
        err => Console.err.println(s"ERROR reading $pdf: ${err.getMessage}"),
        text => {
          val chunks = Chunker.split(text, maxChars = opts.maxChars, overlap = opts.overlap)
          // embed in small batches to avoid huge payloads
          val batchSize = 16
          chunks.grouped(batchSize).zipWithIndex.foreach { case (group, gi) =>
            val vecs = client.embed(group.toVector, opts.model)
            group.zip(vecs).zipWithIndex.foreach { case ((chunk, vec), i) =>
              val globalIdx = gi * batchSize + i
              LuceneIndex.add(
                indexDir = opts.index,
                docId    = base,
                chunkId  = globalIdx,
                text     = chunk,
                vec      = vec,
                pathRel  = s"$base#chunk_$globalIdx"
              )
            }
            println(f"   indexed chunks ${gi*batchSize}%d..${gi*batchSize + group.size - 1}%d")
          }
        }
      )
    }
    println(s"Done. Index at: ${opts.index.toAbsolutePath}")

  private def scanPdfs(d: Path): Vector[Path] =
    if !Files.isDirectory(d) then Vector.empty
    else Files.list(d).iterator().asScala
      .filter(p => Files.isRegularFile(p) && p.getFileName.toString.toLowerCase.endsWith(".pdf"))
      .toVector

  private def stripExt(name: String): String =
    val dot = name.lastIndexOf('.'); if dot > 0 then name.substring(0, dot) else name

  private def parse(args: Array[String]): Option[Opts] =
    var dir: Option[Path] = None
    var pdfs = Vector.empty[Path]
    var index = Option.empty[Path]
    var model = Option.empty[String]
    var maxChars = 1200
    var overlap  = 200
    var i = 0
    while i < args.length do
      args(i) match
        case "--dir" if i+1 < args.length => dir = Some(Paths.get(args(i+1))); i += 2
        case "--index" if i+1 < args.length => index = Some(Paths.get(args(i+1))); i += 2
        case "--model" if i+1 < args.length => model = Some(args(i+1)); i += 2
        case "--max-chars" if i+1 < args.length => maxChars = args(i+1).toInt; i += 2
        case "--overlap" if i+1 < args.length => overlap = args(i+1).toInt; i += 2
        case s if s.startsWith("--") => Console.err.println(s"Unknown option: $s"); return None
        case other => pdfs = pdfs :+ Paths.get(other); i += 1
    Some(Opts(dir, pdfs, index.getOrElse(Paths.get("lucene-index")), model.getOrElse("mxbai-embed-large"), maxChars, overlap))
