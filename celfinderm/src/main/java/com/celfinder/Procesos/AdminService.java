package com.celfinder.Procesos;

import com.celfinder.Model.TelefonoVenta;
import com.celfinder.Model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * Obtiene todos los teléfonos en venta.
     */
    public List<TelefonoVenta> obtenerTodosLosTelefonos() {
        return mongoTemplate.findAll(TelefonoVenta.class, "telefonos_venta");
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
        usuario.getRoles().remove(rol);
        mongoTemplate.save(usuario, "usuarios");
    }
}