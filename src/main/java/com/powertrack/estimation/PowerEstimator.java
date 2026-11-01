package com.powertrack.estimation;

import com.powertrack.monitor.PowerMonitor;
import com.powertrack.settings.SettingsManager;
import java.time.Duration;
import java.time.LocalDateTime;

public class PowerEstimator {
    
    private final SettingsManager settingsManager;
    private final PowerMonitor powerMonitor;

    private int lastBatteryPercent = -1;
    private LocalDateTime lastUpdateTime = null;
    private double currentEstimateWatts = 0.0;
    
    // Fallback static estimate if battery is at 100% or metrics aren't changing.
    // Spec: "Do not use fixed user-entered wattage. If a metric is unavailable, 
    // implement a documented fallback based on observed battery state... rather than simply assuming 45W."
    // If we have full charge capacity, we can track exactly. If at 100%, we cannot measure charge rate.
    // In that case, we can use the last known charging rate, or a fallback.
    private double sessionCumulativeWh = 0.0;
    
    private boolean sessionActive = false;

    public PowerEstimator(SettingsManager settingsManager, PowerMonitor powerMonitor) {
        this.settingsManager = settingsManager;
        this.powerMonitor = powerMonitor;
    }
    
    public void resetSession() {
        lastBatteryPercent = -1;
        lastUpdateTime = null;
        sessionCumulativeWh = 0.0;
        currentEstimateWatts = 0.0;
        sessionActive = true;
    }
    
    public void updateTelemetry() {
        if (!sessionActive) return;

        int currentPercent = powerMonitor.getBatteryPercent();
        LocalDateTime now = LocalDateTime.now();
        double capacityWh = powerMonitor.getFullChargeCapacityWh();

        if (capacityWh <= 0) {
            // Cannot estimate without full charge capacity
            currentEstimateWatts = -1.0;
            return;
        }

        if (lastBatteryPercent != -1 && lastUpdateTime != null && currentPercent != 255) {
            long seconds = Duration.between(lastUpdateTime, now).getSeconds();
            if (seconds > 0) {
                int deltaPercent = currentPercent - lastBatteryPercent;
                
                if (currentPercent == 100) {
                    // At 100%, the battery is full. We cannot measure AC power going to the system,
                    // and battery charging power is effectively zero/unmeasurable via percentage changes.
                    // We return -1 to indicate it is unavailable, rather than claiming 0W for the whole laptop.
                    currentEstimateWatts = -1.0;
                } else if (deltaPercent > 0) {
                    // Charging
                    double deltaWh = (deltaPercent / 100.0) * capacityWh;
                    sessionCumulativeWh += deltaWh;
                    double hours = seconds / 3600.0;
                    currentEstimateWatts = deltaWh / hours;
                } else if (deltaPercent == 0) {
                    // Stable, not at 100%, but no percentage change yet.
                    // Keep the last valid estimate or indicate unavailable.
                    // For simplicity, we just leave currentEstimateWatts as is until a change occurs,
                    // or if it hasn't charged, we show it as unavailable.
                    if (currentEstimateWatts == 0.0) {
                        currentEstimateWatts = -1.0;
                    }
                } else {
                    // Draining or discharging on AC? Unlikely unless heavy load drains battery.
                    currentEstimateWatts = -1.0;
                }
            }
        }

        if (currentPercent != 255) {
            if (lastBatteryPercent == -1 || currentPercent != lastBatteryPercent) {
                lastBatteryPercent = currentPercent;
                lastUpdateTime = now;
            }
        }
    }

    public void endSession() {
        sessionActive = false;
    }

    public double estimateWatts() {
        return currentEstimateWatts;
    }

    public double getSessionCumulativeWh() {
        return sessionCumulativeWh;
    }

    public double calculateCost(double energyWh) {
        double energyKwh = energyWh / 1000.0;
        return energyKwh * settingsManager.getElectricityRate();
    }
}
