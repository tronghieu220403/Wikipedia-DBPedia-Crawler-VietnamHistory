
package oshi.demo.gui;

import java.awt.BorderLayout;
/*
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
*/
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Instant;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.Display;
import oshi.software.os.OperatingSystem;
import oshi.util.EdidUtil;
import oshi.util.FormatUtil;

import javax.swing.*;

/**
 * Displays text in panes covering mostly-static information. Uptime is refreshed every second.
 */
public class OsHwTextPanel extends OshiJPanel { // NOSONAR squid:S110

    private static final long serialVersionUID = 1L;

    private static final String OPERATING_SYSTEM = "Operating System";
    private static final String HARDWARE_INFORMATION = "Hardware Information";
    private static final String PROCESSOR = "Processor";
    private static final String DISPLAYS = "Displays";
    private String osPrefix;

    public OsHwTextPanel(SystemInfo si) {
        super();
        init(si);
    }

    private void init(SystemInfo si) {
        osPrefix = getOsPrefix(si);

        // assign position and constraint for all label to grid

        GridBagConstraints osLabel = new GridBagConstraints();
        GridBagConstraints osConstraints = new GridBagConstraints();
        osConstraints.gridy = 1;
        osConstraints.gridx = 0;
        osConstraints.fill = GridBagConstraints.BOTH;
        // set the spacing between components and the edges of the container that holds them in a GridBagLayout
        osConstraints.insets = new Insets(0, 0, 15, 15); // T,L,B,R

        GridBagConstraints procLabel = (GridBagConstraints) osLabel.clone();
        procLabel.gridy = 2;
        GridBagConstraints procConstraints = (GridBagConstraints) osConstraints.clone();
        procConstraints.gridy = 3;

        GridBagConstraints displayLabel = (GridBagConstraints) procLabel.clone();
        displayLabel.gridy = 4;
        GridBagConstraints displayConstraints = (GridBagConstraints) osConstraints.clone();
        displayConstraints.gridy = 5;
        displayConstraints.insets = new Insets(0, 0, 0, 15); // T,L,B,R

        GridBagConstraints csLabel = (GridBagConstraints) osLabel.clone();
        csLabel.gridx = 1;
        GridBagConstraints csConstraints = new GridBagConstraints();
        csConstraints.gridx = 1;
        csConstraints.gridheight = 6;
        csConstraints.fill = GridBagConstraints.BOTH;

        // assign oshwPanel as main panel 
        JPanel oshwPanel = new JPanel();
        // assign oshwPanel as a GridBag
        oshwPanel.setLayout(new GridBagLayout());

        // add components to GridBag:

        // "Operating System" area
        JTextArea osArea = new JTextArea(0, 0);
        osArea.setEditable(false);
        osArea.setText(updateOsData(si));
        oshwPanel.add(new JLabel(OPERATING_SYSTEM), osLabel);
        oshwPanel.add(osArea, osConstraints);

        // "Processor" area
        JTextArea procArea = new JTextArea(0, 0);
        procArea.setEditable(false);
        procArea.setText(getProc(si));
        oshwPanel.add(new JLabel(PROCESSOR), procLabel);
        oshwPanel.add(procArea, procConstraints);

        // "Display" area
        JTextArea displayArea = new JTextArea(0, 0);
        displayArea.setEditable(false);
        displayArea.setText(getDisplay(si));
        oshwPanel.add(new JLabel(DISPLAYS), displayLabel);
        oshwPanel.add(displayArea, displayConstraints);

        // "Hardware infomation" area
        JTextArea csArea = new JTextArea(0, 0);
        csArea.setEditable(false);
        csArea.setText(getHw(si));
        oshwPanel.add(new JLabel(HARDWARE_INFORMATION), csLabel);
        oshwPanel.add(csArea, csConstraints);

        // Add component to the container.
        // Make this panel scrollable.
        JScrollPane scrollPane = new JScrollPane(oshwPanel);
        this.add(scrollPane, BorderLayout.CENTER);

        // Update up time every second
        Timer timer = new Timer(Config.REFRESH_FAST, e -> osArea.setText(updateOsData(si)));
        timer.start();
    }

    private static String getOsPrefix(SystemInfo si) {
        StringBuilder sb = new StringBuilder(OPERATING_SYSTEM);

        OperatingSystem os = si.getOperatingSystem();
        sb.append(String.valueOf(os));
        sb.append("\n\n").append("Booted: ").append(Instant.ofEpochSecond(os.getSystemBootTime())).append('\n')
                .append("Uptime: ");
        return sb.toString();
    }

    private static String getHw(SystemInfo si) {
        StringBuilder sb = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        ComputerSystem computerSystem = si.getHardware().getComputerSystem();
        try {
            sb.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(computerSystem));
        } catch (JsonProcessingException e) {
            sb.append(e.getMessage());
        }
        return sb.toString();
    }

    private static String getProc(SystemInfo si) {
        StringBuilder sb = new StringBuilder();
        CentralProcessor proc = si.getHardware().getProcessor();
        sb.append(proc.toString());

        return sb.toString();
    }

    private static String getDisplay(SystemInfo si) {
        StringBuilder sb = new StringBuilder();
        List<Display> displays = si.getHardware().getDisplays();
        if (displays.isEmpty()) {
            sb.append("No displays detected.");
        } else {
            int i = 0;
            for (Display display : displays) {
                byte[] edid = display.getEdid();
                byte[][] desc = EdidUtil.getDescriptors(edid);
                String name = "Display " + i;
                for (byte[] b : desc) {
                    if (EdidUtil.getDescriptorType(b) == 0xfc) {
                        name = EdidUtil.getDescriptorText(b);
                    }
                }
                if (i++ > 0) {
                    sb.append('\n');
                }
                sb.append(name).append(": ");
                int hSize = EdidUtil.getHcm(edid);
                int vSize = EdidUtil.getVcm(edid);
                sb.append(String.format("%d x %d cm (%.1f x %.1f in)", hSize, vSize, hSize / 2.54, vSize / 2.54));
            }
        }
        return sb.toString();
    }

    private String updateOsData(SystemInfo si) {
        return osPrefix + FormatUtil.formatElapsedSecs(si.getOperatingSystem().getSystemUptime());
    }
}
