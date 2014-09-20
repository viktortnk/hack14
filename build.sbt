name := "hack"

version := "1.0"

scalaVersion := "2.10.4"

mainClass := Some("TwitterApp")

fork := true

javaOptions in run += "-Dspark.master=local[2]"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.1.0" % "provided",
  "org.apache.hadoop" % "hadoop-client" % "2.3.0" exclude("org.mortbay.jetty", "servlet-api")
    exclude("commons-beanutils", "commons-beanutils-core")
    exclude("commons-logging", "commons-logging")
    exclude("javax.servlet", "servlet-api")
    exclude("com.esotericsoftware.minlog", "minlog"),
  "org.apache.spark" %% "spark-streaming-twitter" % "1.1.0"  exclude("org.mortbay.jetty", "servlet-api")
    exclude("commons-beanutils", "commons-beanutils-core")
    exclude("commons-logging", "commons-logging")
    exclude("org.apache.hadoop", "hadoop-client")
    exclude("commons-collections", "commons-collections")
    exclude("com.esotericsoftware.minlog", "minlog"),
  "org.apache.spark" %% "spark-streaming-kafka" % "1.1.0"  exclude("org.mortbay.jetty", "servlet-api")
    exclude("commons-beanutils", "commons-beanutils-core")
    exclude("commons-logging", "commons-logging")
    exclude("org.apache.hadoop", "hadoop-client")
    exclude("commons-collections", "commons-collections")
    exclude("com.esotericsoftware.minlog", "minlog")
)
