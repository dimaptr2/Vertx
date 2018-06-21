package ru.velkomfood.beagle.services.report.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RestServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestServer.class);
    private static final int PORT = 3007;
    private JsonObject config;
    private SQLClient sqlClient;
    private static final String READ_DB = "SELECT id, cur_date, cur_time FROM ticks WHERE cur_date BETWEEN (?) AND (?) ORDER BY id";

    public RestServer prepare() {

        // Prepare the data source properties
        config = new JsonObject()
                .put("url", "XXXXX")
                .put("driver_class", "org.mariadb.jdbc.Driver")
                .put("user", "XXXXX")
                .put("password", "XXXXX")
                .put("max_pool_size", 30);

        return this;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        sqlClient = JDBCClient.createShared(vertx, config);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route("/beagleinfo/:fromDate/:toDate").handler(this::readTicks);
        server.requestHandler(router::accept)
                .listen(PORT, ar -> {
                    if (ar.succeeded()) {
                        LOGGER.info(String.format("Start Beaglebone\'s report on the port %d", PORT));
                        startFuture.complete();
                    } else {
                        startFuture.fail(ar.cause());
                    }
                });

    }

    /**
     * Read the data from the database and pack it into list of json rows.
     * (Background task (Future))
     * @return
     */
    private Future<JsonArray> readTicks(RoutingContext context) {

        Future<JsonArray> future = Future.future();

        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();
        response.setChunked(true);

        // This is the main request to the database
        sqlClient.getConnection(car -> {
            if (car.succeeded()) {
                String p1 = request.getParam("fromDate");
                String p2 = request.getParam("toDate");
                SQLConnection connection = car.result();
                JsonArray params = new JsonArray()
                        .add(p1)
                        .add(p2);
                connection.queryWithParams(READ_DB, params, fetch -> {
                    connection.close();
                    if (fetch.succeeded()) {
                        response.putHeader("Content-Type", "application/json");
                        List<JsonObject> answer = fetch.result().getRows();
                        if (answer.isEmpty()) {
                            answer.add(new JsonObject().put("Empty", "empty"));
                        }
                        response.write(String.valueOf(answer));
                        response.end();
                    } else {
                        context.fail(fetch.cause());
                    }
                });
            } else {
                context.fail(car.cause());
            }
        });

        return future;
    }

}
