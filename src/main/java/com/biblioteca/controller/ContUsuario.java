package com.biblioteca.controller;

import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.UsuarioDTO;
import com.biblioteca.service.ServiUsuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import com.biblioteca.model.UserActivityDTO;

@RestController
@RequestMapping("/usuarios")
public class ContUsuario {

    @Autowired
    private ServiUsuario serviUsuario;

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<UsuarioDTO>>> getAllUsuarios(Pageable pageable) {
        return ResponseEntity.ok(serviUsuario.getAllUsuarios(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.ok(serviUsuario.getUsuarioById(id));
    }

    @PostMapping
    public ResponseEntity<EntityModel<UsuarioDTO>> createUsuario(@RequestBody Usuario usuario) {
        return ResponseEntity.status(201).body(serviUsuario.createUsuario(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        usuario.setId(id);
        return ResponseEntity.ok(serviUsuario.updateUsuario(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUsuario(@PathVariable Long id) {
        try {
            serviUsuario.deleteUsuario(id);
            return ResponseEntity.ok("User deleted successfully");
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
        PagedModel<EntityModel<PrestamoDTO>> prestamos = serviUsuario.getLoansByUserIdFromDate(id, desde, pageable);
        return ResponseEntity.ok(prestamos);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<CollectionModel<EntityModel<PrestamoDTO>>> getLoanHistory(@PathVariable Long id) {
        CollectionModel<EntityModel<PrestamoDTO>> historial = serviUsuario.getLoanHistory(id);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/{id}/actividad")
    public ResponseEntity<EntityModel<UserActivityDTO>> getUserActivity(@PathVariable Long id) {
        EntityModel<UserActivityDTO> actividad = serviUsuario.getUserActivitySummary(id);
        return ResponseEntity.ok(actividad);
    }
}