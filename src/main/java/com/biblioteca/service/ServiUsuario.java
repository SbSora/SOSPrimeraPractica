package com.biblioteca.service;

import org.springframework.stereotype.Service;

import com.biblioteca.model.Usuario;
import com.biblioteca.model.UsuarioDTO;
import com.biblioteca.repository.RepoUsuario;
import com.biblioteca.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ServiUsuario {
    private final RepoUsuario RepoUsuario;

    public ServiUsuario (RepoUsuario RepoUsuario) {
        this.RepoUsuario = RepoUsuario;
    }

    public Usuario addUser(UsuarioDTO UsuarioDTO) {
        Usuario user = new Usuario();
        user.setUsername(UsuarioDTO.getUsername());
        user.setRegistrationNumber(UsuarioDTO.getRegistrationNumber());
        user.setBirthDate(UsuarioDTO.getBirthDate());
        user.setEmail(UsuarioDTO.getEmail());
        return RepoUsuario.save(user);
    }

    public Usuario getUser(Long id) {
        return RepoUsuario.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuario not found"));
    }

    public Usuario updateUser(Long id, UsuarioDTO UsuarioDTO) {
        Usuario user = getUser(id);
        user.setUsername(UsuarioDTO.getUsername());
        user.setRegistrationNumber(UsuarioDTO.getRegistrationNumber());
        user.setBirthDate(UsuarioDTO.getBirthDate());
        user.setEmail(UsuarioDTO.getEmail());
        return RepoUsuario.save(user);
    }

    public void deleteUser(Long id) {
        if (!RepoUsuario.existsById(id)) {
            throw new ResourceNotFoundException("Usuario not found");
        }
        RepoUsuario.deleteById(id);
    }

    public Page<Usuario> listUsers(Pageable pageable) {
        return RepoUsuario.findAll(pageable);
    }
}