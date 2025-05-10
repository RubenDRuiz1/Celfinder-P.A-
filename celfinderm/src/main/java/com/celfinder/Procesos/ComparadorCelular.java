package com.celfinder.Procesos;

import com.celfinder.Model.Celular;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ComparadorCelular {

    private Celular celular1;
    private Celular celular2;
    private List<String> resultadosComparacion = new ArrayList<>();
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ComparadorCelular(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void registrarCelulares(Celular c1, Celular c2) {
        this.celular1 = c1;
        this.celular2 = c2;

        compararCelulares();
        guardarCelulares();
    }

    private void compararCelulares() {
        compararCaracteristicas("Frecuencia (GHz)", celular1.getGhz(), celular2.getGhz());
        compararCaracteristicas("Cámaras", celular1.getCamara(), celular2.getCamara());
        compararCaracteristicas("RAM (GB)", celular1.getRam(), celular2.getRam());
        compararCaracteristicas("Batería (mAh)", celular1.getBateria(), celular2.getBateria());
        compararCaracteristicas("Almacenamiento (GB)", celular1.getAlmacenamiento(), celular2.getAlmacenamiento());

        determinarMejorDispositivo();
    }

    private void compararCaracteristicas(String nombre, double valor1, double valor2) {
        if (valor1 < valor2) {
            resultadosComparacion.add("La característica de " + nombre + " del dispositivo 1 es inferior al dispositivo 2.");
        } else if (valor1 > valor2) {
            resultadosComparacion.add("La característica de " + nombre + " del dispositivo 1 es superior al dispositivo 2.");
        } else {
            resultadosComparacion.add("Ambos dispositivos son iguales en " + nombre + ".");
        }
    }

    private void determinarMejorDispositivo() {
        int contador1 = 0;
        int contador2 = 0;

        if (celular1.getGhz() > celular2.getGhz()) contador1++;
        else if (celular1.getGhz() < celular2.getGhz()) contador2++;

        if (celular1.getCamara() > celular2.getCamara()) contador1++;
        else if (celular1.getCamara() < celular2.getCamara()) contador2++;

        if (celular1.getRam() > celular2.getRam()) contador1++;
        else if (celular1.getRam() < celular2.getRam()) contador2++;

        if (celular1.getBateria() > celular2.getBateria()) contador1++;
        else if (celular1.getBateria() < celular2.getBateria()) contador2++;

        if (celular1.getAlmacenamiento() > celular2.getAlmacenamiento()) contador1++;
        else if (celular1.getAlmacenamiento() < celular2.getAlmacenamiento()) contador2++;

        if (contador1 > contador2) {
            resultadosComparacion.add("El dispositivo 1 es mejor en " + contador1 + " características.");
        } else if (contador2 > contador1) {
            resultadosComparacion.add("El dispositivo 2 es mejor en " + contador2 + " características.");
        } else {
            resultadosComparacion.add("Ambos dispositivos son igualmente buenos.");
        }
    }

    private void guardarCelulares() {
        try {
            // Inicializar Seleccion en 1 para ambos celulares
            celular1.setSeleccion(1);
            celular2.setSeleccion(1);

            // Guardar celular 1
            mongoTemplate.save(celular1, "celulares");
            // Guardar celular 2
            mongoTemplate.save(celular2, "celulares");

            System.out.println("Celulares guardados en la base de datos.");
        } catch (Exception e) {
            System.err.println("Error al guardar los celulares: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Celular> obtenerCelulares() {
        List<Celular> celulares = new ArrayList<>();
        try {
            celulares = mongoTemplate.findAll(Celular.class, "celulares");
        } catch (Exception e) {
            System.err.println("Error al obtener celulares: " + e.getMessage());
            e.printStackTrace();
        }
        return celulares;
    }

    public List<String> getResultadosComparacion() {
        return resultadosComparacion;
    }
}