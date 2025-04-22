package com.biblioteca.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.UsuarioDTO;
import com.biblioteca.repository.RepoUsuario;
import com.biblioteca.repository.RepoPrestamo;
import com.biblioteca.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class ServiUsuario {
    private final RepoUsuario repoUsuario;
    private final RepoPrestamo repoPrestamo;

    public ServiUsuario(RepoUsuario repoUsuario, RepoPrestamo repoPrestamo) {
        this.repoUsuario = repoUsuario;
        this.repoPrestamo = repoPrestamo;
    }

    @Transactional
    public Usuario addUser(UsuarioDTO usuarioDTO) {
        Usuario user = new Usuario();
        user.setUsername(usuarioDTO.getUsername());
        user.setRegistrationNumber(usuarioDTO.getRegistrationNumber());
        user.setBirthDate(usuarioDTO.getBirthDate());
        user.setEmail(usuarioDTO.getEmail());

        // Assign the lowest available ID
        user.setId(getNextAvailableId());

        return repoUsuario.save(user);
    }

    private Long getNextAvailableId() {
        List<Long> existingIds = repoUsuario.findAllIds();
        long nextId = 1;
        for (Long id : existingIds) {
            if (id > nextId) {
                break;
            }
            nextId = id + 1;
        }
        return nextId;
    }

    public Usuario getUser(Long id) {
        return repoUsuario.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario not found"));
    }

    public Usuario updateUser(Long id, UsuarioDTO usuarioDTO) {
        Usuario user = getUser(id);
        user.setUsername(usuarioDTO.getUsername());
        user.setRegistrationNumber(usuarioDTO.getRegistrationNumber());
        user.setBirthDate(usuarioDTO.getBirthDate());
        user.setEmail(usuarioDTO.getEmail());
        return repoUsuario.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        // Prevent deletion if there are active loans
        boolean hasActiveLoans = repoPrestamo.existsByUserIdAndReturnDateIsNull(id);
        if (hasActiveLoans) {
            throw new IllegalStateException("Cannot delete user with active loans");
        }

        if (!repoUsuario.existsById(id)) {
            throw new ResourceNotFoundException("Usuario not found");
        }

        repoUsuario.deleteById(id);
    }

    public Page<Usuario> listUsers(Pageable pageable) {
        return repoUsuario.findAll(pageable);
    }
}