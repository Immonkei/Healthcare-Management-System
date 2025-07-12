package com.healthcare.dao;

import com.healthcare.model.Patient;
import com.healthcare.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    /**
     * Adds a new patient to the database.
     *
     * @param patient The Patient object to add.
     * @return The generated patient ID if successful, -1 otherwise.
     */
    public int addPatient(Patient patient) {
        String SQL = "INSERT INTO Patients (first_name, last_name, date_of_birth, gender, " +
                "address, city, state, zip_code, phone_number, email) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setDate(3, Date.valueOf(patient.getDateOfBirth())); // Convert LocalDate to java.sql.Date
            pstmt.setString(4, patient.getGender());
            pstmt.setString(5, patient.getAddress());
            pstmt.setString(6, patient.getCity());
            pstmt.setString(7, patient.getState());
            pstmt.setString(8, patient.getZipCode());
            pstmt.setString(9, patient.getPhoneNumber());
            pstmt.setString(10, patient.getEmail());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        patient.setPatientId(generatedId); // Set the ID back to the patient object
                        System.out.println("Patient added with ID: " + generatedId);
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error adding patient: " + ex.getMessage());
            ex.printStackTrace();
        }
        return generatedId;
    }

    /**
     * Retrieves a patient by their ID.
     *
     * @param patientId The ID of the patient to retrieve.
     * @return The Patient object if found, null otherwise.
     */
    public Patient getPatientById(int patientId) {
        String SQL = "SELECT * FROM Patients WHERE patient_id = ?";
        Patient patient = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    patient = new Patient();
                    patient.setPatientId(rs.getInt("patient_id"));
                    patient.setFirstName(rs.getString("first_name"));
                    patient.setLastName(rs.getString("last_name"));
                    patient.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate()); // Convert java.sql.Date to LocalDate
                    patient.setGender(rs.getString("gender"));
                    patient.setAddress(rs.getString("address"));
                    patient.setCity(rs.getString("city"));
                    patient.setState(rs.getString("state"));
                    patient.setZipCode(rs.getString("zip_code"));
                    patient.setPhoneNumber(rs.getString("phone_number"));
                    patient.setEmail(rs.getString("email"));

                    // Handle potential null for registration_date if it's not set
                    Timestamp regTimestamp = rs.getTimestamp("registration_date");
                    if (regTimestamp != null) {
                        patient.setRegistrationDate(regTimestamp.toLocalDateTime()); // Convert Timestamp to LocalDateTime
                    }

                    System.out.println("Patient retrieved: " + patient.getFirstName() + " " + patient.getLastName());
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error getting patient by ID: " + ex.getMessage());
            ex.printStackTrace();
        }
        return patient;
    }

    /**
     * Retrieves all patients from the database.
     *
     * @return A list of all Patient objects.
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String SQL = "SELECT * FROM Patients";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                Patient patient = new Patient();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setFirstName(rs.getString("first_name"));
                patient.setLastName(rs.getString("last_name"));
                patient.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                patient.setGender(rs.getString("gender"));
                patient.setAddress(rs.getString("address"));
                patient.setCity(rs.getString("city"));
                patient.setState(rs.getString("state"));
                patient.setZipCode(rs.getString("zip_code"));
                patient.setPhoneNumber(rs.getString("phone_number"));
                patient.setEmail(rs.getString("email"));

                Timestamp regTimestamp = rs.getTimestamp("registration_date");
                if (regTimestamp != null) {
                    patient.setRegistrationDate(regTimestamp.toLocalDateTime());
                }

                patients.add(patient);
            }
            System.out.println("Retrieved " + patients.size() + " patients.");
        } catch (SQLException ex) {
            System.err.println("Error getting all patients: " + ex.getMessage());
            ex.printStackTrace();
        }
        return patients;
    }

    /**
     * Updates an existing patient record in the database.
     *
     * @param patient The Patient object with updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updatePatient(Patient patient) {
        String SQL = "UPDATE Patients SET first_name = ?, last_name = ?, date_of_birth = ?, " +
                "gender = ?, address = ?, city = ?, state = ?, zip_code = ?, " +
                "phone_number = ?, email = ? WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setString(1, patient.getFirstName());
            pstmt.setString(2, patient.getLastName());
            pstmt.setDate(3, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(4, patient.getGender());
            pstmt.setString(5, patient.getAddress());
            pstmt.setString(6, patient.getCity());
            pstmt.setString(7, patient.getState());
            pstmt.setString(8, patient.getZipCode());
            pstmt.setString(9, patient.getPhoneNumber());
            pstmt.setString(10, patient.getEmail());
            pstmt.setInt(11, patient.getPatientId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Patient with ID " + patient.getPatientId() + " updated successfully.");
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error updating patient: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a patient record from the database by ID.
     *
     * @param patientId The ID of the patient to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deletePatient(int patientId) {
        String SQL = "DELETE FROM Patients WHERE patient_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, patientId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Patient with ID " + patientId + " deleted successfully.");
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error deleting patient: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }
}
