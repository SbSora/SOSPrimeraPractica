package com.biblioteca.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroDTO;
import com.biblioteca.service.ServiLibro;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/libros")
public class ContLibro {
    private final ServiLibro serviLibro;
    private final PagedResourcesAssembler<Libro> bookPagedAssembler;

    public ContLibro(ServiLibro serviLibro, PagedResourcesAssembler<Libro> bookPagedAssembler) {
        this.serviLibro = serviLibro;
        this.bookPagedAssembler = bookPagedAssembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<Libro>> addBook(@RequestBody LibroDTO bookDTO) {
        Libro book = serviLibro.addBook(bookDTO);
        EntityModel<Libro> resource = EntityModel.of(book);
        resource.add(linkTo(methodOn(ContLibro.class).getBook(book.getId())).withSelfRel());
        return ResponseEntity.created(linkTo(methodOn(ContLibro.class).getBook(book.getId())).toUri()).body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Libro>> getBook(@PathVariable Long id) {
        Libro book = serviLibro.getBook(id);
        EntityModel<Libro> resource = EntityModel.of(book);
        resource.add(linkTo(methodOn(ContLibro.class).getBook(id)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Libro>> updateBook(@PathVariable Long id, @RequestBody LibroDTO bookDTO) {
        Libro book = serviLibro.updateBook(id, bookDTO);
        EntityModel<Libro> resource = EntityModel.of(book);
        resource.add(linkTo(methodOn(ContLibro.class).getBook(id)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        serviLibro.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Libro>>> listBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean available,
            Pageable pageable) {
        Page<Libro> books;
        if (title != null) {
            books = serviLibro.listBooksByTitle(title, pageable);
        } else if (Boolean.TRUE.equals(available)) {
            books = serviLibro.listAvailableBooks(pageable);
        } else {
            books = serviLibro.listBooks(pageable);
        }
        PagedModel<EntityModel<Libro>> pagedModel = bookPagedAssembler.toModel(books, book ->
                EntityModel.of(book).add(linkTo(methodOn(ContLibro.class).getBook(book.getId())).withSelfRel()));
        return ResponseEntity.ok(pagedModel);
    }
}