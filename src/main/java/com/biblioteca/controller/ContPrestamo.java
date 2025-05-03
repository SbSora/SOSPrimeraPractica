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

    @PostMapping
    public ResponseEntity<EntityModel<PrestamoDTO>> borrowBook(@Valid @RequestBody PrestamoDTO prestamoDTO) {
        try {
            if (prestamoDTO.getUserId() == null || prestamoDTO.getBookId() == null) {
                throw new BadRequestException("User ID and Book ID are required");
            }
            EntityModel<PrestamoDTO> resource = serviPrestamo.borrowBook(prestamoDTO.getUserId(), prestamoDTO.getBookId());
            return ResponseEntity.created(linkTo(methodOn(ContPrestamo.class).getLoan(resource.getContent().getId())).toUri()).body(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PrestamoDTO>> getLoan(@PathVariable Long id) {
        try {
            EntityModel<PrestamoDTO> resource = serviPrestamo.getLoan(id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<EntityModel<PrestamoDTO>> returnBook(@PathVariable Long id) {
        try {
            EntityModel<PrestamoDTO> resource = serviPrestamo.returnBook(id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}/ampliar")
    public ResponseEntity<EntityModel<PrestamoDTO>> extendLoan(@PathVariable Long id) {
        try {
            EntityModel<PrestamoDTO> resource = serviPrestamo.extendLoan(id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<PrestamoDTO>>> listLoans(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean current,
            @RequestParam(required = false) Boolean historical,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {
        try {
            PagedModel<EntityModel<PrestamoDTO>> pagedModel;
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
            return ResponseEntity.status(404).body(null);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}