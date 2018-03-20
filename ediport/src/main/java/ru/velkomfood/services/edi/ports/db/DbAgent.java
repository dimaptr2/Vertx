package ru.velkomfood.services.edi.ports.db;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import ru.velkomfood.services.edi.ports.erp.ErpAgent;

import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;

public class DbAgent extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbAgent.class);

    private Properties settingsData;
    private JsonObject config;
    private SharedData sharedData;
    private SQLClient sqlClient;

    public DbAgent(Properties settingsData) {
        this.settingsData = settingsData;
        config = new JsonObject()
                .put("url", settingsData.getProperty("url"))
                .put("driver_class", settingsData.getProperty("driver_class"))
                .put("user", settingsData.getProperty("user"))
                .put("password", settingsData.getProperty("password"))
                .put("max_pool_size", Integer.valueOf(settingsData.getProperty("max_pool_size")));
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        sharedData = vertx.sharedData();
        sqlClient = JDBCClient.createShared(vertx, config, "SN1");

        CompositeFuture
                .all(Arrays.asList(saveCustomers(sqlClient), saveMaterials(sqlClient)))
                .setHandler(ar -> {
                    if (!ar.succeeded()) {
                        LOGGER.error(ar.cause());
                    }
                });

    }

    private Future<Void> saveCustomers(SQLClient sql) {

        Future<Void> future = Future.future();
        LocalMap<String, JsonObject> customersMap = sharedData.getLocalMap("customers");
        if (!customersMap.isEmpty()) {
            sql.getConnection(res -> {

            });
        }

        return future;
    }

    private Future<Void> saveMaterials(SQLClient sql) {

        Future<Void> future = Future.future();
        LocalMap<String, JsonObject> materialsMap = sharedData.getLocalMap("materials");
        if (!materialsMap.isEmpty()) {

        }

        return future;
    }


}
