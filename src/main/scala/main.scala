
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
package util

import java.nio.file.Path
import util.{Pdfs, Chunker}
import llm.Ollama
import scala.util.{Success, Failure}

@main def runFullPipeline(): Unit =
  val pdfs = Vector(
    "C:\\Users\\HP\\IdeaProjects\\FirstScala\\MSRCCorpus\\1083142.1083143.pdf",
    "C:\\Users\\HP\\IdeaProjects\\FirstScala\\MSRCCorpus\\1083142.1083144.pdf"  // second PDF
  )

  val ollama = new llm.Ollama()
  val model  = "mxbai-embed-large"

  pdfs.foreach { pdfPath =>
    val path = Path.of(pdfPath)
    println(s"\n📘 Processing: $pdfPath")

    Pdfs.readText(path) match
      case Success(text) =>
        println(s"✅ Extracted ${text.length} characters.")
        val chunks = Chunker.split(text, maxChars = 1000, overlap = 200)
        println(s"🧩 Created ${chunks.size} chunks. Getting embeddings...\n")

        // Get and print embeddings for each chunk
        chunks.take(3).zipWithIndex.foreach { case (chunk, idx) =>
          val emb = ollama.embed(chunk, model)
          println(s"[PDF: ${path.getFileName}] Chunk #$idx → dim=${emb.length}")
          println(emb.mkString("[", ", ", "]"))
          println()
        }

      case Failure(ex) =>
        System.err.println(s"❌ Failed to read '$pdfPath': ${ex.getMessage}")
        ex.printStackTrace()
  }