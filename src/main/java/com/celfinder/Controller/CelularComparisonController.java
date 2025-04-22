package com.celfinder.Controller;

import com.celfinder.Model.Celular;
import com.celfinder.Procesos.ComparadorCelular;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/comparar")
public class CelularComparisonController {

    private final ComparadorCelular comparadorCelulares;

    @Autowired
    public CelularComparisonController(ComparadorCelular comparadorCelulares) {
        this.comparadorCelulares = comparadorCelulares;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        return "registrocelular";
    }

    @PostMapping("/registrar")
    public String registrarCelulares(@RequestParam String nombre,
                                    @RequestParam double ghz,
                                    @RequestParam int camara,
                                    @RequestParam int ram,
                                    @RequestParam int bateria,
                                    @RequestParam int almacenamiento,
                                    @RequestParam String nombreCpu,
                                    @RequestParam String nombre2,
                                    @RequestParam double ghz2,
                                    @RequestParam int camara2,
                                    @RequestParam int ram2,
                                    @RequestParam int bateria2,
                                    @RequestParam int almacenamiento2,
                                    @RequestParam String nombreCpu2,
                                    Model model) {
        // Crear el primer celular
        Celular celular1 = new Celular();
        celular1.setNombre(nombre);
        celular1.setGhz(ghz);
        celular1.setCamara(camara);
        celular1.setRam(ram);
        celular1.setBateria(bateria);
        celular1.setAlmacenamiento(almacenamiento);
        celular1.setNombreCpu(nombreCpu);

        // Crear el segundo celular
        Celular celular2 = new Celular();
        celular2.setNombre(nombre2);
        celular2.setGhz(ghz2);
        celular2.setCamara(camara2);
        celular2.setRam(ram2);
        celular2.setBateria(bateria2);
        celular2.setAlmacenamiento(almacenamiento2);
        celular2.setNombreCpu(nombreCpu2);

        // Registrar los celulares
        comparadorCelulares.registrarCelulares(celular1, celular2);

        // Verificar resultados y agregar al modelo
        List<String> resultados = comparadorCelulares.getResultadosComparacion();
        if (resultados.isEmpty()) {
            model.addAttribute("mensaje", "No se encontraron resultados de comparación.");
        } else {
            model.addAttribute("resultados", resultados);
            model.addAttribute("mensaje", "Los celulares se han registrado correctamente.");
        }

        return "resultadoComparacion";
    }

    @GetMapping("/listar")
    public String listarCelulares(Model model) {
        List<Celular> celulares = comparadorCelulares.obtenerCelulares();
        model.addAttribute("celulares", celulares);
        return "listarCelulares";
    }
}