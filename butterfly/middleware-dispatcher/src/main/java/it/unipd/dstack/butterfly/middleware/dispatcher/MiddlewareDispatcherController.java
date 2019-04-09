package it.unipd.dstack.butterfly.middleware.dispatcher;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import it.unipd.dstack.butterfly.config.ConfigManager;
import it.unipd.dstack.butterfly.producer.avro.Event;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

public class MiddlewareDispatcherController {
    private static final Logger logger = LoggerFactory.getLogger(MiddlewareDispatcherController.class);

    private final String TOPICS_AS_COMMA_STRING;
    private final List<String> TOPICS;
    private final Consumer<String, Event> consumer;

    MiddlewareDispatcherController() {
        this.TOPICS_AS_COMMA_STRING = ConfigManager.getStringProperty("KAFKA_TOPICS");
        this.TOPICS = Arrays.asList(this.TOPICS_AS_COMMA_STRING.split(","));
        this.consumer = createConsumer(this.TOPICS);
    }

    void start() {
        consumer.subscribe(TOPICS);
        while (true) {
            try {
                ConsumerRecords<String, Event> records = consumer.poll(Duration.ofSeconds(2));
                int count = records.count();
                logger.info("Received " + count + " records");
                records.forEach(this::processRecord);
                consumer.commitSync();
            } catch (Exception e) {

            }
        }
    }

    void close() {
        consumer.close();
    }

    private void processRecord(ConsumerRecord<String, Event> record) {
        String topic = record.topic();
        Event event = record.value();
        logger.info("Read message from topic " + topic + ": " + event.toString());
        this.processEvent(event);
    }

    private void processEvent(Event event) {

    }

    /**
     * This will be moved to consumer package
     * @return
     */
    private static <K, V> Consumer<K, V> createConsumer(List<String> topics) {
        Properties props = new Properties();

        // A list of URLs to use for establishing the initial connection to the cluster.
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                ConfigManager.getStringProperty("KAFKA_BOOTSTRAP_SERVERS_CONFIG"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                ConfigManager.getStringProperty("KAFKA_GROUP_ID_CONFIG"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                ConfigManager.getStringProperty("KAFKA_AUTO_OFFSET_RESET_CONFIG", "earliest"));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                ConfigManager.getBooleanProperty("KAFKA_ENABLE_AUTO_COMMIT_CONFIG", false));
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                ConfigManager.getIntProperty("KAFKA_MAX_POLL_RECORDS_CONFIG", 10));
        props.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(SPECIFIC_AVRO_READER_CONFIG, "true");
        props.put(SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        KafkaConsumer<K, V> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(topics);

        return consumer;
    }
}
