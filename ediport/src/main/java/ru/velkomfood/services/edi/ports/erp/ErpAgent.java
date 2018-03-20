package ru.velkomfood.services.edi.ports.erp;

import com.sap.conn.jco.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

import java.util.*;

public class ErpAgent extends AbstractVerticle {

    private static final Logger LOGG = LoggerFactory.getLogger(ErpAgent.class);
    private final String ZERO_GLN = "0000000000000";
    private final String UNIT_GLN = "1111111111111";

    private Properties materialRange;
    private Properties partnersProps;
    private int poolSize;
    private long maxExecuteTime;
    private JCoDestination destination;

    private EventBus eventBus;
    private SharedData sharedData;

    public ErpAgent(Properties materialRange, Properties partnersProps) {
        this.materialRange = materialRange;
        this.partnersProps = partnersProps;
        poolSize = 10;
        maxExecuteTime = 180000; // Three minutes in milliseconds
    }

    public void initDestination(String destName) throws JCoException {
        destination = JCoDestinationManager.getDestination(destName);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        LOGG.info("Start the EDI agent");
        eventBus = vertx.eventBus();
        sharedData = vertx.sharedData();

        // Asynchronous calls into
        CompositeFuture
                .all(Arrays.asList(readMasterData(), startUpRfcServer(), sendIdocs()))
                .setHandler(ar -> {
            if (!ar.succeeded()) {
                LOGG.error(ar.cause());
            }
        });

    }

    private Future<Void> readMasterData() {

        Future<Void> future = Future.future();

        vertx.setTimer(30000, id -> {
            try {
                readCustomersInfo();
                readAllMaterials();
            } catch (JCoException e) {
                e.printStackTrace();
            }
        });

        return future;
    }

    // Read the customers that will use this service
    private void readCustomersInfo() throws JCoException {

        JCoFunction kna1SingleRead = destination.getRepository().getFunction("ZZ_KNA1_SINGLE_READ");
        if (kna1SingleRead == null) {
            throw new RuntimeException("Function ZZ_KNA1_ARRAY_READ not found");
        }

        LocalMap<String, JsonObject> customersMap = sharedData.getLocalMap("customers");

        // Get all partners from the general properties map
        Set<Map.Entry<Object, Object>> pSet = partnersProps.entrySet();
        Iterator<Map.Entry<Object, Object>> it = pSet.iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            String customerId = String.valueOf(entry.getValue());
            kna1SingleRead.getImportParameterList().setValue("I_KUNNR", customerId);
            kna1SingleRead.execute(destination);
            JCoStructure kna1 = kna1SingleRead.getExportParameterList().getStructure("E_KNA1");
            JsonObject customerJson = new JsonObject();
            String f1 = ""; String f2 = ""; String f3 = "";
            for (JCoField f: kna1) {
                switch (f.getName()) {
                    case "KUNNR":
                        customerJson.put("ID", f.getString());
                        break;
                    case "NAME1":
                        customerJson.put("NAME", f.getString());
                        break;
                    case "BBBNR":
                        f1 = f.getString();
                        break;
                    case "BBSNR":
                        f2 = f.getString();
                        break;
                    case "BUBKZ":
                        f3 = f.getString();
                        break;
                }
            }
            String globalNumber = f1 + f2 + f3;
            if (!customersMap.containsKey(customerId) && (!globalNumber.equals(ZERO_GLN) || !globalNumber.equals(UNIT_GLN))) {
                customerJson.put("GLN", globalNumber);
                customersMap.put(customerId, customerJson);
            }
        }

    }

    // Read all materials for the sales
    private void readAllMaterials() throws JCoException {

        JCoFunction bapiMats = destination.getRepository().getFunction("BAPI_MATERIAL_GETLIST");
        JCoTable selection = bapiMats.getTableParameterList().getTable("MATNRSELECTION");
        selection.appendRow();
        selection.setValue("SIGN", materialRange.getProperty("SIGN"));
        selection.setValue("OPTION", materialRange.getProperty("OPTION"));
        selection.setValue("MATNR_LOW", materialRange.getProperty("LOW"));
        selection.setValue("MATNR_HIGH", materialRange.getProperty("HIGH"));

        bapiMats.execute(destination);

        JCoTable matList = bapiMats.getTableParameterList().getTable("MATNRLIST");
        if (matList.getNumRows() > 0) {
            LocalMap<Long, JsonObject> materialsMap = sharedData.getLocalMap("materials");
            do {
                long id = matList.getLong("MATERIAL");
                JsonObject matJson = new JsonObject()
                        .put("ID", id)
                        .put("DESCRIPTION", matList.getString("MATL_DESC"));
                if (!materialsMap.containsKey(id)) {
                    materialsMap.put(id, matJson);
                }
            } while (matList.nextRow());
        }

    }

    // Process the outbound IDOCs from the SAP (Java <- SAP)
    private Future<Void> startUpRfcServer() {

        Future<Void> future = Future.future();

        LOGG.info("Start the RFC server");

        vertx.setPeriodic(10000, id1 -> {
           LOGG.info("I am RFC server");
        });

        return future;
    }

    // Create and send the inbound IDOCs to the SAP system (Java -> SAP)
    private Future<Void> sendIdocs() {

        Future<Void> future = Future.future();

        LOGG.info("Start the IDOC sender");

        vertx.setPeriodic(15000, id2 -> {
            LOGG.info("I am IDOC sender");
        });

        return future;
    }

}
