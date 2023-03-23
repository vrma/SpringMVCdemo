package com.example.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.entities.Estudiante;
import com.example.entities.Facultad;
import com.example.entities.Telefono;
import com.example.services.EstudianteService;
import com.example.services.FacultadService;
import com.example.services.TelefonoService;

@Controller
@RequestMapping("/")
public class MainController {

    private static final Logger LOG = Logger.getLogger("MainController");

    @Autowired
    private EstudianteService estudianteService;

    @Autowired
    private FacultadService facultadService;

    @Autowired
    private TelefonoService telefonoService;

    /**
     * El metodo siguiente devuelve un listado de estudiantes
     */
    @GetMapping("/listar")
    public ModelAndView listar() {

        List<Estudiante> estudiantes = estudianteService.findAll();

        ModelAndView mav = new ModelAndView("views/listarEstudiantes");
        mav.addObject("estudiantes", estudiantes);

        return mav;
    }

    /**
     * Muestra el formulario de alta de estudiante
     */
    @GetMapping("/frmAltaEstudiante")
    public String formularioAltaEstudiante(Model model) {

        List<Facultad> facultades = facultadService.findAll();

        Estudiante estudiante = new Estudiante();

        model.addAttribute("estudiante", estudiante);
        model.addAttribute("facultades", facultades);

        return "views/formularioAltaEstudiante";
    }

    /**
     * Metodo que recibe los datos procedente de los controles del formulario
     */

    @PostMapping("/altaModificacionEstudiante")
    public String altaEstudiante(@ModelAttribute Estudiante estudiante,
            @RequestParam(name = "numerosTelefonos") String telefonosRecibidos) {

        LOG.info("Telefonos recibidos: " + telefonosRecibidos);

        estudianteService.save(estudiante);

        List<String> listadoNumerosTelefonos = null;

        if (telefonosRecibidos != null) {
            String[] arrayTelefonos = telefonosRecibidos.split(";");
            listadoNumerosTelefonos = Arrays.asList(arrayTelefonos);
        }

        if (listadoNumerosTelefonos != null) {
            telefonoService.deleteByEstudiante(estudiante);
            listadoNumerosTelefonos.stream().forEach(n -> {
                Telefono telefonoObject = Telefono
                        .builder()
                        .numero(n)
                        .estudiante(estudiante)
                        .build();

                telefonoService.save(telefonoObject);
            });
        }

        return "redirect:/listar";
    }

    /**
     * Muestra el formulario para Actualizar un Estudiante
     */
    @GetMapping("/frmActualizar/{id}")
    public String frmActualizarEstudiante(@PathVariable(name = "id") int idEstudiante,
            Model model) {

        Estudiante estudiante = estudianteService.findById(idEstudiante);
        List<Telefono> todosTelefonos = telefonoService.findAll();
        List<Telefono> telefonosDelEstudiante = todosTelefonos.stream()
                .filter(telefono -> telefono.getEstudiante().getId() == idEstudiante)
                .collect(Collectors.toList());
        String numerosDeTelefono = telefonosDelEstudiante.stream()
                .map(telefono -> telefono.getNumero())
                .collect(Collectors.joining(";"));
        List<Facultad> facultades = facultadService.findAll();
        model.addAttribute("estudiante", estudiante);
        model.addAttribute("telefonos", numerosDeTelefono);
        model.addAttribute("facultades", facultades);

        return "views/formularioAltaEstudiante";
    }

    @GetMapping("/borrar/{id}")
    public String borrarEstudiante(@PathVariable(name = "id") int idEstudiante) {

        estudianteService.delete(estudianteService.findById(idEstudiante));

        return "redirect:/listar";
    }

    @GetMapping("/detalles/{id}")
    public String estudianteDetails(@PathVariable(name = "id") int id, Model model) {

        Estudiante estudiante = estudianteService.findById(id);
        List<Telefono> telefonos = telefonoService.findByEstudiante(estudiante);

        List<String> numerosTelefono = telefonos.stream()
               .map(t -> t.getNumero())
               .toList();
               
        model.addAttribute("telefonos", numerosTelefono);
        model.addAttribute("estudiante", estudiante);

        return "views/estudianteDetalles";
    }
}
