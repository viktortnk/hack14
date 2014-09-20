import java.io.File

import com.google.common.io.Files
import hack.Tweet
import org.apache.hadoop.mapred.TextOutputFormat
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.SparkConf
import org.apache.spark.streaming.api.java.{JavaPairReceiverInputDStream, JavaPairDStream}
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import parquet.avro.{AvroParquetOutputFormat, AvroWriteSupport}
import parquet.hadoop.ParquetOutputFormat
import twitter4j.json.DataObjectFactory

object Twitter2Hdfs {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Twitter2Hdfs")
    val ssc = new StreamingContext(conf, Minutes(1))
    val zookeeperConnect = args.headOption.getOrElse("localhost:2181")
    val hdfsPrefix = args.tail.headOption.getOrElse("hdfs://10.140.108.171/tmp/twitter")
    val stream =
      KafkaUtils
        .createStream(ssc, zookeeperConnect, "cons4", Map("twitter-sample" -> 4))
        .map({case (k, v) =>
        val st = DataObjectFactory.createStatus(v)
        k -> new Tweet(st.getUser.getName)
      })


    val pairDStream  = new JavaPairDStream(stream)

    val job = new Job()

    val tempDir = Files.createTempDir()
    val outputDir = new File(tempDir, "output").getAbsolutePath
    println(outputDir)

    ParquetOutputFormat.setWriteSupportClass(job, classOf[AvroWriteSupport])

    AvroParquetOutputFormat.setSchema(job, hack.Tweet.SCHEMA$)

    pairDStream.saveAsNewAPIHadoopFiles(hdfsPrefix, "sample", classOf[Void], classOf[String],
      classOf[ParquetOutputFormat[Tweet]], job.getConfiguration)

    ssc.start()
    ssc.awaitTermination()
  }

}
