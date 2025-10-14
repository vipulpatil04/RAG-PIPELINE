package util

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.nio.file.{Files, Path}
import scala.util.{Using, Try, Success, Failure}

object Pdfs:
  /** Reads full text from a PDF file. */
  def readText(path: Path): Try[String] =
    val bytes = Files.readAllBytes(path)
    Using(PDDocument.load(new ByteArrayInputStream(bytes))) { doc =>
      val stripper = PDFTextStripper()
      stripper.getText(doc)
    }

/** Top-level entrypoint to test Pdfs.readText */
@main def runPdfTest(): Unit =
  // set PDF_PATH env var, or put "sample.pdf" in the project root
  val pdfPath = sys.env.getOrElse("PDF_PATH", "C:\\Users\\HP\\IdeaProjects\\FirstScala\\MSRCCorpus\\1083142.1083143.pdf")
  val path = Path.of(pdfPath)

  Pdfs.readText(path) match
    case Success(text) =>
      println(text)
      println(s"\n--- extracted ${text.length} characters ---")
    case Failure(ex) =>
      System.err.println(s"Failed to extract text from '$path': ${ex.getMessage}")
      ex.printStackTrace()
