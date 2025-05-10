package com.celfinder.Model;

import java.io.Serializable;

public class Celular implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private double ghz; 
    private int camara;
    private int ram;
    private int bateria;
    private int almacenamiento;
    private int seleccion;
    private String nombreCpu; 

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getGhz() {
        return ghz;
    }

    public void setGhz(double ghz) {
        this.ghz = ghz;
    }

    public int getCamara() {
        return camara;
    }

    public void setCamara(int camara) {
        this.camara = camara;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public int getBateria() {
        return bateria;
    }

    public void setBateria(int bateria) {
        this.bateria = bateria;
    }

    public int getAlmacenamiento() {
        return almacenamiento;
    }

    public void setAlmacenamiento(int almacenamiento) {
        this.almacenamiento = almacenamiento;
    }

    public int getSeleccion() {
        return seleccion;
    }

    public void setSeleccion(int seleccion) {
        this.seleccion = seleccion;
    }

    public String getNombreCpu() {
        return nombreCpu;
    }

    public void setNombreCpu(String nombreCpu) {
        this.nombreCpu = nombreCpu;
    }
}
