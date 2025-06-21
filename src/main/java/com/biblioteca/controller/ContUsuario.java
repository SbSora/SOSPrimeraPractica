package com.biblioteca.controller;

import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.UsuarioDTO;
import com.biblioteca.service.ServiUsuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import com.biblioteca.model.UserActivityDTO;

import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/usuarios")
public class ContUsuario {

    @Autowired
    private ServiUsuario serviUsuario;

    // Listar todos los usuarios
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<UsuarioDTO>>> listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) {
            return ResponseEntity.badRequest().body(null);
        }
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(serviUsuario.getAllUsuarios(pageable));
    }

    // Obtener un usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioDTO>> obtenerUsuario(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(serviUsuario.getUsuarioById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // Crear un nuevo usuario
    @PostMapping
    public ResponseEntity<EntityModel<UsuarioDTO>> crearUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        EntityModel<UsuarioDTO> resource = serviUsuario.createUsuario(usuarioDTO);
        UsuarioDTO content = resource.getContent();
        if (content == null || content.getId() == null) {
            throw new ResourceNotFoundException("Resource content or ID is null");
        }
        return ResponseEntity.created(linkTo(methodOn(ContUsuario.class).obtenerUsuario(content.getId())).toUri()).body(resource);
    }

    // Actualizar un usuario por ID
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioDTO>> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            usuarioDTO.setId(id);
            return ResponseEntity.ok(serviUsuario.updateUsuario(usuarioDTO));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // Eliminar un usuario por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id) {
        try {
            serviUsuario.deleteUsuario(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // Obtener préstamos de un usuario
    @GetMapping("/{id}/prestamos")
    public ResponseEntity<PagedModel<EntityModel<PrestamoDTO>>> obtenerPrestamosUsuario(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            Pageable pageable) {
        try {
            PagedModel<EntityModel<PrestamoDTO>> prestamos = serviUsuario.getLoansByUserIdFromDate(id, desde, pageable);
            return ResponseEntity.ok(prestamos);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // Obtener historial de préstamos de un usuario
    @GetMapping("/{id}/historial")
    public ResponseEntity<CollectionModel<EntityModel<PrestamoDTO>>> obtenerHistorialPrestamos(@PathVariable Long id) {
        try {
            CollectionModel<EntityModel<PrestamoDTO>> historial = serviUsuario.getLoanHistory(id);
            return ResponseEntity.ok(historial);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // Obtener resumen de actividad de un usuario
    @GetMapping("/{id}/actividad")
    public ResponseEntity<EntityModel<UserActivityDTO>> obtenerResumenActividad(@PathVariable Long id) {
        try {
            EntityModel<UserActivityDTO> actividad = serviUsuario.getUserActivitySummary(id);
            return ResponseEntity.ok(actividad);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}