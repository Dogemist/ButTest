package it.unipd.dstack.butterfly.producer.gitlab;

import it.unipd.dstack.butterfly.config.ConfigManager;
import it.unipd.dstack.butterfly.config.KafkaPropertiesFactory;
import it.unipd.dstack.butterfly.producer.WebhookHandler.WebhookHandler;
import it.unipd.dstack.butterfly.producer.avro.Event;
import it.unipd.dstack.butterfly.producer.gitlab.webhookManager.GitlabWebhookListener;
import it.unipd.dstack.butterfly.producer.gitlab.webhookManager.GitlabWebhookManager;
import it.unipd.dstack.butterfly.producer.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class GitlabProducerController {
    private static final Logger logger = LoggerFactory.getLogger(GitlabProducerController.class);

    private final String SERVICE_NAME;
    private final String KAFKA_TOPIC;
    private final int SERVER_PORT;
    private final String SECRET_TOKEN;
    private GitlabWebhookListener gitlabWebhookListener;
    private GitlabWebhookManager gitlabWebhookManager;
    private Producer<String, Event> producer;
    private CountDownLatch latch = new CountDownLatch(1);

    public GitlabProducerController() {
        this.SERVICE_NAME = ConfigManager.getStringProperty("SERVICE_NAME", "GitlabProducer");
        this.KAFKA_TOPIC = ConfigManager.getStringProperty("KAFKA_TOPIC");
        this.SERVER_PORT = ConfigManager.getIntProperty("SERVER_PORT");
        this.SECRET_TOKEN = ConfigManager.getStringProperty("SECRET_TOKEN");
    }

    public void start() {
        logger.info(SERVICE_NAME, "Service started");
        this.producer = new Producer<>();
        this.gitlabWebhookListener = new GitlabWebhookListenerImpl();
        this.gitlabWebhookManager = new GitlabWebhookManager(this.SECRET_TOKEN, this.gitlabWebhookListener);

        WebhookHandler webhookHandler = new WebhookHandler.Builder()
                .setRoute("/webhooks/gitlab")
                .setMethod(WebhookHandler.HTTP_METHOD.POST)
                .setWebhookConsumer(gitlabWebhookManager::handleEvent)
                .setExceptionConsumer(e -> logger.error(SERVICE_NAME, "Exception", e))
                .create();

        webhookHandler.listen(SERVER_PORT);
        logger.info(SERVICE_NAME, "Listening on port: ", SERVER_PORT);

        try {
            logger.info("Awaiting on latch");
            this.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.close();
        }
    }

    public void close() {
        logger.info("CTRL+C pressed, GitlabProducerController CLOSE()");

        // if the current count equals 0, nothing happens
        this.latch.countDown();

        // removes listeners from gitlabWebhookManager
        this.gitlabWebhookManager.close();

        // terminates the production process
        this.producer.close();
    }

    private class GitlabWebhookListenerImpl implements GitlabWebhookListener {
        private void sendEvent(Event event) {
            logger.info(SERVICE_NAME + " Received " +  event.getEntityType() + " event: " + event.toString());
            ProducerRecord<String, Event> record = new ProducerRecord<>(KAFKA_TOPIC, event);
            GitlabProducerController.this.producer.send(record);
        }

        /**
         * This method is called when a WebHook merge request event has been received
         *
         * @param event the EventObject instance containing info on the merge request
         */
        @Override
        public void handleMergeRequestEvent(Event event) {
            this.sendEvent(event);
        }

        /**
         * This method is called when a WebHook push event has been received.
         *
         * @param event the PushEvent instance
         */
        @Override
        public void handlePushEvent(Event event) {
            this.sendEvent(event);
        }

        /**
         * This method is called when a WebHook issue event has been received.
         *
         * @param event the Event instance containing info about the issue event
         */
        @Override
        public void handleIssueEvent(Event event) {
            this.sendEvent(event);
        }
    }
}
