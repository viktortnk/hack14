package storm

import java.io.{FileInputStream, InputStream}
import java.util.{HashMap, Map, UUID}
import java.util.concurrent.ConcurrentHashMap

import backtype.storm.{Config, LocalCluster, StormSubmitter}
import backtype.storm.spout.SpoutOutputCollector
import backtype.storm.task.{OutputCollector, TopologyContext}
import backtype.storm.topology.{OutputFieldsDeclarer, TopologyBuilder}
import backtype.storm.tuple.{Fields, Tuple, Values}
import org.apache.storm.hdfs.bolt.HdfsBolt
import org.apache.storm.hdfs.bolt.format.{DefaultFileNameFormat, DelimitedRecordFormat, FileNameFormat, RecordFormat}
import org.apache.storm.hdfs.bolt.rotation.{FileRotationPolicy, TimedRotationPolicy}
import org.apache.storm.hdfs.bolt.sync.{CountSyncPolicy, SyncPolicy}
import org.apache.storm.hdfs.common.rotation.MoveFileAction
import org.yaml.snakeyaml.Yaml

object Topology {

  def main(args: Array[String]): Unit = {
    val conf = new Config
    conf.setNumWorkers(1)

    val  spout = new SentenceSpout()

    val syncPolicy = new CountSyncPolicy(1000)

    val rotationPolicy = new TimedRotationPolicy(1.0f, TimedRotationPolicy.TimeUnit.MINUTES)

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

    builder.setSpout("sentence-spout", spout, 1)

    builder.setBolt("bolt", bolt, 4).shuffleGrouping("sentence-spout")

    StormSubmitter.submitTopology("hdfs-topology", conf, builder.createTopology())
  }

//    // use "|" instead of "," for field delimiter
//    RecordFormat format = new DelimitedRecordFormat()
//      .withFieldDelimiter("|");
//
//    Yaml yaml = new Yaml();
//    InputStream in = new FileInputStream(args[1]);
//    Map<String, Object> yamlConf = (Map<String, Object>) yaml.load(in);
//    in.close();
//    config.put("hdfs.config", yamlConf);
//
//    HdfsBolt bolt = new HdfsBolt()
//      .withConfigKey("hdfs.config")
//      .withFsUrl(args[0])
//    .withFileNameFormat(fileNameFormat)
//      .withRecordFormat(format)
//      .withRotationPolicy(rotationPolicy)
//      .withSyncPolicy(syncPolicy)
//      .addRotationAction(new MoveFileAction().toDestination("/dest2/"));
//
//    TopologyBuilder builder = new TopologyBuilder();
//
//    builder.setSpout(SENTENCE_SPOUT_ID, spout, 1);
//    // SentenceSpout --> MyBolt
//    builder.setBolt(BOLT_ID, bolt, 4)
//      .shuffleGrouping(SENTENCE_SPOUT_ID);
//
//    if (args.length == 2) {
//      LocalCluster cluster = new LocalCluster();
//
//      cluster.submitTopology(TOPOLOGY_NAME, config, builder.createTopology());
//      waitForSeconds(120);
//      cluster.killTopology(TOPOLOGY_NAME);
//      cluster.shutdown();
//      System.exit(0);
//    } else if (args.length == 3) {
//      StormSubmitter.submitTopology(args[0], config, builder.createTopology());
//    } else{
//      System.out.println("Usage: HdfsFileTopology [topology name] <yaml config file>");
//    }
//  }
//
//  public static void waitForSeconds(int seconds) {
//    try {
//      Thread.sleep(seconds * 1000);
//    } catch (InterruptedException e) {
//    }
//  }
//

//  public static class MyBolt extends BaseRichBolt {
//
//    private HashMap<String, Long> counts = null;
//    private OutputCollector collector;
//
//    public void prepare(Map config, TopologyContext context, OutputCollector collector) {
//      this.counts = new HashMap<String, Long>();
//      this.collector = collector;
//    }
//
//    public void execute(Tuple tuple) {
//      collector.ack(tuple);
//    }
//
//    public void declareOutputFields(OutputFieldsDeclarer declarer) {
//      // this bolt does not emit anything
//    }
//
//    @Override
//    public void cleanup() {
//    }
//  }
}