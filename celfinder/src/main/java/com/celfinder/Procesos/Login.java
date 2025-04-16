package com.celfinder.Procesos;

import com.celfinder.Model.Usuario;
import com.celfinder.Model.conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login {

    public void registrarUsuario(Usuario usuario) {
        crearTablaUsuarios(); // Verifica y crea la tabla si no existe
        if (nombreUsuarioExiste(usuario.getNombreUsuario())) {
            System.out.println("Error: El nombre de usuario ya está en uso.");
        } else {
            guardarUsuario(usuario);
        }
    }

    public boolean nombreUsuarioExiste(String nombreUsuario) {
        String sql = "SELECT COUNT(*) FROM Usuarios WHERE nombreUsuario = ?";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nombreUsuario);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // Retorna true si existe
            }
        } catch (SQLException e) {
            System.out.println("Error al verificar el nombre de usuario: " + e.getMessage());
        }
        return false;
    }

    private void crearTablaUsuarios() {
        String createTableSQL = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Usuarios' AND xtype='U') " +
                                 "CREATE TABLE Usuarios (Id INT PRIMARY KEY IDENTITY, " +
                                 "nombreUsuario NVARCHAR(50) NOT NULL, " +
                                 "contrasena NVARCHAR(50) NOT NULL)";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(createTableSQL)) {
            pstmt.execute();
            System.out.println("Tabla Usuarios verificada o creada.");
        } catch (SQLException e) {
            System.out.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    private void guardarUsuario(Usuario usuario) {
        String sql = "INSERT INTO Usuarios (nombreUsuario, contrasena) VALUES (?, ?)";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getNombreUsuario());
            pstmt.setString(2, usuario.getContrasena());
            pstmt.executeUpdate();
            System.out.println("Usuario registrado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al registrar el usuario: " + e.getMessage());
        }
    }

    public boolean verificarUsuario(String nombreUsuario, String contrasena) {
        String sql = "SELECT * FROM Usuarios WHERE nombreUsuario = ? AND contrasena = ?";

        try (Connection con = conexion.getConexion();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nombreUsuario);
            pstmt.setString(2, contrasena);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // Devuelve true si se encuentra el usuario
        } catch (SQLException e) {
            System.out.println("Error al verificar el usuario: " + e.getMessage());
            return false;
        }
    }
}
