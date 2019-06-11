package processing

import org.platanios.tensorflow.api.core.Shape
import org.platanios.tensorflow.api.tensors.Tensor

import scala.collection.mutable

/**
  * This file does not do actual tokenization.
  * BERT tokenization is done at Python level and saved as JSON
  */


class Tokenizer(vocab_file: String) {

  val vocab: mutable.HashMap[String, Int] = mutable.HashMap()



}
