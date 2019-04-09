package it.unipd.dstack.butterfly.producer.gitlab.webhookManager;

import org.gitlab4j.api.GitLabApiException;

public class GitlabWebhookException extends RuntimeException {
    public GitlabWebhookException(GitLabApiException exception) {
        super(exception.getMessage());
    }
}
