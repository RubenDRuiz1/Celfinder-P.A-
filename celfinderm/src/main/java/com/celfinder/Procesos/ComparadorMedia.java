package com.celfinder.Procesos;

import com.celfinder.Model.Celular;
import com.celfinder.Model.Celularmedia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class ComparadorMedia {

    private Celular celular;
    private List<String> resultadosComparacion = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ComparadorMedia(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void registrarCelular() {
        celular = new Celular();

        System.out.print("Ingrese el nombre del celular: ");
        celular.setNombre(scanner.nextLine());

        System.out.print("Ingrese el nombre del CPU: ");
        celular.setNombreCpu(scanner.nextLine());

        System.out.print("Ingrese la frecuencia en GHz: ");
        celular.setGhz(scanner.nextDouble());

        System.out.print("Ingrese la cantidad de cámaras: ");
        celular.setCamara(scanner.nextInt());

        System.out.print("Ingrese la cantidad de RAM: ");
        celular.setRam(scanner.nextInt());

        System.out.print("Ingrese la capacidad de batería: ");
        celular.setBateria(scanner.nextInt());

        System.out.print("Ingrese la capacidad de almacenamiento: ");
        celular.setAlmacenamiento(scanner.nextInt());

        // Comparar con Celularmedia
        Celularmedia celularMedia = new Celularmedia(); // Inicializar con valores predeterminados
        compararConMedia(celular, celularMedia);

        // Guardar o actualizar celular en la base de datos
        guardarCelular(celular);
    }

    private void compararConMedia(Celular celular, Celularmedia celularMedia) {
        compararCaracteristicas("Frecuencia (GHz)", celular.getGhz(), celularMedia.getGhz());
        compararCaracteristicas("Cámaras", celular.getCamara(), celularMedia.getCamara());
        compararCaracteristicas("RAM (GB)", celular.getRam(), celularMedia.getRam());
        compararCaracteristicas("Batería (mAh)", celular.getBateria(), celularMedia.getBateria());
        compararCaracteristicas("Almacenamiento (GB)", celular.getAlmacenamiento(), celularMedia.getAlmacenamiento());
    }

    private void compararCaracteristicas(String nombre, double valor1, double valor2) {
        if (valor1 < valor2) {
            resultadosComparacion.add("La característica de " + nombre + " es inferior a la media.");
        } else if (valor1 > valor2) {
            resultadosComparacion.add("La característica de " + nombre + " es superior a la media.");
        } else {
            resultadosComparacion.add("La característica de " + nombre + " es igual a la media.");
        }
    }

    public void guardarCelular(Celular celular) {
        // Verificar si el celular ya existe en la base de datos
        Celular celularExistente = obtenerCelularExistente(celular);
        if (celularExistente != null) {
            // Si existe, incrementar la selección y actualizar
            int nuevaSeleccion = celularExistente.getSeleccion() + 1;
            actualizarSeleccion(celularExistente, nuevaSeleccion);
        } else {
            // Si no existe, guardar como nuevo
            try {
                celular.setSeleccion(1); // Inicializar Seleccion en 1
                mongoTemplate.save(celular, "celulares");
                System.out.println("Celular guardado en la base de datos.");
            } catch (Exception e) {
                System.err.println("Error al guardar el celular: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private Celular obtenerCelularExistente(Celular celular) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("nombre").is(celular.getNombre())
                                     .and("nombreCpu").is(celular.getNombreCpu())
                                     .and("ghz").is(celular.getGhz())
                                     .and("camara").is(celular.getCamara())
                                     .and("ram").is(celular.getRam())
                                     .and("bateria").is(celular.getBateria())
                                     .and("almacenamiento").is(celular.getAlmacenamiento()));
            return mongoTemplate.findOne(query, Celular.class, "celulares");
        } catch (Exception e) {
            System.err.println("Error al buscar celular existente: " + e.getMessage());
            e.printStackTrace();
            return null;
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

    public void actualizarSeleccion(Celular celular, int nuevaSeleccion) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("nombre").is(celular.getNombre())
                                     .and("nombreCpu").is(celular.getNombreCpu())
                                     .and("ghz").is(celular.getGhz())
                                     .and("camara").is(celular.getCamara())
                                     .and("ram").is(celular.getRam())
                                     .and("bateria").is(celular.getBateria())
                                     .and("almacenamiento").is(celular.getAlmacenamiento()));

            Update update = new Update().set("seleccion", nuevaSeleccion);
            mongoTemplate.updateFirst(query, update, Celular.class, "celulares");

            System.out.println("Seleccion actualizada para el celular " + celular.getNombre());
        } catch (Exception e) {
            System.err.println("Error al actualizar seleccion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}