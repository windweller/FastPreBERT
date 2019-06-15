package processing

import org.scalatest.{FlatSpec, FunSuite}

import scala.collection.mutable.ArrayBuffer

class TokenizerTest extends FlatSpec {
  "to_char" should "split strings into char" in {
    val test_string = ArrayBuffer("@USER", "did", "you", "talk", "to", "Rachel", "yet", "?")

    val tokenizer = new Tokenizer(None)
    println(tokenizer.to_char(test_string))
  }

  "load_vocab" should "load in vocabulary" in {
//    val tokenizer = new Tokenizer(Some("/mnt/fs5/anie/twitter_quote_reply_2019_apr2/bert_twitter_char_vocab_w_speical_toks.txt"))
    val tokenizer = new Tokenizer(Some("/Users/aimingnie/Documents/School/NG/bert_twitter_char_vocab_w_speical_toks.txt"))
    println(tokenizer.vocab_list.size)
    println(tokenizer.vocab_list(100))
    println(tokenizer.vocab_list(200))
  }

  "it" should "convert_tokens_to_ids" in {
    val test_string = ArrayBuffer("@USER", "did", "you", "talk", "to", "Rachel", "yet", "?")
    val tokenizer = new Tokenizer(None)


  }

}
