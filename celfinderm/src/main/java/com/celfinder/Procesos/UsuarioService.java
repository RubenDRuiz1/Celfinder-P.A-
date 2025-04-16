package com.celfinder.Procesos;

import com.celfinder.Model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService implements UserDetailsService {

    private final MongoTemplate mongoTemplate;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(MongoTemplate mongoTemplate, PasswordEncoder passwordEncoder) {
        this.mongoTemplate = mongoTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Query query = new Query(Criteria.where("nombreUsuario").is(nombreUsuario));
        Usuario usuario = mongoTemplate.findOne(query, Usuario.class, "usuarios");

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + nombreUsuario);
        }

        // Verificar que el usuario tenga el rol ROLE_USER
        if (!usuario.getRoles().contains("ROLE_USER")) {
            throw new UsernameNotFoundException("El usuario " + nombreUsuario + " no tiene el rol necesario (ROLE_USER).");
        }

        return usuario;
    }

    public boolean nombreUsuarioExiste(String nombreUsuario) {
        Query query = new Query(Criteria.where("nombreUsuario").is(nombreUsuario));
        return mongoTemplate.exists(query, Usuario.class, "usuarios");
    }

    public boolean verificarUsuario(String nombreUsuario, String contrasenaPlano) {
        Query query = new Query(Criteria.where("nombreUsuario").is(nombreUsuario));
        Usuario usuario = mongoTemplate.findOne(query, Usuario.class, "usuarios");

        if (usuario == null) {
            return false;
        }

        // Verificar que el usuario tenga el rol ROLE_USER
        if (!usuario.getRoles().contains("ROLE_USER")) {
            return false;
        }

        return passwordEncoder.matches(contrasenaPlano, usuario.getContrasena());
    }

    public void registrarUsuario(Usuario usuario) {
        if (nombreUsuarioExiste(usuario.getNombreUsuario())) {
            System.out.println("Error: El nombre de usuario ya está en uso.");
            return;
        }

        // Guardar el rol con el prefijo ROLE_
        usuario.getRoles().add("ROLE_USER");
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

        try {
            mongoTemplate.save(usuario, "usuarios");
            System.out.println("Usuario registrado correctamente.");
        } catch (Exception e) {
            System.out.println("Error al registrar el usuario: " + e.getMessage());
        }
    }
}