package com.biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity(name = "usuarios")
@Table(name = "usuarios")
public class Usuario {

    @Id
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "email", nullable = false)
    private String email;

    // Getters reordered explicitly to match desired column order
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRegistrationNumber() { return registrationNumber; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getEmail() { return email; }

    // Setters (order not critical)
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setEmail(String email) { this.email = email; }
}