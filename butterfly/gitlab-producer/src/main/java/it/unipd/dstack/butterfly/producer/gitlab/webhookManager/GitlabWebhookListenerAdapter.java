package it.unipd.dstack.butterfly.producer.gitlab.webhookManager;

import it.unipd.dstack.butterfly.producer.avro.Event;
import it.unipd.dstack.butterfly.producer.avro.Services;
import org.apache.avro.AvroRuntimeException;
import org.gitlab4j.api.webhook.IssueEvent;
import org.gitlab4j.api.webhook.MergeRequestEvent;
import org.gitlab4j.api.webhook.PushEvent;
import org.gitlab4j.api.webhook.WebHookListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that abstracts the Gitlab Client API
 */
public class GitlabWebhookListenerAdapter implements WebHookListener {
    private static final Logger logger = LoggerFactory.getLogger(GitlabWebhookListenerAdapter.class);

    private final GitlabWebhookListener listener;

    public GitlabWebhookListenerAdapter(GitlabWebhookListener listener) {
        this.listener = listener;
    }

    /**
     * This method is called when a WebHook merge request event has been received.
     * It dispatches a single Avro serialized Event that represents a Gitlab Merge Request Event.
     *
     * @param mergeRequestEvent the EventObject instance containing info on the merge request
     */
    public void onMergeRequestEvent(MergeRequestEvent mergeRequestEvent) {
        logger.info("Creating AVRO Event onMergeRequestEvent");
        Event.Builder eventBuilder = Event.newBuilder();
        eventBuilder.setEntityType("merge_request"); // TODO: transform in enum
        eventBuilder.setProjectName(mergeRequestEvent.getRepository().getName());
        eventBuilder.setService(Services.GITLAB);
        eventBuilder.setTitle(mergeRequestEvent.getObjectAttributes().getTitle());
        eventBuilder.setUserEmail(mergeRequestEvent.getUser().getEmail());
        eventBuilder.setEntityId(mergeRequestEvent.getObjectAttributes().getId().toString());
        eventBuilder.setDescription(mergeRequestEvent.getObjectAttributes().getDescription());
        Event event = eventBuilder.build();

        this.listener.handleMergeRequestEvent(event);
        logger.info("Created AVRO Event after onMergeRequestEvent");
    }

    /**
     * This method is called when a WebHook push event has been received.
     * It dispatches a number of Avro serialized Events that represent a Gitlab Push Event.
     * If multiple commits are retrieved at once, a single Event is dispatched for each commit.
     *
     * @param pushEvent the PushEvent instance
     */
    public void onPushEvent(PushEvent pushEvent) {
        logger.info("Creating AVRO Event onPushEvent");
        var commits = pushEvent.getCommits();

        commits.stream().map(commit -> {
            Event.Builder eventBuilder = Event.newBuilder();
            eventBuilder.setEntityType("commit"); // TODO: transform in enum
            eventBuilder.setProjectName(pushEvent.getRepository().getName());
            eventBuilder.setService(Services.GITLAB);
            eventBuilder.setTitle(pushEvent.getEventName());
            eventBuilder.setUserEmail(pushEvent.getUserEmail());

            eventBuilder.setEntityId(commit.getId());
            eventBuilder.setDescription(commit.getMessage());
            Event event = eventBuilder.build();
            return event;
        }).forEach(this.listener::handlePushEvent);
        logger.info("Created AVRO Event after onPushEvent");
    }

    /**
     * This method is called when a WebHook issue event has been received.
     * It dispatches a single Avro serialized Event that represents a Gitlab Issue Event.
     *
     * @param issueEvent the IssueEvent instance
     */
    public void onIssueEvent(IssueEvent issueEvent) {
        logger.info("Creating AVRO Event onIssueEvent " + issueEvent.toString());
        try {
            Event.Builder eventBuilder = Event.newBuilder();
            logger.info("AFTER newBuilder");
            eventBuilder.setEntityType("new issue"); // TODO: transform in enum
            logger.info("AFTER setEntityType");
            eventBuilder.setProjectName(issueEvent.getRepository().getName());
            logger.info("AFTER setProjectName");
            eventBuilder.setService(Services.GITLAB);
            logger.info("AFTER setService");
            eventBuilder.setTitle(issueEvent.getObjectAttributes().getTitle());
            logger.info("AFTER setTitle");
            eventBuilder.setUserEmail(issueEvent.getUser().getEmail());
            logger.info("AFTER setUserEmail");
            eventBuilder.setEntityId(Integer.toString(issueEvent.getObjectAttributes().getId()));
            logger.info("AFTER setEntityId");
            eventBuilder.setDescription(issueEvent.getObjectAttributes().getDescription());
            logger.info("AFTER setDescription");
            Event event = eventBuilder.build();
            logger.info("AFTER build()");
            this.listener.handleIssueEvent(event);
            logger.info("Created AVRO Event after onIssueEvent");
        } catch (AvroRuntimeException e) {
            logger.error("AvroRuntimeException: " + e.getMessage() + " " + e.getStackTrace());
        }
    }
}
