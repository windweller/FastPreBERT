package processing

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


// https://github.com/scala/scala/blob/1486a671c11e5770144404e3df4ee1b31fab2066/src/library/scala/util/Random.scala

// reduce memory option is not possible here
class DocumentDatabase {
  val documents: mutable.Buffer[ArrayBuffer[(String, String)]] = ArrayBuffer()
  val doc_lengths: mutable.Buffer[Int] = ArrayBuffer()

  var doc_cumsum: Option[mutable.Buffer[Int]] = None
  var cumsum_max: Option[Int] = None

  val rnd = new scala.util.Random

  def add_document(document: ArrayBuffer[(String, String)]): Unit = {
    documents += document
    doc_lengths += document.size
  }

  private def precalculate_doc_weights() {
    // This is cumsum
    // https://stackoverflow.com/questions/3224935/in-scala-how-do-i-fold-a-list-and-return-the-intermediate-results
    doc_cumsum = Some(doc_lengths.map{var s = 0; d => {s += d; s}})
    cumsum_max = doc_cumsum.map(a => a.last) //Some(doc_cumsum.get.last)
  }

  def sample_doc(current_idx: Int, sentence_weighted: Boolean = true): ArrayBuffer[(String, String)] = {
    var sampled_doc_index: Int = -1
    if (sentence_weighted) {
      if (doc_cumsum.isEmpty || doc_cumsum.size != doc_lengths.size) {
        precalculate_doc_weights()
      }
      val rand_start = doc_cumsum.get(current_idx)
      val rand_end = rand_start + cumsum_max.get - doc_lengths(current_idx)
//      val sentence_index = rand_start + rnd.nextInt((rand_end - rand_start) + 1)  // Scala's awkward way of sampling between 2 numbers
      val sentence_index = Util.between(rand_start, rand_end)
      sampled_doc_index = Util.searchsorted(doc_cumsum.get, sentence_index)  // let's hope this implementation is the same
    } else {
      sampled_doc_index = current_idx + 1 + rnd.nextInt(doc_lengths.size - 1) // hope we don't need this...not tested
    }
    documents(sampled_doc_index)
  }

  def get_doc(item: Int): ArrayBuffer[(String, String)] = {
    documents(item)
  }

}
