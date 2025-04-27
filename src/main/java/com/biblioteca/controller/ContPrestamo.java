package com.biblioteca.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.service.ServiPrestamo;
import com.biblioteca.exception.BadRequestException;

import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/prestamos")
public class ContPrestamo {
    private final ServiPrestamo serviPrestamo;

    public ContPrestamo(ServiPrestamo serviPrestamo) {
        this.serviPrestamo = serviPrestamo;
    }

    @PostMapping
    public ResponseEntity<EntityModel<PrestamoDTO>> borrowBook(@RequestBody PrestamoDTO prestamoDTO) {
        if (prestamoDTO.getUserId() == null || prestamoDTO.getBookId() == null) {
            throw new BadRequestException("User ID and Book ID are required");
        }
        EntityModel<PrestamoDTO> resource = serviPrestamo.borrowBook(prestamoDTO.getUserId(), prestamoDTO.getBookId());
        return ResponseEntity.created(linkTo(methodOn(ContPrestamo.class).getLoan(resource.getContent().getId())).toUri()).body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PrestamoDTO>> getLoan(@PathVariable Long id) {
        EntityModel<PrestamoDTO> resource = serviPrestamo.getLoan(id);
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<EntityModel<PrestamoDTO>> returnBook(@PathVariable Long id) {
        EntityModel<PrestamoDTO> resource = serviPrestamo.returnBook(id);
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/ampliar")
    public ResponseEntity<EntityModel<PrestamoDTO>> extendLoan(@PathVariable Long id) {
        EntityModel<PrestamoDTO> resource = serviPrestamo.extendLoan(id);
        return ResponseEntity.ok(resource);
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<PrestamoDTO>>> listLoans(
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean current,
            @RequestParam(required = false) Boolean historical,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {
        PagedModel<EntityModel<PrestamoDTO>> pagedModel;
        if (Boolean.TRUE.equals(current)) {
            pagedModel = serviPrestamo.listCurrentLoans(userId, pageable);
        } else if (Boolean.TRUE.equals(historical)) {
            pagedModel = serviPrestamo.listHistoricalLoans(userId, pageable);
        } else if (startDate != null && endDate != null) {
            pagedModel = serviPrestamo.listLoansByDateRange(userId, startDate, endDate, pageable);
        } else {
            throw new BadRequestException("Must specify current, historical, or date range parameters");
        }
        return ResponseEntity.ok(pagedModel);
    }
}