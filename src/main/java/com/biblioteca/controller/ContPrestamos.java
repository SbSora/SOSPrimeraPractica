package com.biblioteca.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.model.Prestamo;
import com.biblioteca.model.PrestamoDTO;
import com.biblioteca.service.ServiPrestamo;
import com.biblioteca.exception.BadRequestException;

import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/prestamos")
public class ContPrestamos {
    private final ServiPrestamo serviPrestamo;
    private final PagedResourcesAssembler<Prestamo> prestamoPagedAssembler;

    public ContPrestamos(ServiPrestamo serviPrestamo, PagedResourcesAssembler<Prestamo> prestamoPagedAssembler) {
        this.serviPrestamo = serviPrestamo;
        this.prestamoPagedAssembler = prestamoPagedAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<Prestamo>> borrowBook(@RequestBody PrestamoDTO prestamoDTO) {
        if (prestamoDTO.getUserId() == null || prestamoDTO.getBookId() == null) {
            throw new BadRequestException("User ID and Book ID are required");
        }
        Prestamo prestamo = serviPrestamo.borrowBook(prestamoDTO.getUserId(), prestamoDTO.getBookId());
        EntityModel<Prestamo> resource = EntityModel.of(prestamo);
        resource.add(linkTo(methodOn(ContPrestamos.class).getLoan(prestamo.getId())).withSelfRel());
        return ResponseEntity.created(linkTo(methodOn(ContPrestamos.class).getLoan(prestamo.getId())).toUri()).body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Prestamo>> getLoan(@PathVariable Long id) {
        Prestamo prestamo = serviPrestamo.getLoan(id);
        EntityModel<Prestamo> resource = EntityModel.of(prestamo);
        resource.add(linkTo(methodOn(ContPrestamos.class).getLoan(id)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<EntityModel<Prestamo>> returnBook(@PathVariable Long id) {
        Prestamo prestamo = serviPrestamo.returnBook(id);
        EntityModel<Prestamo> resource = EntityModel.of(prestamo);
        resource.add(linkTo(methodOn(ContPrestamos.class).getLoan(prestamo.getId())).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/extend")
    public ResponseEntity<EntityModel<Prestamo>> extendLoan(@PathVariable Long id) {
        Prestamo prestamo = serviPrestamo.extendLoan(id);
        EntityModel<Prestamo> resource = EntityModel.of(prestamo);
        resource.add(linkTo(methodOn(ContPrestamos.class).getLoan(prestamo.getId())).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Prestamo>>> listLoans(
            @RequestParam Long userId,
            @RequestParam(required = false) Boolean current,
            @RequestParam(required = false) Boolean historical,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable) {
        Page<Prestamo> prestamos;
        if (Boolean.TRUE.equals(current)) {
            prestamos = serviPrestamo.listCurrentLoans(userId, pageable);
        } else if (Boolean.TRUE.equals(historical)) {
            prestamos = serviPrestamo.listHistoricalLoans(userId, pageable);
        } else if (startDate != null && endDate != null) {
            prestamos = serviPrestamo.listLoansByDateRange(userId, startDate, endDate, pageable);
        } else {
            throw new BadRequestException("Must specify current, historical, or date range parameters");
        }
        PagedModel<EntityModel<Prestamo>> pagedModel = prestamoPagedAssembler.toModel(prestamos, prestamo ->
                EntityModel.of(prestamo).add(linkTo(methodOn(ContPrestamos.class).getLoan(prestamo.getId())).withSelfRel()));
        return ResponseEntity.ok(pagedModel);
    }
}