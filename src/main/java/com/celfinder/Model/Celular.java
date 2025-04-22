package com.celfinder.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "celulares")
public class Celular implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String nombre;
    private double ghz;
    private int camara;
    private int ram;
    private int bateria;
    private int almacenamiento;
    private int seleccion;
    private String nombreCpu;
    private double precio;
    private String estado; // Ej: "nuevo", "usado", "reacondicionado"
    private String descripcion;
    private String vendedorId; // ID del usuario con rol "vendedor"
    private LocalDateTime fechaPublicacion;
    private String estadoVenta; // Ej: "disponible", "reservado", "vendido"
    private String imagenBase64; // Imagen codificada en Base64

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getGhz() { return ghz; }
    public void setGhz(double ghz) { this.ghz = ghz; }
    public int getCamara() { return camara; }
    public void setCamara(int camara) { this.camara = camara; }
    public int getRam() { return ram; }
    public void setRam(int ram) { this.ram = ram; }
    public int getBateria() { return bateria; }
    public void setBateria(int bateria) { this.bateria = bateria; }
    public int getAlmacenamiento() { return almacenamiento; }
    public void setAlmacenamiento(int almacenamiento) { this.almacenamiento = almacenamiento; }
    public int getSeleccion() { return seleccion; }
    public void setSeleccion(int seleccion) { this.seleccion = seleccion; }
    public String getNombreCpu() { return nombreCpu; }
    public void setNombreCpu(String nombreCpu) { this.nombreCpu = nombreCpu; }
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
    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
}