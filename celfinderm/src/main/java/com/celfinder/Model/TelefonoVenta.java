package com.celfinder.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "telefonos_venta")
public class TelefonoVenta implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String nombre;
    private double precio;
    private String estado; // Ej: "nuevo", "usado", "reacondicionado"
    private String descripcion;
    private String vendedorId; // ID del usuario con rol "vendedor"
    private LocalDateTime fechaPublicacion;
    private String estadoVenta; // Ej: "disponible", "reservado", "vendido"
    private Celular especificaciones; // Vincula al modelo Celular existente
    private String imagenBase64; // Imagen codificada en Base64

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getVendedorId() { return vendedorId; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public String getEstadoVenta() { return estadoVenta; }
    public void setEstadoVenta(String estadoVenta) { this.estadoVenta = estadoVenta; }
    public Celular getEspecificaciones() { return especificaciones; }
    public void setEspecificaciones(Celular especificaciones) { this.especificaciones = especificaciones; }
    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
}