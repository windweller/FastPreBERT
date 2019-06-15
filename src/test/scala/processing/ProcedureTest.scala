package processing

import java.io._
import java.nio.file.Paths

import org.apache.commons.io.FileUtils
import org.platanios.tensorflow.api.io.TFRecordWriter
import org.scalatest.FlatSpec

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

import spray.json._
import DefaultJsonProtocol._

class ProcedureTest extends FlatSpec {

  behavior of "ProcedureTest"

  it should "truncate_seq_pair" in {
    var tweet_tup = (ArrayBuffer[String]("@USER", "did", "you", "talk", "to", "Rachel", "yet", "?"), ArrayBuffer[String]("@USER", "she", "is", "not", "here", "today", "."))
    var paired_tweet: ArrayBuffer[String] = ArrayBuffer("[CLS]") ++ tweet_tup._1 ++ ArrayBuffer("[SEP]") ++ tweet_tup._2
    println(paired_tweet)
    Procedure.truncate_seq_pair(tweet_tup._1, tweet_tup._2, 10)
    println(tweet_tup._1)
    println(tweet_tup._2)
  }

  it should "create_instances_from_sentence" in {
    // it's harder to test this, but simply put, we can populate the SentenceDatabase and test!
    val sent_d = new SentenceDatabase()
    var tweet_tup = (ArrayBuffer[String]("@USER", "did", "you", "talk", "to", "Rachel", "yet", "?"), ArrayBuffer[String]("@USER", "she", "is", "not", "here", "today", "."))
    var tweet_tup2 = (ArrayBuffer[String]("@USER", "She", "lost", "her", "bodily", "functions", "!"), ArrayBuffer[String]("@USER", "OH", "MY", "GOD", "MY", "REDOX", "REACTION"))

    sent_d.add_sentence(tweet_tup)
    sent_d.add_sentence(tweet_tup2)

    val tokenizer = new Tokenizer(Some("/Users/aimingnie/Documents/School/NG/bert_twitter_char_vocab_w_speical_toks.txt"))

//    val file = new File("/Users/aimingnie/Documents/School/NG/example.json")
//    val bw = new BufferedWriter(new FileWriter(file))
    val file = new FileOutputStream("/Users/aimingnie/Documents/School/NG/example.json")

    Procedure.create_instances_from_sentence(sent_d, max_seq_length = 300, masked_lm_prob = 0.2,
      max_predictions_per_seq = 60, vocab_list = tokenizer.vocab_list, tokenizer = tokenizer, word_mask_prob = 0.4, tf_record_writer = None, json_file = Some(file))
//    bw.close()
  }

  it should "create tf record" in {
    val sent_d = new SentenceDatabase()
    var tweet_tup = (ArrayBuffer[String]("@USER", "did", "you", "talk", "to", "Rachel", "yet", "?"), ArrayBuffer[String]("@USER", "she", "is", "not", "here", "today", "."))
    var tweet_tup2 = (ArrayBuffer[String]("@USER", "She", "lost", "her", "bodily", "functions", "!"), ArrayBuffer[String]("@USER", "OH", "MY", "GOD", "MY", "REDOX", "REACTION"))

    sent_d.add_sentence(tweet_tup)
    sent_d.add_sentence(tweet_tup2)

    val tokenizer = new Tokenizer(Some("/Users/aimingnie/Documents/School/NG/bert_twitter_char_vocab_w_speical_toks.txt"))

    System.setProperty("file.encoding", "UTF-8")

//    val file = new File("/Users/aimingnie/Documents/School/NG/example.json")
//    val bw = new BufferedWriter(new FileWriter(file))
    val file = new FileOutputStream("/Users/aimingnie/Documents/School/NG/example.json")

    val filename = "/Users/aimingnie/Documents/School/NG/example.tfrecord"

    FileUtils.deleteQuietly(new File(filename))

    val writer = new TFRecordWriter(Paths.get(filename))
    Procedure.create_instances_from_sentence(sent_d, max_seq_length = 300, masked_lm_prob = 0.2,
      max_predictions_per_seq = 60, vocab_list = tokenizer.vocab_list, tokenizer = tokenizer, word_mask_prob = 0.4, tf_record_writer = Some(writer),
      json_file = Some(file))
  }

  it should "parse json" in {
    val source = Source.fromFile("/Users/aimingnie/Documents/School/NG/FastPreBERT/test_train_corpus/test-1-tweets_normalized.json")
    var i = 0
    for (line <- source.getLines()) {
      if (i == 0) {
        val json = line.parseJson
        println(json)
        print(json.convertTo[(Array[String], Array[String])])
      }
      i += 1
    }
  }

}
