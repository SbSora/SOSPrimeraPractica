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

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<UsuarioDTO>>> getAllUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) {
            return ResponseEntity.badRequest().body(null);
        }
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(serviUsuario.getAllUsuarios(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioDTO>> getUsuarioById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(serviUsuario.getUsuarioById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<EntityModel<UsuarioDTO>> createUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        EntityModel<UsuarioDTO> resource = serviUsuario.createUsuario(usuarioDTO);
        return ResponseEntity.created(linkTo(methodOn(ContUsuario.class).getUsuarioById(resource.getContent().getId())).toUri()).body(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioDTO>> updateUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioDTO usuarioDTO) {
        try {
            usuarioDTO.setId(id);
            return ResponseEntity.ok(serviUsuario.updateUsuario(usuarioDTO));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUsuario(@PathVariable Long id) {
        try {
            serviUsuario.deleteUsuario(id);
            return ResponseEntity.ok("Usuario eliminado satisfactoriamente");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/prestamos")
    public ResponseEntity<PagedModel<EntityModel<PrestamoDTO>>> getUserLoans(
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

    @GetMapping("/{id}/historial")
    public ResponseEntity<CollectionModel<EntityModel<PrestamoDTO>>> getLoanHistory(@PathVariable Long id) {
        try {
            CollectionModel<EntityModel<PrestamoDTO>> historial = serviUsuario.getLoanHistory(id);
            return ResponseEntity.ok(historial);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @GetMapping("/{id}/actividad")
    public ResponseEntity<EntityModel<UserActivityDTO>> getUserActivity(@PathVariable Long id) {
        try {
            EntityModel<UserActivityDTO> actividad = serviUsuario.getUserActivitySummary(id);
            return ResponseEntity.ok(actividad);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}