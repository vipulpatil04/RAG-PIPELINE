package llm

import sttp.client3.*
import sttp.client3.circe.*          // JSON <-> HTTP glue
import io.circe.*
import io.circe.generic.semiauto.*   // deriveEncoder/deriveDecoder

// ---------- embeddings ----------
final case class EmbedReq(model: String, input: Vector[String])
final case class EmbedResp(embeddings: Vector[Vector[Float]])

given Encoder[EmbedReq] = deriveEncoder
object EmbedResp:
  given Decoder[EmbedResp] =
    Decoder.instance { c =>
      c.downField("embeddings").as[Vector[Vector[Float]]].map(EmbedResp.apply)
        .orElse(c.downField("embedding").as[Vector[Float]].map(v => EmbedResp(Vector(v))))
    }

// ---------- (optional) chat ----------
final case class ChatMessage(role: String, content: String)
final case class ChatReq(model: String, messages: Vector[ChatMessage], stream: Boolean = false)
final case class ChatMsg(role: String, content: String)
final case class ChatResp(message: ChatMsg)

// Provide encoders for request types so `.body(...)` compiles
given Encoder[ChatMessage] = deriveEncoder
given Encoder[ChatReq]     = deriveEncoder

// Decoders for chat response
object ChatResp:
  given Decoder[ChatMsg]  = deriveDecoder
  given Decoder[ChatResp] = deriveDecoder

/** Minimal Ollama client for embeddings + chat. */
class Ollama(base: String = sys.env.getOrElse("OLLAMA_HOST","http://127.0.0.1:11434")):
  private val be   = HttpClientSyncBackend()
  private val eurl = uri"$base/api/embeddings"
  private val curl = uri"$base/api/chat"

  /** Batch-embed texts. Returns L2-normalized vectors (good for cosine/IP). */
  def embed(texts: Vector[String], model: String): Vector[Array[Float]] =
    if texts.isEmpty then Vector.empty
    else
      val req = basicRequest
        .post(eurl)
        .body(EmbedReq(model, texts))    // uses Encoder[EmbedReq]
        .response(asJson[EmbedResp])     // uses Decoder[EmbedResp]
      val out = req.send(be).body.fold(throw _, _.embeddings.map(_.toArray))
      out.map(Ollama.l2normalize)

  /** Simple chat (not required for indexing). */
  def chat(messages: Vector[ChatMessage], model: String): String =
    val req = basicRequest
      .post(curl)
      .body(ChatReq(model, messages))    // uses Encoder[ChatReq]
      .response(asJson[ChatResp])
    req.send(be).body.fold(throw _, _.message.content)

object Ollama:
  def l2normalize(v: Array[Float]): Array[Float] =
    val n = math.sqrt(v.foldLeft(0.0)((a,b) => a + b*b))
    if n == 0 then v else v.map(_ / n.toFloat)
