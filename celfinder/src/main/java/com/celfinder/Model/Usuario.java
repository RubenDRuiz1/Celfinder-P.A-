package com.celfinder.Model;

import java.io.Serializable;

public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id; // Nuevo campo para el ID
    private String nombreUsuario;
    private String contrasena;
    private String nombreadmin;
    private String contrasenaadmin;

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreadmin() {
        return nombreadmin;
    }

    public void setNombreadmin(String nombreadmin) {
        this.nombreadmin = nombreadmin;
    }

    public String getContrasenaadmin() {
        return contrasenaadmin;
    }

    public void setContrasenaadmin(String contrasenaadmin) {
        this.contrasenaadmin = contrasenaadmin;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
