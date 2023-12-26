package coop.pi.data.config;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.google.gson.Gson;
import coop.pi.mqtt.PublishMessage;
import coop.pi.mqtt.ShadowTopic;

public class ConfigProvider {

    private static final long PUBLISH_TIMEOUT = 3000;
    private static final Gson GSON = new Gson();

    private final AWSIotMqttClient client;
    private CoopConfig config = null;

    public ConfigProvider(AWSIotMqttClient client) {
        this.client = client;
    }

    public void setConfig(CoopConfig config) {
        this.config = config;
    }

    public CoopConfig getConfig() {
        return this.config;
    }

    public void reportBackToIot() {

        IotState state = new IotState();
        state.setReported(config);

        IotShadowRequest response = new IotShadowRequest();
        response.setState(state);

        try {
            PublishMessage report = new PublishMessage(ShadowTopic.UPDATE, GSON.toJson(response));
            this.client.publish(report, PUBLISH_TIMEOUT);
        } catch (AWSIotException e) {
            throw new RuntimeException(e);
        }
    }


}
