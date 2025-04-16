package com.celfinder.Controller;

import com.celfinder.Model.Usuario;
import com.celfinder.Procesos.LoginAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admins")
public class AdminController {

    private final LoginAdmin loginAdmin = new LoginAdmin();

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "RegisterAdmin"; // Devuelve el nombre del archivo HTML (sin .html)
    }

    @GetMapping("/login")
    public String mostrarLogin() {
        return "LoginAdmin"; // Devuelve el nombre del archivo HTML (sin .html)
    }

    @PostMapping("/registrar")
    public ResponseEntity<String> registrarAdmin(@RequestBody Usuario admin) {
        // Verificar si el administrador ya existe
        if (loginAdmin.verificarAdmin(admin.getNombreadmin(), admin.getContrasenaadmin())) {
            return ResponseEntity.status(409).body("El nombre del administrador ya está en uso");
        }

        // Registrar el nuevo administrador
        loginAdmin.registrarAdmin(admin);
        return ResponseEntity.ok("Administrador registrado correctamente");
    }

    @PostMapping("/ingresar")
    public ResponseEntity<String> ingresarAdmin(@RequestParam String nombreAdmin, @RequestParam String contrasenaAdmin) {
        boolean validAdmin = loginAdmin.verificarAdmin(nombreAdmin, contrasenaAdmin);
        
        if (validAdmin) {
            return ResponseEntity.ok("Ingreso exitoso");
        }
        return ResponseEntity.status(401).body("Nombre de administrador o contraseña incorrectos");
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Usuario>> listarAdmins() {
        List<Usuario> admins = loginAdmin.obtenerAdmins(); // Obtener la lista de administradores
        return ResponseEntity.ok(admins);
    }
}
