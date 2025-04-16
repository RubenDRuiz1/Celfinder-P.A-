package com.celfinder.Controller;

import com.celfinder.Model.Usuario;
import com.celfinder.Procesos.Login;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final Login login = new Login();

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        return "Register"; // Devuelve el nombre del archivo HTML (sin .html)
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        return "Login"; // Devuelve el nombre del archivo HTML (sin .html)
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        if (login.nombreUsuarioExiste(usuario.getNombreUsuario())) {
            redirectAttributes.addFlashAttribute("mensaje", "El nombre de usuario ya está en uso");
            return "redirect:/usuarios/registro"; // Redirige de vuelta al registro
        }

        login.registrarUsuario(usuario); // Guarda el usuario en la base de datos
        return "redirect:/usuarios/login"; // Redirige al login
    }

    @PostMapping("/ingresar")
    public String ingresarUsuario(@RequestParam String nombreUsuario, @RequestParam String contrasena, Model model) {
        boolean validUser = login.verificarUsuario(nombreUsuario, contrasena);
        if (validUser) {
            return "redirect:/menu"; // Cambia esto según tu lógica de éxito
        }
        model.addAttribute("mensaje", "Nombre de usuario o contraseña incorrectos");
        return "Login"; // Vuelve a la página de login con un mensaje de error
    }
}
