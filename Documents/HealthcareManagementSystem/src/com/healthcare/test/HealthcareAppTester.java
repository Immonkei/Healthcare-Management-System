package com.healthcare.test;

import com.healthcare.dao.AppointmentDAO;
import com.healthcare.dao.DoctorDAO;
import com.healthcare.dao.PatientDAO;
import com.healthcare.dao.MedicalRecordDAO;
import com.healthcare.model.MedicalRecord;
import com.healthcare.model.Patient;
import com.healthcare.model.Doctor;
import com.healthcare.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class HealthcareAppTester {
    public static void main(String[] args) {
        System.out.println("--- Starting DAO Tests ---");

        PatientDAO patientDAO = new PatientDAO();
        DoctorDAO doctorDAO = new DoctorDAO();
        AppointmentDAO appointmentDAO = new AppointmentDAO();
        MedicalRecordDAO medicalRecordDAO = new MedicalRecordDAO();

        // --- Test Patient Operations ---
        System.out.println("\n--- Testing Patient DAO ---");

        // 1. Add a new patient
        Patient newPatient = new Patient(
                "John", "Doe", LocalDate.of(1990, 5, 15), "Male",
                "123 Main St", "Anytown", "CA", "90210", "555-1234", "john.doe@example.com"
        );
        int patientId = patientDAO.addPatient(newPatient); // This will set the ID in newPatient object
        System.out.println("New Patient added with ID: " + patientId);

        // 2. Get patient by ID
        if (patientId != -1) {
            Patient retrievedPatient = patientDAO.getPatientById(patientId);
            if (retrievedPatient != null) {
                System.out.println("Retrieved Patient: " + retrievedPatient.getFirstName() + " " + retrievedPatient.getLastName());
            }
        }

        // 3. Update patient
        if (newPatient.getPatientId() != 0) { // Check if ID was set by addPatient
            newPatient.setEmail("john.doe.updated@example.com");
            newPatient.setPhoneNumber("555-5678");
            boolean updated = patientDAO.updatePatient(newPatient);
            System.out.println("Patient updated: " + updated);
            // Verify update
            Patient updatedPatient = patientDAO.getPatientById(newPatient.getPatientId());
            if(updatedPatient != null) {
                System.out.println("Updated Patient's Email: " + updatedPatient.getEmail());
            }
        }


        // 4. Get all patients
        List<Patient> allPatients = patientDAO.getAllPatients();
        System.out.println("Total Patients in DB: " + allPatients.size());
        // You can loop through them to print details if you want:
        // for (Patient p : allPatients) { System.out.println(p.getFirstName() + " " + p.getLastName()); }


        // --- Test Doctor Operations ---
        System.out.println("\n--- Testing Doctor DAO ---");

        // 1. Add a new doctor
        Doctor newDoctor = new Doctor("Alice", "Smith", "Cardiologist", "555-9876", "alice.smith@example.com");
        int doctorId = doctorDAO.addDoctor(newDoctor);
        System.out.println("New Doctor added with ID: " + doctorId);

        // 2. Get doctor by ID
        if (doctorId != -1) {
            Doctor retrievedDoctor = doctorDAO.getDoctorById(doctorId);
            if (retrievedDoctor != null) {
                System.out.println("Retrieved Doctor: " + retrievedDoctor.getFirstName() + " " + retrievedDoctor.getSpecialization());
            }
        }

        // --- Test Appointment Operations ---
        System.out.println("\n--- Testing Appointment DAO ---");

        // 1. Add a new appointment (requires valid patient and doctor IDs)
        if (patientId != -1 && doctorId != -1) {
            Appointment newAppointment = new Appointment(
                    patientId, doctorId, LocalDate.now().plusDays(7), LocalTime.of(10, 30),
                    "Routine Checkup"
            );
            int appointmentId = appointmentDAO.addAppointment(newAppointment);
            System.out.println("New Appointment added with ID: " + appointmentId);

            // 2. Get appointment by ID
            if (appointmentId != -1) {
                Appointment retrievedAppointment = appointmentDAO.getAppointmentById(appointmentId);
                if (retrievedAppointment != null) {
                    System.out.println("Retrieved Appointment for Patient " + retrievedAppointment.getPatientId() + " on " + retrievedAppointment.getAppointmentDate());
                }
            }

            // 3. Update appointment status
            if (newAppointment.getAppointmentId() != 0) {
                newAppointment.setStatus("Completed");
                boolean updatedAppt = appointmentDAO.updateAppointment(newAppointment);
                System.out.println("Appointment status updated: " + updatedAppt);
            }
        } else {
            System.out.println("Skipping Appointment tests: Patient or Doctor ID not available.");
        }


        // --- Test Medical Record Operations ---
        System.out.println("\n--- Testing Medical Record DAO ---");

        // 1. Add a new medical record (requires a valid patient ID, doctor ID is optional)
        if (patientId != -1) {
            MedicalRecord newMedicalRecord = new MedicalRecord(
                    patientId, doctorId, // Link to the created patient and doctor
                    "Common Cold", "Rest and Fluids", "Patient advised to stay home."
            );
            int recordId = medicalRecordDAO.addMedicalRecord(newMedicalRecord);
            System.out.println("New Medical Record added with ID: " + recordId);

            // Add another record without a specific doctor
            MedicalRecord newMedicalRecord2 = new MedicalRecord(
                    patientId, null, // No specific doctor for this record
                    "Follow-up", "No new symptoms", "Patient doing well."
            );
            int recordId2 = medicalRecordDAO.addMedicalRecord(newMedicalRecord2);
            System.out.println("New Medical Record (without doctor) added with ID: " + recordId2);


            // 2. Get medical record by ID
            if (recordId != -1) {
                MedicalRecord retrievedRecord = medicalRecordDAO.getMedicalRecordById(recordId);
                if (retrievedRecord != null) {
                    System.out.println("Retrieved Medical Record Diagnosis: " + retrievedRecord.getDiagnosis());
                }
            }

            // 3. Get all medical records for a patient
            List<MedicalRecord> patientRecords = medicalRecordDAO.getMedicalRecordsByPatientId(patientId);
            System.out.println("Medical Records for Patient " + patientId + ": " + patientRecords.size());

        } else {
            System.out.println("Skipping Medical Record tests: Patient ID not available.");
        }


        // --- Clean Up (Optional: Delete records after testing) ---
        System.out.println("\n--- Cleaning Up Test Data (Optional) ---");
        if (patientId != -1) {
            // Delete in reverse order of dependencies: Appointments, Medical Records, then Patient, Doctor
            appointmentDAO.getAllAppointments().stream()
                    .filter(a -> a.getPatientId() == patientId)
                    .forEach(a -> appointmentDAO.deleteAppointment(a.getAppointmentId()));

            medicalRecordDAO.getMedicalRecordsByPatientId(patientId)
                    .forEach(mr -> medicalRecordDAO.deleteMedicalRecord(mr.getRecordId()));

            patientDAO.deletePatient(patientId);
        }
        if (doctorId != -1) {
            doctorDAO.deleteDoctor(doctorId);
        }

        System.out.println("\n--- DAO Tests Complete ---");
    }

}
