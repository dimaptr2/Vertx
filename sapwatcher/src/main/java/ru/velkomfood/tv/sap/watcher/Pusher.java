package ru.velkomfood.tv.sap.watcher;

import ru.velkomfood.tv.sap.watcher.starter.StartEngine;

public class Pusher {

    public static void main(String[] args) {
        StartEngine engine = StartEngine.create();
        engine.build();
    }

}
