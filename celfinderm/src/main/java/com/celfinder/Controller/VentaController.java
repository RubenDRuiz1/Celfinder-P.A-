package com.celfinder.Controller;

import com.celfinder.Model.Celular;
import com.celfinder.Model.TelefonoVenta;
import com.celfinder.Model.Usuario;
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

    @Autowired
    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Muestra el formulario para publicar un nuevo teléfono.
     */
    @GetMapping("/publicar")
    public String mostrarFormularioPublicacion(Model model) {
        TelefonoVenta telefono = new TelefonoVenta();
        telefono.setEspecificaciones(new Celular());
        model.addAttribute("telefono", telefono);
        return "publicarTelefono";
    }

    /**
     * Procesa la publicación de un nuevo teléfono, incluyendo la imagen en Base64.
     */
    @PostMapping("/publicar")
    public String publicarTelefono(@ModelAttribute TelefonoVenta telefono,
                                   @RequestParam("imagen") MultipartFile imagen,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Validaciones básicas
            if (telefono.getNombre() == null || telefono.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del teléfono es obligatorio.");
            }
            if (telefono.getPrecio() < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }
            if (telefono.getEstado() == null || telefono.getEstado().trim().isEmpty()) {
                throw new IllegalArgumentException("El estado del teléfono es obligatorio.");
            }

            // Obtener el ID del usuario autenticado
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Debes estar autenticado para publicar un teléfono.");
            }
            Usuario usuario = (Usuario) authentication.getPrincipal();
            telefono.setVendedorId(usuario.getId());

            // Procesar la imagen si se proporcionó
            if (!imagen.isEmpty()) {
                String imagenBase64 = Base64.getEncoder().encodeToString(imagen.getBytes());
                telefono.setImagenBase64(imagenBase64);
            }

            ventaService.publicarTelefono(telefono);
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

    /**
     * Lista todos los teléfonos disponibles para la venta.
     */
    @GetMapping("/listar")
    public String listarTelefonos(Model model, Authentication authentication) {
        try {
            List<TelefonoVenta> telefonos = ventaService.obtenerTelefonosEnVenta();
            model.addAttribute("telefonos", telefonos != null ? telefonos : new ArrayList<>());

            // Añadir los roles del usuario autenticado al modelo
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

    /**
     * Busca teléfonos según los filtros proporcionados (nombre, estado, precio).
     */
    @GetMapping("/buscar")
    public String buscarTelefonos(@RequestParam(required = false) String nombre,
                                  @RequestParam(required = false) String estado,
                                  @RequestParam(required = false) Double precioMin,
                                  @RequestParam(required = false) Double precioMax,
                                  Model model) {
        try {
            List<TelefonoVenta> telefonos = ventaService.buscarTelefonos(nombre, estado, precioMin, precioMax);
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

    /**
     * Muestra los detalles de un teléfono específico.
     */
    @GetMapping("/detalle/{id}")
    public String mostrarDetalleTelefono(@PathVariable String id, Model model, Authentication authentication) {
        try {
            TelefonoVenta telefono = ventaService.obtenerTelefonoPorId(id);
            if (telefono == null) {
                model.addAttribute("error", "El teléfono no existe.");
                return "redirect:/ventas/listar";
            }
            model.addAttribute("telefono", telefono);
            List<TelefonoVenta> otrosTelefonos = ventaService.obtenerTelefonosEnVenta();
            model.addAttribute("otrosTelefonos", otrosTelefonos != null ? otrosTelefonos : new ArrayList<>());

            // Añadir los roles del usuario autenticado al modelo
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Usuario) {
                Usuario usuario = (Usuario) authentication.getPrincipal();
                Set<String> roles = usuario.getRoles();
                model.addAttribute("userRoles", roles);
            } else {
                model.addAttribute("userRoles", new ArrayList<>());
            }

            return "detalleTelefono";
        } catch (Exception e) {
            model.addAttribute("error", "Error al mostrar los detalles del teléfono: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    /**
     * Procesa la compra de un teléfono.
     */
    @PostMapping("/comprar/{id}")
    public String comprarTelefono(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            ventaService.comprarTelefono(id);
            redirectAttributes.addFlashAttribute("mensaje", "Compra realizada con éxito.");
            return "redirect:/ventas/listar";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/detalle/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al realizar la compra: " + e.getMessage());
            return "redirect:/ventas/detalle/" + id;
        }
    }

    /**
     * Elimina un teléfono de la base de datos.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminarTelefono(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            ventaService.eliminarTelefono(id);
            redirectAttributes.addFlashAttribute("mensaje", "Teléfono eliminado correctamente.");
            return "redirect:/ventas/listar";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el teléfono: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    /**
     * Compara dos teléfonos y muestra los resultados.
     */
    @GetMapping("/comparar/{id}")
    public String compararConOtroTelefono(@PathVariable String id,
                                          @RequestParam String idOtroTelefono,
                                          Model model) {
        try {
            // Validar que no se compare el teléfono consigo mismo
            if (id.equals(idOtroTelefono)) {
                model.addAttribute("error", "No puedes comparar un teléfono consigo mismo.");
                return "redirect:/ventas/detalle/" + id;
            }
            List<String> resultados = ventaService.compararConOtroTelefono(id, idOtroTelefono);
            TelefonoVenta telefono1 = ventaService.obtenerTelefonoPorId(id);
            TelefonoVenta telefono2 = ventaService.obtenerTelefonoPorId(idOtroTelefono);
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

    /**
     * Compara un teléfono con la media del mercado.
     */
    @GetMapping("/comparar-media/{id}")
    public String compararConMedia(@PathVariable String id, Model model) {
        try {
            List<String> resultados = ventaService.compararConMedia(id);
            TelefonoVenta telefono = ventaService.obtenerTelefonoPorId(id);
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
}