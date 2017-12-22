package ru.velkomfood.mrp2.infosys;

import io.vertx.core.Vertx;
import ru.velkomfood.mrp2.infosys.core.Watcher;

// Here is the start point of Vertx application
public class Pusher {

    public static void main(String[] args) throws Exception {

        Vertx vertx = Vertx.vertx();
        Watcher watcher = new Watcher();
        vertx.deployVerticle(watcher);

    }
    
}
