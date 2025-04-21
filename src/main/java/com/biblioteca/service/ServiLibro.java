package com.biblioteca.service;

import com.biblioteca.exception.BadRequestException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.LibroDTO;
import com.biblioteca.repository.RepoLibro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ServiLibro {
    private final RepoLibro repoLibro;

    public ServiLibro(RepoLibro repoLibro) {
        this.repoLibro = repoLibro;
    }

    public Libro addBook(LibroDTO libroDTO) {
        Libro libro = new Libro();
        libro.setTitle(libroDTO.getTitle());
        libro.setAuthors(libroDTO.getAuthors());
        libro.setEdition(libroDTO.getEdition());
        libro.setIsbn(libroDTO.getIsbn());
        libro.setPublisher(libroDTO.getPublisher());
        libro.setAvailable(true);
        return repoLibro.save(libro);
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

    public void deleteBook(Long id) {
        Libro libro = getBook(id);
        if (!libro.isAvailable()) {
            throw new BadRequestException("No se puede eliminar un libro que está prestado");
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