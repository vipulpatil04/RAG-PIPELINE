package index

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.*
import org.apache.lucene.index.*
import org.apache.lucene.search.*          // <-- correct package
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.index.VectorSimilarityFunction
import java.nio.file.{Files, Path}
import scala.util.Using

/** Wraps a Lucene index with a vector field "vec" (cosine). */
object LuceneIndex:
  final val VecField       = "vec"
  final val TextFieldName  = "text"
  final val DocIdField     = "doc_id"
  final val ChunkIdField   = "chunk_id"
  final val PathField      = "path"

  /** Create/append and add a single chunk + vector. */
  def add(
           indexDir: Path,
           docId: String,
           chunkId: Int,
           text: String,
           vec: Array[Float],
           pathRel: String
         ): Unit =
    Files.createDirectories(indexDir)
    val analyzer = new StandardAnalyzer()
    val cfg = IndexWriterConfig(analyzer)
    Using.resource(FSDirectory.open(indexDir)) { dir =>
      Using.resource(new IndexWriter(dir, cfg)) { iw =>
        val doc = new Document()
        doc.add(new StringField(DocIdField, docId, Field.Store.YES))
        doc.add(new StringField(ChunkIdField, chunkId.toString, Field.Store.YES))
        doc.add(new StringField(PathField, pathRel, Field.Store.YES))
        doc.add(new TextField(TextFieldName, text, Field.Store.YES))
        doc.add(new KnnFloatVectorField(VecField, vec, VectorSimilarityFunction.COSINE))
        iw.addDocument(doc)
        iw.commit()
      }
    }

  final case class Hit(score: Float, docId: String, chunkId: Int, pathRel: String, text: String)

  /** k-NN search by vector; returns top-k hits with stored fields. */
  def search(indexDir: Path, queryVec: Array[Float], k: Int): Vector[Hit] =
    Using.resource(FSDirectory.open(indexDir)) { dir =>
      Using.resource(DirectoryReader.open(dir)) { reader =>
        val searcher = new IndexSearcher(reader)
        // Either approach is fine; using the field helper here:
        val q = KnnFloatVectorField.newVectorQuery(VecField, queryVec, k)
        val top = searcher.search(q, k)
        top.scoreDocs.toVector.map { sd =>
          val d = searcher.doc(sd.doc)
          Hit(
            score   = sd.score,
            docId   = d.get(DocIdField),
            chunkId = d.get(ChunkIdField).toInt,
            pathRel = d.get(PathField),
            text    = d.get(TextFieldName)
          )
        }
      }
    }
