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
public class ContPrestamo {
    private final ServiPrestamo serviPrestamo;
    private final PagedResourcesAssembler<PrestamoDTO> prestamoPagedAssembler;

    public ContPrestamo(ServiPrestamo serviPrestamo, PagedResourcesAssembler<PrestamoDTO> prestamoPagedAssembler) {
        this.serviPrestamo = serviPrestamo;
        this.prestamoPagedAssembler = prestamoPagedAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<PrestamoDTO>> borrowBook(@RequestBody PrestamoDTO prestamoDTO) {
        if (prestamoDTO.getUserId() == null || prestamoDTO.getBookId() == null) {
            throw new BadRequestException("User ID and Book ID are required");
        }
        Prestamo prestamo = serviPrestamo.borrowBook(prestamoDTO.getUserId(), prestamoDTO.getBookId());
        PrestamoDTO dto = convertToPrestamoDTO(prestamo);
        EntityModel<PrestamoDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(dto.getId())).withRel("return"));
        resource.add(linkTo(methodOn(ContPrestamo.class).extendLoan(dto.getId())).withRel("extend"));
        return ResponseEntity.created(linkTo(methodOn(ContPrestamo.class).getLoan(dto.getId())).toUri()).body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PrestamoDTO>> getLoan(@PathVariable Long id) {
        Prestamo prestamo = serviPrestamo.getLoan(id);
        PrestamoDTO dto = convertToPrestamoDTO(prestamo);
        EntityModel<PrestamoDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(id)).withSelfRel());
        if (dto.getReturnDate() == null) { // Only add return/extend links for active loans
            resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(id)).withRel("return"));
            resource.add(linkTo(methodOn(ContPrestamo.class).extendLoan(id)).withRel("extend"));
        }
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<EntityModel<PrestamoDTO>> returnBook(@PathVariable Long id) {
        Prestamo prestamo = serviPrestamo.returnBook(id);
        PrestamoDTO dto = convertToPrestamoDTO(prestamo);
        EntityModel<PrestamoDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContUsuario.class).getUserLoans(dto.getUserId(), null, Pageable.unpaged())).withRel("user-loans"));
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}/ampliar")
    public ResponseEntity<EntityModel<PrestamoDTO>> extendLoan(@PathVariable Long id) {
        Prestamo prestamo = serviPrestamo.extendLoan(id);
        PrestamoDTO dto = convertToPrestamoDTO(prestamo);
        EntityModel<PrestamoDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(dto.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(dto.getId())).withRel("return"));
        resource.add(linkTo(methodOn(ContUsuario.class).getUserLoans(dto.getUserId(), null, Pageable.unpaged())).withRel("user-loans"));
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
        Page<PrestamoDTO> prestamos;
        if (Boolean.TRUE.equals(current)) {
            prestamos = serviPrestamo.listCurrentLoans(userId, pageable).map(this::convertToPrestamoDTO);
        } else if (Boolean.TRUE.equals(historical)) {
            prestamos = serviPrestamo.listHistoricalLoans(userId, pageable).map(this::convertToPrestamoDTO);
        } else if (startDate != null && endDate != null) {
            prestamos = serviPrestamo.listLoansByDateRange(userId, startDate, endDate, pageable).map(this::convertToPrestamoDTO);
        } else {
            throw new BadRequestException("Must specify current, historical, or date range parameters");
        }
        PagedModel<EntityModel<PrestamoDTO>> pagedModel = prestamoPagedAssembler.toModel(prestamos, prestamoDTO -> {
            EntityModel<PrestamoDTO> resource = EntityModel.of(prestamoDTO);
            resource.add(linkTo(methodOn(ContPrestamo.class).getLoan(prestamoDTO.getId())).withSelfRel());
            if (prestamoDTO.getReturnDate() == null) { // Only add return/extend links for active loans
                resource.add(linkTo(methodOn(ContPrestamo.class).returnBook(prestamoDTO.getId())).withRel("return"));
                resource.add(linkTo(methodOn(ContPrestamo.class).extendLoan(prestamoDTO.getId())).withRel("extend"));
            }
            return resource;
        });
        return ResponseEntity.ok(pagedModel);
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