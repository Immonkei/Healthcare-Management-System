package com.healthcare.dao;

import com.healthcare.model.Appointment;
import com.healthcare.util.DatabaseConnection;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AppointmentDAO {
    /**
     * Adds a new appointment to the database.
     *
     * @param appointment The Appointment object to add.
     * @return The generated appointment ID if successful, -1 otherwise.
     */
    public int addAppointment(Appointment appointment) {
        String SQL = "INSERT INTO Appointments (patient_id, doctor_id, appointment_date, appointment_time, reason, status) VALUES (?, ?, ?, ?, ?, ?)";
        int generatedId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setDate(3, Date.valueOf(appointment.getAppointmentDate())); // Convert LocalDate to java.sql.Date
            pstmt.setTime(4, Time.valueOf(appointment.getAppointmentTime()));   // Convert LocalTime to java.sql.Time
            pstmt.setString(5, appointment.getReason());
            pstmt.setString(6, appointment.getStatus());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                        appointment.setAppointmentId(generatedId); // Set the ID back to the appointment object
                        System.out.println("Appointment added with ID: " + generatedId);
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error adding appointment: " + ex.getMessage());
            ex.printStackTrace();
        }
        return generatedId;
    }

    /**
     * Retrieves an appointment by its ID.
     *
     * @param appointmentId The ID of the appointment to retrieve.
     * @return The Appointment object if found, null otherwise.
     */
    public Appointment getAppointmentById(int appointmentId) {
        String SQL = "SELECT * FROM Appointments WHERE appointment_id = ?";
        Appointment appointment = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    appointment = new Appointment();
                    appointment.setAppointmentId(rs.getInt("appointment_id"));
                    appointment.setPatientId(rs.getInt("patient_id"));
                    appointment.setDoctorId(rs.getInt("doctor_id"));
                    appointment.setAppointmentDate(rs.getDate("appointment_date").toLocalDate()); // Convert java.sql.Date to LocalDate
                    appointment.setAppointmentTime(rs.getTime("appointment_time").toLocalTime()); // Convert java.sql.Time to LocalTime
                    appointment.setReason(rs.getString("reason"));
                    appointment.setStatus(rs.getString("status"));
                    System.out.println("Appointment retrieved: ID " + appointment.getAppointmentId());
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error getting appointment by ID: " + ex.getMessage());
            ex.printStackTrace();
        }
        return appointment;
    }

    /**
     * Retrieves all appointments from the database.
     *
     * @return A list of all Appointment objects.
     */
    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();
        String SQL = "SELECT * FROM Appointments";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(rs.getInt("appointment_id"));
                appointment.setPatientId(rs.getInt("patient_id"));
                appointment.setDoctorId(rs.getInt("doctor_id"));
                appointment.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
                appointment.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
                appointment.setReason(rs.getString("reason"));
                appointment.setStatus(rs.getString("status"));
                appointments.add(appointment);
            }
            System.out.println("Retrieved " + appointments.size() + " appointments.");
        } catch (SQLException ex) {
            System.err.println("Error getting all appointments: " + ex.getMessage());
            ex.printStackTrace();
        }
        return appointments;
    }

    /**
     * Updates an existing appointment record in the database.
     *
     * @param appointment The Appointment object with updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateAppointment(Appointment appointment) {
        String SQL = "UPDATE Appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, " +
                "appointment_time = ?, reason = ?, status = ? WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            pstmt.setTime(4, Time.valueOf(appointment.getAppointmentTime()));
            pstmt.setString(5, appointment.getReason());
            pstmt.setString(6, appointment.getStatus());
            pstmt.setInt(7, appointment.getAppointmentId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Appointment with ID " + appointment.getAppointmentId() + " updated successfully.");
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error updating appointment: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes an appointment record from the database by ID.
     *
     * @param appointmentId The ID of the appointment to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteAppointment(int appointmentId) {
        String SQL = "DELETE FROM Appointments WHERE appointment_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, appointmentId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Appointment with ID " + appointmentId + " deleted successfully.");
                return true;
            }
        } catch (SQLException ex) {
            System.err.println("Error deleting appointment: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

}
