package com.celfinder.Controller;

import com.celfinder.Model.TelefonoVenta;
import com.celfinder.Model.Usuario;
import com.celfinder.Procesos.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Muestra la página principal de administración.
     */
    @GetMapping
    public String mostrarPanelAdmin() {
        return "adminPanel";
    }

    /**
     * Lista todos los usuarios registrados.
     */
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        try {
            List<Usuario> usuarios = adminService.obtenerTodosLosUsuarios();
            model.addAttribute("usuarios", usuarios != null ? usuarios : new ArrayList<>());
            return "listarUsuarios";
        } catch (Exception e) {
            model.addAttribute("error", "Error al listar los usuarios: " + e.getMessage());
            model.addAttribute("usuarios", new ArrayList<>());
            return "listarUsuarios";
        }
    }

    /**
     * Lista todos los teléfonos, incluyendo información del vendedor.
     */
    @GetMapping("/telefonos")
    public String listarTelefonos(Model model) {
        try {
            List<TelefonoVenta> telefonos = adminService.obtenerTodosLosTelefonos();
            model.addAttribute("telefonos", telefonos != null ? telefonos : new ArrayList<>());
            model.addAttribute("adminService", adminService); // Para acceder al servicio en la vista
            return "listarTelefonosAdmin";
        } catch (Exception e) {
            model.addAttribute("error", "Error al listar los teléfonos: " + e.getMessage());
            model.addAttribute("telefonos", new ArrayList<>());
            return "listarTelefonosAdmin";
        }
    }

    /**
     * Muestra el formulario para asignar o remover roles a un usuario.
     */
    @GetMapping("/asignar-rol/{id}")
    public String mostrarFormularioAsignarRol(@PathVariable String id, Model model) {
        try {
            Usuario usuario = adminService.obtenerUsuarioPorId(id);
            if (usuario == null) {
                model.addAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/usuarios";
            }
            model.addAttribute("usuario", usuario);
            return "asignarRol";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    /**
     * Procesa la asignación de un rol a un usuario.
     */
    @PostMapping("/asignar-rol/{id}")
    public String asignarRol(@PathVariable String id, @RequestParam String rol, RedirectAttributes redirectAttributes) {
        try {
            adminService.asignarRol(id, rol);
            redirectAttributes.addFlashAttribute("mensaje", "Rol asignado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al asignar el rol: " + e.getMessage());
            return "redirect:/admin/asignar-rol/" + id;
        }
    }

    /**
     * Procesa la remoción de un rol de un usuario.
     */
    @PostMapping("/remover-rol/{id}")
    public String removerRol(@PathVariable String id, @RequestParam String rol, RedirectAttributes redirectAttributes) {
        try {
            adminService.removerRol(id, rol);
            redirectAttributes.addFlashAttribute("mensaje", "Rol removido correctamente.");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al remover el rol: " + e.getMessage());
            return "redirect:/admin/asignar-rol/" + id;
        }
    }
}