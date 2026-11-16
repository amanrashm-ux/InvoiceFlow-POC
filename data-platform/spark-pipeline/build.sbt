
ThisBuild / scalaVersion := "2.12.18"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.invoiceflow"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % "3.5.0",
  "com.rabbitmq" % "amqp-client" % "5.21.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.2",
  "net.snowflake" % "spark-snowflake_2.12" % "3.2.0-spark_3.5",
  "net.snowflake" % "snowflake-jdbc" % "4.0.2"
)