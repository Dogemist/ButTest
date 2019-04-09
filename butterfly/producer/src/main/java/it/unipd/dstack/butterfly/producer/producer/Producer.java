package it.unipd.dstack.butterfly.producer.producer;

import it.unipd.dstack.butterfly.config.KafkaPropertiesFactory;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Producer<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private KafkaProducer<K, V> producer;
    private TestCallback callback;

    public Producer() {
        this.producer = kafkaProducerFactory(KafkaPropertiesFactory.defaultKafkaPropertiesFactory());
        this.callback = new TestCallback();
    }

    public Producer(Properties properties) {
        this.producer = kafkaProducerFactory(properties);
    }

    private static <K, V> KafkaProducer<K, V> kafkaProducerFactory(Properties properties) {
        return new KafkaProducer<>(properties);
    }

    public void send(ProducerRecord<K, V> record) {
        try {
            producer.send(record, this.callback);
            logger.info("Successfully sent out message to topic " + record.topic());
        } catch (SerializationException e) {
            logger.error("SerializationException on send, " + e.getMessage() + ", " + e.getCause());
            throw e;
        }
    }

    /**
     * Closes the producer
     */
    public void close() {
        logger.info("Closing Kafka producer connection...");
        producer.flush();
        producer.close();
    }

    private static class TestCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null) {
                logger.error("Error while producing message to topic :" + recordMetadata);
                e.printStackTrace();
            } else {
                String message = String.format("sent message to topic:%s partition:%s  offset:%s", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
                logger.info(message);
            }
        }
    }
}
