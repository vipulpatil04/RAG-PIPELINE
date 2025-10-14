package llm
import util.Pdfs
import util.Chunker
import sttp.client3.*
import sttp.model.MediaType
import io.circe.*
import io.circe.parser.*

class Ollama(base: String = sys.env.getOrElse("OLLAMA_HOST", "http://127.0.0.1:11434")):
  private val backend = HttpClientSyncBackend()
  private val eurl    = uri"$base/api/embeddings"

  /** Returns one embedding for one input string. Uses "prompt" as required by Ollama. */
  def embed(text: String, model: String): Array[Float] =
    val payload = s"""{"model":"$model","prompt":${Json.fromString(text).noSpaces}}"""

    val resp = basicRequest
      .post(eurl)
      .contentType(MediaType.ApplicationJson)
      .header("Accept", "application/json")
      .body(payload)
      .response(asStringAlways)
      .send(backend)

    val json = parse(resp.body).getOrElse(
      throw new RuntimeException(s"Non-JSON from Ollama (${resp.code}): ${resp.body.take(500)}")
    )
    val c = json.hcursor

    c.downField("error").as[String].toOption.foreach { msg =>
      throw new RuntimeException(s"Ollama error: $msg")
    }

    val arrJson: Vector[Json] =
      c.downField("embedding").as[Vector[Json]].getOrElse {
        c.downField("embeddings").as[Vector[Vector[Json]]].getOrElse(Vector.empty).headOption.getOrElse(Vector.empty)
      }

    if arrJson.isEmpty then
      throw new RuntimeException(s"Empty embedding from model '$model'. Body: ${resp.body.take(1000)}")

    arrJson.flatMap(_.asNumber.map(n => n.toDouble.toFloat)).toArray

@main def run(): Unit =
  val cli = new llm.Ollama()
  val emb = cli.embed("hello", "mxbai-embed-large")
  println(s"Embedding length: ${emb.length}")
  println("Embedding vector:")
  println(emb.mkString("[", ", ", "]"))

@main def runPipeline(): Unit =
  val pdfDir   = "C:\\Users\\HP\\IdeaProjects\\FirstScala\\MSRCCorpus"
  val pdfFiles = Vector("1083142.1083143.pdf", "1083144.1083145.pdf") // change to your two PDF names

  val ollama = new Ollama()
  val model  = "mxbai-embed-large"

  pdfFiles.foreach { filename =>
    val path = java.nio.file.Path.of(pdfDir, filename)
    println(s"\n=== Processing: $filename ===")

    Pdfs.readText(path) match
      case scala.util.Success(text) =>
        val chunks = Chunker.split(text, maxChars = 1200, overlap = 200)
        println(s"Extracted ${text.length} chars → ${chunks.size} chunks")

        chunks.zipWithIndex.foreach { (chunk, i) =>
          val emb = ollama.embed(chunk, model)
          println(s"Chunk[$i] → embedding dim=${emb.length}, first 8 vals: ${emb.take(8).mkString("[",", ","]")}")
        }
      case scala.util.Failure(ex) =>
        System.err.println(s"Failed to read $filename: ${ex.getMessage}")
  }
