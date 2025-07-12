package com.healthcare.ui;

import com.healthcare.dao.MedicalRecordDAO;
import com.healthcare.dao.PatientDAO;
import com.healthcare.dao.DoctorDAO;
import com.healthcare.model.MedicalRecord;
import com.healthcare.model.Patient;
import com.healthcare.model.Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime; // Used for displaying the timestamp from DB
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordPanel extends JPanel {
    private MedicalRecordDAO medicalRecordDAO;
    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;

    private DefaultTableModel tableModel;
    private JTable medicalRecordTable;

    // Form components
    private JComboBox<String> patientComboBox;
    private JComboBox<String> doctorComboBox; // Doctor is optional, can be null
    private JTextArea diagnosisArea;
    private JTextArea treatmentArea;
    private JTextArea notesArea;

    // Maps to quickly get ID from selected name in ComboBox
    private Map<String, Integer> patientMap;
    private Map<String, Integer> doctorMap; // Can store null for "No Doctor" option

    // For update functionality - store selected record ID
    private int selectedRecordId = -1;

    public MedicalRecordPanel() {
        medicalRecordDAO = new MedicalRecordDAO();
        patientDAO = new PatientDAO();
        doctorDAO = new DoctorDAO();

        setLayout(new BorderLayout(10, 10));

        // --- Form Panel (North) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Medical Record"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize form fields
        patientComboBox = new JComboBox<>();
        doctorComboBox = new JComboBox<>();
        diagnosisArea = new JTextArea(4, 25);
        JScrollPane diagnosisScrollPane = new JScrollPane(diagnosisArea);
        treatmentArea = new JTextArea(4, 25);
        JScrollPane treatmentScrollPane = new JScrollPane(treatmentArea);
        notesArea = new JTextArea(4, 25);
        JScrollPane notesScrollPane = new JScrollPane(notesArea);

        // Populate Patient and Doctor ComboBoxes
        populateComboBoxes();

        // Helper to add label-field pairs
        int row = 0;
        row = addFormField(formPanel, gbc, "Patient:", patientComboBox, row);
        row = addFormField(formPanel, gbc, "Doctor (Optional):", doctorComboBox, row);
        row = addFormField(formPanel, gbc, "Diagnosis:", diagnosisScrollPane, row);
        row = addFormField(formPanel, gbc, "Treatment:", treatmentScrollPane, row);
        row = addFormField(formPanel, gbc, "Notes:", notesScrollPane, row);

        // Buttons
        JButton addButton = new JButton("Add Record");
        JButton updateButton = new JButton("Update Record");
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
        String[] columnNames = {"ID", "Patient", "Doctor", "Date", "Diagnosis", "Treatment", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        medicalRecordTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(medicalRecordTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        addButton.addActionListener(e -> addMedicalRecord());
        updateButton.addActionListener(e -> updateMedicalRecord());
        deleteButton.addActionListener(e -> deleteSelectedMedicalRecord());
        clearButton.addActionListener(e -> clearForm());

        // Listener for table row selection to populate form for update
        medicalRecordTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && medicalRecordTable.getSelectedRow() != -1) {
                populateFormFromTable();
            }
        });

        // Populate table on load
        loadMedicalRecordsIntoTable();
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

        // Doctor is optional, so we add a "No Doctor" option
        doctorComboBox.addItem("-- Select Doctor (Optional) --");
        doctorMap.put("-- Select Doctor (Optional) --", null); // Map this to null
        List<Doctor> doctors = doctorDAO.getAllDoctors();
        for (Doctor d : doctors) {
            String fullName = d.getFirstName() + " " + d.getLastName() + " (" + d.getSpecialization() + ")";
            doctorComboBox.addItem(fullName);
            doctorMap.put(fullName, d.getDoctorId());
        }

        if (patients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one Patient to record medical history.", "Missing Data", JOptionPane.WARNING_MESSAGE);
        }
    }


    private void addMedicalRecord() {
        try {
            if (patientComboBox.getSelectedItem() == null || patientComboBox.getSelectedItem().toString().contains("No Patients")) {
                JOptionPane.showMessageDialog(this, "Please select a patient.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int patientId = patientMap.get(patientComboBox.getSelectedItem().toString());

            Integer doctorId = null; // Default to null for optional doctor
            if (doctorComboBox.getSelectedItem() != null && !doctorComboBox.getSelectedItem().toString().contains("-- Select Doctor")) {
                doctorId = doctorMap.get(doctorComboBox.getSelectedItem().toString());
            }

            String diagnosis = diagnosisArea.getText().trim();
            String treatment = treatmentArea.getText().trim();
            String notes = notesArea.getText().trim();

            if (diagnosis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Diagnosis is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MedicalRecord newRecord = new MedicalRecord(patientId, doctorId, diagnosis, treatment, notes);
            int recordId = medicalRecordDAO.addMedicalRecord(newRecord);

            if (recordId != -1) {
                JOptionPane.showMessageDialog(this, "Medical record added successfully! ID: " + recordId, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMedicalRecordsIntoTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add medical record. Check logs for details.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateMedicalRecord() {
        if (selectedRecordId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (patientComboBox.getSelectedItem() == null) { // Patient selection is required for a record
                JOptionPane.showMessageDialog(this, "Patient selection is empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int patientId = patientMap.get(patientComboBox.getSelectedItem().toString());

            Integer doctorId = null; // Default to null for optional doctor
            if (doctorComboBox.getSelectedItem() != null && !doctorComboBox.getSelectedItem().toString().contains("-- Select Doctor")) {
                doctorId = doctorMap.get(doctorComboBox.getSelectedItem().toString());
            }

            String diagnosis = diagnosisArea.getText().trim();
            String treatment = treatmentArea.getText().trim();
            String notes = notesArea.getText().trim();

            if (diagnosis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Diagnosis is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MedicalRecord recordToUpdate = new MedicalRecord(selectedRecordId, patientId, doctorId, null, diagnosis, treatment, notes); // recordDate handled by DB
            boolean updated = medicalRecordDAO.updateMedicalRecord(recordToUpdate);

            if (updated) {
                JOptionPane.showMessageDialog(this, "Medical record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMedicalRecordsIntoTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update medical record.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteSelectedMedicalRecord() {
        int selectedRow = medicalRecordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medical record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int recordIdToDelete = (int) tableModel.getValueAt(selectedRow, 0); // ID is in the first column

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete medical record ID: " + recordIdToDelete + "? This action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = medicalRecordDAO.deleteMedicalRecord(recordIdToDelete);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Medical record deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadMedicalRecordsIntoTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete medical record.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadMedicalRecordsIntoTable() {
        tableModel.setRowCount(0); // Clear existing data
        // It's generally better to load all records and filter in UI, or add a search feature.
        // For simplicity, we'll load all for now, but a filter by patient would be common.
        List<MedicalRecord> records = medicalRecordDAO.getMedicalRecordsByPatientId(0); // Get all patients' records (assuming PatientDAO.getAllPatients() and then iterating)
        // Correction: MedicalRecordDAO only has getMedicalRecordsByPatientId. Let's make a get All in DAO for this.
        // For now, let's just get all patients and get their records, or you can add a `getAllMedicalRecords()` to your MedicalRecordDAO

        // Temporary workaround if getAllMedicalRecords isn't in DAO:
        List<Patient> allPatients = patientDAO.getAllPatients();
        for(Patient p : allPatients) {
            List<MedicalRecord> patientRecords = medicalRecordDAO.getMedicalRecordsByPatientId(p.getPatientId());
            for (MedicalRecord record : patientRecords) {
                String patientName = "N/A";
                Patient patient = patientDAO.getPatientById(record.getPatientId());
                if (patient != null) {
                    patientName = patient.getFirstName() + " " + patient.getLastName();
                }

                String doctorName = "N/A";
                if (record.getDoctorId() != null) {
                    Doctor doctor = doctorDAO.getDoctorById(record.getDoctorId());
                    if (doctor != null) {
                        doctorName = doctor.getFirstName() + " " + doctor.getLastName() + " (" + doctor.getSpecialization() + ")";
                    }
                }

                tableModel.addRow(new Object[]{
                        record.getRecordId(),
                        patientName,
                        doctorName,
                        record.getRecordDate(), // LocalDateTime will print nicely
                        record.getDiagnosis(),
                        record.getTreatment(),
                        record.getNotes()
                });
            }
        }

        if (tableModel.getRowCount() == 0) {
            System.out.println("No medical records found.");
        }
    }

    private void clearForm() {
        if (patientComboBox.getItemCount() > 0) patientComboBox.setSelectedIndex(0);
        if (doctorComboBox.getItemCount() > 0) doctorComboBox.setSelectedIndex(0);
        diagnosisArea.setText("");
        treatmentArea.setText("");
        notesArea.setText("");
        selectedRecordId = -1; // Reset selected ID
    }

    private void populateFormFromTable() {
        int selectedRow = medicalRecordTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedRecordId = (int) tableModel.getValueAt(selectedRow, 0); // Record ID

            // Populate patient and doctor dropdowns by their names
            String patientFullName = (String) tableModel.getValueAt(selectedRow, 1);
            patientComboBox.setSelectedItem(patientFullName);

            String doctorFullName = (String) tableModel.getValueAt(selectedRow, 2);
            // Handle "N/A" or "No Doctor" for doctor selection
            if (doctorFullName.equals("N/A") || doctorFullName.contains("No Doctor")) {
                doctorComboBox.setSelectedItem("-- Select Doctor (Optional) --");
            } else {
                doctorComboBox.setSelectedItem(doctorFullName);
            }

            diagnosisArea.setText((String) tableModel.getValueAt(selectedRow, 4));
            treatmentArea.setText((String) tableModel.getValueAt(selectedRow, 5));
            notesArea.setText((String) tableModel.getValueAt(selectedRow, 6));
        }
    }
}
