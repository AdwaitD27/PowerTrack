package com.powertrack.session;

import com.powertrack.database.DatabaseManager;
import com.powertrack.estimation.PowerEstimator;
import java.time.Duration;
import java.time.LocalDateTime;

public class SessionManager {
    
    private final DatabaseManager dbManager;
    private final PowerEstimator powerEstimator;

    private boolean isSessionActive = false;
    private LocalDateTime sessionStartTime;

    public SessionManager(DatabaseManager dbManager, PowerEstimator powerEstimator) {
        this.dbManager = dbManager;
        this.powerEstimator = powerEstimator;
    }

    public void initialize(boolean isAcConnected) {
        if (isAcConnected) {
            startSession();
        }
    }

    public void handleAcState(boolean isAcConnected) {
        if (isAcConnected && !isSessionActive) {
            startSession();
        } else if (!isAcConnected && isSessionActive) {
            endSession();
        }
    }

    private void startSession() {
        isSessionActive = true;
        sessionStartTime = LocalDateTime.now();
        powerEstimator.resetSession();
    }

    private void endSession() {
        if (!isSessionActive) return;
        
        LocalDateTime endTime = LocalDateTime.now();
        long durationMinutes = Duration.between(sessionStartTime, endTime).toMinutes();
        
        powerEstimator.updateTelemetry();
        
        double estimatedWatts = powerEstimator.estimateWatts();
        double energyWh = powerEstimator.getSessionCumulativeWh();
        double estimatedCost = powerEstimator.calculateCost(energyWh);

        dbManager.saveSession(sessionStartTime, endTime, durationMinutes, estimatedWatts, energyWh, estimatedCost);
        
        powerEstimator.endSession();
        isSessionActive = false;
        sessionStartTime = null;
    }

    public boolean isSessionActive() {
        return isSessionActive;
    }

    public long getCurrentSessionDurationMinutes() {
        if (!isSessionActive || sessionStartTime == null) return 0;
        return Duration.between(sessionStartTime, LocalDateTime.now()).toMinutes();
    }
    
    public void stop() {
        if (isSessionActive) {
            endSession();
        }
    }
}
