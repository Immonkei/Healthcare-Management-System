package com.healthcare.ui;

import com.healthcare.ui.PatientPanel;
import com.healthcare.ui.DoctorPanel;
import com.healthcare.ui.AppointmentPanel;
import com.healthcare.ui.MedicalRecordPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class HealthcareApp extends JFrame{
    private JTabbedPane mainTabbedPane; // A tabbed pane for different modules

    public HealthcareApp() {
        // Frame Setup
        setTitle("Healthcare Management System");
        setSize(1000, 700); // Increased size for better layout
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window on the screen

        // Main Layout
        setLayout(new BorderLayout());

        // Create a Menu Bar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0)); // Exit application on click
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Create Tabbed Pane for Modules
        mainTabbedPane = new JTabbedPane();

        // Add welcome/dashboard panel
        JPanel dashboardPanel = createDashboardPanel();
        mainTabbedPane.addTab("Dashboard", dashboardPanel);

        // TODO: Add actual module panels later (Patients, Doctors, Appointments, Medical History)
        // For now, let's add placeholder panels.
        mainTabbedPane.addTab("Patients", new PatientPanel()); // Add our new PatientPanel here
        mainTabbedPane.addTab("Doctors", new DoctorPanel()); // Add our new DoctorPanel here
        mainTabbedPane.addTab("Appointments", new AppointmentPanel()); // Add our new AppointmentPanel here
        mainTabbedPane.addTab("Medical History", new MedicalRecordPanel()); // Add our new MedicalRecordPanel here


        add(mainTabbedPane, BorderLayout.CENTER); // Add tabbed pane to the frame

        setVisible(true); // Make the frame visible
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255)); // Light blue background

        JLabel welcomeLabel = new JLabel("Welcome to Healthcare Management System!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 30));
        welcomeLabel.setForeground(new Color(25, 25, 112)); // Dark blue text
        panel.add(welcomeLabel, BorderLayout.CENTER);

        JLabel instructionLabel = new JLabel("Use the tabs above to navigate through different modules.", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0)); // Add some padding
        panel.add(instructionLabel, BorderLayout.SOUTH);

        return panel;
    }


    public static void main(String[] args) {
        // Ensure Swing operations are performed on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HealthcareApp();
            }
        });
    }
}
