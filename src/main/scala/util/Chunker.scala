package util

object Chunker:
  /** Normalize whitespace and trim ends. */
  def normalize(s: String): String =
    s.replaceAll("\\s+"," ").trim

  /**
   * Split text into chunks of up to `maxChars` with overlap `overlap`.
   * Tries to cut at a sentence boundary when possible.
   */
  def split(s: String, maxChars: Int = 1800, overlap: Int = 250): Vector[String] =
    require(maxChars > 0, "maxChars must be > 0")
    require(overlap >= 0 && overlap < maxChars, "0 <= overlap < maxChars required")

    val clean = normalize(s)
    val out   = Vector.newBuilder[String]
    var i     = 0

    while i < clean.length do
      val end   = (i + maxChars).min(clean.length)
      val slice = clean.substring(i, end)

      // Prefer cutting at a sentence-ish boundary if the cut is not too early.
      val cut   = slice.lastIndexWhere(ch => ch == '.' || ch == '!' || ch == '?' || ch == '\n')
      val piece =
        if cut >= (maxChars * 0.6).toInt then slice.substring(0, cut + 1)
        else slice


      out += piece
      // advance by stride = piece.length - overlap (at least 1)
      i += (piece.length - overlap).max(1)

    out.result()
