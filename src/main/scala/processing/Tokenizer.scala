package processing

import org.platanios.tensorflow.api.core.Shape
import org.platanios.tensorflow.api.tensors.Tensor

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.io.Source

/**
  * This file does not do actual tokenization.
  * BERT tokenization is done at Python level and saved as JSON
  */

class Tokenizer(vocab_file: Option[String]) {

  //hashmap lookup might take a long time...can consider switching to Buffer
  val vocab: mutable.HashMap[String, Int] = mutable.HashMap()
  var vocab_list: ArrayBuffer[String] = ArrayBuffer()  //constant append time
  var sample_vocab_list: ArrayBuffer[String] = ArrayBuffer()

  val special_tokens: mutable.Set[String] = mutable.Set("[UNK]", "[CLS]", "[SEP]", "[MASK]", "URL", "@USER")
  val no_rep_special_tokens: mutable.Set[String] = mutable.Set("[CLS]", "[SEP]", "[MASK]")

  // load in vocab here
  vocab_file.foreach{ vocab_file_loc: String =>
    // only when this is non-empty, we fill in both
    var index = 0
    for (line <- Source.fromFile(vocab_file_loc).getLines) {
      vocab += (line -> index)
      vocab_list += line
      index += 1
    }
  }

  def get_vocab(): ArrayBuffer[String] = {
    // this returns vocab for sampling purposes
    if (sample_vocab_list.isEmpty) {
      vocab_list.foreach{x => if (!no_rep_special_tokens.contains(x)) {sample_vocab_list += x}}
    }
    sample_vocab_list
  }

  def to_char(tokens: ArrayBuffer[String]): ArrayBuffer[String] = {
    val char_output = ArrayBuffer[String]()
    tokens.foreach{token: String =>
      if (special_tokens.contains(token)) {
        char_output += token
      } else {
        char_output ++= token.sliding(1).toList
      }
      char_output += "[WB]"
    }
    char_output
  }

  def convert_tokens_to_ids(tokens: ArrayBuffer[String], max_seq_len: Int): Array[Long] = {
    // Converts a sequence of ids in wordpiece tokens using the vocab
    // I know I can use map...

    var i = 0
    val ids: Array[Long] = Array.fill(max_seq_len)(0L)

    while (i < tokens.size) {
      ids(i) = vocab.getOrElse(tokens(i), vocab("[UNK]"))
      i += 1
    }

    ids
  }

}