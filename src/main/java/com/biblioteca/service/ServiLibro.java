package com.biblioteca.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroDTO;
import com.biblioteca.repository.RepoLibro;
import com.biblioteca.repository.RepoPrestamo;

@Service
public class ServiLibro {
    private final RepoLibro repoLibro;
    private final RepoPrestamo repoPrestamo;

    public ServiLibro(RepoLibro repoLibro, RepoPrestamo repoPrestamo) {
        this.repoLibro = repoLibro;
        this.repoPrestamo = repoPrestamo;
    }

    @Transactional
    public Libro addBook(LibroDTO libroDTO) {
        Libro libro = new Libro();
        libro.setTitle(libroDTO.getTitle());
        libro.setAuthors(libroDTO.getAuthors());
        libro.setEdition(libroDTO.getEdition());
        libro.setIsbn(libroDTO.getIsbn());
        libro.setPublisher(libroDTO.getPublisher());
        libro.setAvailable(true);

        // Assign the lowest available ID
        libro.setId(getNextAvailableId());

        return repoLibro.save(libro);
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

    public Libro getBook(Long id) {
        return repoLibro.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
    }

    public Libro updateBook(Long id, LibroDTO libroDTO) {
        Libro libro = getBook(id);
        libro.setTitle(libroDTO.getTitle());
        libro.setAuthors(libroDTO.getAuthors());
        libro.setEdition(libroDTO.getEdition());
        libro.setIsbn(libroDTO.getIsbn());
        libro.setPublisher(libroDTO.getPublisher());
        return repoLibro.save(libro);
    }

    @Transactional
    public void deleteBook(Long id) {
        // Prevent deletion if there are active loans
        boolean hasActiveLoans = repoPrestamo.existsByBookIdAndReturnDateIsNull(id);
        if (hasActiveLoans) {
            throw new IllegalStateException("Cannot delete book with active loans");
        }

        if (!repoLibro.existsById(id)) {
            throw new ResourceNotFoundException("Libro no encontrado");
        }

        repoLibro.deleteById(id);
    }

    public Page<Libro> listBooks(Pageable pageable) {
        return repoLibro.findAll(pageable);
    }

    public Page<Libro> listBooksByTitle(String title, Pageable pageable) {
        return repoLibro.findByTitleContaining(title, pageable);
    }

    public Page<Libro> listAvailableBooks(Pageable pageable) {
        return repoLibro.findByAvailableTrue(pageable);
    }
}