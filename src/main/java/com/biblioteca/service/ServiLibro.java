package com.biblioteca.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.controller.ContLibro;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroDTO;
import com.biblioteca.repository.RepoLibro;
import com.biblioteca.repository.RepoPrestamo;

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

        LibroDTO savedLibroDTO = convertToLibroDTO(savedLibro);
        EntityModel<LibroDTO> resource = EntityModel.of(savedLibroDTO);
        resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(savedLibroDTO.getId())).withSelfRel());
        return resource;
    }

    public EntityModel<LibroDTO> getBook(Long id) {
        Libro libro = repoLibro.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
        LibroDTO dto = convertToLibroDTO(libro);
        EntityModel<LibroDTO> resource = EntityModel.of(dto);
        resource.add(linkTo(methodOn(ContLibro.class).obtenerLibro(id)).withSelfRel());
        resource.add(linkTo(methodOn(ContLibro.class).eliminarLibro(id)).withRel("delete"));
        return resource;
    }

    @Transactional
    public void updateBook(Long id, LibroDTO libroDTO) {
        Libro libro = repoLibro.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
        libro.setTitle(libroDTO.getTitle());
        libro.setAuthors(libroDTO.getAuthors());
        libro.setEdition(libroDTO.getEdition());
        libro.setIsbn(libroDTO.getIsbn());
        libro.setPublisher(libroDTO.getPublisher());
        repoLibro.save(libro);
    }

    @Transactional
    public void deleteBook(Long id) {
        boolean hasActiveLoans = repoPrestamo.existsByBookIdAndReturnDateIsNull(id);
        if (hasActiveLoans) {
            throw new IllegalStateException("Cannot delete book with active loans");
        }

        if (!repoLibro.existsById(id)) {
            throw new ResourceNotFoundException("Libro no encontrado");
        }

        repoLibro.deleteById(id);
    }

    public PagedModel<EntityModel<LibroDTO>> listBooks(Pageable pageable) {
        Page<Libro> libros = repoLibro.findAll(pageable);
        if (libros.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron libros");
        }
        return bookPagedAssembler.toModel(libros.map(this::convertToLibroDTO));
    }

    public PagedModel<EntityModel<LibroDTO>> listBooksByTitle(String title, Pageable pageable) {
        Page<Libro> libros = repoLibro.findByTitleContaining(title, pageable);
        if (libros.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron libros con ese título");
        }
        return bookPagedAssembler.toModel(libros.map(this::convertToLibroDTO));
    }

    public PagedModel<EntityModel<LibroDTO>> listAvailableBooks(Pageable pageable) {
        Page<Libro> libros = repoLibro.findByAvailableTrue(pageable);
        if (libros.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron libros disponibles");
        }
        return bookPagedAssembler.toModel(libros.map(this::convertToLibroDTO));
    }

    public PagedModel<EntityModel<LibroDTO>> listUnavailableBooks(Pageable pageable) {
        Page<Libro> libros = repoLibro.findByAvailableFalse(pageable);
        if (libros.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron libros no disponibles");
        }
        return bookPagedAssembler.toModel(libros.map(this::convertToLibroDTO));
    }

    public PagedModel<EntityModel<LibroDTO>> listBooksByTitleAndAvailable(String title, boolean available, Pageable pageable) {
        Page<Libro> libros = repoLibro.findByTitleContainingAndAvailable(title, available, pageable);
        if (libros.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron libros con ese título y disponibilidad");
        }
        return bookPagedAssembler.toModel(libros.map(this::convertToLibroDTO));
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

    public boolean existeLibroPorId(Long id) {
        return repoLibro.existsById(id);
    }

    public boolean existeLibroPorTitulo(String titulo) {
        return repoLibro.findByTitleContaining(titulo, Pageable.unpaged()).hasContent();
    }

    public Libro crearLibro(Libro libro) {
        libro.setAvailable(true); // Ensure new books are marked as available
        return repoLibro.save(libro);
    }

    public Page<Libro> buscarLibros(String titulo, Pageable pageable) {
        if (titulo != null) {
            return repoLibro.findByTitleContaining(titulo, pageable);
        } else {
            return repoLibro.findAll(pageable);
        }
    }

    public void eliminarLibro(Long id) {
        if (!repoLibro.existsById(id)) {
            throw new ResourceNotFoundException("Libro no encontrado");
        }
        repoLibro.deleteById(id);
    }
}