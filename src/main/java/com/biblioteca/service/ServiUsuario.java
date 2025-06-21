package com.biblioteca.service;

import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.UsuarioDTO;
import com.biblioteca.repository.RepoPrestamo;
import com.biblioteca.repository.RepoUsuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.biblioteca.model.UserActivityDTO;
import com.biblioteca.controller.ContUsuario;
import com.biblioteca.controller.ContPrestamo;
import com.biblioteca.service.ServiPrestamo;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Service
public class ServiUsuario {

    @Autowired
    private RepoUsuario repoUsuario;

    @Autowired
    private RepoPrestamo repoPrestamo;

    @Autowired
    private ServiPrestamo serviPrestamo;

    @Autowired
    private PagedResourcesAssembler<UsuarioDTO> usuarioPagedAssembler;

    @Autowired
    private PagedResourcesAssembler<PrestamoDTO> prestamoPagedAssembler;

    public PagedModel<EntityModel<UsuarioDTO>> getAllUsuarios(Pageable pageable) {
        Page<UsuarioDTO> usuarioPage = repoUsuario.findAll(pageable).map(this::convertToUsuarioDTO);
        return usuarioPagedAssembler.toModel(usuarioPage, usuarioDTO -> {
            EntityModel<UsuarioDTO> model = EntityModel.of(usuarioDTO);
            model.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(usuarioDTO.getId())).withSelfRel());
            return model;
        });
    }

    public EntityModel<UsuarioDTO> getUsuarioById(Long id) {
        Usuario usuario = repoUsuario.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        UsuarioDTO usuarioDTO = convertToUsuarioDTO(usuario);
        EntityModel<UsuarioDTO> model = EntityModel.of(usuarioDTO);
        model.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(id)).withSelfRel());
        model.add(linkTo(methodOn(ContUsuario.class).obtenerPrestamosUsuario(id, null, Pageable.unpaged())).withRel("loans"));
        model.add(linkTo(methodOn(ContUsuario.class).obtenerHistorialPrestamos(id)).withRel("history"));
        model.add(linkTo(methodOn(ContUsuario.class).obtenerResumenActividad(id)).withRel("activity"));
        model.add(linkTo(methodOn(ContUsuario.class).eliminarUsuario(id)).withRel("delete"));
        return model;
    }

    @Transactional
    public EntityModel<UsuarioDTO> createUsuario(UsuarioDTO usuarioDTO) {
        List<Long> existingIds = repoUsuario.findAllIds();
        long nextId = 1;
        if (!existingIds.isEmpty()) {
            for (Long id : existingIds) {
                if (id != nextId) {
                    break;
                }
                nextId++;
            }
        }
        Usuario usuario = new Usuario();
        usuario.setId(nextId);
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setRegistrationNumber(usuarioDTO.getRegistrationNumber());
        usuario.setBirthDate(usuarioDTO.getBirthDate());
        usuario.setEmail(usuarioDTO.getEmail());
        Usuario savedUsuario = repoUsuario.save(usuario);
        UsuarioDTO savedUsuarioDTO = convertToUsuarioDTO(savedUsuario);
        EntityModel<UsuarioDTO> model = EntityModel.of(savedUsuarioDTO);
        model.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(savedUsuarioDTO.getId())).withSelfRel());
        model.add(linkTo(methodOn(ContUsuario.class).listarUsuarios(0, 10)).withRel("users"));
        return model;
    }

    public EntityModel<UsuarioDTO> updateUsuario(UsuarioDTO usuarioDTO) {
        if (!repoUsuario.existsById(usuarioDTO.getId())) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        Usuario usuario = new Usuario();
        usuario.setId(usuarioDTO.getId());
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setRegistrationNumber(usuarioDTO.getRegistrationNumber());
        usuario.setBirthDate(usuarioDTO.getBirthDate());
        usuario.setEmail(usuarioDTO.getEmail());
        Usuario updatedUsuario = repoUsuario.save(usuario);
        UsuarioDTO updatedUsuarioDTO = convertToUsuarioDTO(updatedUsuario);
        EntityModel<UsuarioDTO> model = EntityModel.of(updatedUsuarioDTO);
        model.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(updatedUsuarioDTO.getId())).withSelfRel());
        model.add(linkTo(methodOn(ContUsuario.class).eliminarUsuario(updatedUsuarioDTO.getId())).withRel("delete"));
        return model;
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

        // Find all Prestamo records associated with the user
        List<Prestamo> prestamos = repoPrestamo.findByUsuarioId(id);
        // Set usuario to null in all associated Prestamo records
        for (Prestamo prestamo : prestamos) {
            prestamo.setUsuario(null);
            repoPrestamo.save(prestamo);
        }

        // Delete the user
        repoUsuario.deleteById(id);
    }

    public PagedModel<EntityModel<PrestamoDTO>> getLoansByUserIdFromDate(Long userId, LocalDate desde, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        Page<PrestamoDTO> prestamoPage = serviPrestamo.getLoansByUserIdFromDate(userId, desde, pageable)
                .map(this::convertToPrestamoDTO);
        if (prestamoPage.isEmpty()) {
            throw new ResourceNotFoundException("No loans found for the user from the specified date");
        }
        return prestamoPagedAssembler.toModel(prestamoPage, prestamoDTO -> {
            EntityModel<PrestamoDTO> model = EntityModel.of(prestamoDTO);
            if (prestamoDTO.getReturnDate() == null) { // Only add return/extend links for active loans
                model.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(prestamoDTO.getId())).withRel("return"));
                model.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(prestamoDTO.getId())).withRel("extend"));
            }
            return model;
        });
    }

    public CollectionModel<EntityModel<PrestamoDTO>> getLoanHistory(Long userId) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        List<EntityModel<PrestamoDTO>> prestamoModels = serviPrestamo.getLoanHistory(userId).stream()
                .map(this::convertToPrestamoDTO)
                .map(prestamoDTO -> EntityModel.of(prestamoDTO))
                .collect(Collectors.toList());
        CollectionModel<EntityModel<PrestamoDTO>> model = CollectionModel.of(prestamoModels);
        model.add(linkTo(methodOn(ContUsuario.class).obtenerHistorialPrestamos(userId)).withSelfRel());
        model.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(userId)).withRel("user"));
        return model;
    }

    public EntityModel<UserActivityDTO> getUserActivitySummary(Long userId) {
        Usuario usuario = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    
        List<EntityModel<PrestamoDTO>> activeLoans = serviPrestamo.getLoansByUserIdFromDate(userId, null, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::convertToPrestamoDTO)
                .map(prestamoDTO -> {
                    EntityModel<PrestamoDTO> model = EntityModel.of(prestamoDTO);
                    if (prestamoDTO.getReturnDate() == null) { // Only add return/extend links for active loans
                        model.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(prestamoDTO.getId())).withRel("return"));
                        model.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(prestamoDTO.getId())).withRel("extend"));
                    }
                    return model;
                })
                .collect(Collectors.toList());
    
        List<EntityModel<PrestamoDTO>> recentLoans = serviPrestamo.getLoanHistory(userId).stream()
                .map(this::convertToPrestamoDTO)
                .map(prestamoDTO -> EntityModel.of(prestamoDTO))
                .collect(Collectors.toList());
    
        UserActivityDTO activityDTO = new UserActivityDTO(usuario, 
                activeLoans.stream().map(EntityModel::getContent).collect(Collectors.toList()), 
                recentLoans.stream().map(EntityModel::getContent).collect(Collectors.toList()));
        EntityModel<UserActivityDTO> model = EntityModel.of(activityDTO);
        model.add(linkTo(methodOn(ContUsuario.class).obtenerResumenActividad(userId)).withSelfRel());
        model.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(userId)).withRel("user"));
        model.add(linkTo(methodOn(ContUsuario.class).obtenerPrestamosUsuario(userId, null, Pageable.unpaged())).withRel("loans"));
        return model;
    }

    private PrestamoDTO convertToPrestamoDTO(Prestamo prestamo) {
        PrestamoDTO dto = new PrestamoDTO();
        dto.setId(prestamo.getId());
        dto.setUserId(prestamo.getUsuario() != null ? prestamo.getUsuario().getId() : null);
        dto.setBookId(prestamo.getLibro() != null ? prestamo.getLibro().getId() : null);
        dto.setLoanDate(prestamo.getLoanDate());
        dto.setDueDate(prestamo.getDueDate());
        dto.setReturnDate(prestamo.getReturnDate());
        dto.setPenaltyUntil(prestamo.getPenaltyUntil());
        return dto;
    }

    private UsuarioDTO convertToUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setRegistrationNumber(usuario.getRegistrationNumber());
        dto.setBirthDate(usuario.getBirthDate());
        dto.setEmail(usuario.getEmail());
        return dto;
    }
}