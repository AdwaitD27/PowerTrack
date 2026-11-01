package com.powertrack;

import com.powertrack.database.DatabaseManager;
import com.powertrack.estimation.PowerEstimator;
import com.powertrack.monitor.PowerMonitor;
import com.powertrack.session.SessionManager;
import com.powertrack.settings.SettingsManager;
import com.powertrack.ui.Dashboard;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private Dashboard dashboard;

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.initialize();

        SettingsManager settingsManager = new SettingsManager(dbManager);
        PowerMonitor powerMonitor = new PowerMonitor();
        PowerEstimator powerEstimator = new PowerEstimator(settingsManager, powerMonitor);
        SessionManager sessionManager = new SessionManager(dbManager, powerEstimator);

        dashboard = new Dashboard(powerMonitor, sessionManager, dbManager, settingsManager, powerEstimator);
        dashboard.start(primaryStage);
    }

    @Override
    public void stop() {
        if (dashboard != null) {
            dashboard.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
