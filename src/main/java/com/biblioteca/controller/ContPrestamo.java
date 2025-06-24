package com.biblioteca.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.service.ServiPrestamo;
import com.biblioteca.exception.BadRequestException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.exception.ForbiddenException;

import java.time.LocalDate;

import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/prestamos")
public class ContPrestamo {
    private final ServiPrestamo serviPrestamo;

    public ContPrestamo(ServiPrestamo serviPrestamo) {
        this.serviPrestamo = serviPrestamo;
    }

    // Crear un nuevo préstamo
    @PostMapping
    public ResponseEntity<EntityModel<PrestamoDTO>> crearPrestamo(@Valid @RequestBody PrestamoDTO prestamoDTO) {
        try {
            if (prestamoDTO.getUserId() == null || prestamoDTO.getBookId() == null) {
                throw new BadRequestException("User ID and Book ID are required");
            }
            EntityModel<PrestamoDTO> resource = serviPrestamo.borrowBook(prestamoDTO.getUserId(), prestamoDTO.getBookId());
            PrestamoDTO content = resource.getContent();
            if (content == null || content.getId() == null) {
                throw new ResourceNotFoundException("Resource content or ID is null");
            }
            return ResponseEntity.created(linkTo(ContPrestamo.class).slash(content.getId()).toUri()).body(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).build();
        }
    }

    // Obtener un préstamo por ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PrestamoDTO>> obtenerPrestamo(@PathVariable Long id) {
        try {
            EntityModel<PrestamoDTO> resource = serviPrestamo.obtenerPrestamo(id);
            resource.add(linkTo(methodOn(ContPrestamo.class).obtenerPrestamo(id)).withSelfRel());
            resource.add(linkTo(methodOn(ContPrestamo.class).actualizarEstadoDevolucion(id)).withRel("estado"));
            resource.add(linkTo(methodOn(ContPrestamo.class).actualizarFechaVencimiento(id)).withRel("fecha"));
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).build();
        }
    }

    // Actualizar el estado de devolución de un préstamo
    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> actualizarEstadoDevolucion(@PathVariable Long id) {
        try {
            serviPrestamo.returnBook(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).build();
        }
    }

    // Actualizar la fecha de vencimiento de un préstamo
    @PutMapping("/{id}/fecha")
    public ResponseEntity<Void> actualizarFechaVencimiento(@PathVariable Long id) {
        try {
            serviPrestamo.extendLoan(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).build();
        }
    }

    // Listar préstamos con filtros opcionales
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<PrestamoDTO>>> listarPrestamos(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean current,
            @RequestParam(required = false) Boolean historical,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {
        try {
            PagedModel<EntityModel<PrestamoDTO>> pagedModel;
            if (userId != null && startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new BadRequestException("La fecha de inicio debe ser anterior a la fecha de fin");
            }
            if (userId != null) {
                if (Boolean.TRUE.equals(current)) {
                    pagedModel = serviPrestamo.listCurrentLoans(userId, pageable);
                } else if (Boolean.TRUE.equals(historical)) {
                    pagedModel = serviPrestamo.listHistoricalLoans(userId, pageable);
                } else if (startDate != null && endDate != null) {
                    pagedModel = serviPrestamo.listLoansByDateRange(userId, startDate, endDate, pageable);
                } else {
                    throw new BadRequestException("Must specify current, historical, or date range parameters when userId is provided");
                }
            } else {
                pagedModel = serviPrestamo.listAllLoans(pageable);
            }
            return ResponseEntity.ok(pagedModel);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}