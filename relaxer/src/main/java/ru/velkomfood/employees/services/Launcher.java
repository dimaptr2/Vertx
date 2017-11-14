package ru.velkomfood.employees.services;

import io.vertx.core.Vertx;
import ru.velkomfood.employees.services.verticles.RestScheduler;

public class Launcher {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();
        RestScheduler scheduler = new RestScheduler();
        vertx.deployVerticle(scheduler);

    }

}
