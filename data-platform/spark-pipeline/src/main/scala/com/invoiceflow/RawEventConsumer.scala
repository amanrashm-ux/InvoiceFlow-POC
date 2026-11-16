
package com.invoiceflow

import com.rabbitmq.client.{ConnectionFactory, DeliverCallback}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}
import java.time.Instant

object RawEventConsumer {

  def main(args: Array[String]): Unit = {

    // RabbitMQ configuration
    val host = sys.env.getOrElse("RABBITMQ_HOST", "localhost")
    val user = sys.env.getOrElse("RABBITMQ_USER", "invoiceflow")
    val pass = sys.env.getOrElse(
      "RABBITMQ_PASSWORD",
      "change-this-rabbit-password"
    )
    val queue = sys.env.getOrElse(
      "RABBITMQ_QUEUE",
      "analytics.domain-events"
    )

    // Raw event storage location
    val path = Paths.get(
      sys.env.getOrElse("RAW_EVENT_PATH", "./data/raw-events.jsonl")
    )

    Option(path.getParent).foreach(Files.createDirectories(_))

    // Establish RabbitMQ connection
    val factory = new ConnectionFactory()
    factory.setHost(host)
    factory.setUsername(user)
    factory.setPassword(pass)

    val connection =
      factory.newConnection("invoiceflow-raw-event-consumer")

    val channel = connection.createChannel()

    // Process up to 20 unacknowledged messages
    channel.basicQos(20)

    // RabbitMQ message callback
    val callback: DeliverCallback = (_, delivery) => {

      try {

        // Read incoming event
        val body = new String(
          delivery.getBody,
          StandardCharsets.UTF_8
        )

        // Add ingestion timestamp
        val line =
          s"""{"ingestedAt":"${Instant.now()}","event":$body}""" + "\n"

        // Append event to JSONL file
        Files.write(
          path,
          line.getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE,
          StandardOpenOption.APPEND
        )

        // Acknowledge only after successful file write
        channel.basicAck(
          delivery.getEnvelope.getDeliveryTag,
          false
        )

      } catch {

        case exception: Exception =>

          System.err.println(
            s"Failed to process event: ${exception.getMessage}"
          )

          // Requeue message if processing fails
          channel.basicNack(
            delivery.getEnvelope.getDeliveryTag,
            false,
            true
          )
      }
    }

    // Start consuming messages
    channel.basicConsume(
      queue,
      false,
      callback,
      _ => ()
    )

    println(s"Consuming queue: $queue")
    println(s"Writing raw events to: $path")

    // Keep consumer alive
    Thread.currentThread().join()
  }
}