package processing

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.Searching

object Util {

  val rnd = new scala.util.Random

  def searchsorted(doc_cumsum: mutable.Buffer[Int], sentence_index:Int): Int = {
    // this also equals to side='right'
    // cumsum means that left < right
    var index: Int = -100
    var iter: Int = 0
    doc_cumsum.foreach{curr: Int =>
      if (sentence_index < curr) {
        index = iter
      } else {
        iter += 1
      }
    }
    if (index == -100) {
      iter
    } else {
      index
    }
  }

  def nextInt(n: Int): Int = rnd.nextInt(n)
  def nextInt(): Int = rnd.nextInt()

  /** Returns a pseudorandom, uniformly distributed int value between min
    *  (inclusive) and the specified value max (exclusive), drawn from this
    *  random number generator's sequence.
    */
  def between(minInclusive: Int, maxExclusive: Int): Int = {
    require(minInclusive < maxExclusive, "Invalid bounds")

    val difference = maxExclusive - minInclusive
    if (difference >= 0) {
      nextInt(difference) + minInclusive
    } else {
      /* The interval size here is greater than Int.MaxValue,
       * so the loop will exit with a probability of at least 1/2.
       */
      @tailrec
      def loop(): Int = {
        val n = nextInt()
        if (n >= minInclusive && n < maxExclusive) n
        else loop()
      }
      loop()
    }
  }

//  {
//    "train_corpus": "",
//    "train_file_filter": "tweets.txt",
//    "output_dir": "",
//    "vocab_path": "",
//    "bert_model": "",
//    "do_lower_case": true,
//    "do_bpe": false,
//    "start_epoch": 0,
//    "epochs_to_generate": 30,
//    "min_word_seq_len": 3,
//    "max_seq_len": 300,
//    "word_or_char_mask_prob": 0.6,
//    "masked_lm_prob": 0.2,
//    "max_predictions_per_seq": 60,
//    "tfrecord": true,
//    "json": true
//  }
  case class Config(train_corpus: String, train_file_filter: String,
                    output_dir: String, vocab_path: String, bert_model: String,
                    do_lower_case: Boolean, do_bpe: Boolean, start_epoch: Int,
                    epochs_to_generate: Int, min_word_seq_len: Int, max_seq_len: Int,
                    word_mask_prob: Double, masked_lm_prob: Double, max_predictions_per_seq: Int,
                    tfrecord: Boolean, json: Boolean)


}
