package coop.pi;

import com.amazonaws.services.iot.client.AWSIotException;
import com.google.gson.Gson;
import coop.pi.data.config.ConfigProvider;
import coop.pi.data.metric.Metric;
import coop.pi.mqtt.*;
import coop.pi.service.PiRunner;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;

public class CoopRunner extends PiRunner {

    private static final Duration PUBLISH_DURATION = Duration.minutes(1);
    private static final long MQTT_TIMEOUT = 5000;

    private ConfigProvider provider;
    private long lastPublish = 0;

    @Override
    protected void init() {

    }

    @Override
    protected void connected() {
        this.provider = new ConfigProvider(client());
    }

    @Override
    protected void subscribed() {
        PublishMessage message = new PublishMessage(ShadowTopic.GET, "{}");
        publish(message);
    }

    @Override
    protected void invoke() {

        long timeSinceLastPublish = System.currentTimeMillis() - lastPublish;
        if (timeSinceLastPublish >= PUBLISH_DURATION.toMillis()) {
            publish("component-1234", "temperature", 70);
            lastPublish = System.currentTimeMillis();
        }

    }

    @Override
    protected void handleError(Throwable t) {
        System.out.println("Error: " + t.getMessage());
    }

    @Override
    protected List<ShadowSubscription> subscriptions() {
        return Arrays.asList(
                new UpdateSubscription(provider),
                new SyncConfigSubscription(provider)
        );
    }

    private void publish(String component, String metricName, long value) {
        if (this.provider.getConfig() == null || this.provider.getConfig().getCoopId() == null) {
            return;
        }

        Metric metric = new Metric();
        metric.setDt(System.currentTimeMillis());
        metric.setCoopId(this.provider.getConfig().getCoopId());
        metric.setComponentId(component);
        metric.setMetric(metricName);
        metric.setValue(value);

        PublishMessage message = new PublishMessage(ShadowTopic.METRIC, new Gson().toJson(metric));
        publish(message);
    }

    private void publish(PublishMessage message) {
        try {
            client().publish(message, MQTT_TIMEOUT);
        } catch (AWSIotException e) {
            throw new RuntimeException(e);
        }

    }
}
