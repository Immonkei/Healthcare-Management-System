package com.healthcare.ui;

import com.healthcare.dao.AppointmentDAO;
import com.healthcare.dao.DoctorDAO;
import com.healthcare.dao.PatientDAO;
import com.healthcare.model.Appointment;
import com.healthcare.model.Doctor;
import com.healthcare.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AppointmentPanel extends JPanel {
    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;

    private DefaultTableModel tableModel;
    private JTable appointmentTable;

    // Form components
    private JComboBox<String> patientComboBox;
    private JComboBox<String> doctorComboBox;
    private JTextField appointmentDateField; // YYYY-MM-DD
    private JTextField appointmentTimeField; // HH:MM (24-hour format)
    private JTextArea reasonArea;
    private JComboBox<String> statusComboBox;

    // Maps to quickly get ID from selected name in ComboBox
    private Map<String, Integer> patientMap;
    private Map<String, Integer> doctorMap;

    // For update functionality - store selected appointment ID
    private int selectedAppointmentId = -1;

    public AppointmentPanel() {
        appointmentDAO = new AppointmentDAO();
        patientDAO = new PatientDAO(); // Initialize PatientDAO
        doctorDAO = new DoctorDAO();   // Initialize DoctorDAO

        setLayout(new BorderLayout(10, 10));

        // --- Form Panel (North) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Schedule/Update Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize form fields
        patientComboBox = new JComboBox<>();
        doctorComboBox = new JComboBox<>();
        appointmentDateField = new JTextField(10);
        appointmentTimeField = new JTextField(8);
        reasonArea = new JTextArea(3, 20);
        JScrollPane reasonScrollPane = new JScrollPane(reasonArea);
        statusComboBox = new JComboBox<>(new String[]{"Scheduled", "Completed", "Cancelled"});

        // Populate Patient and Doctor ComboBoxes
        populateComboBoxes();

        // Helper to add label-field pairs
        int row = 0;
        row = addFormField(formPanel, gbc, "Patient:", patientComboBox, row);
        row = addFormField(formPanel, gbc, "Doctor:", doctorComboBox, row);
        row = addFormField(formPanel, gbc, "Date (YYYY-MM-DD):", appointmentDateField, row);
        row = addFormField(formPanel, gbc, "Time (HH:MM):", appointmentTimeField, row);
        row = addFormField(formPanel, gbc, "Reason:", reasonScrollPane, row); // Use scroll pane for JTextArea
        row = addFormField(formPanel, gbc, "Status:", statusComboBox, row);

        // Buttons
        JButton addButton = new JButton("Schedule Appointment");
        JButton updateButton = new JButton("Update Appointment");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // --- Table Panel (Center) ---
        String[] columnNames = {"ID", "Patient", "Doctor", "Date", "Time", "Reason", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        addButton.addActionListener(e -> addAppointment());
        updateButton.addActionListener(e -> updateAppointment());
        deleteButton.addActionListener(e -> deleteSelectedAppointment());
        clearButton.addActionListener(e -> clearForm());

        // Listener for table row selection to populate form for update
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appointmentTable.getSelectedRow() != -1) {
                populateFormFromTable();
            }
        });

        // Populate table on load
        loadAppointmentsIntoTable();
    }

    private int addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
        row++;
        return row;
    }

    private void populateComboBoxes() {
        patientComboBox.removeAllItems();
        doctorComboBox.removeAllItems();
        patientMap = new HashMap<>();
        doctorMap = new HashMap<>();

        List<Patient> patients = patientDAO.getAllPatients();
        if (patients.isEmpty()) {
            patientComboBox.addItem("No Patients Available");
            patientComboBox.setEnabled(false);
        } else {
            patientComboBox.setEnabled(true);
            for (Patient p : patients) {
                String fullName = p.getFirstName() + " " + p.getLastName() + " (ID: " + p.getPatientId() + ")";
                patientComboBox.addItem(fullName);
                patientMap.put(fullName, p.getPatientId());
            }
        }

        List<Doctor> doctors = doctorDAO.getAllDoctors();
        if (doctors.isEmpty()) {
            doctorComboBox.addItem("No Doctors Available");
            doctorComboBox.setEnabled(false);
        } else {
            doctorComboBox.setEnabled(true);
            for (Doctor d : doctors) {
                String fullName = d.getFirstName() + " " + d.getLastName() + " (" + d.getSpecialization() + ")";
                doctorComboBox.addItem(fullName);
                doctorMap.put(fullName, d.getDoctorId());
            }
        }

        if (patients.isEmpty() || doctors.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one Patient and one Doctor before scheduling appointments.", "Missing Data", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addAppointment() {
        try {
            if (patientComboBox.getSelectedItem() == null || doctorComboBox.getSelectedItem() == null ||
                    patientComboBox.getSelectedItem().toString().contains("No Patients") ||
                    doctorComboBox.getSelectedItem().toString().contains("No Doctors")) {
                JOptionPane.showMessageDialog(this, "Please ensure at least one patient and one doctor exist.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int patientId = patientMap.get(patientComboBox.getSelectedItem().toString());
            int doctorId = doctorMap.get(doctorComboBox.getSelectedItem().toString());
            LocalDate apptDate = LocalDate.parse(appointmentDateField.getText().trim());
            LocalTime apptTime = LocalTime.parse(appointmentTimeField.getText().trim());
            String reason = reasonArea.getText().trim();
            String status = (String) statusComboBox.getSelectedItem();

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Reason for appointment is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Appointment newAppointment = new Appointment(patientId, doctorId, apptDate, apptTime, reason);
            newAppointment.setStatus(status); // Set initial status, though DAO defaults to "Scheduled"
            int appointmentId = appointmentDAO.addAppointment(newAppointment);

            if (appointmentId != -1) {
                JOptionPane.showMessageDialog(this, "Appointment scheduled successfully! ID: " + appointmentId, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointmentsIntoTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to schedule appointment. Check logs for details.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date (YYYY-MM-DD) or Time (HH:MM) format.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateAppointment() {
        if (selectedAppointmentId == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (patientComboBox.getSelectedItem() == null || doctorComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "Patient or Doctor selection is empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int patientId = patientMap.get(patientComboBox.getSelectedItem().toString());
            int doctorId = doctorMap.get(doctorComboBox.getSelectedItem().toString());
            LocalDate apptDate = LocalDate.parse(appointmentDateField.getText().trim());
            LocalTime apptTime = LocalTime.parse(appointmentTimeField.getText().trim());
            String reason = reasonArea.getText().trim();
            String status = (String) statusComboBox.getSelectedItem();

            if (reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Reason for appointment is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Appointment appointmentToUpdate = new Appointment(selectedAppointmentId, patientId, doctorId, apptDate, apptTime, reason, status);
            boolean updated = appointmentDAO.updateAppointment(appointmentToUpdate);

            if (updated) {
                JOptionPane.showMessageDialog(this, "Appointment updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointmentsIntoTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update appointment.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date (YYYY-MM-DD) or Time (HH:MM) format.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int appointmentIdToDelete = (int) tableModel.getValueAt(selectedRow, 0); // ID is in the first column

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete appointment ID: " + appointmentIdToDelete + "? This action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = appointmentDAO.deleteAppointment(appointmentIdToDelete);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Appointment deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointmentsIntoTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete appointment.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadAppointmentsIntoTable() {
        tableModel.setRowCount(0); // Clear existing data
        List<Appointment> appointments = appointmentDAO.getAllAppointments();

        for (Appointment appt : appointments) {
            // To display patient and doctor names instead of just IDs in the table
            String patientName = "N/A";
            Patient patient = patientDAO.getPatientById(appt.getPatientId());
            if (patient != null) {
                patientName = patient.getFirstName() + " " + patient.getLastName();
            }

            String doctorName = "N/A";
            Doctor doctor = doctorDAO.getDoctorById(appt.getDoctorId());
            if (doctor != null) {
                doctorName = doctor.getFirstName() + " " + doctor.getLastName() + " (" + doctor.getSpecialization() + ")";
            }

            tableModel.addRow(new Object[]{
                    appt.getAppointmentId(),
                    patientName,
                    doctorName,
                    appt.getAppointmentDate(),
                    appt.getAppointmentTime(),
                    appt.getReason(),
                    appt.getStatus()
            });
        }
        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
        }
    }

    private void clearForm() {
        if (patientComboBox.getItemCount() > 0) patientComboBox.setSelectedIndex(0);
        if (doctorComboBox.getItemCount() > 0) doctorComboBox.setSelectedIndex(0);
        appointmentDateField.setText("");
        appointmentTimeField.setText("");
        reasonArea.setText("");
        statusComboBox.setSelectedItem("Scheduled");
        selectedAppointmentId = -1; // Reset selected ID
    }

    private void populateFormFromTable() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedAppointmentId = (int) tableModel.getValueAt(selectedRow, 0); // Appointment ID

            // Populate patient and doctor dropdowns by their names
            String patientFullName = (String) tableModel.getValueAt(selectedRow, 1);
            patientComboBox.setSelectedItem(patientFullName);

            String doctorFullName = (String) tableModel.getValueAt(selectedRow, 2);
            doctorComboBox.setSelectedItem(doctorFullName);


            // Date and Time
            LocalDate date = (LocalDate) tableModel.getValueAt(selectedRow, 3);
            LocalTime time = (LocalTime) tableModel.getValueAt(selectedRow, 4);
            appointmentDateField.setText(date.toString());
            appointmentTimeField.setText(time.toString());

            // Reason and Status
            reasonArea.setText((String) tableModel.getValueAt(selectedRow, 5));
            statusComboBox.setSelectedItem((String) tableModel.getValueAt(selectedRow, 6));
        }
    }
}
