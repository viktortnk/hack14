import AssemblyKeys._

name := "storm-hdfs"

 // put this at the top of the file

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case PathList(ps @ _*) if ps.last endsWith "package-info.class" => MergeStrategy.first
  case x => old(x)
}
}

scalaVersion := "2.10.4"

version := "1.0"

resolvers += "Whisk Snapshots" at "http://whisklabs.github.io/mvn-repo/snapshots/"

resolvers += "clojars" at "http://clojars.org/repo"

libraryDependencies ++= Seq(
  "org.apache.storm" % "storm-core" % "0.9.3-incubating-SNAPSHOT" % "provided",
  "org.apache.storm" % "storm-kafka" % "0.9.3-incubating-SNAPSHOT",
  "org.apache.storm" % "storm-hdfs" % "0.9.3-incubating-SNAPSHOT"
    exclude("commons-beanutils", "commons-beanutils-core")
    exclude("commons-collections", "commons-collections"),
  "org.apache.kafka" % "kafka_2.10" % "0.8.1.1"
    exclude("javax.jms", "jms")
    exclude("com.sun.jdmk", "jmxtools")
    exclude("com.sun.jmx", "jmxri")
    exclude("org.slf4j", "slf4j-simple")
    exclude("log4j", "log4j")
    exclude("org.apache.zookeeper", "zookeeper")
)
