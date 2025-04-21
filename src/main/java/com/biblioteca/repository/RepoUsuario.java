package com.biblioteca.repository;

// Repository: UserRepository
import org.springframework.data.jpa.repository.JpaRepository;

import com.biblioteca.model.Usuario;

public interface RepoUsuario extends JpaRepository<Usuario, Long> {
}
