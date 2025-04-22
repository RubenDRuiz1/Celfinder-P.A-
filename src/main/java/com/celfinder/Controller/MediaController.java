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
        return "comparadormedia";
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

        // Registrar el celular y comparar con la media
        comparadorMedia.registrarCelular(celular);

        // Agregar resultados al modelo
        List<String> resultados = comparadorMedia.getResultadosComparacion();
        model.addAttribute("mensajes", resultados);
        model.addAttribute("mensaje", resultados.isEmpty() ? "Celular registrado correctamente." : null);

        return "resultadomedia";
    }

    @GetMapping("/listar")
    public String listarCelulares(Model model) {
        List<Celular> celulares = comparadorMedia.obtenerCelulares();
        model.addAttribute("celulares", celulares);
        return "listarCelulares";
    }
}