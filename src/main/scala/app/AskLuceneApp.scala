package app

import llm.Ollama
import index.LuceneIndex
import java.nio.file.{Paths, Path}
import scala.util.Try

/** Query the index: embed the question, do k-NN on vectors, print top-k chunks. */
object AskLuceneApp:
  final case class Opts(index: Path, model: String, k: Int, q: String)
  def main(args: Array[String]): Unit =
    val opts = parse(args).getOrElse {
      println("""Usage:
                |  runMain app.AskLuceneApp --index <dir> --model <ollama-model> --k 5 "your question here"
                |""".stripMargin)
      sys.exit(1)
    }
    val client = new Ollama()
    val qvec = client.embed(Vector(opts.q), opts.model).headOption.getOrElse {
      Console.err.println("failed to embed query"); sys.exit(2); Array.emptyFloatArray
    }
    val hits = LuceneIndex.search(opts.index, qvec, opts.k)
    println(s"Top ${hits.size} results:")
    hits.zipWithIndex.foreach { case (h, i) =>
      println(f"\n#${i+1}%d  score=${h.score}%.4f  ${h.docId} [chunk ${h.chunkId}]  ${h.pathRel}")
      println(h.text.take(600) + (if h.text.length > 600 then " …" else ""))
    }

  private def parse(args: Array[String]): Option[Opts] =
    if args.isEmpty then None
    else
      var index = Paths.get("lucene-index")
      var model = "mxbai-embed-large"
      var k = 5
      val tail = args.last
      if tail.startsWith("--") then return None
      var i = 0
      while i < args.length - 1 do
        args(i) match
          case "--index" if i+1 < args.length => index = Paths.get(args(i+1)); i += 2
          case "--model" if i+1 < args.length => model = args(i+1); i += 2
          case "--k" if i+1 < args.length     => k = args(i+1).toInt; i += 2
          case s if s.startsWith("--")        => Console.err.println(s"Unknown: $s"); return None
          case _                               => i += 1
      Some(Opts(index, model, k, q = tail))
