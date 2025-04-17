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

@Component
public class ComparadorMedia {

    private Celular celular;
    private List<String> resultadosComparacion = new ArrayList<>();
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ComparadorMedia(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Método ajustado para aceptar un Celular y compararlo con la media predefinida
    public void guardarCelular(Celular celular) {
        this.celular = celular;
        this.resultadosComparacion.clear();
        compararConMedia(celular, new Celularmedia());
    }

    private void compararConMedia(Celular celular, Celularmedia celularMedia) {
        compararCaracteristicas("Frecuencia (GHz)", celular.getGhz(), celularMedia.getGhz());
        compararCaracteristicas("Cámara (MP)", celular.getCamara(), celularMedia.getCamara());
        compararCaracteristicas("RAM (GB)", celular.getRam(), celularMedia.getRam());
        compararCaracteristicas("Batería (mAh)", celular.getBateria(), celularMedia.getBateria());
        compararCaracteristicas("Almacenamiento (GB)", celular.getAlmacenamiento(), celularMedia.getAlmacenamiento());
    }

    private void compararCaracteristicas(String nombre, double valor1, double valor2) {
        if (valor1 < valor2) {
            resultadosComparacion.add(String.format("%s: %.2f es inferior a la media del mercado (%.2f).", nombre, valor1, valor2));
        } else if (valor1 > valor2) {
            resultadosComparacion.add(String.format("%s: %.2f es superior a la media del mercado (%.2f).", nombre, valor1, valor2));
        } else {
            resultadosComparacion.add(String.format("%s: %.2f es igual a la media del mercado (%.2f).", nombre, valor1, valor2));
        }
    }

    // Método para registrar un celular (usado en /compararmedia, no en /ventas/comparar-media)
    public void registrarCelular(Celular celular) {
        this.celular = celular;
        // Comparar con Celularmedia
        Celularmedia celularMedia = new Celularmedia();
        compararConMedia(celular, celularMedia);
        // Guardar o actualizar celular en la base de datos
        guardarCelularEnBaseDatos(celular);
    }

    private void guardarCelularEnBaseDatos(Celular celular) {
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
        } catch (Exception e) {
            System.err.println("Error al actualizar seleccion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}