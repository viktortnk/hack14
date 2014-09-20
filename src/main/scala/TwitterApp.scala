import java.util.Properties

import _root_.kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import org.apache.spark._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._

object TwitterApp {
  def main(args: Array[String]): Unit = {

    val producer = new KafkaProducer

    println(System.getProperty("spark.master"))

    val conf = new SparkConf().setAppName("TwitterApp")
    val ssc = new StreamingContext(conf, Seconds(2))

    val sample = new TwitterDStream(ssc, None, Seq.empty, StorageLevel.MEMORY_AND_DISK_SER_2)

    sample.foreachRDD { rdd =>
      rdd.foreach { statusJson =>
        println(statusJson)
        producer.send(statusJson)
      }
    }

    ssc.start()
    ssc.awaitTermination()

  }
}

class KafkaProducer extends Serializable {

  val props = new Properties()
  props.put("metadata.broker.list", "localhost:9092")
  props.put("serializer.class", "kafka.serializer.StringEncoder")
  @transient lazy val config = new ProducerConfig(props)
  @transient lazy val producer = new Producer[String, String](config)
  val topic = "twitter-sample"

  def send(str: String): Unit  = {
    val msg = new KeyedMessage[String, String](topic, str)
    producer.send(msg)
  }

}