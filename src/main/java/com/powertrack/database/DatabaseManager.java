package com.powertrack.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:powertrack.db";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void initialize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // Create power_sessions table
            String createSessionsTable = """
                CREATE TABLE IF NOT EXISTS power_sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    duration_minutes INTEGER NOT NULL,
                    estimated_watts REAL NOT NULL,
                    energy_wh REAL NOT NULL,
                    estimated_cost REAL NOT NULL
                )
                """;
            stmt.execute(createSessionsTable);

            // Create settings table
            String createSettingsTable = """
                CREATE TABLE IF NOT EXISTS settings (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
                """;
            stmt.execute(createSettingsTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void saveSession(LocalDateTime startTime, LocalDateTime endTime, long durationMinutes, 
                            double estimatedWatts, double energyWh, double estimatedCost) {
        String insertSql = "INSERT INTO power_sessions(start_time, end_time, duration_minutes, estimated_watts, energy_wh, estimated_cost) VALUES(?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, startTime.format(FORMATTER));
            pstmt.setString(2, endTime.format(FORMATTER));
            pstmt.setLong(3, durationMinutes);
            pstmt.setDouble(4, estimatedWatts);
            pstmt.setDouble(5, energyWh);
            pstmt.setDouble(6, estimatedCost);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getCurrentMonthEnergyWh() {
        return aggregateCurrentMonth("energy_wh");
    }

    public double getCurrentMonthCost() {
        return aggregateCurrentMonth("estimated_cost");
    }

    private double aggregateCurrentMonth(String column) {
        LocalDateTime now = LocalDateTime.now();
        String currentMonthPrefix = String.format("%04d-%02d", now.getYear(), now.getMonthValue());
        
        String sql = "SELECT SUM(" + column + ") as total FROM power_sessions WHERE start_time LIKE ?";
        
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currentMonthPrefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT value FROM settings WHERE key = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public void saveSetting(String key, String value) {
        String sql = "INSERT INTO settings(key, value) VALUES(?, ?) ON CONFLICT(key) DO UPDATE SET value = excluded.value";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
