package ru.velkomfood.services.edi.ports;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.h2.jdbcx.JdbcDataSource;
import ru.velkomfood.services.edi.ports.db.DbAgent;
import ru.velkomfood.services.edi.ports.erp.ErpAgent;
import ru.velkomfood.services.edi.ports.provider.EdiAgent;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Pusher {

    public static void main(String[] args) throws Exception {

        Map<String, Properties> settingsCache = readSettings();
        initializeDatabase(settingsCache.get("db-config"));

        // Verticles preparation
        EdiAgent edi = new EdiAgent(settingsCache);
        DbAgent db = new DbAgent(settingsCache.get("db-config"));

        DeploymentOptions optionsErp = new DeploymentOptions().setWorker(true);
        ErpAgent erp = new ErpAgent(settingsCache.get("materials-range"), settingsCache.get("partners"));
        erp.initDestination("SN1");

        Vertx vertx = Vertx.vertx();
        // Start the main verticles
        vertx.deployVerticle(edi);
        vertx.deployVerticle(db);
        vertx.deployVerticle(erp, optionsErp);

    }

    // Read all internal properties
    private static Map<String, Properties> readSettings() throws IOException {

        Map<String, Properties> settings = new ConcurrentHashMap<>();

        Properties providerData = new Properties();
        InputStream is1 = Pusher.class.getResourceAsStream("/provider.properties");
        providerData.load(is1);
        is1.close();

        Properties materialRange = new Properties();
        InputStream is2 = Pusher.class.getResourceAsStream("/materials-range.properties");
        materialRange.load(is2);
        is2.close();

        Properties partnersData = new Properties();
        InputStream is3 = Pusher.class.getResourceAsStream("/edipartners.properties");
        partnersData.load(is3);
        is3.close();

        Properties dbProperties = new Properties();
        InputStream is4 = Pusher.class.getResourceAsStream("/dbinitialization.properties");
        dbProperties.load(is4);
        is4.close();

        settings.put("provider", providerData);
        settings.put("materials-range", materialRange);
        settings.put("partners", partnersData);
        settings.put("db-config", dbProperties);

        return settings;
    }

    private static void initializeDatabase(Properties dbProps) throws SQLException {

        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(dbProps.getProperty("url"));
        ds.setUser(dbProps.getProperty("user"));
        ds.setPassword(dbProps.getProperty("password"));

        Connection connection = ds.getConnection();
        Statement stmt = connection.createStatement();

        try {
            stmt.execute(dbProps.getProperty("CUSTOMERS_TABLE"));
            stmt.execute(dbProps.getProperty("MATERIALS_TABLE"));
        } finally {
            stmt.close();
            connection.close();
        }

    }

}
