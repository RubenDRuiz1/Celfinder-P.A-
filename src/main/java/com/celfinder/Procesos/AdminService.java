package com.celfinder.Procesos;

import com.celfinder.Model.Celular;
import com.celfinder.Model.SolicitudCompra;
import com.celfinder.Model.SolicitudVendedor;
import com.celfinder.Model.Notificacion;
import com.celfinder.Model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public AdminService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Obtiene todos los usuarios registrados.
     */
    public List<Usuario> obtenerTodosLosUsuarios() {
        return mongoTemplate.findAll(Usuario.class, "usuarios");
    }

    /**
     * Obtiene todos los teléfonos registrados.
     */
    public List<Celular> obtenerTodosLosTelefonos() {
        return mongoTemplate.findAll(Celular.class, "celulares");
    }

    /**
     * Busca teléfonos por nombre o estado de venta.
     */
    public List<Celular> buscarTelefonos(String nombre, String estadoVenta) {
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (nombre != null && !nombre.isEmpty()) {
            criteriaList.add(Criteria.where("nombre").regex(nombre, "i"));
        }
        if (estadoVenta != null && !estadoVenta.isEmpty()) {
            criteriaList.add(Criteria.where("estadoVenta").is(estadoVenta));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return mongoTemplate.find(query, Celular.class, "celulares");
    }

    /**
     * Obtiene un usuario por su ID.
     */
    public Usuario obtenerUsuarioPorId(String id) {
        return mongoTemplate.findById(id, Usuario.class, "usuarios");
    }

    /**
     * Asigna un rol a un usuario.
     */
    public void asignarRol(String usuarioId, String rol) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        if (!rol.equals("ROLE_VENDEDOR") && !rol.equals("ROLE_ADMIN")) {
            throw new IllegalArgumentException("Rol inválido. Use ROLE_VENDEDOR o ROLE_ADMIN.");
        }
        if (usuario.getRoles().contains(rol)) {
            throw new IllegalArgumentException("El usuario ya tiene el rol " + rol);
        }
        usuario.getRoles().add(rol);
        mongoTemplate.save(usuario, "usuarios");
    }

    /**
     * Remueve un rol de un usuario.
     */
    public void removerRol(String usuarioId, String rol) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        if (!rol.equals("ROLE_VENDEDOR") && !rol.equals("ROLE_ADMIN")) {
            throw new IllegalArgumentException("Rol inválido. Use ROLE_VENDEDOR o ROLE_ADMIN.");
        }
        if (rol.equals("ROLE_USER")) {
            throw new IllegalArgumentException("No se puede remover el rol ROLE_USER.");
        }
        if (!usuario.getRoles().contains(rol)) {
            throw new IllegalArgumentException("El usuario no tiene el rol " + rol);
        }
        usuario.getRoles().remove(rol);
        mongoTemplate.save(usuario, "usuarios");
    }

    /**
     * Elimina un usuario.
     */
    public void eliminarUsuario(String usuarioId) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado.");
        }
        Query telefonosQuery = new Query(Criteria.where("vendedorId").is(usuarioId));
        if (mongoTemplate.exists(telefonosQuery, Celular.class, "celulares")) {
            throw new IllegalStateException("No se puede eliminar el usuario porque tiene teléfonos registrados.");
        }
        Query solicitudesQuery = new Query(Criteria.where("compradorId").is(usuarioId).orOperator(Criteria.where("vendedorId").is(usuarioId)));
        if (mongoTemplate.exists(solicitudesQuery, SolicitudCompra.class, "solicitudesCompra")) {
            throw new IllegalStateException("No se puede eliminar el usuario porque tiene solicitudes asociadas.");
        }
        mongoTemplate.remove(usuario, "usuarios");
    }

    /**
     * Crea una solicitud para convertirse en vendedor.
     */
    public void crearSolicitudVendedor(SolicitudVendedor solicitud) {
        Query query = new Query(Criteria.where("usuarioId").is(solicitud.getUsuarioId()).and("estado").is("pendiente"));
        if (mongoTemplate.exists(query, SolicitudVendedor.class, "solicitudesVendedor")) {
            throw new IllegalStateException("Ya existe una solicitud pendiente para este usuario.");
        }
        solicitud.setId(UUID.randomUUID().toString());
        solicitud.setFechaSolicitud(LocalDateTime.now());
        solicitud.setEstado("pendiente");
        mongoTemplate.save(solicitud, "solicitudesVendedor");
    }

    /**
     * Obtiene todas las solicitudes de vendedor pendientes.
     */
    public List<SolicitudVendedor> obtenerSolicitudesVendedorPendientes() {
        Query query = new Query(Criteria.where("estado").is("pendiente"));
        return mongoTemplate.find(query, SolicitudVendedor.class, "solicitudesVendedor");
    }

    /**
     * Obtiene una solicitud de vendedor por su ID.
     */
    public SolicitudVendedor obtenerSolicitudVendedorPorId(String id) {
        return mongoTemplate.findById(id, SolicitudVendedor.class, "solicitudesVendedor");
    }

    /**
     * Gestiona una solicitud de vendedor (aprobar/rechazar).
     */
    public void gestionarSolicitudVendedor(String solicitudId, String accion, String comentarioAdmin) {
        SolicitudVendedor solicitud = obtenerSolicitudVendedorPorId(solicitudId);
        if (solicitud == null) {
            throw new IllegalArgumentException("La solicitud no existe.");
        }
        if (!solicitud.getEstado().equals("pendiente")) {
            throw new IllegalStateException("La solicitud ya ha sido gestionada.");
        }
        solicitud.setEstado(accion.equals("aprobar") ? "aprobada" : "rechazada");
        solicitud.setComentarioAdmin(comentarioAdmin != null ? comentarioAdmin : "");
        solicitud.setFechaRespuesta(LocalDateTime.now());
        if (accion.equals("aprobar")) {
            asignarRol(solicitud.getUsuarioId(), "ROLE_VENDEDOR");
        }
        mongoTemplate.save(solicitud, "solicitudesVendedor");
        // Crear notificación para el usuario
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuarioId(solicitud.getUsuarioId());
        notificacion.setMensaje("Tu solicitud para ser vendedor ha sido " + (accion.equals("aprobar") ? "aprobada" : "rechazada") + ". Comentario: " + (comentarioAdmin != null ? comentarioAdmin : "Ninguno"));
        notificacion.setFecha(LocalDateTime.now());
        crearNotificacion(notificacion);
    }

    /**
     * Obtiene el historial de solicitudes de vendedor de un usuario.
     */
    public List<SolicitudVendedor> obtenerHistorialSolicitudesVendedor(String usuarioId) {
        Query query = new Query(Criteria.where("usuarioId").is(usuarioId));
        return mongoTemplate.find(query, SolicitudVendedor.class, "solicitudesVendedor");
    }

    /**
     * Crea una notificación para un usuario.
     */
    public void crearNotificacion(Notificacion notificacion) {
        notificacion.setFecha(LocalDateTime.now());
        mongoTemplate.save(notificacion, "notificaciones");
    }

    /**
     * Crea una notificación para todos los usuarios.
     */
    public void crearNotificacionParaTodos(String mensaje) {
        List<Usuario> usuarios = mongoTemplate.findAll(Usuario.class, "usuarios");
        if (usuarios.isEmpty()) {
            throw new IllegalStateException("No hay usuarios registrados.");
        }
        for (Usuario usuario : usuarios) {
            Notificacion notificacion = new Notificacion();
            notificacion.setUsuarioId(usuario.getId());
            notificacion.setMensaje(mensaje);
            notificacion.setFecha(LocalDateTime.now());
            mongoTemplate.save(notificacion, "notificaciones");
        }
    }

    /**
     * Obtiene las notificaciones de un usuario.
     */
    public List<Notificacion> obtenerNotificacionesPorUsuario(String usuarioId) {
        Query query = new Query(Criteria.where("usuarioId").is(usuarioId));
        return mongoTemplate.find(query, Notificacion.class, "notificaciones");
    }
}