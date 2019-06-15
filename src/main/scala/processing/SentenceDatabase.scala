package processing


import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class SentenceDatabase {
  val sentences: ArrayBuffer[(ArrayBuffer[String], ArrayBuffer[String])] = ArrayBuffer()
  val rnd = new scala.util.Random

  def add_sentence(sentence: (ArrayBuffer[String], ArrayBuffer[String])): Unit = {
    sentences += sentence
  }

  def sample_sent(current_idx: Int): (ArrayBuffer[String], ArrayBuffer[String]) = {
    // we still sample future sentence pairs
    val random_sent_idx = Util.between(current_idx+1, sentences.size)
    sentences(random_sent_idx)
  }
}