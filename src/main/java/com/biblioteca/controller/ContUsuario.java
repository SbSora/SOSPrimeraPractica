package com.biblioteca.controller;

import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.UsuarioDTO;
import com.biblioteca.repository.RepoUsuario;
import com.biblioteca.service.ServiUsuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import com.biblioteca.model.UserActivityDTO;

@RestController
@RequestMapping("/usuarios")
public class ContUsuario {

    @Autowired
    private ServiUsuario serviUsuario;

    @Autowired
    private RepoUsuario repoUsuario; // Add this line

    // Existing endpoints (from previous test cases)
    @GetMapping
    public Page<UsuarioDTO> getAllUsuarios(Pageable pageable) {
    return repoUsuario.findAll(pageable).map(this::convertToUsuarioDTO);
}

private UsuarioDTO convertToUsuarioDTO(Usuario usuario) {
    UsuarioDTO dto = new UsuarioDTO();
    dto.setUsername(usuario.getUsername());
    dto.setRegistrationNumber(usuario.getRegistrationNumber());
    dto.setBirthDate(usuario.getBirthDate());
    dto.setEmail(usuario.getEmail());
    return dto;
}
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable Long id) {
        return ResponseEntity.ok(serviUsuario.getUsuarioById(id));
    }

    @PostMapping
    public ResponseEntity<Usuario> createUsuario(@RequestBody Usuario usuario) {
        return ResponseEntity.status(201).body(serviUsuario.createUsuario(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> updateUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
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

    // New endpoints
    @GetMapping("/{id}/prestamos")
    public ResponseEntity<Page<PrestamoDTO>> getUserLoans(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            Pageable pageable) {
        Page<PrestamoDTO> prestamos = serviUsuario.getLoansByUserIdFromDate(id, desde, pageable);
        return ResponseEntity.ok(prestamos);
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<PrestamoDTO>> getLoanHistory(@PathVariable Long id) {
        List<PrestamoDTO> historial = serviUsuario.getLoanHistory(id);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/{id}/actividad")
    public ResponseEntity<UserActivityDTO> getUserActivity(@PathVariable Long id) {
        UserActivityDTO actividad = serviUsuario.getUserActivitySummary(id);
        return ResponseEntity.ok(actividad);
    }
}