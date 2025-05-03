package com.biblioteca.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.model.LibroDTO;
import com.biblioteca.service.ServiLibro;
import com.biblioteca.exception.ResourceNotFoundException;

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
        try {
            EntityModel<LibroDTO> resource = serviLibro.getBook(id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<LibroDTO>> updateBook(@PathVariable Long id, @Valid @RequestBody LibroDTO bookDTO) {
        try {
            EntityModel<LibroDTO> resource = serviLibro.updateBook(id, bookDTO);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        try {
            serviLibro.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<LibroDTO>>> listBooks(
            @RequestParam(required = false, name = "titulo") String title,
            @RequestParam(required = false, name = "disponible") Boolean available,
            Pageable pageable) {
        try {
            PagedModel<EntityModel<LibroDTO>> pagedModel;
            if (title != null && available != null) {
                pagedModel = serviLibro.listBooksByTitleAndAvailable(title, available, pageable);
            } else if (title != null) {
                pagedModel = serviLibro.listBooksByTitle(title, pageable);
            } else if (available != null) {
                if (available) {
                    pagedModel = serviLibro.listAvailableBooks(pageable);
                } else {
                    pagedModel = serviLibro.listUnavailableBooks(pageable);
                }
            } else {
                pagedModel = serviLibro.listBooks(pageable);
            }
            return ResponseEntity.ok(pagedModel);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}