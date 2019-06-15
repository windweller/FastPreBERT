package processing

import java.io._

import org.platanios.tensorflow.api.io.TFRecordWriter

import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import spray.json._
import DefaultJsonProtocol._
import file.TFRecordWriterUtil._
import org.tensorflow.example.{Example, Features}
import org.apache.commons.io.{FileUtils, IOUtils}

object Procedure {

//  instance = {
//    "tokens": tokens,
//    "segment_ids": segment_ids,
//    "is_random_next": is_random_next,
//    "masked_lm_positions": masked_lm_positions,
//    "masked_lm_labels": masked_lm_labels}
  case class Instance(tokens: ArrayBuffer[String], segment_ids: ArrayBuffer[Long],
                      is_random_next: Boolean, masked_lm_positions: ArrayBuffer[Int],
                      masked_lm_labels:ArrayBuffer[String])

  implicit val instanceJsonWriter: JsonWriter[Instance] = new JsonWriter[Instance] {
    def write(inst: Instance): JsValue = {
      JsObject(
        "tokens" -> JsArray(inst.tokens.map(_.toJson).toVector),
        "segment_ids" -> JsArray(inst.segment_ids.map(_.toJson).toVector),
        "is_random_next" -> JsBoolean(inst.is_random_next),
        "masked_lm_positions" -> JsArray(inst.masked_lm_positions.map(_.toJson).toVector),
        "masked_lm_labels" -> JsArray(inst.masked_lm_labels.map(_.toJson).toVector)
      )
    }
  }


  /**
    * This should handle NSP...but also contain the main loop
    * TODO: write main sampling code
    *
    * We don't need to hold anything in memory, we just print
    */

  def truncate_seq_pair(tokens_a: ArrayBuffer[String],
                                tokens_b: ArrayBuffer[String],
                                max_num_tokens: Int): Unit = {
    var exit_loop = false
    while(!exit_loop) {
      var total_length = tokens_a.size + tokens_b.size
      if (total_length <= max_num_tokens) {
        exit_loop = true
      }
      // trunc_tokens = tokens_a if len(tokens_a) > len(tokens_b) else tokens_b
      var trunc_tokens: ArrayBuffer[String] = null
      if (tokens_a.size > tokens_b.size) {
        trunc_tokens = tokens_a
      } else {
        trunc_tokens = tokens_b
      }
      // assert len(trunc_tokens) >= 1 (hard to run assertions here)

      // We want to sometimes truncate from the front and sometimes from the
      // back to add more randomness and avoid biases.

      if (Random.nextFloat() < 0.5) {
        trunc_tokens.remove(0)  // remove first
      } else {
        trunc_tokens.remove(trunc_tokens.size - 1)  // remove end
      } // both are mutable operations, so we don't return anything
    }
  }

  def create_instances_from_sentence(sent_database: SentenceDatabase,
                                      max_seq_length: Int,
                                     masked_lm_prob: Double,
                                      max_predictions_per_seq: Int,
                                     vocab_list: ArrayBuffer[String],
                                      tokenizer: Tokenizer,
                                      word_mask_prob: Double,  // in a char masking setting
                                     do_bpe: Boolean=false,
                                     do_seg: Boolean=false,  // continuous segmentation
                                     window_max_size: Int=6,
                                     sample_window: Boolean=true,  //whether we sample window
                                     num_print_to_json: Int=500,
                                     tf_record_writer: Option[TFRecordWriter]=None,
                                     json_file: Option[FileOutputStream]=None): Unit = {

    // Let's first create NSP and such...then worry about writing record file
    // no longer sampling from one document...

    // this is still not ultra-efficient...but should be faster than Python!!

    var max_num_tokens = max_seq_length - 3

    var i = 0
    var printed_to_json = 0
    while (i < sent_database.sentences.size) {
      val segment = sent_database.sentences(i)
      val (segA, segB) = segment

      // Random next
      var is_random_next = false
      var tokens_b: ArrayBuffer[String] = null
      var tokens_a: ArrayBuffer[String] = null  // bad habits but faster and less memory

      // if it's the last sentence pair in the database, it's always "is_random_next=false"
      if (Random.nextFloat() < 0.5 && i != sent_database.sentences.size - 1) {
        is_random_next = true
        tokens_b = sent_database.sample_sent(i)._2  // we are getting S2
        i -= 1  // put this segment back and generate again!
      }
      else {
        is_random_next = false
        tokens_b = segB
      }

      if (!do_bpe) {
        tokens_a = tokenizer.to_char(segA)
        tokens_b = tokenizer.to_char(tokens_b)
      }
      else {
        tokens_a = segA
        tokens_b = tokens_b
      }

      truncate_seq_pair(tokens_a, tokens_b, max_num_tokens)

      var tokens: ArrayBuffer[String] = ArrayBuffer("[CLS]") ++ tokens_a ++ ArrayBuffer("[SEP]") ++ tokens_b ++ ArrayBuffer("[SEP]")

      // The segment IDs are 0 for the [CLS] token, the A tokens and the first [SEP]
      // They are 1 for the B tokens and the final [SEP]
      // segment_ids = [0 for _ in range(len(tokens_a) + 2)] + [1 for _ in range(len(tokens_b) + 1)]

      var segment_ids = ArrayBuffer.fill(tokens_a.size + 2)(0L) ++ ArrayBuffer.fill(tokens_b.size + 1)(1L)

      // the mixture is 60%/40%

      // larger than 0.4, we do word masking (60% of time)
      // char-level/word-level mix masking
      var masked_lm_positions: ArrayBuffer[Int] = null
      var masked_lm_labels: ArrayBuffer[String] = null
      if (do_bpe) {
        // BPE!
        var tup = MLM.create_masked_lm_predictions(tokens, masked_lm_prob,max_predictions_per_seq, vocab_list)
        tokens = tup._1
        masked_lm_positions = tup._2
        masked_lm_labels = tup._3
      }
      else if (do_seg) {
        var tup = MLM.create_masked_lm_predictions_segment(tokens, masked_lm_prob,max_predictions_per_seq, window_max_size, sample_window, vocab_list)
        tokens = tup._1
        masked_lm_positions = tup._2
        masked_lm_labels = tup._3
      }
      else {
        // 0.6 work-level masking would trigger this
        if (Random.nextFloat() <= word_mask_prob) {
          var tup = MLM.create_masked_lm_predictions_word(tokens, masked_lm_prob,max_predictions_per_seq, vocab_list)
          tokens = tup._1
          masked_lm_positions = tup._2
          masked_lm_labels = tup._3
        }
        else {
          var tup = MLM.create_masked_lm_predictions_char(tokens, masked_lm_prob,max_predictions_per_seq, vocab_list)
          tokens = tup._1
          masked_lm_positions = tup._2
          masked_lm_labels = tup._3
        }
      }

      // Write to JSONS
      // https://medium.com/@stijnvermeeren.be/implicit-resolution-in-scala-an-example-with-spray-json-2de66e508e5a
      // Write to UTF-8
      // https://stackoverflow.com/questions/1001540/how-to-write-a-utf-8-file-with-java
      if (!json_file.isEmpty && printed_to_json < num_print_to_json) {
        var inst = Instance(tokens, segment_ids, is_random_next, masked_lm_positions, masked_lm_labels)
//        json_file.get.write(inst.toJson.prettyPrint + '\n')
//        FileUtils.writeStringToFile(json_file.get, inst.toJson.prettyPrint + '\n', "UTF-8")
        IOUtils.write(inst.toJson.toString() + '\n', json_file.get, "UTF-8")
        printed_to_json += 1
      }

      // we automatically close this from inside
      // because TFRecord writer can take longer
      if (printed_to_json >= num_print_to_json) {
        json_file.get.close()
      }

//      if (i >= num_print_to_json && tf_record_writer.isEmpty) {
//        i = sent_database.sentences.size // we exit early if we don't need to print TFRecord!
//      }


      if (!tf_record_writer.isEmpty) {
        // convert tokens to indiciess
        // perform a check on whether it's not good
        if (masked_lm_positions.size > max_predictions_per_seq && masked_lm_positions.size == masked_lm_labels.size) {}
        else {
          // we add padding to these functions already
          val input_ids = tokenizer.convert_tokens_to_ids(tokens, max_seq_length)
          val mask_array: Array[Long] = Array.fill(tokens.size)(1L) ++ Array.fill(max_seq_length - tokens.size)(0L)
          val masked_label_ids = tokenizer.convert_tokens_to_ids(masked_lm_labels, max_predictions_per_seq)
          val segment_array: Array[Long] = segment_ids.toArray ++ Array.fill(max_seq_length - segment_ids.size)(0L)
          val lm_position_array: Array[Long] = masked_lm_positions.map(e => e.toLong).toArray ++ Array.fill(max_predictions_per_seq - masked_lm_positions.size)(0L)
          val lm_label_array = masked_label_ids
          val masked_lm_weights = Array.fill(masked_lm_labels.size)(1.0f) ++ Array.fill(max_predictions_per_seq - masked_lm_labels.size)(0.0f)

          // now build feature!
          var features = Features.newBuilder()
          features.putFeature("input_ids", intVectorFeature(input_ids))
          features.putFeature("input_mask", intVectorFeature(mask_array))
          features.putFeature("segment_ids", intVectorFeature(segment_array))
          features.putFeature("masked_lm_positions", intVectorFeature(lm_position_array))
          features.putFeature("masked_lm_ids", intVectorFeature(lm_label_array))
          features.putFeature("masked_lm_weights", floatVectorFeature(masked_lm_weights))
          val is_next_int = if (is_random_next) 1L else 0L
          features.putFeature("next_sentence_labels", intVectorFeature(Array[Long](is_next_int)))

//          println("masked_label_ids length: " + masked_label_ids.length)
//          println("masked_lm_ids length: " + lm_label_array.length)
//          println("segment_ids length: " + segment_array.length)

          val tf_example = Example.newBuilder().setFeatures(features).build()
          tf_record_writer.get.write(tf_example)
        }
      }
      i += 1
    }

    if (!json_file.isEmpty) {json_file.get.flush(); json_file.get.close()}
    if (!tf_record_writer.isEmpty) {tf_record_writer.get.flush(); tf_record_writer.get.close()}

  }


}