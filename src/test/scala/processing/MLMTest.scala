package processing

import org.scalatest.FlatSpec

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class MLMTest extends FlatSpec {

  behavior of "MLMTest"

  it should "create_masked_lm_predictions" in {

    val tokenizer = new Tokenizer(None)

    var tweet_tup = (ArrayBuffer[String]("@USER", "did", "you", "talk", "to", "Rachel", "yet", "?"), ArrayBuffer[String]("@USER", "she", "is", "not", "here", "today", "."))
    var paired_tweet: ArrayBuffer[String] = ArrayBuffer("[CLS]") ++ tweet_tup._1 ++ ArrayBuffer("[SEP]") ++ tweet_tup._2
    println(paired_tweet)
    var vocab_list = ArrayBuffer[String]()

    val vocab_string = ArrayBuffer("did", "you", "talk", "to", "Rachel", "yet", "?", "she", "is", "not", "here", "today", ".")

    vocab_list += "[CLS]"
    vocab_list += "[SEP]"
    vocab_string.mkString(" ").sliding(1).foreach(x => vocab_list += x)

    println(vocab_list)

    tokenizer.vocab_list = vocab_list

    vocab_list = tokenizer.get_vocab()

    println(vocab_list)

    paired_tweet = tokenizer.to_char(paired_tweet)

    var result = MLM.create_masked_lm_predictions_char(paired_tweet, 0.2, 60, vocab_list)
    println(result._1)
    println(result._2)
    println(result._3)

    paired_tweet = ArrayBuffer("[CLS]") ++ tweet_tup._1 ++ ArrayBuffer("[SEP]") ++ tweet_tup._2
    paired_tweet = tokenizer.to_char(paired_tweet)
    result = MLM.create_masked_lm_predictions_char(paired_tweet, 0.2, 60, vocab_list)
    println(result._1)
    println(result._2)
    println(result._3)
  }

  it should "create word level mixin" in {
    val tokenizer = new Tokenizer(None)

    var tweet_tup = (ArrayBuffer[String]("@USER", "did", "you", "talk", "to", "Rachel", "yet", "?"), ArrayBuffer[String]("@USER", "she", "is", "not", "here", "today", "."))
    var paired_tweet: ArrayBuffer[String] = ArrayBuffer("[CLS]") ++ tweet_tup._1 ++ ArrayBuffer("[SEP]") ++ tweet_tup._2
    var vocab_list = ArrayBuffer[String]()

    val vocab_string = ArrayBuffer("did", "you", "talk", "to", "Rachel", "yet", "?", "she", "is", "not", "here", "today", ".")

    vocab_list += "[CLS]"
    vocab_list += "[SEP]"
    vocab_string.mkString(" ").sliding(1).foreach(x => vocab_list += x)

    tokenizer.vocab_list = vocab_list

    vocab_list = tokenizer.get_vocab()

    // we always call to_char() first
    paired_tweet = tokenizer.to_char(paired_tweet)

    // TODO: this is NOT working...let's fix it
    // TODO: fix it and you can move on!!!
    var result = MLM.create_masked_lm_predictions_word(paired_tweet, 0.2, 60, vocab_list)

    println(result._1)
    println(result._2)
    println(result._3)
  }

  // Nice job!!
  it should "create segment based masking" in {

    val tokenizer = new Tokenizer(None)

    var tweet_tup = (ArrayBuffer[String]("@USER", "did", "you", "talk", "to", "Rachel", "yet", "?"), ArrayBuffer[String]("@USER", "she", "is", "not", "here", "today", "."))
    var paired_tweet: ArrayBuffer[String] = ArrayBuffer("[CLS]") ++ tweet_tup._1 ++ ArrayBuffer("[SEP]") ++ tweet_tup._2
    println(paired_tweet)
    var vocab_list = ArrayBuffer[String]()

    val vocab_string = ArrayBuffer("did", "you", "talk", "to", "Rachel", "yet", "?", "she", "is", "not", "here", "today", ".")

    vocab_list += "[CLS]"
    vocab_list += "[SEP]"
    vocab_string.mkString(" ").sliding(1).foreach(x => vocab_list += x)

    println(vocab_list)

    tokenizer.vocab_list = vocab_list

    vocab_list = tokenizer.get_vocab()

    println(vocab_list)

    paired_tweet = tokenizer.to_char(paired_tweet)

    var result = MLM.create_masked_lm_predictions_segment(paired_tweet, 0.2, 60, 6, true, vocab_list)
    println(result._1)
    println(result._2)
    println(result._3)

    paired_tweet = ArrayBuffer("[CLS]") ++ tweet_tup._1 ++ ArrayBuffer("[SEP]") ++ tweet_tup._2
    paired_tweet = tokenizer.to_char(paired_tweet)
    result = MLM.create_masked_lm_predictions_segment(paired_tweet, 0.2, 60, 6, true, vocab_list)
    println(result._1)
    println(result._2)
    println(result._3)
  }

}
