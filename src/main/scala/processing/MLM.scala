package processing

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Random

/**
  * Here we add the procedure for MLM and NSP
  * We stopped doing document-based sampling
  */

object MLM {

  def create_masked_lm_predictions(tokens: ArrayBuffer[String],
                                   masked_lm_prob: Double,
                                   max_predictions_per_seq: Int,
                                   vocab_list: ArrayBuffer[String]): (ArrayBuffer[String], ArrayBuffer[Int], ArrayBuffer[String]) = {
    var cand_indices = ArrayBuffer[Int]()

    var i = 0
    while (i < tokens.size) {
      if (tokens(i) != "[CLS]" && tokens(i) != "[SEP]") {
        cand_indices += i
      }
      i += 1
    }

    val num_to_mask = math.min(max_predictions_per_seq, math.max(1, math.round(tokens.size * masked_lm_prob))).toInt

    cand_indices = Random.shuffle(cand_indices)
    val mask_indices = cand_indices.take(num_to_mask).sorted  // directly take a shuffled list, instead of Python's "sample"

    val masked_token_labels = ArrayBuffer[String]()
    mask_indices.foreach{ index =>
      var masked_token = ""
      if (Random.nextFloat() < 0.8) {
        masked_token = "[MASK]"
      } else {
        // 10% of the time, keep original
        if (Random.nextFloat() < 0.5) {
          masked_token = tokens(index)
        } else {
          masked_token = vocab_list(Random.nextInt(vocab_list.size))  // randomly choose a vocab!
        }
      }
      masked_token_labels += tokens(index)
      tokens(index) = masked_token  // replace the original token with the masked token
    }

    (tokens, mask_indices, masked_token_labels)
  }

  // create_masked_lm_predictions_seq
  def create_masked_lm_predictions_char(tokens: ArrayBuffer[String],
                                   masked_lm_prob: Double,
                                   max_predictions_per_seq: Int,
                                   vocab_list: ArrayBuffer[String]): (ArrayBuffer[String], ArrayBuffer[Int], ArrayBuffer[String]) = {
    /**
      * vocab_list: should be the list that we are willing to sample from...
      * special tokens will be removed from this list.
      */
    var cand_indices = ArrayBuffer[Int]()

    var i = 0
    while (i < tokens.size) {
      if (tokens(i) != "[CLS]" && tokens(i) != "[SEP]" && tokens(i) != "[WB]") {
        cand_indices += i
      }
      i += 1
    }

    val num_to_mask = math.min(max_predictions_per_seq, math.max(1, math.round(tokens.size * masked_lm_prob))).toInt

    cand_indices = Random.shuffle(cand_indices)
    val mask_indices = cand_indices.take(num_to_mask).sorted  // directly take a shuffled list, instead of Python's "sample"

    val masked_token_labels = ArrayBuffer[String]()
    mask_indices.foreach{ index =>
      var masked_token = ""
      if (Random.nextFloat() < 0.8) {
        masked_token = "[MASK]"
      } else {
        // 10% of the time, keep original
        if (Random.nextFloat() < 0.5) {
          masked_token = tokens(index)
        } else {
          masked_token = vocab_list(Random.nextInt(vocab_list.size))  // randomly choose a vocab!
        }
      }
      masked_token_labels += tokens(index)
      tokens(index) = masked_token  // replace the original token with the masked token
    }

    (tokens, mask_indices, masked_token_labels)
  }

  // word-level masking/noising
  def create_masked_lm_predictions_word(tokens: ArrayBuffer[String],
                                        masked_lm_prob: Double,
                                        max_predictions_per_seq: Int,
                                        vocab_list: ArrayBuffer[String]): (ArrayBuffer[String], ArrayBuffer[Int], ArrayBuffer[String]) = {
    /**
      * vocab_list: should be the list that we are willing to sample from...
      * special tokens will be removed from this list.
      */
    var cand_word_indices = ArrayBuffer[ArrayBuffer[Int]]()
    var word_indices = ArrayBuffer[Int]()

    //for ((token, i) <- tokens.view.zipWithIndex)
     tokens.view.zipWithIndex.foreach{ orig_tup =>
      val (token, i) = orig_tup

      if (token == "[CLS]" || token == "[SEP]") {} // "continue"
      else {
        if (token == "[WB]") {
          cand_word_indices += word_indices
          word_indices = ArrayBuffer[Int]()
        } else {
          token.sliding(1).zipWithIndex.foreach{tup =>
              word_indices += i + tup._2
          }
        }
      }
    }
    cand_word_indices = Random.shuffle(cand_word_indices)

    var num_to_mask = math.min(max_predictions_per_seq, math.max(1, math.round(tokens.size * masked_lm_prob))).toInt

    var num_masked_tokens = 0
    var mask_indices = ArrayBuffer[Int]()

    // this is basically "taking" a randomly shuffled word list to mask
    cand_word_indices.foreach { word =>
      if (num_masked_tokens + word.size < num_to_mask) {
        mask_indices ++= word
        num_masked_tokens += word.size
      }
    }

    mask_indices = mask_indices.sorted

    val masked_token_labels = ArrayBuffer[String]()
    val mask_char_indices = ArrayBuffer[Int]()

    mask_indices.foreach { index =>
      mask_char_indices += index
      var masked_token = ""
      if (Random.nextFloat() < 0.8) {
        masked_token = "[MASK]"
      } else {
        // 10% of the time, keep original
        if (Random.nextFloat() < 0.5) {
          masked_token = tokens(index)
        } else {
          masked_token = vocab_list(Random.nextInt(vocab_list.size))  // randomly choose a vocab!
        }
      }
      masked_token_labels += tokens(index)
      tokens(index) = masked_token  // replace the original token with the masked token
    }

    (tokens, mask_char_indices, masked_token_labels)
  }

  // sequence-level continuous segment masking/noising
  // Procedure:
  // Sample with gaps
  // 1. Prepare indices to sample. Indices must be max(window_size) apart
  // 2. Sample len(indicies) * prob
  // 3. For each indicies, sample a window size j, run mask/noise
  // the expected size is:
  def create_masked_lm_predictions_segment(tokens: ArrayBuffer[String],
                                        masked_lm_prob: Double,
                                        max_predictions_per_seq: Int,
                                        window_max_size: Int,
                                           sample_window: Boolean,
                                        vocab_list: ArrayBuffer[String]): (ArrayBuffer[String], ArrayBuffer[Int], ArrayBuffer[String]) = {
    /**
      * vocab_list: should be the list that we are willing to sample from...
      * special tokens will be removed from this list.
      */
    var cand_indices = ArrayBuffer[Int]()

    for ((token, i) <- tokens.view.zipWithIndex) {
      if (token == "[CLS]" || token == "[SEP]") {} // "continue"
      else {
        cand_indices += i
      }
    }

    var segments = cand_indices.sliding(window_max_size, window_max_size) //return an iterator

    // we shuffle and directly start masking! We add another segment to mask if it doesn't exceed expected number!
    segments = Random.shuffle(segments)

    var num_to_mask = math.min(max_predictions_per_seq, math.max(1, math.round(tokens.size * masked_lm_prob))).toInt

    var num_masked_tokens = 0
    var mask_indices = ArrayBuffer[Int]()

    // TODO: we can't just keep masking until we reach 60 characters
    // TODO: this needs to scale with
    segments.foreach { seg =>
      val sampled_window_size = Random.nextInt(window_max_size)
      val start = window_max_size - sampled_window_size //if max, then (0, max); if smaller, then (1, max-1)
      if (num_masked_tokens + seg.size < num_to_mask) {
        if (sample_window) {
          val chosen_window = seg.slice(start, start+sampled_window_size)
          mask_indices ++= chosen_window
          num_masked_tokens += chosen_window.size
        } else {
          mask_indices ++= seg
          num_masked_tokens += seg.size
        }
      }
    }

    mask_indices = mask_indices.sorted

    val masked_token_labels = ArrayBuffer[String]()
    mask_indices.foreach{ index =>
      var masked_token = ""
      if (Random.nextFloat() < 0.8) {
        masked_token = "[MASK]"
      } else {
        // 10% of the time, keep original
        if (Random.nextFloat() < 0.5) {
          masked_token = tokens(index)
        } else {
          masked_token = vocab_list(Random.nextInt(vocab_list.size))  // randomly choose a vocab!
        }
      }
      masked_token_labels += tokens(index)
      tokens(index) = masked_token  // replace the original token with the masked token
    }
    (tokens, mask_indices, masked_token_labels)
  }

}
