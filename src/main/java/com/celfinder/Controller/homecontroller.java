package com.celfinder.Controller;

import com.celfinder.Model.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Set;

@Controller
public class homecontroller {

    @GetMapping("/menu")
    public String mostrarRegistro(Model model, Authentication authentication) {
        // Pasar los roles del usuario al modelo
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Usuario) {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Set<String> roles = usuario.getRoles();
            model.addAttribute("userRoles", roles);
        } else {
            model.addAttribute("userRoles", new ArrayList<>());
        }
        return "menu"; // Retorna la vista del formulario
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index"; // Retorna la vista del formulario
    }
}