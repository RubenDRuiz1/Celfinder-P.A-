package com.celfinder.Controller;

import com.celfinder.Model.Celular;
import com.celfinder.Model.SolicitudCompra;
import com.celfinder.Model.SolicitudVendedor;
import com.celfinder.Model.Notificacion;
import com.celfinder.Model.Usuario;
import com.celfinder.Procesos.AdminService;
import com.celfinder.Procesos.UsuarioService;
import com.celfinder.Procesos.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final UsuarioService usuarioService;
    private final AdminService adminService;

    @Autowired
    public VentaController(VentaService ventaService, UsuarioService usuarioService, AdminService adminService) {
        this.ventaService = ventaService;
        this.usuarioService = usuarioService;
        this.adminService = adminService;
    }

    @GetMapping("/publicar")
    public String mostrarFormularioPublicacion(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || !hasRequiredRole(authentication)) {
            redirectAttributes.addFlashAttribute("error", "Solo los vendedores o administradores pueden publicar teléfonos.");
            return "redirect:/ventas/listar";
        }
        Celular celular = new Celular();
        model.addAttribute("telefono", celular);
        return "publicarTelefono";
    }

    @PostMapping("/publicar")
    public String publicarTelefono(@ModelAttribute Celular celular,
                                  @RequestParam("imagen") MultipartFile imagen,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (celular.getNombre() == null || celular.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del teléfono es obligatorio.");
            }
            if (celular.getPrecio() < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }
            if (celular.getEstado() == null || celular.getEstado().trim().isEmpty()) {
                throw new IllegalArgumentException("El estado del teléfono es obligatorio.");
            }

            if (authentication == null || !authentication.isAuthenticated() || !hasRequiredRole(authentication)) {
                throw new IllegalStateException("Solo los vendedores o administradores pueden publicar teléfonos.");
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            celular.setVendedorId(usuario.getId()); // Asignar el ID del usuario autenticado (vendedor o admin)

            if (!imagen.isEmpty()) {
                String imagenBase64 = Base64.getEncoder().encodeToString(imagen.getBytes());
                celular.setImagenBase64(imagenBase64);
            }

            ventaService.publicarTelefono(celular);
            redirectAttributes.addFlashAttribute("mensaje", "Teléfono publicado correctamente.");
            return "redirect:/ventas/listar";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar la imagen: " + e.getMessage());
            return "redirect:/ventas/publicar";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/publicar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al publicar el teléfono: " + e.getMessage());
            return "redirect:/ventas/publicar";
        }
    }

    @GetMapping("/listar")
    public String listarTelefonos(Model model, Authentication authentication) {
        try {
            List<Celular> telefonos = ventaService.obtenerTelefonosEnVenta();
            model.addAttribute("telefonos", telefonos != null ? telefonos : new ArrayList<>());

            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Usuario) {
                Usuario usuario = (Usuario) authentication.getPrincipal();
                Set<String> roles = usuario.getRoles();
                model.addAttribute("userRoles", roles);
            } else {
                model.addAttribute("userRoles", new ArrayList<>());
            }

            return "listarTelefonos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al listar los teléfonos: " + e.getMessage());
            return "listarTelefonos";
        }
    }

    @GetMapping("/buscar")
    public String buscarTelefonos(@RequestParam(required = false) String nombre,
                                 @RequestParam(required = false) String estado,
                                 @RequestParam(required = false) Double precioMin,
                                 @RequestParam(required = false) Double precioMax,
                                 Model model) {
        try {
            List<Celular> telefonos = ventaService.buscarTelefonos(nombre, estado, precioMin, precioMax);
            model.addAttribute("telefonos", telefonos != null ? telefonos : new ArrayList<>());
            model.addAttribute("nombre", nombre);
            model.addAttribute("estado", estado);
            model.addAttribute("precioMin", precioMin);
            model.addAttribute("precioMax", precioMax);
            return "listarTelefonos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al buscar los teléfonos: " + e.getMessage());
            model.addAttribute("telefonos", new ArrayList<>());
            return "listarTelefonos";
        }
    }

    @GetMapping("/detalle/{id}")
    public String mostrarDetalleTelefono(@PathVariable String id, Model model, Authentication authentication) {
        try {
            Celular telefono = ventaService.obtenerTelefonoPorId(id);
            if (telefono == null) {
                model.addAttribute("error", "El teléfono no existe.");
                return "redirect:/ventas/listar";
            }
            model.addAttribute("telefono", telefono);
            List<Celular> otrosTelefonos = ventaService.obtenerTelefonosEnVenta();
            model.addAttribute("otrosTelefonos", otrosTelefonos != null ? otrosTelefonos : new ArrayList<>());

            String nombreVendedor = usuarioService.obtenerNombreUsuarioPorId(telefono.getVendedorId());
            model.addAttribute("nombreVendedor", nombreVendedor);

            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Usuario) {
                Usuario usuario = (Usuario) authentication.getPrincipal();
                Set<String> roles = usuario.getRoles();
                model.addAttribute("userRoles", roles);
                model.addAttribute("userId", usuario.getId());
            } else {
                model.addAttribute("userRoles", new ArrayList<>());
                model.addAttribute("userId", null);
            }

            return "detalleTelefono";
        } catch (Exception e) {
            model.addAttribute("error", "Error al mostrar los detalles del teléfono: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    @PostMapping("/comprar/{id}")
    public String iniciarCompra(@PathVariable String id, Authentication authentication, RedirectAttributes redirectAttributes, Model model) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Debes estar autenticado para realizar una compra.");
            }
            Celular celular = ventaService.obtenerTelefonoPorId(id);
            if (celular == null || !celular.getEstadoVenta().equals("disponible")) {
                throw new IllegalStateException("El teléfono no está disponible para la compra.");
            }

            Usuario comprador = (Usuario) authentication.getPrincipal();
            if (comprador.getId().equals(celular.getVendedorId())) {
                throw new IllegalStateException("No puedes comprar tu propio teléfono.");
            }

            SolicitudCompra solicitud = new SolicitudCompra();
            solicitud.setCelularId(celular.getId());
            solicitud.setNombreCelular(celular.getNombre());
            solicitud.setCompradorId(comprador.getId());
            solicitud.setNombreComprador(usuarioService.obtenerNombreUsuarioPorId(comprador.getId()));
            solicitud.setVendedorId(celular.getVendedorId());

            model.addAttribute("solicitud", solicitud);
            model.addAttribute("celularId", id);
            return "formularioCompra";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/detalle/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al iniciar la compra: " + e.getMessage());
            return "redirect:/ventas/detalle/" + id;
        }
    }

    @PostMapping("/procesar-compra")
    public String procesarCompra(@ModelAttribute SolicitudCompra solicitud, 
                                @RequestParam String celularId,
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Debes estar autenticado para procesar la compra.");
            }
            Usuario comprador = (Usuario) authentication.getPrincipal();
            Celular celular = ventaService.obtenerTelefonoPorId(celularId);
            if (celular == null || !celular.getEstadoVenta().equals("disponible")) {
                throw new IllegalStateException("El teléfono no está disponible para la compra.");
            }

            if (solicitud.getDireccionComprador() == null || solicitud.getDireccionComprador().trim().isEmpty()) {
                throw new IllegalArgumentException("La dirección es obligatoria.");
            }
            if (solicitud.getCorreoComprador() == null || !solicitud.getCorreoComprador().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new IllegalArgumentException("El correo electrónico no es válido.");
            }
            if (solicitud.getNumeroContactoComprador() == null || !solicitud.getNumeroContactoComprador().matches("\\d{10}")) {
                throw new IllegalArgumentException("El número de contacto debe tener 10 dígitos.");
            }

            solicitud.setCompradorId(comprador.getId());
            solicitud.setNombreComprador(usuarioService.obtenerNombreUsuarioPorId(comprador.getId()));
            solicitud.setVendedorId(celular.getVendedorId()); // Asegurar que se usa el vendedorId del celular
            solicitud.setCelularId(celularId);
            solicitud.setNombreCelular(celular.getNombre());

            ventaService.crearSolicitudCompra(solicitud);
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud de compra enviada correctamente. El vendedor revisará tu solicitud.");
            return "redirect:/ventas/confirmacion-compra";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/comprar/" + celularId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al procesar la compra: " + e.getMessage());
            return "redirect:/ventas/comprar/" + celularId;
        }
    }

    @GetMapping("/confirmacion-compra")
    public String mostrarConfirmacionCompra(Model model) {
        return "confirmacionCompra";
    }

    @GetMapping("/gestionar-solicitudes")
    public String gestionarSolicitudes(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes estar autenticado para gestionar solicitudes.");
                return "redirect:/ventas/listar";
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            List<SolicitudCompra> solicitudes = ventaService.obtenerSolicitudesPorVendedor(usuario.getId());
            model.addAttribute("solicitudes", solicitudes != null ? solicitudes : new ArrayList<>());
            return "gestionarSolicitudes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar las solicitudes: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    @PostMapping("/gestionar-solicitud/{id}")
    public String gestionarSolicitud(@PathVariable String id, 
                                    @RequestParam String accion, 
                                    @RequestParam(required = false) String descripcionVendedor, 
                                    Authentication authentication, 
                                    RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Debes estar autenticado para gestionar solicitudes.");
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            SolicitudCompra solicitud = ventaService.obtenerSolicitudPorId(id);
            if (solicitud == null) {
                throw new IllegalArgumentException("La solicitud no existe.");
            }
            if (!usuario.getId().equals(solicitud.getVendedorId())) {
                throw new IllegalStateException("Solo el vendedor asignado puede gestionar esta solicitud.");
            }

            ventaService.gestionarSolicitudCompra(id, accion, descripcionVendedor);
            redirectAttributes.addFlashAttribute("mensaje", "Solicitud " + (accion.equals("autorizar") ? "autorizada" : "rechazada") + " correctamente.");
            return "redirect:/ventas/gestionar-solicitudes";
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/gestionar-solicitudes";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al gestionar la solicitud: " + e.getMessage());
            return "redirect:/ventas/gestionar-solicitudes";
        }
    }

    @GetMapping("/historial-compras")
    public String mostrarHistorialCompras(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes estar autenticado para ver tu historial de compras.");
                return "redirect:/ventas/listar";
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            List<SolicitudCompra> historial = ventaService.obtenerHistorialCompras(usuario.getId());
            model.addAttribute("historial", historial != null ? historial : new ArrayList<>());
            return "historialCompras";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el historial de compras: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    @GetMapping("/historial-ventas")
    public String mostrarHistorialVentas(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes estar autenticado para ver tu historial de ventas.");
                return "redirect:/ventas/listar";
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            List<SolicitudCompra> historial = ventaService.obtenerHistorialVentas(usuario.getId());
            model.addAttribute("historial", historial != null ? historial : new ArrayList<>());
            return "historialVentas";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el historial de ventas: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    @GetMapping("/historial-solicitudes-vendedor")
    public String mostrarHistorialSolicitudesVendedor(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes estar autenticado para ver tu historial de solicitudes de vendedor.");
                return "redirect:/ventas/listar";
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            List<SolicitudVendedor> historial = adminService.obtenerHistorialSolicitudesVendedor(usuario.getId());
            model.addAttribute("solicitudes", historial != null ? historial : new ArrayList<>());
            return "historialSolicitudesVendedor";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar el historial de solicitudes: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    @GetMapping("/notificaciones")
    public String mostrarNotificaciones(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes estar autenticado para ver tus notificaciones.");
                return "redirect:/ventas/listar";
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            List<Notificacion> notificaciones = adminService.obtenerNotificacionesPorUsuario(usuario.getId());
            model.addAttribute("notificaciones", notificaciones != null ? notificaciones : new ArrayList<>());
            return "notificaciones";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar las notificaciones: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    @GetMapping("/comparar/{id}")
    public String compararConOtroTelefono(@PathVariable String id,
                                         @RequestParam String idOtroTelefono,
                                         Model model) {
        try {
            if (id.equals(idOtroTelefono)) {
                model.addAttribute("error", "No puedes comparar un teléfono consigo mismo.");
                return "redirect:/ventas/detalle/" + id;
            }
            List<String> resultados = ventaService.compararConOtroTelefono(id, idOtroTelefono);
            Celular telefono1 = ventaService.obtenerTelefonoPorId(id);
            Celular telefono2 = ventaService.obtenerTelefonoPorId(idOtroTelefono);
            model.addAttribute("telefono1", telefono1);
            model.addAttribute("telefono2", telefono2);
            model.addAttribute("resultados", resultados != null ? resultados : new ArrayList<>());
            return "resultadoComparacion";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/ventas/detalle/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error al comparar los teléfonos: " + e.getMessage());
            return "redirect:/ventas/detalle/" + id;
        }
    }

    @GetMapping("/comparar-media/{id}")
    public String compararConMedia(@PathVariable String id, Model model) {
        try {
            List<String> resultados = ventaService.compararConMedia(id);
            Celular telefono = ventaService.obtenerTelefonoPorId(id);
            model.addAttribute("telefono", telefono);
            model.addAttribute("resultados", resultados != null ? resultados : new ArrayList<>());
            return "resultadomedia";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/ventas/detalle/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error al comparar con la media: " + e.getMessage());
            return "redirect:/ventas/detalle/" + id;
        }
    }

    private boolean hasRequiredRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Usuario usuario = (Usuario) authentication.getPrincipal();
        Set<String> roles = usuario.getRoles();
        return roles.contains("ROLE_VENDEDOR") || roles.contains("ROLE_ADMIN");
    }
}