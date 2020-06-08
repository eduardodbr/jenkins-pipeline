import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructType}
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import com.datastax.spark.connector._
import com.datastax.spark.connector.cql.CassandraConnectorConf
import org.apache.spark.sql.cassandra._
import com.datastax.spark.connector.streaming._


object SparkConsumer {


  def main(args: Array[String]): Unit = {
    new _SparkConsumer().run()
  }

  class _SparkConsumer() {

    val spark = SparkSession
      .builder()
      .appName("SparkSensorGen")
      .getOrCreate()

    spark.sparkContext.setLogLevel("ERROR")

    def run() : String= {

      val kafkaServers  = System.getenv("KAFKA_BROKER")
      val topic         = System.getenv("TOPIC")
      val cassandraServer = System.getenv("CASSANDRA")

      println("Reading from kafka "+ kafkaServers + " topic "+ topic)
      println("Writting to "+ cassandraServer)

      spark.setCassandraConf(CassandraConnectorConf.ConnectionHostParam.option(cassandraServer))
      //spark.conf.set("spark.cassandra.connection.host", cassandraServer)
     // val schema = new StructType()
       // .add("foo", StringType)

      val r = scala.util.Random

      import spark.implicits._

      val source = spark
        .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", kafkaServers)
        .option("subscribe", topic)
        .load()
        .select(col("value").cast(StringType), col("timestamp") as "broker_ts", col("topic"),col("partition"), col("offset"))

      val out = source
        .withColumn("key",lit("device-"+r.nextInt(100)))
        .writeStream
        .option("checkpointLocation", "checkpoint/")
        .outputMode(OutputMode.Append())
        .format("org.apache.spark.sql.cassandra")
        .option("keyspace", "test")
        .option("table", "kv")
        .trigger(Trigger.ProcessingTime("10 seconds"))
        .start()

      out.awaitTermination()

      return ""
    }

  }
}
