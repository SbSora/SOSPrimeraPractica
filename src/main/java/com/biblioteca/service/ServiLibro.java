package com.biblioteca.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroDTO;
import com.biblioteca.model.Prestamo;
import com.biblioteca.repository.RepoLibro;
import com.biblioteca.repository.RepoPrestamo;
import com.biblioteca.controller.ContLibro;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Service
public class ServiLibro {
    private final RepoLibro repoLibro;
    private final RepoPrestamo repoPrestamo;
    private final PagedResourcesAssembler<LibroDTO> bookPagedAssembler;

    public ServiLibro(RepoLibro repoLibro, RepoPrestamo repoPrestamo, PagedResourcesAssembler<LibroDTO> bookPagedAssembler) {
        this.repoLibro = repoLibro;
        this.repoPrestamo = repoPrestamo;
        this.bookPagedAssembler = bookPagedAssembler;
    }

    @Transactional
    public EntityModel<LibroDTO> addBook(LibroDTO libroDTO) {
        Libro libro = new Libro();
        libro.setTitle(libroDTO.getTitle());
        libro.setAuthors(libroDTO.getAuthors());
        libro.setEdition(libroDTO.getEdition());
        libro.setIsbn(libroDTO.getIsbn());
        libro.setPublisher(libroDTO.getPublisher());
        libro.setAvailable(true);

        // Assign the lowest available ID
        libro.setId(getNextAvailableId());

        Libro savedLibro = repoLibro.save(libro);
        LibroDTO savedDTO = convertToLibroDTO(savedLibro);
        EntityModel<LibroDTO> resource = EntityModel.of(savedDTO);
        resource.add(linkTo(methodOn(ContLibro.class).getBook(savedDTO.getId())).withSelfRel());
        resource.add(linkTo(methodOn(ContLibro.class).listBooks(null, null, Pageable.unpaged())).withRel("books"));
        return resource;
    }

    private Long getNextAvailableId() {
        List<Long> existingIds = repoLibro.findAllIds();
        long nextId = 1;
        for (Long id : existingIds) {
            if (id > nextId) {
                break;
            }
            nextId = id + 1;
        }
        return nextId;
    }

    public EntityModel<LibroDTO> getBook(Long id) {
        Libro libro = repoLibro.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
        LibroDTO dto = convertToLibroDTO(libro);
        EntityModel<LibroDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContLibro.class).getBook(id)).withSelfRel());
        resource.add(linkTo(methodOn(ContLibro.class).deleteBook(id)).withRel("delete"));
        return resource;
    }

    public EntityModel<LibroDTO> updateBook(Long id, LibroDTO libroDTO) {
        Libro libro = repoLibro.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
        libro.setTitle(libroDTO.getTitle());
        libro.setAuthors(libroDTO.getAuthors());
        libro.setEdition(libroDTO.getEdition());
        libro.setIsbn(libroDTO.getIsbn());
        libro.setPublisher(libroDTO.getPublisher());
        Libro updatedLibro = repoLibro.save(libro);
        LibroDTO updatedDTO = convertToLibroDTO(updatedLibro);
        EntityModel<LibroDTO> resource = EntityModel.of(updatedDTO);
        resource.add(linkTo(methodOn(ContLibro.class).getBook(id)).withSelfRel());
        resource.add(linkTo(methodOn(ContLibro.class).deleteBook(id)).withRel("delete"));
        return resource;
    }

    @Transactional
    public void deleteBook(Long id) {
        // Prevent deletion if there are active loans
        boolean hasActiveLoans = repoPrestamo.existsByBookIdAndReturnDateIsNull(id);
        if (hasActiveLoans) {
            throw new IllegalStateException("No se puede eliminar un libro con préstamos activos");
        }

        if (!repoLibro.existsById(id)) {
            throw new ResourceNotFoundException("Libro no encontrado");
        }

        // Find all Prestamo records associated with the book
        List<Prestamo> prestamos = repoPrestamo.findByLibroId(id);
        // Set libro to null in all associated Prestamo records
        for (Prestamo prestamo : prestamos) {
            prestamo.setLibro(null);
            repoPrestamo.save(prestamo);
        }

        // Delete the book
        repoLibro.deleteById(id);
    }

    public PagedModel<EntityModel<LibroDTO>> listBooks(Pageable pageable) {
        Page<LibroDTO> books = repoLibro.findAll(pageable).map(this::convertToLibroDTO);
        return bookPagedAssembler.toModel(books, book -> {
            EntityModel<LibroDTO> resource = EntityModel.of(book);
            resource.add(linkTo(methodOn(ContLibro.class).getBook(book.getId())).withSelfRel());
            return resource;
        });
    }

    public PagedModel<EntityModel<LibroDTO>> listBooksByTitle(String title, Pageable pageable) {
        Page<LibroDTO> books = repoLibro.findByTitleContaining(title, pageable).map(this::convertToLibroDTO);
        if (books.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron libros con el título: " + title);
        }
        return bookPagedAssembler.toModel(books, book -> {
            EntityModel<LibroDTO> resource = EntityModel.of(book);
            resource.add(linkTo(methodOn(ContLibro.class).getBook(book.getId())).withSelfRel());
            return resource;
        });
    }

    public PagedModel<EntityModel<LibroDTO>> listAvailableBooks(Pageable pageable) {
        Page<LibroDTO> books = repoLibro.findByAvailableTrue(pageable).map(this::convertToLibroDTO);
        return bookPagedAssembler.toModel(books, book -> {
            EntityModel<LibroDTO> resource = EntityModel.of(book);
            resource.add(linkTo(methodOn(ContLibro.class).getBook(book.getId())).withSelfRel());
            return resource;
        });
    }

    public PagedModel<EntityModel<LibroDTO>> listUnavailableBooks(Pageable pageable) {
        Page<LibroDTO> books = repoLibro.findByAvailableFalse(pageable).map(this::convertToLibroDTO);
        return bookPagedAssembler.toModel(books, book -> {
            EntityModel<LibroDTO> resource = EntityModel.of(book);
            resource.add(linkTo(methodOn(ContLibro.class).getBook(book.getId())).withSelfRel());
            return resource;
        });
    }

    public PagedModel<EntityModel<LibroDTO>> listBooksByTitleAndAvailable(String title, boolean available, Pageable pageable) {
        Page<LibroDTO> books = repoLibro.findByTitleContainingAndAvailable(title, available, pageable).map(this::convertToLibroDTO);
        if (books.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron libros con el título: " + title);
        }
        return bookPagedAssembler.toModel(books, book -> {
            EntityModel<LibroDTO> resource = EntityModel.of(book);
            resource.add(linkTo(methodOn(ContLibro.class).getBook(book.getId())).withSelfRel());
            return resource;
        });
    }

    private LibroDTO convertToLibroDTO(Libro libro) {
        LibroDTO dto = new LibroDTO();
        dto.setId(libro.getId());
        dto.setTitle(libro.getTitle());
        dto.setAuthors(libro.getAuthors());
        dto.setEdition(libro.getEdition());
        dto.setIsbn(libro.getIsbn());
        dto.setPublisher(libro.getPublisher());
        dto.setAvailable(libro.isAvailable());
        return dto;
    }
}