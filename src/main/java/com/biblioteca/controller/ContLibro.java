package com.biblioteca.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.model.LibroDTO;
import com.biblioteca.service.ServiLibro;

import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/libros")
public class ContLibro {
    private final ServiLibro serviLibro;

    public ContLibro(ServiLibro serviLibro) {
        this.serviLibro = serviLibro;
    }

    @PostMapping
    public ResponseEntity<EntityModel<LibroDTO>> addBook(@Valid @RequestBody LibroDTO bookDTO) {
        EntityModel<LibroDTO> resource = serviLibro.addBook(bookDTO);
        return ResponseEntity.created(linkTo(methodOn(ContLibro.class).getBook(resource.getContent().getId())).toUri()).body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<LibroDTO>> getBook(@PathVariable Long id) {
        EntityModel<LibroDTO> resource = serviLibro.getBook(id);
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<LibroDTO>> updateBook(@PathVariable Long id, @Valid @RequestBody LibroDTO bookDTO) {
        EntityModel<LibroDTO> resource = serviLibro.updateBook(id, bookDTO);
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        serviLibro.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<LibroDTO>>> listBooks(
            @RequestParam(required = false, name = "titulo") String title,
            @RequestParam(required = false, name = "disponible") Boolean available,
            Pageable pageable) {
        PagedModel<EntityModel<LibroDTO>> pagedModel;
        if (title != null) {
            pagedModel = serviLibro.listBooksByTitle(title, pageable);
        } else if (Boolean.TRUE.equals(available)) {
            pagedModel = serviLibro.listAvailableBooks(pageable);
        } else {
            pagedModel = serviLibro.listBooks(pageable);
        }
        return ResponseEntity.ok(pagedModel);
    }
}