import org.platanios.tensorflow.api.core.Shape
import org.platanios.tensorflow.api.tensors.Tensor

class Tokenizer {
  val tensor = Tensor.zeros[Int](Shape(2, 5))

}
