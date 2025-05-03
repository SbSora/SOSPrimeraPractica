package com.biblioteca.service;

import com.biblioteca.exception.BadRequestException;
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

    public EntityModel<PrestamoDTO> getLoan(Long id) {
        Prestamo prestamo = repoPrestamo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));
        PrestamoDTO dto = convertToPrestamoDTO(prestamo);
        EntityModel<PrestamoDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(id)).withSelfRel());
        if (dto.getReturnDate() == null) { // Only add return/extend links for active loans
            resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(id)).withRel("return"));
            resource.add(linkTo(methodOn(ContPrestamo.class).extendLoan(id)).withRel("extend"));
        }
        resource.add(linkTo(methodOn(ContUsuario.class).getUsuarioById(dto.getUserId())).withRel("user"));
        resource.add(linkTo(methodOn(ContLibro.class).getBook(dto.getBookId())).withRel("book"));
        return resource;
    }

    @Transactional
    public EntityModel<PrestamoDTO> borrowBook(Long userId, Long bookId) {
        Usuario usuario = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Libro libro = repoLibro.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        // Check for maximum active loans (e.g., 3)
        Page<Prestamo> activeLoans = repoPrestamo.findByUserIdAndReturnDateIsNull(userId, Pageable.unpaged());
        if (activeLoans.getTotalElements() >= 3) {
            throw new BadRequestException("El usuario ya tiene el máximo de préstamos activos (3)");
        }

        // Check for penalties
        activeLoans.getContent().stream()
                .filter(prestamo -> prestamo.getPenaltyUntil() != null && prestamo.getPenaltyUntil().isAfter(LocalDate.now()))
                .findFirst()
                .ifPresent(prestamo -> {
                    throw new BadRequestException("El usuario tiene una penalización activa hasta " + prestamo.getPenaltyUntil());
                });

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
        resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(dto.getId())).withRel("return"));
        resource.add(linkTo(methodOn(ContPrestamo.class).extendLoan(dto.getId())).withRel("extend"));
        resource.add(linkTo(methodOn(ContUsuario.class).getUsuarioById(dto.getUserId())).withRel("user"));
        resource.add(linkTo(methodOn(ContLibro.class).getBook(dto.getBookId())).withRel("book"));
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
        resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContUsuario.class).getUserLoans(dto.getUserId(), null, Pageable.unpaged())).withRel("user-loans"));
        resource.add(linkTo(methodOn(ContUsuario.class).getUsuarioById(dto.getUserId())).withRel("user"));
        resource.add(linkTo(methodOn(ContLibro.class).getBook(dto.getBookId())).withRel("book"));
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
        resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(dto.getId())).withRel("return"));
        resource.add(linkTo(methodOn(ContUsuario.class).getUserLoans(dto.getUserId(), null, Pageable.unpaged())).withRel("user-loans"));
        resource.add(linkTo(methodOn(ContUsuario.class).getUsuarioById(dto.getUserId())).withRel("user"));
        resource.add(linkTo(methodOn(ContLibro.class).getBook(dto.getBookId())).withRel("book"));
        return resource;
    }

    public PagedModel<EntityModel<PrestamoDTO>> listAllLoans(Pageable pageable) {
        Page<PrestamoDTO> prestamos = repoPrestamo.findAll(pageable).map(this::convertToPrestamoDTO);
        if (prestamos.isEmpty()) {
            throw new ResourceNotFoundException("No loans found");
        }
        return prestamoPagedAssembler.toModel(prestamos, prestamoDTO -> {
            EntityModel<PrestamoDTO> resource = EntityModel.of(prestamoDTO);
            resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(prestamoDTO.getId())).withSelfRel());
            if (prestamoDTO.getReturnDate() == null) {
                resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(prestamoDTO.getId())).withRel("return"));
                resource.add(linkTo(methodOn(ContPrestamo.class).extendLoan(prestamoDTO.getId())).withRel("extend"));
            }
            resource.add(linkTo(methodOn(ContUsuario.class).getUsuarioById(prestamoDTO.getUserId())).withRel("user"));
            resource.add(linkTo(methodOn(ContLibro.class).getBook(prestamoDTO.getBookId())).withRel("book"));
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
            resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(prestamoDTO.getId())).withSelfRel());
            if (prestamoDTO.getReturnDate() == null) {
                resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(prestamoDTO.getId())).withRel("return"));
                resource.add(linkTo(methodOn(ContPrestamo.class).extendLoan(prestamoDTO.getId())).withRel("extend"));
            }
            resource.add(linkTo(methodOn(ContUsuario.class).getUsuarioById(prestamoDTO.getUserId())).withRel("user"));
            resource.add(linkTo(methodOn(ContLibro.class).getBook(prestamoDTO.getBookId())).withRel("book"));
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
            resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(prestamoDTO.getId())).withSelfRel());
            resource.add(linkTo(methodOn(ContUsuario.class).getUsuarioById(prestamoDTO.getUserId())).withRel("user"));
            resource.add(linkTo(methodOn(ContLibro.class).getBook(prestamoDTO.getBookId())).withRel("book"));
            return resource;
        });
    }

    public PagedModel<EntityModel<PrestamoDTO>> listLoansByDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date must be before end date");
        }
        Page<PrestamoDTO> prestamos = repoPrestamo.findByUserIdAndLoanDateBetween(userId, startDate, endDate, pageable).map(this::convertToPrestamoDTO);
        return prestamoPagedAssembler.toModel(prestamos, prestamoDTO -> {
            EntityModel<PrestamoDTO> resource = EntityModel.of(prestamoDTO);
            resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(prestamoDTO.getId())).withSelfRel());
            if (prestamoDTO.getReturnDate() == null) {
                resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(prestamoDTO.getId())).withRel("return"));
                resource.add(linkTo(methodOn(ContPrestamo.class).extendLoan(prestamoDTO.getId())).withRel("extend"));
            }
            resource.add(linkTo(methodOn(ContUsuario.class).getUsuarioById(prestamoDTO.getUserId())).withRel("user"));
            resource.add(linkTo(methodOn(ContLibro.class).getBook(prestamoDTO.getBookId())).withRel("book"));
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