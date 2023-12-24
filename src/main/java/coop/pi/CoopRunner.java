package coop.pi;

import coop.pi.mqtt.ShadowSubscription;
import coop.pi.mqtt.UpdateSubscription;
import coop.pi.service.PiRunner;

import java.util.Arrays;
import java.util.List;

public class CoopRunner extends PiRunner {

    @Override
    protected void init() {

    }

    @Override
    protected void invoke() {

    }

    @Override
    protected void handleError(Throwable t) {

    }

    @Override
    protected List<ShadowSubscription> subscriptions() {
        return Arrays.asList(
                new UpdateSubscription(client())
        );
    }
}
