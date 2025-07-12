package com.healthcare.dao;

import com.healthcare.model.Doctor;
import com.healthcare.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {
    /**
     * Adds a new doctor to the database.
     *
     * @param doctor The Doctor object to add.
     * @return The generated doctor ID if successful, -1 otherwise.
     */
    public int addDoctor(Doctor doctor) {
        String SQL = "INSERT INTO Doctors (first_name, last_name, specialization, phone_number, email) VALUES (?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getLastName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getPhoneNumber());
            pstmt.setString(5, doctor.getEmail());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        doctor.setDoctorId(generatedId); // Set the ID back to the doctor object
                        System.out.println("Doctor added with ID: " + generatedId);
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error adding doctor: " + ex.getMessage());
            ex.printStackTrace();
        }
        return generatedId;
    }

    /**
     * Retrieves a doctor by their ID.
     *
     * @param doctorId The ID of the doctor to retrieve.
     * @return The Doctor object if found, null otherwise.
     */
    public Doctor getDoctorById(int doctorId) {
        String SQL = "SELECT * FROM Doctors WHERE doctor_id = ?";
        Doctor doctor = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    doctor = new Doctor();
                    doctor.setDoctorId(rs.getInt("doctor_id"));
                    doctor.setFirstName(rs.getString("first_name"));
                    doctor.setLastName(rs.getString("last_name"));
                    doctor.setSpecialization(rs.getString("specialization"));
                    doctor.setPhoneNumber(rs.getString("phone_number"));
                    doctor.setEmail(rs.getString("email"));
                    System.out.println("Doctor retrieved: " + doctor.getFirstName() + " " + doctor.getLastName());
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error getting doctor by ID: " + ex.getMessage());
            ex.printStackTrace();
        }
        return doctor;
    }

    /**
     * Retrieves all doctors from the database.
     *
     * @return A list of all Doctor objects.
     */
    public List<Doctor> getAllDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String SQL = "SELECT * FROM Doctors";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorId(rs.getInt("doctor_id"));
                doctor.setFirstName(rs.getString("first_name"));
                doctor.setLastName(rs.getString("last_name"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setPhoneNumber(rs.getString("phone_number"));
                doctor.setEmail(rs.getString("email"));
                doctors.add(doctor);
            }
            System.out.println("Retrieved " + doctors.size() + " doctors.");
        } catch (SQLException ex) {
            System.err.println("Error getting all doctors: " + ex.getMessage());
            ex.printStackTrace();
        }
        return doctors;
    }

    /**
     * Updates an existing doctor record in the database.
     *
     * @param doctor The Doctor object with updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateDoctor(Doctor doctor) {
        String SQL = "UPDATE Doctors SET first_name = ?, last_name = ?, specialization = ?, " +
                "phone_number = ?, email = ? WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getLastName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getPhoneNumber());
            pstmt.setString(5, doctor.getEmail());
            pstmt.setInt(6, doctor.getDoctorId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Doctor with ID " + doctor.getDoctorId() + " updated successfully.");
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error updating doctor: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes a doctor record from the database by ID.
     *
     * @param doctorId The ID of the doctor to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteDoctor(int doctorId) {
        String SQL = "DELETE FROM Doctors WHERE doctor_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, doctorId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Doctor with ID " + doctorId + " deleted successfully.");
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error deleting doctor: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }
}
