package coop.pi.mqtt;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.google.gson.Gson;
import coop.pi.data.config.ConfigProvider;
import coop.pi.data.config.IotShadowRequest;
import coop.pi.data.config.IotState;

public class SyncConfigSubscription extends ShadowSubscription {

    private final ConfigProvider provider;

    public SyncConfigSubscription(ConfigProvider provider) {
        super(ShadowTopic.GET_ACCEPTED);
        this.provider = provider;
    }

    @Override
    public void onMessage(AWSIotMessage raw) {

        try {
            System.out.println("Syncing config...");

            Gson gson = new Gson();
            IotShadowRequest request = gson.fromJson(raw.getStringPayload(), IotShadowRequest.class);
            IotState state = request.getState();
            provider.setConfig(state.getDesired());
            provider.reportBackToIot();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
