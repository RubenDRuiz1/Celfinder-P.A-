package com.celfinder.Controller;

import com.celfinder.Model.SolicitudVendedor;
import com.celfinder.Model.Celular;
import com.celfinder.Model.Notificacion;
import com.celfinder.Model.Usuario;
import com.celfinder.Procesos.AdminService;
import com.celfinder.Procesos.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    private final VentaService ventaService;

    @Autowired
    public AdminController(AdminService adminService, VentaService ventaService) {
        this.adminService = adminService;
        this.ventaService = ventaService;
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
     * Elimina un usuario.
     */
    @PostMapping("/eliminar-usuario/{id}")
    public String eliminarUsuario(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            adminService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    /**
     * Lista todos los teléfonos, con filtros.
     */
    @GetMapping("/telefonos")
    public String listarTelefonos(@RequestParam(required = false) String nombre,
                                 @RequestParam(required = false) String estadoVenta,
                                 Model model) {
        try {
            List<Celular> telefonos;
            if (nombre != null || estadoVenta != null) {
                telefonos = adminService.buscarTelefonos(nombre, estadoVenta);
            } else {
                telefonos = adminService.obtenerTodosLosTelefonos();
            }
            model.addAttribute("telefonos", telefonos != null ? telefonos : new ArrayList<>());
            model.addAttribute("adminService", adminService);
            model.addAttribute("nombre", nombre);
            model.addAttribute("estadoVenta", estadoVenta);
            return "listarTelefonosAdmin";
        } catch (Exception e) {
            model.addAttribute("error", "Error al listar los teléfonos: " + e.getMessage());
            model.addAttribute("telefonos", new ArrayList<>());
            return "listarTelefonosAdmin";
        }
    }

    /**
     * Elimina un teléfono.
     */
    @PostMapping("/eliminar-telefono/{id}")
    public String eliminarTelefono(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            ventaService.eliminarTelefono(id);
            redirectAttributes.addFlashAttribute("mensaje", "Teléfono eliminado correctamente.");
            return "redirect:/admin/telefonos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el teléfono: " + e.getMessage());
            return "redirect:/admin/telefonos";
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

    /**
     * Muestra el formulario para solicitar ser vendedor.
     */
    @GetMapping("/solicitar-vendedor")
    public String mostrarFormularioSolicitudVendedor(Model model) {
        model.addAttribute("solicitud", new SolicitudVendedor());
        return "formularioSolicitudVendedor";
    }

    /**
     * Procesa la solicitud para convertirse en vendedor.
     */
    @PostMapping("/solicitar-vendedor")
    public String procesarSolicitudVendedor(@ModelAttribute SolicitudVendedor solicitud,
                                           Authentication authentication,
                                           RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Debes estar autenticado para enviar una solicitud.");
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            solicitud.setUsuarioId(usuario.getId());
            if (usuario.getRoles().contains("ROLE_VENDEDOR")) {
                throw new IllegalStateException("Ya eres vendedor.");
            }
            if (solicitud.getNombreCompleto() == null || solicitud.getNombreCompleto().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre completo es obligatorio.");
            }
            if (solicitud.getCorreo() == null || !solicitud.getCorreo().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("El correo electrónico no es válido.");
            }
            if (solicitud.getTelefono() == null || !solicitud.getTelefono().matches("\\d{10}")) {
                throw new IllegalArgumentException("El número de teléfono debe tener 10 dígitos.");
            }
            if (solicitud.getDireccion() == null || solicitud.getDireccion().trim().isEmpty()) {
                throw new IllegalArgumentException("La dirección es obligatoria.");
            }
            adminService.crearSolicitudVendedor(solicitud);
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud enviada correctamente. Un administrador la revisará.");
            return "redirect:/ventas/listar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar la solicitud: " + e.getMessage());
            return "redirect:/admin/solicitar-vendedor";
        }
    }

    /**
     * Lista todas las solicitudes de vendedor pendientes.
     */
    @GetMapping("/solicitudes-vendedor")
    public String listarSolicitudesVendedor(Model model) {
        try {
            List<SolicitudVendedor> solicitudes = adminService.obtenerSolicitudesVendedorPendientes();
            model.addAttribute("solicitudes", solicitudes != null ? solicitudes : new ArrayList<>());
            model.addAttribute("adminService", adminService);
            return "listarSolicitudesVendedor";
        } catch (Exception e) {
            model.addAttribute("error", "Error al listar las solicitudes: " + e.getMessage());
            model.addAttribute("solicitudes", new ArrayList<>());
            return "listarSolicitudesVendedor";
        }
    }

    /**
     * Gestiona una solicitud de vendedor.
     */
    @PostMapping("/gestionar-solicitud-vendedor/{id}")
    public String gestionarSolicitudVendedor(@PathVariable String id,
                                            @RequestParam String accion,
                                            @RequestParam(required = false) String comentarioAdmin,
                                            RedirectAttributes redirectAttributes) {
        try {
            adminService.gestionarSolicitudVendedor(id, accion, comentarioAdmin);
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud " + (accion.equals("aprobar") ? "aprobada" : "rechazada") + " correctamente.");
            return "redirect:/admin/solicitudes-vendedor";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al gestionar la solicitud: " + e.getMessage());
            return "redirect:/admin/solicitudes-vendedor";
        }
    }

    /**
     * Muestra el formulario para crear una notificación.
     */
    @GetMapping("/crear-notificacion")
    public String mostrarFormularioCrearNotificacion(Model model) {
        model.addAttribute("notificacion", new Notificacion());
        return "crearNotificacion";
    }

    /**
     * Procesa la creación de una notificación.
     */
    @PostMapping("/crear-notificacion")
    public String crearNotificacion(@ModelAttribute Notificacion notificacion,
                                   @RequestParam(required = false) boolean enviarATodos,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (notificacion.getMensaje() == null || notificacion.getMensaje().trim().isEmpty()) {
                throw new IllegalArgumentException("El mensaje es obligatorio.");
            }
            if (enviarATodos) {
                adminService.crearNotificacionParaTodos(notificacion.getMensaje());
                redirectAttributes.addFlashAttribute("mensaje", "Notificación enviada a todos los usuarios correctamente.");
            } else {
                if (notificacion.getUsuarioId() == null || notificacion.getUsuarioId().trim().isEmpty()) {
                    throw new IllegalArgumentException("El ID del usuario es obligatorio.");
                }
                if (adminService.obtenerUsuarioPorId(notificacion.getUsuarioId()) == null) {
                    throw new IllegalArgumentException("El usuario especificado no existe.");
                }
                adminService.crearNotificacion(notificacion);
                redirectAttributes.addFlashAttribute("mensaje", "Notificación enviada correctamente.");
            }
            return "redirect:/admin";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/crear-notificacion";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al enviar la notificación: " + e.getMessage());
            return "redirect:/admin/crear-notificacion";
        }
    }
}