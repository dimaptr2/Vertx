package ru.velkomfood.mrp2.infosys.core;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class Watcher extends AbstractVerticle {

    private final int PORT = 8189;
    private HttpServer httpServer;

    @Override
    public void start() {

        httpServer = this.vertx.createHttpServer();
        Router router0 = Router.router(this.vertx);

        // Post request from the index.html
        router0.post("/search").handler(routingContext -> {

            HttpServerRequest request = routingContext.request();
            HttpServerResponse response = routingContext.response();
            routingContext.next();
        });

        // Add the public catalog with the web pages
        router0.route().handler(StaticHandler.create());
        // Start a listening process
        httpServer.requestHandler(router0::accept).listen(PORT);

        // Have to tell us about a server is started
        String startMessage = String.format("Start server on the port %d\n", PORT);
        System.out.append(startMessage);

    } // end of starting method

    private void showMessageAboutExecution(long v1, long v2) {

        long delta = (v2 - v1) / 1000;
        String tom = "seconds";

        if (delta > 59) {
            tom = "minutes";
            delta /= 60;
            if (delta > 59) {
                tom = "hours";
                delta /= 60;
            }
        }

        String about = String.format("Time of execution is %d %s\n", delta, tom);
        System.out.append(about);

    }

}
