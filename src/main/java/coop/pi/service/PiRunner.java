package coop.pi.service;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil;
import coop.pi.Context;
import coop.pi.mqtt.ShadowSubscription;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public abstract class PiRunner {

    private final String clientEndpoint = Context.getInstance().endpoint();
    private final String clientId = Context.getInstance().clientId();
    private final String certificateFile = Context.getInstance().certKey();
    private final String privateKeyFile = Context.getInstance().privateKey();

    private AWSIotMqttClient client;

    private boolean shouldRun = true;
    private boolean isRunning = true;

    public void start() {
        this.shouldRun = true;
        this.isRunning = true;

        init();
        connect();
        subscribe();

        while(this.shouldRun) {
            try {
                invoke();
                Thread.sleep(1000);
            } catch (Throwable t) {
                log.warn(t);
                handleError(t);
            }
        }

        this.isRunning = false;
    }

    public void stop() {
        this.shouldRun = true;
    }

    private void connect() {
        try {

            SampleUtil.KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
            this.client = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
            this.client.connect();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void subscribe() {
        try {
            for(ShadowSubscription subscription : subscriptions()) {
                client().subscribe(subscription);
            }
        } catch (AWSIotException e) {
            throw new RuntimeException(e);
        }
    }

    protected AWSIotMqttClient client() {
        if (this.client == null) {
            throw new IllegalStateException("Client is not initialized.");
        }

        return this.client;
    }

    protected abstract void init();
    protected abstract void invoke();
    protected abstract void handleError(Throwable t);
    protected abstract List<ShadowSubscription> subscriptions();


}
