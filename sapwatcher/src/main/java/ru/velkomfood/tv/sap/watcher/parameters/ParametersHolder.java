package ru.velkomfood.tv.sap.watcher.parameters;

public class ParametersHolder {

    private static final ParametersHolder instance = new ParametersHolder();

    private ParametersHolder() {
        configure();
    }

    public static ParametersHolder create() {
        return instance;
    }

    // private section

    private void configure() {

    }

}
