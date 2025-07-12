package com.healthcare.ui;

import com.healthcare.dao.DoctorDAO;
import com.healthcare.model.Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DoctorPanel extends JPanel {
    private DoctorDAO doctorDAO;
    private DefaultTableModel tableModel;
    private JTable doctorTable;

    // Form components
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField specializationField;
    private JTextField phoneField;
    private JTextField emailField;

    // For update functionality - store selected doctor ID
    private int selectedDoctorId = -1;

    public DoctorPanel() {
        doctorDAO = new DoctorDAO();
        setLayout(new BorderLayout(10, 10)); // Add some spacing

        // --- Form Panel (North) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add/Update Doctor"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize form fields
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        specializationField = new JTextField(25);
        phoneField = new JTextField(15);
        emailField = new JTextField(25);

        // Helper to add label-field pairs
        int row = 0;
        row = addFormField(formPanel, gbc, "First Name:", firstNameField, row);
        row = addFormField(formPanel, gbc, "Last Name:", lastNameField, row);
        row = addFormField(formPanel, gbc, "Specialization:", specializationField, row);
        row = addFormField(formPanel, gbc, "Phone:", phoneField, row);
        row = addFormField(formPanel, gbc, "Email:", emailField, row);

        // Buttons
        JButton addButton = new JButton("Add Doctor");
        JButton updateButton = new JButton("Update Doctor");
        JButton deleteButton = new JButton("Delete Selected Doctor");
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
        String[] columnNames = {"ID", "First Name", "Last Name", "Specialization", "Email", "Phone"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };
        doctorTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(doctorTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Action Listeners ---
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addDoctor();
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateDoctor();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedDoctor();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        // Listener for table row selection to populate form for update
        doctorTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && doctorTable.getSelectedRow() != -1) {
                populateFormFromTable();
            }
        });

        // Populate table on load
        loadDoctorsIntoTable();
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

    private void addDoctor() {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String specialization = specializationField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || specialization.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First Name, Last Name, and Specialization are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Doctor newDoctor = new Doctor(firstName, lastName, specialization, phone, email);
            int doctorId = doctorDAO.addDoctor(newDoctor);

            if (doctorId != -1) {
                JOptionPane.showMessageDialog(this, "Doctor added successfully! ID: " + doctorId, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctorsIntoTable(); // Refresh table
                clearForm(); // Clear form after successful addition
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add doctor. Check logs for details.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateDoctor() {
        if (selectedDoctorId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String specialization = specializationField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || specialization.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First Name, Last Name, and Specialization are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Doctor doctorToUpdate = new Doctor(selectedDoctorId, firstName, lastName, specialization, phone, email);
            boolean updated = doctorDAO.updateDoctor(doctorToUpdate);

            if (updated) {
                JOptionPane.showMessageDialog(this, "Doctor updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctorsIntoTable(); // Refresh table
                clearForm(); // Clear form after successful update
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update doctor.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteSelectedDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int doctorIdToDelete = (int) tableModel.getValueAt(selectedRow, 0); // Assuming ID is in the first column

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete doctor ID: " + doctorIdToDelete + "? This action cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean deleted = doctorDAO.deleteDoctor(doctorIdToDelete);
            if (deleted) {
                JOptionPane.showMessageDialog(this, "Doctor deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctorsIntoTable(); // Refresh table
                clearForm(); // Clear form
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete doctor. Ensure no related appointments or medical records exist.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadDoctorsIntoTable() {
        tableModel.setRowCount(0); // Clear existing data
        List<Doctor> doctors = doctorDAO.getAllDoctors();
        for (Doctor doctor : doctors) {
            tableModel.addRow(new Object[]{
                    doctor.getDoctorId(),
                    doctor.getFirstName(),
                    doctor.getLastName(),
                    doctor.getSpecialization(),
                    doctor.getEmail(),
                    doctor.getPhoneNumber()
            });
        }
    }

    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        specializationField.setText("");
        phoneField.setText("");
        emailField.setText("");
        selectedDoctorId = -1; // Reset selected ID
    }

    private void populateFormFromTable() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow != -1) {
            selectedDoctorId = (int) tableModel.getValueAt(selectedRow, 0);
            firstNameField.setText((String) tableModel.getValueAt(selectedRow, 1));
            lastNameField.setText((String) tableModel.getValueAt(selectedRow, 2));
            specializationField.setText((String) tableModel.getValueAt(selectedRow, 3));
            emailField.setText((String) tableModel.getValueAt(selectedRow, 4)); // Email is at index 4
            phoneField.setText((String) tableModel.getValueAt(selectedRow, 5)); // Phone is at index 5
        }
    }
}
