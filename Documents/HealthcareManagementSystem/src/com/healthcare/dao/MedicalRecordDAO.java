package com.healthcare.dao;

import com.healthcare.model.MedicalRecord;
import com.healthcare.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAO {

    /**
     * Adds a new medical record to the database.
     *
     * @param record The MedicalRecord object to add.
     * @return The generated record ID if successful, -1 otherwise.
     */
    public int addMedicalRecord(MedicalRecord record) {
        String SQL = "INSERT INTO Medical_History (patient_id, doctor_id, diagnosis, treatment, notes) VALUES (?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, record.getPatientId());
            if (record.getDoctorId() != null) {
                pstmt.setInt(2, record.getDoctorId());
            } else {
                pstmt.setNull(2, Types.INTEGER); // Set to SQL NULL if doctorId is null
            }
            pstmt.setString(3, record.getDiagnosis());
            pstmt.setString(4, record.getTreatment());
            pstmt.setString(5, record.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        record.setRecordId(generatedId); // Set the ID back to the record object
                        System.out.println("Medical record added with ID: " + generatedId);
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error adding medical record: " + ex.getMessage());
            ex.printStackTrace();
        }
        return generatedId;
    }

    /**
     * Retrieves a medical record by its ID.
     *
     * @param recordId The ID of the medical record to retrieve.
     * @return The MedicalRecord object if found, null otherwise.
     */
    public MedicalRecord getMedicalRecordById(int recordId) {
        String SQL = "SELECT * FROM Medical_History WHERE record_id = ?";
        MedicalRecord record = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, recordId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    record = new MedicalRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setPatientId(rs.getInt("patient_id"));

                    // Handle nullable doctor_id
                    int doctorId = rs.getInt("doctor_id");
                    if (rs.wasNull()) { // Check if the last read column was SQL NULL
                        record.setDoctorId(null);
                    } else {
                        record.setDoctorId(doctorId);
                    }

                    // Handle registration_date which is a DATETIME
                    Timestamp recordTimestamp = rs.getTimestamp("record_date");
                    if (recordTimestamp != null) {
                        record.setRecordDate(recordTimestamp.toLocalDateTime());
                    } else {
                        // This case shouldn't happen if record_date has DEFAULT CURRENT_TIMESTAMP
                        record.setRecordDate(null); // Or LocalDateTime.now() as a fallback
                    }

                    record.setDiagnosis(rs.getString("diagnosis"));
                    record.setTreatment(rs.getString("treatment"));
                    record.setNotes(rs.getString("notes"));
                    System.out.println("Medical record retrieved: ID " + record.getRecordId());
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error getting medical record by ID: " + ex.getMessage());
            ex.printStackTrace();
        }
        return record;
    }

    /**
     * Retrieves all medical records for a specific patient.
     *
     * @param patientId The ID of the patient.
     * @return A list of MedicalRecord objects for the given patient.
     */
    public List<MedicalRecord> getMedicalRecordsByPatientId(int patientId) {
        List<MedicalRecord> records = new ArrayList<>();
        String SQL = "SELECT * FROM Medical_History WHERE patient_id = ? ORDER BY record_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MedicalRecord record = new MedicalRecord();
                    record.setRecordId(rs.getInt("record_id"));
                    record.setPatientId(rs.getInt("patient_id"));

                    int doctorId = rs.getInt("doctor_id");
                    if (rs.wasNull()) {
                        record.setDoctorId(null);
                    } else {
                        record.setDoctorId(doctorId);
                    }

                    Timestamp recordTimestamp = rs.getTimestamp("record_date");
                    if (recordTimestamp != null) {
                        record.setRecordDate(recordTimestamp.toLocalDateTime());
                    } else {
                        record.setRecordDate(null);
                    }

                    record.setDiagnosis(rs.getString("diagnosis"));
                    record.setTreatment(rs.getString("treatment"));
                    record.setNotes(rs.getString("notes"));
                    records.add(record);
                }
                System.out.println("Retrieved " + records.size() + " medical records for patient ID " + patientId);
            }
        } catch (SQLException ex) {
            System.err.println("Error getting medical records by patient ID: " + ex.getMessage());
            ex.printStackTrace();
        }
        return records;
    }

    /**
     * Updates an existing medical record in the database.
     *
     * @param record The MedicalRecord object with updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateMedicalRecord(MedicalRecord record) {
        String SQL = "UPDATE Medical_History SET patient_id = ?, doctor_id = ?, diagnosis = ?, " +
                "treatment = ?, notes = ? WHERE record_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, record.getPatientId());
            if (record.getDoctorId() != null) {
                pstmt.setInt(2, record.getDoctorId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, record.getDiagnosis());
            pstmt.setString(4, record.getTreatment());
            pstmt.setString(5, record.getNotes());
            pstmt.setInt(6, record.getRecordId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Medical record with ID " + record.getRecordId() + " updated successfully.");
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error updating medical record: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a medical record from the database by ID.
     *
     * @param recordId The ID of the medical record to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteMedicalRecord(int recordId) {
        String SQL = "DELETE FROM Medical_History WHERE record_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, recordId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Medical record with ID " + recordId + " deleted successfully.");
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error deleting medical record: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }
}
