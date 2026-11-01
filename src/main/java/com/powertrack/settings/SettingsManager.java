package com.powertrack.settings;

import com.powertrack.database.DatabaseManager;

public class SettingsManager {
    private static final String RATE_KEY = "electricity_rate";
    private static final double DEFAULT_RATE = 8.0; // ₹8.0 per kWh as default

    private final DatabaseManager dbManager;
    private double currentRate;

    public SettingsManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        loadSettings();
    }

    private void loadSettings() {
        String rateStr = dbManager.getSetting(RATE_KEY, String.valueOf(DEFAULT_RATE));
        try {
            currentRate = Double.parseDouble(rateStr);
        } catch (NumberFormatException e) {
            currentRate = DEFAULT_RATE;
        }
    }

    public double getElectricityRate() {
        return currentRate;
    }

    public void setElectricityRate(double rate) {
        this.currentRate = rate;
        dbManager.saveSetting(RATE_KEY, String.valueOf(rate));
    }
}
