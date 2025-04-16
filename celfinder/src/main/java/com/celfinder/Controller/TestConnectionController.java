package com.celfinder.Controller;

import com.celfinder.Model.conexion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;

@RestController
public class TestConnectionController {

    @GetMapping("/test-connection")
    public String testConnection() {
        Connection connection = conexion.getConexion();
        
        if (connection != null) {
            try {
                connection.close(); // Cierra la conexión después de la prueba
            } catch (Exception e) {
                return "Conexión exitosa, pero no se pudo cerrar la conexión: " + e.getMessage();
            }
            return "Conexión exitosa a la base de datos.";
        } else {
            return "Fallo al conectar a la base de datos.";
        }
    }
}
