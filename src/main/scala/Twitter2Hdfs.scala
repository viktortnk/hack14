import java.io.File

import com.google.common.io.Files
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.api.java.JavaPairDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

object Twitter2Hdfs {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Twitter2Hdfs")
    val ssc = new StreamingContext(conf, Minutes(1))
    val zookeeperConnect = args.headOption.getOrElse("localhost:2181")
    val hdfsPrefix = args.drop(1).headOption.getOrElse("hdfs://10.140.108.171/tmp/twitter")
    val stream =
      KafkaUtils
        .createStream(ssc, zookeeperConnect, "cons4", Map("twitter-sample" -> 4))

    val pairDStream  = new JavaPairDStream(stream)

    val tempDir = Files.createTempDir()
    val outputDir = new File(tempDir, "output").getAbsolutePath
    println(outputDir)

    pairDStream.saveAsNewAPIHadoopFiles(hdfsPrefix, "txt", classOf[Void], classOf[Text],
      classOf[TextOutputFormat[_, _]])

    ssc.start()
    ssc.awaitTermination()
  }

}
