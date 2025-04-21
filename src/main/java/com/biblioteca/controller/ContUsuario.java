package com.biblioteca.controller;

// Controller: ContUsuario {
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.UsuarioDTO;
import com.biblioteca.service.ServiUsuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/usuarios")
public class ContUsuario {
    private final ServiUsuario ServiUsuario;

    public ContUsuario (ServiUsuario ServiUsuario) {
        this.ServiUsuario = ServiUsuario;
    }

    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> addUser(@RequestBody UsuarioDTO userDTO) {
        Usuario user = ServiUsuario.addUser(userDTO);
        EntityModel<Usuario> resource = EntityModel.of(user);
        resource.add(linkTo(methodOn(ContUsuario.class).getUser(user.getId())).withSelfRel());
        return ResponseEntity.created(linkTo(methodOn(ContUsuario.class).getUser(user.getId())).toUri()).body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> getUser(@PathVariable Long id) {
        Usuario user = ServiUsuario.getUser(id);
        EntityModel<Usuario> resource = EntityModel.of(user);
        resource.add(linkTo(methodOn(ContUsuario.class).getUser(id)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> updateUser(@PathVariable Long id, @RequestBody UsuarioDTO userDTO) {
        Usuario user = ServiUsuario.updateUser(id, userDTO);
        EntityModel<Usuario> resource = EntityModel.of(user);
        resource.add(linkTo(methodOn(ContUsuario.class).getUser(id)).withSelfRel());
        return ResponseEntity.ok(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        ServiUsuario.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<Usuario>> listUsers(Pageable pageable) {
        Page<Usuario> users = ServiUsuario.listUsers(pageable);
        return ResponseEntity.ok(users);
    }
}