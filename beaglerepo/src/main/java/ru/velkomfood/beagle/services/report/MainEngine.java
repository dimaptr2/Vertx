package ru.velkomfood.beagle.services.report;

import io.vertx.core.Vertx;
import ru.velkomfood.beagle.services.report.server.RestServer;

public class MainEngine {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();
        RestServer server = new RestServer().prepare();
        vertx.deployVerticle(server);

    }

}
