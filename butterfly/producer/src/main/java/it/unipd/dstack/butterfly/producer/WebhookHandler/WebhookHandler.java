package it.unipd.dstack.butterfly.producer.WebhookHandler;

import spark.Request;
import spark.Response;
import spark.Spark;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Consumer;

public class WebhookHandler {
    private final String route;
    private final HTTP_METHOD method;
    private final Consumer<Exception> exceptionConsumer;
    private final Consumer<HttpServletRequest> webhookConsumer;

    private WebhookHandler(String route,
                          HTTP_METHOD method,
                          Consumer<Exception> exceptionConsumer,
                          Consumer<HttpServletRequest> webhookConsumer) {
        this.route = route;
        this.method = method;
        this.exceptionConsumer = exceptionConsumer;
        this.webhookConsumer = webhookConsumer;

        Spark.initExceptionHandler(exceptionConsumer);
    }

    public synchronized void listen(int port) {
        Spark.port(port);
        attachWebhookHandlerToMethod();
    }

    public String getRoute() {
        return route;
    }

    public HTTP_METHOD getMethod() {
        return method;
    }

    private String handleWebhook(Request request, Response response) {
        webhookConsumer.accept(request.raw());
        response.status(200);
        return "";
    }

    private void attachWebhookHandlerToMethod() {
        switch (method) {
            case GET:
                Spark.get(route, this::handleWebhook);
                break;
            case POST:
                Spark.post(route, this::handleWebhook);
                break;
        }
    }

    public enum HTTP_METHOD {
        GET,
        POST,
    }

    public static class Builder {
        private String route = "/";
        private HTTP_METHOD method = HTTP_METHOD.POST;
        private Consumer<Exception> exceptionConsumer = (e) -> {};

        private Consumer<HttpServletRequest> webhookConsumer;

        public Builder setRoute(String route) {
            this.route = route;
            return this;
        }

        public Builder setMethod(HTTP_METHOD method) {
            this.method = method;
            return this;
        }

        public Builder setExceptionConsumer(Consumer<Exception> exceptionConsumer) {
            this.exceptionConsumer = exceptionConsumer;
            return this;
        }

        public Builder setWebhookConsumer(Consumer<HttpServletRequest> webhookConsumer) {
            this.webhookConsumer = webhookConsumer;
            return this;
        }

        public WebhookHandler create() {
            if (webhookConsumer == null) {
                throw new NullPointerException("webhookConsumer must be defined");
            }

            return new WebhookHandler(route, method, exceptionConsumer, webhookConsumer);
        }
    }
}
