import processing.Util._

object Configs {

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

  val local_test_config = Config("/Users/aimingnie/Documents/School/NG/FastPreBERT/test_train_corpus/", "tweets_normalized.json",
    "/mnt/fs5/anie/twitter_quote_reply_char_bert_2019_june15_no_2017_04_scala/",
    "/Users/aimingnie/Documents/School/NG/bert_twitter_char_vocab_w_speical_toks.txt",
    "bert-base-uncased", do_lower_case = false, do_bpe = false, 0, 30, 3, 300, 0.6, 0.2, 60, tfrecord = true, json = true)

  val node14_char_noising_config = Config("/mnt/fs5/anie/twitter_quote_reply_2019_apr2/", "tweets_normalized.json",
                                          "/mnt/fs5/anie/twitter_quote_reply_char_bert_2019_june15_no_2017_04_scala/",
  "/mnt/fs5/anie/twitter_quote_reply_2019_apr2/bert_twitter_char_vocab_w_speical_toks_no_unused.txt",
  "bert-base-uncased", do_lower_case = false, do_bpe = false, 0, 30, 3, 300, 0.6, 0.2, 60, tfrecord = true, json = true)

}
