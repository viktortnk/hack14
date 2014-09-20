name := "hack"

version := "1.0"

scalaVersion := "2.10.4"

mainClass := Some("TwitterApp")

fork := true

javaOptions in run += "-Dspark.master=local[2]"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.1.0" % "provided",
  "com.twitter" % "parquet-avro" % "1.6.0rc2"
    exclude("commons-beanutils", "commons-beanutils-core")
    exclude("commons-collections", "commons-collections")
    exclude("commons-logging", "commons-logging")
    exclude("commons-collections", "commons-collections"),
  "org.apache.spark" %% "spark-streaming-twitter" % "1.1.0"  exclude("org.mortbay.jetty", "servlet-api")
    exclude("commons-beanutils", "commons-beanutils-core")
    exclude("commons-collections", "commons-collections")
    exclude("commons-logging", "commons-logging")
    exclude("commons-collections", "commons-collections")
    exclude("com.esotericsoftware.minlog", "minlog"),
  "org.apache.spark" %% "spark-streaming-kafka" % "1.1.0"  exclude("org.mortbay.jetty", "servlet-api")
    exclude("commons-beanutils", "commons-beanutils-core")
    exclude("commons-collections", "commons-collections")
    exclude("commons-logging", "commons-logging")
    exclude("commons-collections", "commons-collections")
    exclude("com.esotericsoftware.minlog", "minlog")
)
