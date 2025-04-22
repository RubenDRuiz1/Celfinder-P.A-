package com.celfinder.Controller;

import com.celfinder.Model.Usuario;
import com.celfinder.Procesos.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        return "Register";
    }

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        return "Login";
    }

    @PostMapping("/registrar")
    public String registrarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        if (usuarioService.nombreUsuarioExiste(usuario.getNombreUsuario())) {
            redirectAttributes.addFlashAttribute("mensaje", "El nombre de usuario ya está en uso");
            return "redirect:/usuarios/registro";
        }

        usuarioService.registrarUsuario(usuario);
        return "redirect:/usuarios/login";
    }
}