package com.celfinder.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "solicitudesCompra")
public class SolicitudCompra {

    @Id
    private String id;
    private String celularId;
    private String nombreCelular;
    private String compradorId;
    private String nombreComprador;
    private String vendedorId;
    private String direccionComprador;
    private String correoComprador;
    private String numeroContactoComprador;
    private String estado; // "pendiente", "autorizada", "rechazada"
    private String descripcionVendedor; // Mensaje opcional del vendedor al autorizar/rechazar
    private LocalDateTime fechaSolicitud;

    // Constructor vacío
    public SolicitudCompra() {
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCelularId() {
        return celularId;
    }

    public void setCelularId(String celularId) {
        this.celularId = celularId;
    }

    public String getNombreCelular() {
        return nombreCelular;
    }

    public void setNombreCelular(String nombreCelular) {
        this.nombreCelular = nombreCelular;
    }

    public String getCompradorId() {
        return compradorId;
    }

    public void setCompradorId(String compradorId) {
        this.compradorId = compradorId;
    }

    public String getNombreComprador() {
        return nombreComprador;
    }

    public void setNombreComprador(String nombreComprador) {
        this.nombreComprador = nombreComprador;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(String vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getDireccionComprador() {
        return direccionComprador;
    }

    public void setDireccionComprador(String direccionComprador) {
        this.direccionComprador = direccionComprador;
    }

    public String getCorreoComprador() {
        return correoComprador;
    }

    public void setCorreoComprador(String correoComprador) {
        this.correoComprador = correoComprador;
    }

    public String getNumeroContactoComprador() {
        return numeroContactoComprador;
    }

    public void setNumeroContactoComprador(String numeroContactoComprador) {
        this.numeroContactoComprador = numeroContactoComprador;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescripcionVendedor() {
        return descripcionVendedor;
    }

    public void setDescripcionVendedor(String descripcionVendedor) {
        this.descripcionVendedor = descripcionVendedor;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}