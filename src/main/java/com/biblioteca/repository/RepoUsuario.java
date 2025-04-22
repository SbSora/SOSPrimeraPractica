package com.biblioteca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.biblioteca.model.Usuario;
import java.util.List;

public interface RepoUsuario extends JpaRepository<Usuario, Long> {
    @Query("SELECT u.id FROM usuarios u ORDER BY u.id")
    List<Long> findAllIds();
}