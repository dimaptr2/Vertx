package ru.velkomfood.tv.sap.watcher.starter;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import ru.velkomfood.tv.sap.watcher.parameters.ParametersHolder;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StartEngine {

    private static final StartEngine instance = new StartEngine();
    private Vertx vertx;
    private Queue<Verticle> componentsQueue;

    private StartEngine() {
        configure();
    }

    public static StartEngine create() {
        return instance;
    }

    public void build() {

        Logger logger = createLogger(getClass());

        for (int i = (componentsQueue.size() - 1); i >= 0; i--) {
            vertx.deployVerticle(componentsQueue.poll());
        }

        if (componentsQueue.isEmpty()) {
            logger.info("Components is deployed successfully");
        } else {
            logger.error("Not all components is deployed");
        }

    }

    // private section

    private void configure() {

        vertx = Vertx.vertx();
        componentsQueue = new ConcurrentLinkedQueue<>();
        ParametersHolder holder = createGeneralParametersHolder();

    }

    private ParametersHolder createGeneralParametersHolder() {
        return ParametersHolder.create();
    }

    private Logger createLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

}
