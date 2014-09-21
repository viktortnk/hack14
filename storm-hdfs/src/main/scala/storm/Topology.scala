package storm

import backtype.storm.spout.SchemeAsMultiScheme
import backtype.storm.topology.TopologyBuilder
import backtype.storm.{Config, StormSubmitter}
import org.apache.storm.hdfs.bolt.HdfsBolt
import org.apache.storm.hdfs.bolt.format.{DefaultFileNameFormat, DelimitedRecordFormat}
import org.apache.storm.hdfs.bolt.rotation.TimedRotationPolicy
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy
import storm.kafka.{KafkaSpout, SpoutConfig, StringScheme, ZkHosts}

object Topology {

  def main(args: Array[String]): Unit = {
    val conf = new Config
    conf.setNumWorkers(1)

    val syncPolicy = new CountSyncPolicy(20000)

    val rotationPolicy = new TimedRotationPolicy(5.0f, TimedRotationPolicy.TimeUnit.MINUTES)

    val spoutConf = {
      val zkHosts = new ZkHosts("130.211.97.93:2181")
      val zkRoot = "/sample2hdfs"
      val spoutConfig = new SpoutConfig(zkHosts, "twitter-sample", zkRoot, "twitter-stream-spout")
      spoutConfig.zkServers = java.util.Arrays.asList("130.211.97.93")
      spoutConfig.zkPort = 2181
      spoutConfig.scheme = new SchemeAsMultiScheme(new StringScheme)
      if(args.nonEmpty) spoutConfig.forceFromStart = true
      spoutConfig
    }

    val spout = new KafkaSpout(spoutConf)

    val fileNameFormat = new DefaultFileNameFormat()
      .withPath("/tmp/")
      .withExtension(".txt")

    val format = new DelimitedRecordFormat().withFieldDelimiter("|")

    val bolt = new HdfsBolt()
      .withFsUrl("hdfs://10.140.108.171")
      .withFileNameFormat(fileNameFormat)
      .withRecordFormat(format)
      .withRotationPolicy(rotationPolicy)
      .withSyncPolicy(syncPolicy)

    val builder = new TopologyBuilder()

    builder.setSpout("twitter-spout", spout, 4)

    builder.setBolt("twitter-processor", bolt, 4).shuffleGrouping("twitter-spout")

    StormSubmitter.submitTopology("hdfs-topology", conf, builder.createTopology())

  }
}