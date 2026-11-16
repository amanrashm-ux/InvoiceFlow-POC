package com.invoiceflow
import com.rabbitmq.client.{ConnectionFactory, DeliverCallback}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files,Paths,StandardOpenOption}
import java.time.Instant
object RawEventConsumer {
 def main(args:Array[String]):Unit={
  val host=sys.env.getOrElse("RABBITMQ_HOST","localhost");
  val user=sys.env.getOrElse("RABBITMQ_USER","invoiceflow");v
  al pass=sys.env.getOrElse("RABBITMQ_PASSWORD","change-this-rabbit-password")
  val queue=sys.env.getOrElse("RABBITMQ_QUEUE","analytics.domain-events");
  val path=Paths.get(sys.env.getOrElse("RAW_EVENT_PATH","./data/raw-events.jsonl"));O
  ption(path.getParent).foreach(p=>Files.createDirectories(p))
  val f=new ConnectionFactory();f.setHost(host);f.setUsername(user);
  f.setPassword(pass);val conn=f.newConnection("invoiceflow-raw-event-consumer");v
  al ch=conn.createChannel();ch.basicQos(20)
  val cb:DeliverCallback=(_,delivery)=>{val body=new String(delivery.getBody,StandardCharsets.UTF_8);
   val line=s"{\"ingestedAt\":\"${Instant.now()}\",\"event\":$body}\n";
   try{Files.write(path,line.getBytes(StandardCharsets.UTF_8),StandardOpenOption.CREATE,StandardOpenOption.APPEND);ch.basicAck(delivery.getEnvelope.getDeliveryTag,false)}catch{case e:Exception=>System.err.println(e.getMessage);ch.basicNack(delivery.getEnvelope.getDeliveryTag,false,true)}}
  ch.basicConsume(queue,false,cb,_=>());println(s"Consuming $queue; raw file $path");Thread.currentThread().join()
 }
}
