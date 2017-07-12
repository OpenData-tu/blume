package edu.tu_berlin.ise.opendata.blume;

import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;


/**
 * Created by aardila on 7/1/17.
 */
public class KafkaQueue implements AutoCloseable {

    private Producer<String, String> producer;
    private final String topic;

    public KafkaQueue(String topic, String server) {

        if (topic == null || topic.isEmpty()) {
            throw new IllegalArgumentException("topic");
        }
        if (server == null || server.isEmpty()) {
            throw new IllegalArgumentException("server");
        }

        this.topic = topic;

        Properties props = new Properties();
        props.put("bootstrap.servers", server);
        //props.put("metadata-broker-list", server);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(props);
    }

    public Future<RecordMetadata> publish(String message) {
        ProducerRecord<String, String> record =
                new ProducerRecord<String, String>(topic, message);

        return producer.send(record);
    }

    @Override
    public void close() throws Exception {
        producer.close();
    }
}
