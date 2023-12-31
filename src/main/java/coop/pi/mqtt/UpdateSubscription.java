package coop.pi.mqtt;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.google.gson.Gson;
import coop.pi.data.config.ConfigProvider;
import coop.pi.data.config.IotShadowRequest;
import coop.pi.data.config.IotState;

public class UpdateSubscription extends ShadowSubscription {

    private final ConfigProvider provider;

    public UpdateSubscription(ConfigProvider provider) {
        super(ShadowTopic.UPDATE_ACCEPTED);
        this.provider = provider;
    }

    @Override
    public void onMessage(AWSIotMessage raw) {
        super.onMessage(raw);

        Gson gson = new Gson();
        IotShadowRequest request = gson.fromJson(raw.getStringPayload(), IotShadowRequest.class);
        IotState state = request.getState();

        if (state.getDesired() != null) {
            provider.setConfig(state.getDesired());
            provider.reportBackToIot();
        }
    }
}
