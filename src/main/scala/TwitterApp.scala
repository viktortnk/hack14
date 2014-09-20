import java.util.Properties

import _root_.kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import org.apache.spark._
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming._
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationBuilder

object TwitterApp {
  def main(args: Array[String]): Unit = {

    val brokerList = args.headOption.getOrElse("localhost:9092")

    val producer = new KafkaProducer(brokerList)

    println(System.getProperty("spark.master"))

    val conf = new SparkConf().setAppName("TwitterApp")
    val ssc = new StreamingContext(conf, Seconds(2))

    val auth = new OAuthAuthorization(new ConfigurationBuilder().build)

    val sample = new TwitterDStream(ssc, Some(auth), Seq.empty, StorageLevel.MEMORY_AND_DISK_SER_2)

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

class KafkaProducer(brokerList: String) extends Serializable {

  val props = new Properties()
  props.put("metadata.broker.list", brokerList)
  props.put("serializer.class", "kafka.serializer.StringEncoder")
  @transient lazy val config = new ProducerConfig(props)
  @transient lazy val producer = new Producer[String, String](config)
  val topic = "twitter-sample"

  def send(str: String): Unit  = {
    val msg = new KeyedMessage[String, String](topic, str)
    producer.send(msg)
  }

}