package com.powertrack.monitor;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class PowerMonitor {

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        class SYSTEM_POWER_STATUS extends Structure {
            public byte ACLineStatus;
            public byte BatteryFlag;
            public byte BatteryLifePercent;
            public byte SystemStatusFlag;
            public int BatteryLifeTime;
            public int BatteryFullLifeTime;

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList("ACLineStatus", "BatteryFlag", "BatteryLifePercent", 
                                     "SystemStatusFlag", "BatteryLifeTime", "BatteryFullLifeTime");
            }
        }

        boolean GetSystemPowerStatus(SYSTEM_POWER_STATUS result);
    }

    private final Kernel32.SYSTEM_POWER_STATUS powerStatus;
    private double fullChargeCapacityWh = -1.0;

    public PowerMonitor() {
        powerStatus = new Kernel32.SYSTEM_POWER_STATUS();
        updateCapacityFromPowercfg();
    }

    public void update() {
        Kernel32.INSTANCE.GetSystemPowerStatus(powerStatus);
    }

    public boolean isAcConnected() {
        return powerStatus.ACLineStatus == 1;
    }

    public int getBatteryPercent() {
        return powerStatus.BatteryLifePercent;
    }

    public double getFullChargeCapacityWh() {
        return fullChargeCapacityWh;
    }

    private void updateCapacityFromPowercfg() {
        try {
            ProcessBuilder pb = new ProcessBuilder("powercfg", "/batteryreport", "/xml");
            Process p = pb.start();
            p.waitFor();

            File report = new File("battery-report.xml");
            if (report.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(report))) {
                    String line;
                    String lastCapacity = null;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("FullChargeCapacity=\"")) {
                            lastCapacity = line;
                        }
                    }
                    if (lastCapacity != null) {
                        int start = lastCapacity.indexOf("\"") + 1;
                        int end = lastCapacity.lastIndexOf("\"");
                        String val = lastCapacity.substring(start, end);
                        double mWh = Double.parseDouble(val);
                        fullChargeCapacityWh = mWh / 1000.0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
