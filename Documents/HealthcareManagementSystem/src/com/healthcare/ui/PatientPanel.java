package com.healthcare.ui;

import com.healthcare.dao.PatientDAO;
import com.healthcare.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PatientPanel extends JPanel {
    private PatientDAO patientDAO;
    private DefaultTableModel tableModel;
    private JTable patientTable;

    // Form components
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField dobField; // YYYY-MM-DD
    private JComboBox<String> genderComboBox;
    private JTextField addressField;
    private JTextField cityField;
    private JTextField stateField;
    private JTextField zipCodeField;
    private JTextField phoneField;
    private JTextField emailField;

    public PatientPanel() {
        patientDAO = new PatientDAO();
        setLayout(new BorderLayout(10, 10)); // Add some spacing

        // --- Form Panel (North) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Patient"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize form fields
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        dobField = new JTextField(10); // Format YYYY-MM-DD
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        addressField = new JTextField(30);
        cityField = new JTextField(15);
        stateField = new JTextField(10);
        zipCodeField = new JTextField(10);
        phoneField = new JTextField(15);
        emailField = new JTextField(25);

        // Helper to add label-field pairs
        int row = 0;
        row = addFormField(formPanel, gbc, "First Name:", firstNameField, row);
        row = addFormField(formPanel, gbc, "Last Name:", lastNameField, row);
        row = addFormField(formPanel, gbc, "Date of Birth (YYYY-MM-DD):", dobField, row);
        row = addFormField(formPanel, gbc, "Gender:", genderComboBox, row);
        row = addFormField(formPanel, gbc, "Address:", addressField, row);
        row = addFormField(formPanel, gbc, "City:", cityField, row);
        row = addFormField(formPanel, gbc, "State:", stateField, row);
        row = addFormField(formPanel, gbc, "Zip Code:", zipCodeField, row);
        row = addFormField(formPanel, gbc, "Phone:", phoneField, row);
        row = addFormField(formPanel, gbc, "Email:", emailField, row);

        // Buttons
        JButton addButton = new JButton("Add Patient");
        JButton updateButton = new JButton("Update Patient");
        JButton deleteButton = new JButton("Delete Selected Patient");
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
        String[] columnNames = {"ID", "First Name", "Last Name", "DOB", "Gender", "Email", "Phone"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        patientTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(patientTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPatient();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Implement update logic (requires selecting a row and populating fields)
                JOptionPane.showMessageDialog(PatientPanel.this, "Update function not yet fully implemented. Select a row first, then click update.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedPatient();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // Populate table on load
        loadPatientsIntoTable();
    }

    private int addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1.0; // Allow field to expand
        panel.add(field, gbc);
        row++;
        return row;
    }

    private void addPatient() {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            LocalDate dob = LocalDate.parse(dobField.getText().trim()); // Parse date
            String gender = (String) genderComboBox.getSelectedItem();
            String address = addressField.getText().trim();
            String city = cityField.getText().trim();
            String state = stateField.getText().trim();
            String zipCode = zipCodeField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || dobField.getText().trim().isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First Name, Last Name, Date of Birth, and Email are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Patient newPatient = new Patient(firstName, lastName, dob, gender, address, city, state, zipCode, phone, email);
            int patientId = patientDAO.addPatient(newPatient);

            if (patientId != -1) {
                JOptionPane.showMessageDialog(this, "Patient added successfully! ID: " + patientId, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatientsIntoTable(); // Refresh table
                clearForm(); // Clear form after successful addition
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add patient. Check logs for details.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date of Birth format. Please use YYYY-MM-DD.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteSelectedPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patient to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int patientId = (int) tableModel.getValueAt(selectedRow, 0); // Assuming ID is in the first column

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete patient ID: " + patientId + "? This action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = patientDAO.deletePatient(patientId);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Patient deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatientsIntoTable(); // Refresh table
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete patient. Ensure no related appointments or medical records exist.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void loadPatientsIntoTable() {
        tableModel.setRowCount(0); // Clear existing data
        List<Patient> patients = patientDAO.getAllPatients();
        for (Patient patient : patients) {
            tableModel.addRow(new Object[]{
                    patient.getPatientId(),
                    patient.getFirstName(),
                    patient.getLastName(),
                    patient.getDateOfBirth(), // LocalDate will print nicely
                    patient.getGender(),
                    patient.getEmail(),
                    patient.getPhoneNumber()
            });
        }
    }

    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        dobField.setText("");
        genderComboBox.setSelectedItem("Male"); // Reset to default
        addressField.setText("");
        cityField.setText("");
        stateField.setText("");
        zipCodeField.setText("");
        phoneField.setText("");
        emailField.setText("");
    }

    // TODO: Later, implement method to populate form fields from selected table row for updates.
    // patientTable.getSelectionModel().addListSelectionListener(e -> { ... });

}
