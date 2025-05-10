package com.celfinder.Procesos;

import com.celfinder.Model.Celular;
import com.celfinder.Model.TelefonoVenta;
import com.celfinder.Procesos.ComparadorCelular;
import com.celfinder.Procesos.ComparadorMedia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    private final MongoTemplate mongoTemplate;
    private final ComparadorCelular comparadorCelular;
    private final ComparadorMedia comparadorMedia;

    @Autowired
    public VentaService(MongoTemplate mongoTemplate, ComparadorCelular comparadorCelular, ComparadorMedia comparadorMedia) {
        this.mongoTemplate = mongoTemplate;
        this.comparadorCelular = comparadorCelular;
        this.comparadorMedia = comparadorMedia;
    }

    public void publicarTelefono(TelefonoVenta telefono) {
        telefono.setFechaPublicacion(LocalDateTime.now());
        telefono.setEstadoVenta("disponible");
        // Nota: El vendedorId debe ser establecido por la lógica de autenticación
        mongoTemplate.save(telefono, "telefonos_venta");
    }

    public List<TelefonoVenta> obtenerTelefonosEnVenta() {
        Query query = new Query(Criteria.where("estadoVenta").is("disponible"));
        return mongoTemplate.find(query, TelefonoVenta.class, "telefonos_venta");
    }

    public TelefonoVenta obtenerTelefonoPorId(String id) {
        return mongoTemplate.findById(id, TelefonoVenta.class, "telefonos_venta");
    }

    public List<TelefonoVenta> buscarTelefonos(String nombre, String estado, Double precioMin, Double precioMax) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (nombre != null && !nombre.isEmpty()) {
            criteriaList.add(Criteria.where("nombre").regex(nombre, "i"));
        }
        if (estado != null && !estado.isEmpty()) {
            criteriaList.add(Criteria.where("estado").is(estado));
        }
        if (precioMin != null) {
            criteriaList.add(Criteria.where("precio").gte(precioMin));
        }
        if (precioMax != null) {
            criteriaList.add(Criteria.where("precio").lte(precioMax));
        }
        criteriaList.add(Criteria.where("estadoVenta").is("disponible"));

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return mongoTemplate.find(query, TelefonoVenta.class, "telefonos_venta");
    }

    public void comprarTelefono(String id) {
        TelefonoVenta telefono = obtenerTelefonoPorId(id);
        if (telefono != null && "disponible".equals(telefono.getEstadoVenta())) {
            telefono.setEstadoVenta("vendido");
            mongoTemplate.save(telefono, "telefonos_venta");
        } else {
            throw new IllegalStateException("El teléfono no está disponible para la compra.");
        }
    }

    public void eliminarTelefono(String id) {
        TelefonoVenta telefono = obtenerTelefonoPorId(id);
        if (telefono != null) {
            mongoTemplate.remove(telefono, "telefonos_venta");
        }
    }

    public List<String> compararConOtroTelefono(String id, String idOtroTelefono) {
        TelefonoVenta telefono1 = obtenerTelefonoPorId(id);
        TelefonoVenta telefono2 = obtenerTelefonoPorId(idOtroTelefono);
        if (telefono1 == null || telefono2 == null) {
            throw new IllegalArgumentException("Uno o ambos teléfonos no existen.");
        }
        comparadorCelular.registrarCelulares(telefono1.getEspecificaciones(), telefono2.getEspecificaciones());
        return comparadorCelular.getResultadosComparacion();
    }

    public List<String> compararConMedia(String id) {
        TelefonoVenta telefono = obtenerTelefonoPorId(id);
        if (telefono == null) {
            throw new IllegalArgumentException("El teléfono no existe.");
        }
        comparadorMedia.guardarCelular(telefono.getEspecificaciones());
        return comparadorMedia.getResultadosComparacion();
    }
}