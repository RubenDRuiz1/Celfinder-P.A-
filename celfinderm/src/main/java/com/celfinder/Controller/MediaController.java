package com.celfinder.Controller;

import com.celfinder.Model.Celular;
import com.celfinder.Model.Celularmedia;
import com.celfinder.Procesos.ComparadorMedia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/compararmedia")
public class MediaController {

    private final ComparadorMedia comparadorMedia;
    private final Celularmedia celularMedia;

    @Autowired
    public MediaController(ComparadorMedia comparadorMedia) {
        this.comparadorMedia = comparadorMedia;
        this.celularMedia = new Celularmedia(); // Inicializar con valores predeterminados
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        return "comparadormedia"; // Retorna la vista del formulario
    }

    @PostMapping("/registrar")
    public String registrarCelular(@RequestParam String nombre,
                                   @RequestParam String nombreCpu,
                                   @RequestParam double ghz,
                                   @RequestParam int camara,
                                   @RequestParam int ram,
                                   @RequestParam int bateria,
                                   @RequestParam int almacenamiento,
                                   Model model) {
        // Crear el celular
        Celular celular = new Celular();
        celular.setNombre(nombre);
        celular.setNombreCpu(nombreCpu);
        celular.setGhz(ghz);
        celular.setCamara(camara);
        celular.setRam(ram);
        celular.setBateria(bateria);
        celular.setAlmacenamiento(almacenamiento);

        // Comparar con Celularmedia
        List<String> mensajes = new ArrayList<>();
        compararConMedia(celular, celularMedia, mensajes);

        // Guardar el celular
        comparadorMedia.guardarCelular(celular);

        // Agregar resultados al modelo
        model.addAttribute("mensajes", mensajes);
        model.addAttribute("mensaje", mensajes.isEmpty() ? "Celular registrado correctamente." : null);

        return "resultadomedia"; // Retorna la vista de resultados
    }

    private void compararConMedia(Celular celular, Celularmedia celularMedia, List<String> mensajes) {
        compararCaracteristica("Frecuencia (GHz)", celular.getGhz(), celularMedia.getGhz(), mensajes);
        compararCaracteristica("Cámaras", celular.getCamara(), celularMedia.getCamara(), mensajes);
        compararCaracteristica("RAM", celular.getRam(), celularMedia.getRam(), mensajes);
        compararCaracteristica("Batería", celular.getBateria(), celularMedia.getBateria(), mensajes);
        compararCaracteristica("Almacenamiento", celular.getAlmacenamiento(), celularMedia.getAlmacenamiento(), mensajes);
    }

    private void compararCaracteristica(String nombre, double valorCelular, double valorMedia, List<String> mensajes) {
        if (valorCelular < valorMedia) {
            mensajes.add(nombre + ": Esta característica es inferior a la media actual.");
        } else if (valorCelular == valorMedia) {
            mensajes.add(nombre + ": Se encuentra en la media actual del mercado.");
        } else {
            mensajes.add(nombre + ": Está por encima de la media actual del mercado.");
        }
    }

    @GetMapping("/listar")
    public String listarCelulares(Model model) {
        List<Celular> celulares = comparadorMedia.obtenerCelulares();
        model.addAttribute("celulares", celulares); // Agregar la lista de celulares al modelo
        return "listarCelulares"; // Retorna la vista para listar celulares
    }
}