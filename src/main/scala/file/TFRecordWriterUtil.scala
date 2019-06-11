package file

import java.io.File

import scala.io.Source
import java.nio.file.Paths
import java.util

import org.platanios.tensorflow.api._
import org.platanios.tensorflow.api.io.TFRecordWriter
import org.platanios.tensorflow.api.ops.Parsing.FixedLengthFeature
import com.google.protobuf.ByteString
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import org.tensorflow.example._


object TFRecordWriterUtil {

  def int64Feature(in: Long): Feature = {

    val valueBuilder = Int64List.newBuilder()
    valueBuilder.addValue(in)

    Feature.newBuilder()
      .setInt64List(valueBuilder.build())
      .build()
  }


  def floatFeature(in: Float): Feature = {
    val valueBuilder = FloatList.newBuilder()
    valueBuilder.addValue(in)
    Feature.newBuilder()
      .setFloatList(valueBuilder.build())
      .build()
  }

  def floatVectorFeature(in: Array[Float]): Feature = {
    val valueBuilder = FloatList.newBuilder()
    in.foreach(valueBuilder.addValue)

    Feature.newBuilder()
      .setFloatList(valueBuilder.build())
      .build()
  }

  def bytesFeature(in: Array[Byte]): Feature = {
    val valueBuilder = BytesList.newBuilder()
    valueBuilder.addValue(ByteString.copyFrom(in))
    Feature.newBuilder()
      .setBytesList(valueBuilder.build())
      .build()
  }

  def createTFRecordFileExample(filename: String): Unit = {
    // verify if this works...
    //  Source.fromResource(filename)
    val writer = new TFRecordWriter(Paths.get(filename))
    val features = Features.newBuilder()
    features.putFeature("length", int64Feature(15))
    val example = Example.newBuilder().setFeatures(features).build()
    writer.write(example)
    writer.close()
  }

}
