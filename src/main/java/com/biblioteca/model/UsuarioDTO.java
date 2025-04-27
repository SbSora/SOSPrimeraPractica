package com.biblioteca.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UsuarioDTO {
    private Long id;

    @NotBlank(message = "El nombre de usuario no puede estar en blanco")
    private String username;

    @NotBlank(message = "El número de registro no puede estar en blanco")
    private String registrationNumber;

    @NotNull(message = "La fecha de nacimiento no puede ser nula")
    private LocalDate birthDate;

    @NotBlank(message = "El correo electrónico no puede estar en blanco")
    private String email;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}