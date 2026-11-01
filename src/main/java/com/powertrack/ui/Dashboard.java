package com.powertrack.ui;

import com.powertrack.database.DatabaseManager;
import com.powertrack.estimation.PowerEstimator;
import com.powertrack.monitor.PowerMonitor;
import com.powertrack.session.SessionManager;
import com.powertrack.settings.SettingsManager;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Dashboard {

    private final PowerMonitor powerMonitor;
    private final SessionManager sessionManager;
    private final DatabaseManager databaseManager;
    private final SettingsManager settingsManager;
    private final PowerEstimator powerEstimator;
    private Timeline timeline;

    private Label statusLabel;
    private Label batteryLabel;
    private Label sessionDurationLabel;
    private Label estimatedPowerLabel;
    private Label currentEnergyLabel;
    private Label currentCostLabel;
    private Label monthlyEnergyLabel;
    private Label monthlyCostLabel;

    public Dashboard(PowerMonitor powerMonitor, SessionManager sessionManager, 
                     DatabaseManager databaseManager, SettingsManager settingsManager, 
                     PowerEstimator powerEstimator) {
        this.powerMonitor = powerMonitor;
        this.sessionManager = sessionManager;
        this.databaseManager = databaseManager;
        this.settingsManager = settingsManager;
        this.powerEstimator = powerEstimator;
    }

    public void start(Stage primaryStage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f4f4f4;");

        // UI Components
        statusLabel = new Label("UNKNOWN");
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 22));

        batteryLabel = new Label("Battery: --%");
        batteryLabel.setFont(Font.font("System", 14));

        sessionDurationLabel = new Label("Current Session: --h --m");
        sessionDurationLabel.setFont(Font.font("System", 14));

        estimatedPowerLabel = new Label("Est. Battery Charging Power: -- W");
        estimatedPowerLabel.setFont(Font.font("System", 14));
        
        currentEnergyLabel = new Label("Est. Battery Charging Energy: -- Wh");
        currentEnergyLabel.setFont(Font.font("System", 14));
        
        currentCostLabel = new Label("Cost (Battery Charging): ₹--");
        currentCostLabel.setFont(Font.font("System", 14));

        Label thisMonthLabel = new Label("THIS MONTH");
        thisMonthLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        thisMonthLabel.setPadding(new Insets(15, 0, 5, 0));

        monthlyEnergyLabel = new Label("Total Est. Battery Energy: -- kWh");
        monthlyEnergyLabel.setFont(Font.font("System", 14));

        monthlyCostLabel = new Label("Total Cost (Battery Charging): ₹--");
        monthlyCostLabel.setFont(Font.font("System", 14));

        // Settings Box
        HBox settingsBox = new HBox(10);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setPadding(new Insets(15, 0, 0, 0));
        Label rateLabel = new Label("Rate (₹/kWh):");
        TextField rateField = new TextField(String.valueOf(settingsManager.getElectricityRate()));
        rateField.setPrefWidth(60);
        Button saveRateBtn = new Button("Save");
        saveRateBtn.setOnAction(e -> {
            try {
                double newRate = Double.parseDouble(rateField.getText());
                settingsManager.setElectricityRate(newRate);
                updateMonthlyStats();
            } catch (NumberFormatException ex) {
                rateField.setText(String.valueOf(settingsManager.getElectricityRate()));
            }
        });
        settingsBox.getChildren().addAll(rateLabel, rateField, saveRateBtn);

        root.getChildren().addAll(
            statusLabel, 
            batteryLabel, 
            sessionDurationLabel, 
            estimatedPowerLabel,
            currentEnergyLabel,
            currentCostLabel,
            thisMonthLabel,
            monthlyEnergyLabel,
            monthlyCostLabel,
            settingsBox
        );

        Scene scene = new Scene(root, 380, 480);
        primaryStage.setTitle("PowerTrack");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> stop());

        // Initialize state
        powerMonitor.update();
        sessionManager.initialize(powerMonitor.isAcConnected());

        updateMonthlyStats();
        startBackgroundMonitoring();
    }

    private void startBackgroundMonitoring() {
        // Use JavaFX Timeline for safe UI updates without spawning extra threads manually
        timeline = new Timeline(new KeyFrame(Duration.seconds(10), e -> {
            powerMonitor.update();
            boolean isAcConnected = powerMonitor.isAcConnected();
            int batteryPercent = powerMonitor.getBatteryPercent();

            sessionManager.handleAcState(isAcConnected);
            powerEstimator.updateTelemetry();

            updateUi(isAcConnected, batteryPercent);
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
        // Initial UI update
        updateUi(powerMonitor.isAcConnected(), powerMonitor.getBatteryPercent());
    }

    private void updateUi(boolean isAcConnected, int batteryPercent) {
        if (isAcConnected) {
            statusLabel.setText("POWER CONNECTED");
            statusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
        } else {
            statusLabel.setText("POWER DISCONNECTED");
            statusLabel.setTextFill(javafx.scene.paint.Color.RED);
        }

        batteryLabel.setText("Battery: " + (batteryPercent == 255 ? "Unknown" : batteryPercent + "%"));

        if (sessionManager.isSessionActive()) {
            long minutes = sessionManager.getCurrentSessionDurationMinutes();
            long hours = minutes / 60;
            long mins = minutes % 60;
            sessionDurationLabel.setText(String.format("Current Session: %02dh %02dm", hours, mins));
            
            double watts = powerEstimator.estimateWatts();
            if (watts < 0) {
                estimatedPowerLabel.setText("Est. Battery Charging Power: N/A");
            } else {
                estimatedPowerLabel.setText(String.format("Est. Battery Charging Power: %.1f W", watts));
            }
            
            currentEnergyLabel.setText(String.format("Est. Battery Charging Energy: %.2f Wh", powerEstimator.getSessionCumulativeWh()));
            currentCostLabel.setText(String.format("Cost (Battery Charging): ₹%.4f", powerEstimator.calculateCost(powerEstimator.getSessionCumulativeWh())));
        } else {
            sessionDurationLabel.setText("Current Session: --h --m");
            estimatedPowerLabel.setText("Est. Battery Charging Power: -- W");
            currentEnergyLabel.setText("Est. Battery Charging Energy: -- Wh");
            currentCostLabel.setText("Cost (Battery Charging): ₹--");
        }
        updateMonthlyStats();
    }

    private void updateMonthlyStats() {
        double energyWh = databaseManager.getCurrentMonthEnergyWh();
        double cost = databaseManager.getCurrentMonthCost();

        if (sessionManager.isSessionActive()) {
            double currentSessionEnergy = powerEstimator.getSessionCumulativeWh();
            energyWh += currentSessionEnergy;
            cost += powerEstimator.calculateCost(currentSessionEnergy);
        }

        double energyKwh = energyWh / 1000.0;
        
        monthlyEnergyLabel.setText(String.format("Total Est. Battery Energy: %.2f kWh", energyKwh));
        monthlyCostLabel.setText(String.format("Total Cost (Battery Charging): ₹%.2f", cost));
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
        sessionManager.stop();
    }
}
