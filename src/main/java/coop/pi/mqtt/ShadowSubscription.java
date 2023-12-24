package coop.pi.mqtt;

import com.amazonaws.services.iot.client.*;

public class ShadowSubscription extends AWSIotTopic {

    public ShadowSubscription(ShadowTopic topic) {
        super(topic.topic(), AWSIotQos.QOS1);
    }

    @Override
    public void onMessage(AWSIotMessage raw) {
        System.out.println(
                "\n---------- BEGIN " + getTopic() + "-----------\n" +
                raw.getStringPayload() +
                "\n----------- END " + getTopic() + "------------\n"
        );
    }
}