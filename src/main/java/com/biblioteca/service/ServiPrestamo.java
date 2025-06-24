package com.biblioteca.service;

import com.biblioteca.exception.BadRequestException;
import com.biblioteca.exception.ForbiddenException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.RepoLibro;
import com.biblioteca.repository.RepoPrestamo;
import com.biblioteca.repository.RepoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import com.biblioteca.controller.ContPrestamo;
import com.biblioteca.controller.ContUsuario;
import com.biblioteca.controller.ContLibro;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Service
public class ServiPrestamo {

    private final RepoPrestamo repoPrestamo;
    private final RepoUsuario repoUsuario;
    private final RepoLibro repoLibro;
    private final PagedResourcesAssembler<PrestamoDTO> prestamoPagedAssembler;

    public ServiPrestamo(RepoPrestamo repoPrestamo, RepoUsuario repoUsuario, RepoLibro repoLibro, PagedResourcesAssembler<PrestamoDTO> prestamoPagedAssembler) {
        this.repoPrestamo = repoPrestamo;
        this.repoUsuario = repoUsuario;
        this.repoLibro = repoLibro;
        this.prestamoPagedAssembler = prestamoPagedAssembler;
    }

    public EntityModel<PrestamoDTO> obtenerPrestamo(Long id) {
        Prestamo prestamo = repoPrestamo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));
        PrestamoDTO dto = convertToPrestamoDTO(prestamo); // Change made here
        EntityModel<PrestamoDTO> recurso = EntityModel.of(dto);
        recurso.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(id)).withSelfRel());
        if (dto.getReturnDate() == null) {
            recurso.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(id)).withRel("estado"));
            recurso.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(id)).withRel("fecha"));
        }
        recurso.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(dto.getUserId())).withRel("usuario"));
        recurso.add(linkTo(methodOn(ContLibro.class).obtenerLibro(dto.getBookId())).withRel("libro"));
        return recurso;
    }

    @Transactional
    public EntityModel<PrestamoDTO> borrowBook(Long userId, Long bookId) {
        // Verificar si el usuario tiene una penalización activa
        if (repoPrestamo.hasActivePenalty(userId, LocalDate.now())) {
            throw new ForbiddenException("El usuario tiene una penalización activa y no puede realizar préstamos");
        }

        Usuario usuario = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Libro libro = repoLibro.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        Page<Prestamo> activeLoans = repoPrestamo.findByUserIdAndReturnDateIsNull(userId, Pageable.unpaged());
        if (activeLoans.getTotalElements() >= 3) {
            throw new BadRequestException("El usuario ya tiene el máximo de préstamos activos (3)");
        }

        if (!libro.isAvailable()) {
            throw new BadRequestException("El libro no está disponible");
        }

        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setLoanDate(LocalDate.now());
        prestamo.setDueDate(LocalDate.now().plusWeeks(2));
        prestamo.setReturnDate(null);
        prestamo.setPenaltyUntil(null);
        prestamo.setId(getNextAvailableId());

        libro.setAvailable(false);
        repoLibro.save(libro);
        Prestamo savedPrestamo = repoPrestamo.save(prestamo);

        PrestamoDTO dto = convertToPrestamoDTO(savedPrestamo);
        EntityModel<PrestamoDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(dto.getId())).withRel("estado"));
        resource.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(dto.getId())).withRel("fecha"));
        resource.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(dto.getUserId())).withRel("usuario"));
        resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(dto.getBookId())).withRel("libro"));
        return resource;
    }

    private Long getNextAvailableId() {
        List<Long> existingIds = repoPrestamo.findAllIds();
        long nextId = 1;
        for (Long id : existingIds) {
            if (id > nextId) {
                break;
            }
            nextId = id + 1;
        }
        return nextId;
    }

    @Transactional
    public EntityModel<PrestamoDTO> returnBook(Long loanId) {
        Prestamo prestamo = repoPrestamo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));

        if (prestamo.getReturnDate() != null) {
            throw new BadRequestException("El libro ya fue devuelto");
        }

        prestamo.setReturnDate(LocalDate.now());
        Libro libro = prestamo.getLibro();
        libro.setAvailable(true);

        if (prestamo.getDueDate().isBefore(LocalDate.now())) {
            prestamo.setPenaltyUntil(LocalDate.now().plusWeeks(1));
        }

        repoLibro.save(libro);
        Prestamo updatedPrestamo = repoPrestamo.save(prestamo);

        PrestamoDTO dto = convertToPrestamoDTO(updatedPrestamo);
        EntityModel<PrestamoDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(dto.getUserId())).withRel("usuario"));
        resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(dto.getBookId())).withRel("libro"));
        return resource;
    }

    @Transactional
    public EntityModel<PrestamoDTO> extendLoan(Long loanId) {
        Prestamo prestamo = repoPrestamo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));

        if (prestamo.getReturnDate() != null) {
            throw new BadRequestException("No se puede extender un préstamo ya devuelto");
        }

        if (prestamo.getDueDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se puede extender un préstamo vencido");
        }

        prestamo.setDueDate(prestamo.getDueDate().plusWeeks(2));
        Prestamo updatedPrestamo = repoPrestamo.save(prestamo);

        PrestamoDTO dto = convertToPrestamoDTO(updatedPrestamo);
        EntityModel<PrestamoDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(dto.getId())).withRel("estado"));
        resource.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(dto.getId())).withRel("fecha"));
        resource.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(dto.getUserId())).withRel("usuario"));
        resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(dto.getBookId())).withRel("libro"));
        return resource;
    }

    public PagedModel<EntityModel<PrestamoDTO>> listAllLoans(Pageable pageable) {
        Page<PrestamoDTO> prestamos = repoPrestamo.findAll(pageable).map(this::convertToPrestamoDTO);
        if (prestamos.isEmpty()) {
            throw new ResourceNotFoundException("No loans found");
        }
        return prestamoPagedAssembler.toModel(prestamos, prestamoDTO -> {
            EntityModel<PrestamoDTO> resource = EntityModel.of(prestamoDTO);
            resource.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(prestamoDTO.getId())).withSelfRel());
            if (prestamoDTO.getReturnDate() == null) {
                resource.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(prestamoDTO.getId())).withRel("estado"));
                resource.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(prestamoDTO.getId())).withRel("fecha"));
            }
            resource.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(prestamoDTO.getUserId())).withRel("usuario"));
            resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(prestamoDTO.getBookId())).withRel("libro"));
            return resource;
        });
    }

    public PagedModel<EntityModel<PrestamoDTO>> listCurrentLoans(Long userId, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        Page<PrestamoDTO> prestamos = repoPrestamo.findByUserIdAndReturnDateIsNull(userId, pageable).map(this::convertToPrestamoDTO);
        return prestamoPagedAssembler.toModel(prestamos, prestamoDTO -> {
            EntityModel<PrestamoDTO> resource = EntityModel.of(prestamoDTO);
            resource.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(prestamoDTO.getId())).withSelfRel());
            if (prestamoDTO.getReturnDate() == null) {
                resource.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(prestamoDTO.getId())).withRel("estado"));
                resource.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(prestamoDTO.getId())).withRel("fecha"));
            }
            resource.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(prestamoDTO.getUserId())).withRel("usuario"));
            resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(prestamoDTO.getBookId())).withRel("libro"));
            return resource;
        });
    }

    public PagedModel<EntityModel<PrestamoDTO>> listHistoricalLoans(Long userId, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        Page<PrestamoDTO> prestamos = repoPrestamo.findByUserIdAndReturnDateIsNotNull(userId, pageable).map(this::convertToPrestamoDTO);
        return prestamoPagedAssembler.toModel(prestamos, prestamoDTO -> {
            EntityModel<PrestamoDTO> resource = EntityModel.of(prestamoDTO);
            resource.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(prestamoDTO.getId())).withSelfRel());
            resource.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(prestamoDTO.getUserId())).withRel("usuario"));
            resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(prestamoDTO.getBookId())).withRel("libro"));
            return resource;
        });
    }

    public PagedModel<EntityModel<PrestamoDTO>> listLoansByDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Las fechas de inicio y fin son obligatorias");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        Page<PrestamoDTO> prestamos = repoPrestamo.findByUserIdAndLoanDateBetween(userId, startDate, endDate, pageable).map(this::convertToPrestamoDTO);
        return prestamoPagedAssembler.toModel(prestamos, prestamoDTO -> {
            EntityModel<PrestamoDTO> resource = EntityModel.of(prestamoDTO);
            resource.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(prestamoDTO.getId())).withSelfRel());
            if (prestamoDTO.getReturnDate() == null) {
                resource.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(prestamoDTO.getId())).withRel("estado"));
                resource.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(prestamoDTO.getId())).withRel("fecha"));
            }
            resource.add(linkTo(methodOn(ContUsuario.class).obtenerUsuario(prestamoDTO.getUserId())).withRel("usuario"));
            resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(prestamoDTO.getBookId())).withRel("libro"));
            return resource;
        });
    }

    // Methods for ContUsuario
    public Page<Prestamo> getLoansByUserIdFromDate(Long userId, LocalDate desde, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        if (desde == null) {
            desde = LocalDate.of(1970, 1, 1);
        }
        return repoPrestamo.findByUserIdAndReturnDateIsNullAndLoanDateGreaterThanEqual(userId, desde, pageable);
    }

    public List<Prestamo> getLoanHistory(Long userId) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return repoPrestamo.findTop5ByUserIdAndReturnDateIsNotNullOrderByReturnDateDesc(userId);
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
}