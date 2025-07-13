package com.healthcare.ui;

import com.healthcare.dao.PatientDAO;
import com.healthcare.model.Patient;
import com.github.lgooddatepicker.components.DatePicker; // Import LGoodDatePicker's DatePicker
import com.github.lgooddatepicker.components.DatePickerSettings; // Import DatePickerSettings

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
    private DatePicker dobPicker; // Changed to LGoodDatePicker's DatePicker
    private JComboBox<String> genderComboBox;
    private JTextField addressField;
    private JTextField cityField;
    private JTextField stateField;
    private JTextField zipCodeField;
    private JTextField phoneField;
    private JTextField emailField;

    // For update functionality - store selected patient ID
    private int selectedPatientId = -1; // Corrected variable name for clarity

    // Search components
    private JTextField searchField;
    private JButton searchButton;

    public PatientPanel() {
        patientDAO = new PatientDAO();
        setLayout(new BorderLayout(10, 10)); // Add some spacing

        // --- Form Panel (North) ---
        JPanel topPanel = new JPanel(new BorderLayout(5, 5)); // A new panel to hold form and search
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Patient"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize form fields
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);

        // Initialize LGoodDatePicker
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("yyyy-MM-dd"); // Set the display format
        dobPicker = new DatePicker(dateSettings); // Initialize DatePicker with settings

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
        row = addFormField(formPanel, gbc, "Date of Birth:", dobPicker, row); // Use dobPicker
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
        gbc.fill = GridBagConstraints.NONE; // Reset fill for button panel
        gbc.anchor = GridBagConstraints.CENTER; // Center buttons
        formPanel.add(buttonPanel, gbc);

        topPanel.add(formPanel, BorderLayout.CENTER); // Add form to the center of topPanel

        // --- Search Panel (below form in NORTH region) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Patients"));
        searchField = new JTextField(25);
        searchButton = new JButton("Search");
        JButton resetButton = new JButton("Reset Search"); // New button to clear search and show all

        searchPanel.add(new JLabel("Search Term:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(resetButton); // Add reset button

        topPanel.add(searchPanel, BorderLayout.SOUTH); // Add search panel to the south of topPanel

        add(topPanel, BorderLayout.NORTH); // Add the combined topPanel to the main PatientPanel's NORTH


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
                updatePatient(); // Call updatePatient directly
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

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                loadPatientsIntoTable(null); // Load all if search term is empty
            }
        });

        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        // Listener for table row selection to populate form for update
        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && patientTable.getSelectedRow() != -1) {
                populateFormFromTable();
            }
        });

        // Populate table on load
        loadPatientsIntoTable(null); // Initial load of all patients
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
            LocalDate dob = dobPicker.getDate(); // Get LocalDate directly from DatePicker
            String gender = (String) genderComboBox.getSelectedItem();
            String address = addressField.getText().trim();
            String city = cityField.getText().trim();
            String state = stateField.getText().trim();
            String zipCode = zipCodeField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || dob == null || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First Name, Last Name, Date of Birth, and Email are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Patient newPatient = new Patient(firstName, lastName, dob, gender, address, city, state, zipCode, phone, email);
            int patientId = patientDAO.addPatient(newPatient);

            if (patientId != -1) {
                JOptionPane.showMessageDialog(this, "Patient added successfully! ID: " + patientId, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatientsIntoTable(null); // Refresh table with all patients
                clearForm(); // Clear form after successful addition
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add patient. Check logs for details.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updatePatient() {
        if (selectedPatientId == -1) { // Using selectedPatientId
            JOptionPane.showMessageDialog(this, "Please select a patient from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            LocalDate dob = dobPicker.getDate(); // Get LocalDate directly from DatePicker
            String gender = (String) genderComboBox.getSelectedItem();
            String address = addressField.getText().trim();
            String city = cityField.getText().trim();
            String state = stateField.getText().trim();
            String zipCode = zipCodeField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || dob == null || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First Name, Last Name, Date of Birth, and Email are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Patient patientToUpdate = new Patient(selectedPatientId, firstName, lastName, dob, gender, address, city, state, zipCode, phone, email, null);
            boolean updated = patientDAO.updatePatient(patientToUpdate);

            if (updated) {
                JOptionPane.showMessageDialog(this, "Patient updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatientsIntoTable(null); // Refresh table with all patients
                clearForm(); // Clear form after successful update
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update patient.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

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
                loadPatientsIntoTable(null); // Refresh table
                clearForm(); // Clear form
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete patient. Ensure no related appointments or medical records exist.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Modified loadPatientsIntoTable to accept an optional list for search results
    private void loadPatientsIntoTable(List<Patient> patientsToDisplay) {
        tableModel.setRowCount(0); // Clear existing data
        List<Patient> patients;

        if (patientsToDisplay != null) {
            patients = patientsToDisplay;
        } else {
            patients = patientDAO.getAllPatients(); // Default: load all if no specific list is provided
        }

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

    // Add this new method to handle search button click
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term.", "Search Error", JOptionPane.WARNING_MESSAGE);
            loadPatientsIntoTable(null); // Load all if search term is empty
            return;
        }

        List<Patient> searchResults = patientDAO.searchPatients(searchTerm);
        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No patients found matching the search term.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }
        loadPatientsIntoTable(searchResults); // Load search results into the table
    }

    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        dobPicker.setDate(null); // Clear DatePicker
        genderComboBox.setSelectedItem("Male"); // Reset to default
        addressField.setText("");
        cityField.setText("");
        stateField.setText("");
        zipCodeField.setText("");
        phoneField.setText("");
        emailField.setText("");
        selectedPatientId = -1; // Reset selected ID
    }

    private void populateFormFromTable() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedPatientId = (int) tableModel.getValueAt(selectedRow, 0); // Get patient ID from table

            firstNameField.setText((String) tableModel.getValueAt(selectedRow, 1));
            lastNameField.setText((String) tableModel.getValueAt(selectedRow, 2));

            // Set LocalDate directly to DatePicker
            LocalDate dob = (LocalDate) tableModel.getValueAt(selectedRow, 3);
            dobPicker.setDate(dob); // Set LocalDate directly

            genderComboBox.setSelectedItem((String) tableModel.getValueAt(selectedRow, 4));
            emailField.setText((String) tableModel.getValueAt(selectedRow, 5));
            phoneField.setText((String) tableModel.getValueAt(selectedRow, 6));

            // Retrieve full address, city, state, zip from DAO if not in table model
            Patient fullPatient = patientDAO.getPatientById(selectedPatientId);
            if (fullPatient != null) {
                addressField.setText(fullPatient.getAddress());
                cityField.setText(fullPatient.getCity());
                stateField.setText(fullPatient.getState());
                zipCodeField.setText(fullPatient.getZipCode());
            }
        }
    }
}
