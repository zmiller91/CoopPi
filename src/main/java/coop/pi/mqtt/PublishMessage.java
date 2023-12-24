package coop.pi.mqtt;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

public class PublishMessage extends AWSIotMessage {
    public PublishMessage(ShadowTopic topic, String payload) {
        super(topic.topic(), AWSIotQos.QOS1, payload);
    }

    @Override
    public void onSuccess() {
        System.out.println("Successfully sent message");
        System.out.println(new String(getPayload()));
    }

    @Override
    public void onFailure() {
        System.out.println("Failed to send message");
    }

    @Override
    public void onTimeout() {
        System.out.println("Timed out sending message");
    }
}