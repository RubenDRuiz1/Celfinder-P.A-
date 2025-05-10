package com.celfinder.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class homecontroller {
	@GetMapping("/menu")
    public String mostrarRegistro(Model model) {
        return "menu"; // Retorna la vista del formulario
    }
	
	@GetMapping("/")
    public String index(Model model) {
        return "index"; // Retorna la vista del formulario
    }
}
