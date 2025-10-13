package util

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.nio.file.{Files, Path}
import scala.util.{Using, Try}

object Pdfs:
  /** Reads full text from a PDF file. */
  def readText(path: Path): Try[String] =
    val bytes = Files.readAllBytes(path)
    Using(PDDocument.load(new ByteArrayInputStream(bytes))) { doc =>
      val stripper = PDFTextStripper()
      stripper.getText(doc)
    }
