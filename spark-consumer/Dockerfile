ARG SPARK_IMAGE=gcr.io/spark-operator/spark:v2.4.5
FROM ${SPARK_IMAGE}

COPY target/scala-2.11/spark-consumer-assembly-0.1.jar /opt/spark/examples/jars/


ENTRYPOINT ["/opt/entrypoint.sh"]