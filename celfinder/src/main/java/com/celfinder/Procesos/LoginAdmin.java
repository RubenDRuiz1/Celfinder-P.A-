package com.celfinder.Procesos;

import com.celfinder.Model.Usuario;
import com.celfinder.Model.conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginAdmin {
    // Método para registrar un nuevo administrador
    public void registrarAdmin(Usuario admin) {
        crearTablaAdmins(); // Verifica y crea la tabla si no existe
        guardarAdmin(admin);
    }

    // Método para crear la tabla de administradores si no existe
    private void crearTablaAdmins() {
        String createTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Admins' AND xtype='U') " +
                                 "CREATE TABLE Admins (Id INT PRIMARY KEY IDENTITY, " +
                                 "nombreAdmin NVARCHAR(50) NOT NULL, " +
                                 "contrasena NVARCHAR(50) NOT NULL)";

        try (Connection con = conexion.getConexion(); 
             PreparedStatement pstmt = con.prepareStatement(createTableSQL)) {
            pstmt.execute();
            System.out.println("Tabla Admins verificada o creada.");
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    // Método para guardar un administrador en la base de datos
    private void guardarAdmin(Usuario admin) {
        String sql = "INSERT INTO Admins (nombreAdmin, contrasena) VALUES (?, ?)";

        try (Connection con = conexion.getConexion(); 
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, admin.getNombreadmin());
            pstmt.setString(2, admin.getContrasenaadmin());
            pstmt.executeUpdate();
            System.out.println("Administrador registrado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al registrar el administrador: " + e.getMessage());
        }
    }

    // Método para verificar el ingreso de un administrador
    public boolean verificarAdmin(String nombreAdmin, String contrasena) {
        String sql = "SELECT * FROM Admins WHERE nombreAdmin = ? AND contrasena = ?";

        try (Connection con = conexion.getConexion(); 
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nombreAdmin);
            pstmt.setString(2, contrasena);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Devuelve true si se encuentra el administrador
        } catch (SQLException e) {
            System.out.println("Error al verificar el administrador: " + e.getMessage());
            return false;
        }
    }

    // Método para obtener todos los administradores
    public List<Usuario> obtenerAdmins() {
        List<Usuario> admins = new ArrayList<>();
        String sql = "SELECT nombreAdmin, contrasena FROM Admins";

        try (Connection con = conexion.getConexion(); 
             PreparedStatement pstmt = con.prepareStatement(sql); 
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Usuario admin = new Usuario();
                admin.setNombreadmin(rs.getString("nombreAdmin"));
                admin.setContrasenaadmin(rs.getString("contrasena"));
                admins.add(admin);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener administradores: " + e.getMessage());
        }

        return admins;
    }
}
