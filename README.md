# 🏥 Healthcare Management System

This is a **desktop application** developed in **Java Swing** for managing core operations within a healthcare facility. It provides functionalities for **patient registration**, **doctor management**, **appointment scheduling**, and **recording medical history**, all backed by a **MySQL database**.

---

## 🌟 Features

### 🧍 Patient Management
- Register new patients with detailed personal information.
- View a list of all registered patients.
- Update existing patient details.
- Delete patient records.
- **Search** patients by first name, last name, email, or phone number(Future Plan).

### 👨‍⚕️ Doctor Management
- Add new doctors with specialization and contact information.
- View, update, and delete doctor records.

### 📅 Appointment Management
- Schedule appointments linking patients to doctors.
- View, update, or delete appointments.
- Modify date, time, reason, and status.

### 📋 Medical History Management
- Record diagnoses, treatments, and notes for patients.
- Link medical records to patients (and optionally doctors).
- View, update, and delete medical records.

### 🗃️ MySQL Integration
- All data is stored persistently in a **MySQL relational database**.

---

## 🛠️ Technologies Used

- **Backend**: Java  
- **Database**: MySQL  
- **Connectivity**: JDBC (Java Database Connectivity)  
- **Frontend**: Java Swing (GUI)  
- **IDE**: IntelliJ IDEA (recommended)

---

## 🗄️ Database Setup

### 1. Install MySQL Server
Use MySQL Community Server or packages like **XAMPP/WAMP/MAMP**.

### 2. Create the Database

```sql
CREATE DATABASE healthcare_system_db;
USE healthcare_system_db;
```

### 3. Create Tables

```sql
-- Patients Table
CREATE TABLE Patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10),
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    zip_code VARCHAR(10),
    phone_number VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Doctors Table
CREATE TABLE Doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100) UNIQUE
);

-- Appointments Table
CREATE TABLE Appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    reason TEXT,
    status VARCHAR(20) DEFAULT 'Scheduled',
    FOREIGN KEY (patient_id) REFERENCES Patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES Doctors(doctor_id)
);

-- Medical History Table
CREATE TABLE Medical_History (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT,
    record_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    diagnosis TEXT,
    treatment TEXT,
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES Patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES Doctors(doctor_id)
);
```

---

## 📁 Project Structure

```
HealthcareManagementSystem/
├── src/
│   └── com/
│       └── healthcare/
│           ├── model/        // POJOs for DB entities
│           │   ├── Appointment.java
│           │   ├── Doctor.java
│           │   ├── MedicalRecord.java
│           │   └── Patient.java
│           ├── dao/          // DAO for CRUD operations
│           │   ├── AppointmentDAO.java
│           │   ├── DoctorDAO.java
│           │   ├── MedicalRecordDAO.java
│           │   └── PatientDAO.java
│           ├── ui/           // Swing UI Panels and App
│           │   ├── AppointmentPanel.java
│           │   ├── DoctorPanel.java
│           │   ├── HealthcareApp.java
│           │   ├── MedicalRecordPanel.java
│           │   └── PatientPanel.java
│           ├── util/         // Utilities (DB connection)
│           │   └── DatabaseConnection.java
│           └── test/         // Testing Classes
│               └── HealthcareAppTester.java
├── lib/
│   └── mysql-connector-j-x.x.x.jar
└── HealthcareManagementSystem.iml
```

---

## 🚀 Getting Started

### ✅ Prerequisites
- **Java JDK** 8 or higher (JDK 23 used during development)
- **MySQL Server**
- **IntelliJ IDEA** (or any Java IDE)

---

### 🧰 Steps to Run

1. **Clone or Download** the project.

2. **Open in IntelliJ IDEA.**

3. **Download MySQL JDBC Driver:**
   - Download from: [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)
   - Add `.jar` file to the `lib/` directory.

4. **Add JDBC Driver in IntelliJ:**
   - `Right-click project` → **Open Module Settings (F4)** → **Libraries** → `+` → **Java** → Select `.jar`

5. **Configure Database Credentials:**
   Edit `DatabaseConnection.java`:
   ```java
   private static final String USER = "root";
   private static final String PASS = "your_mysql_password";
   ```

6. **Build the Project:**
   - In IntelliJ: `Build > Rebuild Project`

7. **Run the App:**
   - Open `HealthcareApp.java`
   - Right-click → **Run 'HealthcareApp.main()'**

---

## 🧪 Optional: Testing

Run `HealthcareAppTester.java` to verify DAO operations and database connection:
- It performs CRUD operations and cleans up afterward.

---

## 💡 Future Enhancements

- ✅ Improved UI/UX: Validation, date pickers, advanced filters.
- 🔐 User Authentication: Role-based login (admin, doctor, receptionist).
- 📊 Reporting: Generate patient/doctor activity reports.
- ⏰ Conflict Detection: Prevent overlapping appointments.
- 🧱 MVC Pattern: Better modularity and scalability.

---
