package com.celfinder.Model;

/**
 *
 * @author E302
 */
public class Celularmedia {
    
    private String nombre;
    private double Ghz=2;
    private int camara=40;
    private int ram=6;
    private int bateria=3500;
    private int almacenamiento=128;
    


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getGhz() {
        return Ghz;
    }

    public void setGhz(double Ghz) {
        this.Ghz = Ghz;
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
    
    
    
}

