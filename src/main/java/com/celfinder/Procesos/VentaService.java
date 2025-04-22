package com.celfinder.Procesos;

import com.celfinder.Model.Celular;
import com.celfinder.Model.SolicitudCompra;
import com.celfinder.Model.Notificacion;
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

    public void publicarTelefono(Celular celular) {
        celular.setFechaPublicacion(LocalDateTime.now());
        celular.setEstadoVenta("disponible");
        mongoTemplate.save(celular, "celulares");
    }

    public List<Celular> obtenerTelefonosEnVenta() {
        Query query = new Query(Criteria.where("estadoVenta").is("disponible"));
        return mongoTemplate.find(query, Celular.class, "celulares");
    }

    public Celular obtenerTelefonoPorId(String id) {
        return mongoTemplate.findById(id, Celular.class, "celulares");
    }

    public SolicitudCompra obtenerSolicitudPorId(String id) {
        return mongoTemplate.findById(id, SolicitudCompra.class, "solicitudesCompra");
    }

    public List<Celular> buscarTelefonos(String nombre, String estado, Double precioMin, Double precioMax) {
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

        return mongoTemplate.find(query, Celular.class, "celulares");
    }

    public void crearSolicitudCompra(SolicitudCompra solicitud) {
        Query query = new Query(Criteria.where("celularId").is(solicitud.getCelularId()).and("estado").is("pendiente"));
        if (mongoTemplate.exists(query, SolicitudCompra.class, "solicitudesCompra")) {
            throw new IllegalStateException("Ya existe una solicitud pendiente para este celular.");
        }

        // Validar que el vendedorId sea correcto
        Celular celular = obtenerTelefonoPorId(solicitud.getCelularId());
        if (celular == null || !celular.getEstadoVenta().equals("disponible")) {
            throw new IllegalStateException("El teléfono no está disponible para la compra.");
        }
        if (!celular.getVendedorId().equals(solicitud.getVendedorId())) {
            throw new IllegalStateException("El vendedor especificado no coincide con el vendedor del teléfono.");
        }

        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setEstado("pendiente");
        mongoTemplate.save(solicitud, "solicitudesCompra");
    }

    public List<SolicitudCompra> obtenerSolicitudesPorVendedor(String vendedorId) {
        Query query = new Query(Criteria.where("vendedorId").is(vendedorId).and("estado").is("pendiente"));
        return mongoTemplate.find(query, SolicitudCompra.class, "solicitudesCompra");
    }

    public List<SolicitudCompra> obtenerHistorialCompras(String compradorId) {
        Query query = new Query(Criteria.where("compradorId").is(compradorId));
        return mongoTemplate.find(query, SolicitudCompra.class, "solicitudesCompra");
    }

    public List<SolicitudCompra> obtenerHistorialVentas(String vendedorId) {
        Query query = new Query(Criteria.where("vendedorId").is(vendedorId));
        return mongoTemplate.find(query, SolicitudCompra.class, "solicitudesCompra");
    }

    public void gestionarSolicitudCompra(String solicitudId, String accion, String descripcionVendedor) {
        SolicitudCompra solicitud = obtenerSolicitudPorId(solicitudId);
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud no existe.");
        }

        if (!solicitud.getEstado().equals("pendiente")) {
            throw new IllegalStateException("La solicitud ya ha sido gestionada.");
        }

        solicitud.setEstado(accion.equals("autorizar") ? "autorizada" : "rechazada");
        solicitud.setDescripcionVendedor(descripcionVendedor != null ? descripcionVendedor : "");

        if (accion.equals("autorizar")) {
            Celular celular = obtenerTelefonoPorId(solicitud.getCelularId());
            if (celular != null && celular.getEstadoVenta().equals("disponible")) {
                celular.setEstadoVenta("vendido");
                mongoTemplate.save(celular, "celulares");
            } else {
                throw new IllegalStateException("El celular no está disponible para la venta.");
            }
        }

        mongoTemplate.save(solicitud, "solicitudesCompra");

        // Crear notificación para el comprador
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(solicitud.getCompradorId());
        notificacion.setMensaje(accion.equals("autorizar")
            ? "Su compra del teléfono '" + solicitud.getNombreCelular() + "' ha sido realizada con éxito."
            : "Su compra del teléfono '" + solicitud.getNombreCelular() + "' fue rechazada.");
        notificacion.setFecha(LocalDateTime.now());
        mongoTemplate.save(notificacion, "notificaciones");
    }

    public void eliminarTelefono(String id) {
        Celular celular = obtenerTelefonoPorId(id);
        if (celular != null) {
            Query query = new Query(Criteria.where("celularId").is(id).and("estado").is("pendiente"));
            if (mongoTemplate.exists(query, SolicitudCompra.class, "solicitudesCompra")) {
                throw new IllegalStateException("No se puede eliminar el teléfono porque tiene solicitudes pendientes.");
            }
            mongoTemplate.remove(celular, "celulares");
        } else {
            throw new IllegalArgumentException("El teléfono no existe.");
        }
    }

    public List<String> compararConOtroTelefono(String id, String idOtroTelefono) {
        Celular celular1 = obtenerTelefonoPorId(id);
        Celular celular2 = obtenerTelefonoPorId(idOtroTelefono);
        if (celular1 == null || celular2 == null) {
            throw new IllegalArgumentException("Uno o ambos teléfonos no existen.");
        }
        comparadorCelular.registrarCelulares(celular1, celular2);
        return comparadorCelular.getResultadosComparacion();
    }

    public List<String> compararConMedia(String id) {
        Celular celular = obtenerTelefonoPorId(id);
        if (celular == null) {
            throw new IllegalArgumentException("El teléfono no existe.");
        }
        comparadorMedia.guardarCelular(celular);
        return comparadorMedia.getResultadosComparacion();
    }
}