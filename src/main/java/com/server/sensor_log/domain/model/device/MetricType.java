package com.server.sensor_log.domain.model.device;

public enum MetricType {

    TEMPERATURE("Temperatura", "°C"),
    HUMIDITY("Umidade", "%"),
    LIGHT_INTENSITY("Intensidade luminosa", "%"),
    CO2("Dióxido de carbono", "ppm"),
    PRESSURE("Pressão", "hPa"),
    VOLTAGE("Tensão", "V"),
    CURRENT("Corrente", "A"),
    POWER("Potência", "W"),
    ENERGY("Energia", "Wh"),
    RPM("Rotação", "RPM"),
    DISTANCE("Distância", "m"),
    SPEED("Velocidade", "m/s"),
    WEIGHT("Peso", "kg"),
    SOUND("Som", "dB");

    private final String description;
    private final String unit;

    MetricType(String description, String unit) {
        this.description = description;
        this.unit = unit;
    }

    public String description() {
        return description;
    }

    public String unit() {
        return unit;
    }
}