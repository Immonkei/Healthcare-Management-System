package com.healthcare.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {

//    DB Credeital
    private static final String DB_URL = "jdbc:mysql://localhost:3306/healthcare_system_db";
    private static final String USER = "root";
    private static final String PASS = "nith@ite";



    /**
     * Establishes and returns a database connection.
     *
     * @return Connection object if successful, null otherwise.
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Register JDBC driver (optional for newer JDBC versions, but good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Database connected successfully!");
        } catch (SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * Closes the provided database connection.
     *
     * @param conn The Connection object to close.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Database connection closed.");
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Simple test to ensure the connection works
        Connection testConn = getConnection();
        if (testConn != null) {
            System.out.println("Test connection successful!");
            closeConnection(testConn);
        } else {
            System.out.println("Test connection failed.");
        }
    }
}
