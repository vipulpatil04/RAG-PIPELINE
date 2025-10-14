package util

import util.Pdfs
import util.Chunker
import java.nio.file.{Paths, Files}
import scala.util.Try

/** Small CLI app: read a PDF, chunk it, print stats and first few chunks. */
object ChunkPdfApp:
  private case class Opts(path: String, maxChars: Int = 1200, overlap: Int = 200, preview: Int = 3)

  def main(args: Array[String]): Unit =
    val opts = parseArgs(args).getOrElse {
      println("Usage: run <pdfPath> [--max-chars N] [--overlap N] [--preview K]")
      sys.exit(1)
    }

    val pdfPath = Paths.get(opts.path)
    if !Files.exists(pdfPath) then
      Console.err.println(s"ERROR: File not found: ${pdfPath.toAbsolutePath}")
      sys.exit(2)

    Pdfs.readText(pdfPath).fold(
      err => {
        Console.err.println(s"ERROR: Failed to read PDF: ${err.getMessage}")
        sys.exit(3)
      },
      text => {
        val chunks = Chunker.split(text, maxChars = opts.maxChars, overlap = opts.overlap)
        println(s"PDF: ${pdfPath.getFileName}")
        println(s"Chars total: ${text.length}")
        println(s"Chunks: ${chunks.size} (maxChars=${opts.maxChars}, overlap=${opts.overlap})")

        val show = math.min(opts.preview, chunks.size)
        println(s"\n--- Preview: first $show chunk(s) ---")
        chunks.take(show).zipWithIndex.foreach { case (c, i) =>
          println(s"\n[Chunk #$i] len=${c.length}")
          println(c.take(400) + (if c.length > 400 then " …" else ""))
        }
      }
    )

  private def parseArgs(args: Array[String]): Option[Opts] =
    if args.isEmpty then None
    else
      var opts = Opts(path = args(0))
      var i = 1
      while i < args.length do
        args(i) match
          case "--max-chars" if i + 1 < args.length =>
            opts = opts.copy(maxChars = args(i + 1).toInt); i += 2
          case "--overlap" if i + 1 < args.length =>
            opts = opts.copy(overlap = args(i + 1).toInt); i += 2
          case "--preview" if i + 1 < args.length =>
            opts = opts.copy(preview = args(i + 1).toInt); i += 2
          case other =>
            Console.err.println(s"Unknown/invalid arg: $other"); return None
      Some(opts)
