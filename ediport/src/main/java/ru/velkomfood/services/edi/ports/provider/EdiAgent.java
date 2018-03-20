package ru.velkomfood.services.edi.ports.provider;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class EdiAgent extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(EdiAgent.class);

    private Map<String, Properties> generalSettings;

    public EdiAgent(Map<String, Properties> generalSettings) {
        this.generalSettings = generalSettings;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        startFuture.setHandler(startUpHttpClient()).completer();
    }

    private Future<Void> startUpHttpClient() {

        Future<Void> future = Future.future();

        return future;
    }
}
