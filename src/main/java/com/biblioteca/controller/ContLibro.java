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

    // Crear un nuevo libro
    @PostMapping
    public ResponseEntity<EntityModel<LibroDTO>> crearLibro(@Valid @RequestBody LibroDTO libroDTO) {
        EntityModel<LibroDTO> resource = serviLibro.addBook(libroDTO);
        return ResponseEntity.created(linkTo(ContLibro.class).slash(resource.getContent().getId()).toUri()).body(resource);
    }

    // Obtener un libro por ID
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<LibroDTO>> obtenerLibro(@PathVariable Long id) {
        try {
            EntityModel<LibroDTO> resource = serviLibro.getBook(id);
            return ResponseEntity.ok(resource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        }
    }

    // Actualizar un libro por ID
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarLibro(@PathVariable Long id, @Valid @RequestBody LibroDTO libroDTO) {
        try {
            serviLibro.updateBook(id, libroDTO);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).build();
        }
    }

    // Eliminar un libro por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(@PathVariable Long id) {
        try {
            serviLibro.deleteBook(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Listar libros con filtros opcionales
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<LibroDTO>>> listarLibros(
            @RequestParam(required = false, name = "titulo") String titulo,
            @RequestParam(required = false, name = "disponible") Boolean disponible,
            Pageable pageable) {
        try {
            PagedModel<EntityModel<LibroDTO>> pagedModel;
            if (titulo != null && disponible != null) {
                pagedModel = serviLibro.listBooksByTitleAndAvailable(titulo, disponible, pageable);
            } else if (titulo != null) {
                pagedModel = serviLibro.listBooksByTitle(titulo, pageable);
            } else if (disponible != null) {
                if (disponible) {
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