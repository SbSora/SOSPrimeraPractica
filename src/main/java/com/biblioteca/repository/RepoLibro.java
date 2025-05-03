package com.biblioteca.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.biblioteca.model.Libro;
import java.util.List;

public interface RepoLibro extends JpaRepository<Libro, Long> {
    @Query("SELECT b FROM libros b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Libro> findByTitleContaining(String title, Pageable pageable);

    Page<Libro> findByAvailableTrue(Pageable pageable);

    Page<Libro> findByAvailableFalse(Pageable pageable);

    Page<Libro> findByTitleContainingAndAvailable(String title, boolean available, Pageable pageable);

    @Query("SELECT b.id FROM libros b ORDER BY b.id")
    List<Long> findAllIds();
}