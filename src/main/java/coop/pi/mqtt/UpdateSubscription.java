package coop.pi.mqtt;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.google.gson.Gson;
import coop.pi.config.IotShadowRequest;
import coop.pi.config.IotState;

public class UpdateSubscription extends ShadowSubscription {

    private static final long PUBLISH_TIMEOUT = 3000;

    private final AWSIotMqttClient client;

    public UpdateSubscription(AWSIotMqttClient client) {
        super(ShadowTopic.UPDATE_ACCEPTED);
        this.client = client;
    }

    @Override
    public void onMessage(AWSIotMessage raw) {
        super.onMessage(raw);

        Gson gson = new Gson();
        IotShadowRequest request = gson.fromJson(raw.getStringPayload(), IotShadowRequest.class);
        IotState state = request.getState();

        if (state.getDesired() != null) {

            IotState newState = new IotState();
            newState.setReported(state.getDesired());

            IotShadowRequest response = new IotShadowRequest();
            response.setVersion(request.getVersion());
            response.setState(newState);

            state.setReported(state.getDesired());
            state.setDesired(null);

            try {
                PublishMessage report = new PublishMessage(ShadowTopic.UPDATE, gson.toJson(response));
                client.publish(report, PUBLISH_TIMEOUT);
            } catch (AWSIotException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
