package com.biblioteca.service;

import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.RepoPrestamo;
import com.biblioteca.repository.RepoUsuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.biblioteca.model.UserActivityDTO;

@Service
public class ServiUsuario {

    @Autowired
    private RepoUsuario repoUsuario;

    @Autowired
    private RepoPrestamo repoPrestamo;

    @Autowired
    private ServiPrestamo serviPrestamo;

    // Existing methods
    public Page<Usuario> getAllUsuarios(Pageable pageable) {
        return repoUsuario.findAll(pageable);
    }

    public Usuario getUsuarioById(Long id) {
        return repoUsuario.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public Usuario createUsuario(Usuario usuario) {
        return repoUsuario.save(usuario);
    }

    public Usuario updateUsuario(Usuario usuario) {
        if (!repoUsuario.existsById(usuario.getId())) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return repoUsuario.save(usuario);
    }

    @Transactional
    public void deleteUsuario(Long id) {
        boolean hasActiveLoans = repoPrestamo.existsByUserIdAndReturnDateIsNull(id);
        if (hasActiveLoans) {
            throw new IllegalStateException("Cannot delete user with active loans");
        }
        if (!repoUsuario.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        repoUsuario.deleteById(id);
    }

    // New methods
    public Page<PrestamoDTO> getLoansByUserIdFromDate(Long userId, LocalDate desde, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return serviPrestamo.getLoansByUserIdFromDate(userId, desde, pageable)
                .map(this::convertToPrestamoDTO);
    }

    public List<PrestamoDTO> getLoanHistory(Long userId) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return serviPrestamo.getLoanHistory(userId).stream()
                .map(this::convertToPrestamoDTO)
                .collect(Collectors.toList());
    }

    public UserActivityDTO getUserActivitySummary(Long userId) {
        Usuario usuario = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    
        List<PrestamoDTO> activeLoans = serviPrestamo.getLoansByUserIdFromDate(userId, null, Pageable.unpaged())
                .getContent() // Convert Page<Prestamo> to List<Prestamo>
                .stream()
                .map(this::convertToPrestamoDTO)
                .collect(Collectors.toList());
    
        List<PrestamoDTO> recentLoans = serviPrestamo.getLoanHistory(userId).stream()
                .map(this::convertToPrestamoDTO)
                .collect(Collectors.toList());
    
        return new UserActivityDTO(usuario, activeLoans, recentLoans);
    }

    private PrestamoDTO convertToPrestamoDTO(Prestamo prestamo) {
        PrestamoDTO dto = new PrestamoDTO();
        dto.setUserId(prestamo.getUsuario() != null ? prestamo.getUsuario().getId() : null);
        dto.setBookId(prestamo.getLibro() != null ? prestamo.getLibro().getId() : null);
        dto.setLoanDate(prestamo.getLoanDate());
        dto.setDueDate(prestamo.getDueDate());
        dto.setReturnDate(prestamo.getReturnDate());
        dto.setPenaltyUntil(prestamo.getPenaltyUntil());
        return dto;
    }
}