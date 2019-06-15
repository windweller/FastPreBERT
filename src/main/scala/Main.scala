
import java.io.File
import java.nio.file.Files
import java.text.DecimalFormat

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils
import org.platanios.tensorflow.api.core.Shape
import org.platanios.tensorflow.api.io.TFRecordWriter
import org.platanios.tensorflow.api.tensors.Tensor
import org.tensorflow.example.Feature
import processing.SentenceDatabase
import spray.json._
import DefaultJsonProtocol._
import me.tongfei.progressbar.{ProgressBar, ProgressBarBuilder, ProgressBarStyle}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

// the if main thing will be here
// instead of using command line arguments, which is exceptionally hard in Scala
// we use JSON pre-stored configs
// then all we need is just do `sbt run`

object Main extends App {

  if (args.length == 0) {
    println("need to select the config file")
    System.exit(0)
  }
  else {
    println("Using configuration " + args(0))

    var config: processing.Util.Config = null
    if (args(0) == "node14_char_noising_config") {
      config = Configs.node14_char_noising_config
    }
    else if (args(0) == "local_test_config") {
      config = Configs.local_test_config
    }
    else {
      println("configuration " + args(0) + " not found")
      System.exit(0)
    }

    val sent_d = new SentenceDatabase()

    // now we need to do File IO to load in dataset...

    val d = new File(config.train_corpus)
    val train_files = d.listFiles.filter(f => f.isFile && f.getName.contains(config.train_file_filter)).toList

    println("Grabbed training files: ")
    println(train_files)

    // loading and building up dataset
    // TODO: do progress bar
    // this(task, initialMax, 1000, System.err, ProgressBarStyle.COLORFUL_UNICODE_BLOCK, "", 1, false, null);
    val pb = new ProgressBar("Loading sentences", 32000000, 1000, System.err,
      ProgressBarStyle.COLORFUL_UNICODE_BLOCK, "", 1, true, new DecimalFormat("#.#"))
    var cumulative_sent = 0
    train_files.foreach{f =>
      Source.fromFile(f).getLines().foreach{json_line =>
        val json = json_line.parseJson
        val tup = json.convertTo[(Array[String], Array[String])]
        sent_d.add_sentence((tup._1.to[ArrayBuffer], tup._2.to[ArrayBuffer]))
        cumulative_sent += 1
        pb.step()
        if (cumulative_sent >= 32000000) {
          pb.maxHint(32000000 + 10000)
        }
      }
    }
    pb.close()

    println("Number of sentences in corpus: " + sent_d.sentences.size)

    // now process and write to files!! also add progress bar
    // for all 20-30 epochs, we generate concurrently
    // TODO: do progress bar
    // TODO: transfer code for one epoch, then add concurrency


  }

}
