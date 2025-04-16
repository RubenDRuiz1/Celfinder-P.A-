package com.celfinder.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {
    
    public static Connection getConexion() {
        String conexionUrl = "jdbc:sqlserver://RUBEN\\MSSQLSERVER01;databaseName=Celfinder;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
        System.out.println("Intentando conectar a: " + conexionUrl);

        try {
            // Cargar la biblioteca de autenticación
            System.loadLibrary("mssql-jdbc_auth-12.10.0.x64");
            System.out.println("Biblioteca de autenticación cargada.");
            Connection con = DriverManager.getConnection(conexionUrl);
            System.out.println("Conexión establecida correctamente.");
            return con;
        } catch (SQLException ex) {
            System.err.println("Error SQL al conectar: " + ex.getMessage() + " (Error Code: " + ex.getErrorCode() + ")");
            ex.printStackTrace();
            return null;
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Error al cargar la biblioteca de autenticación: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}