package com.biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

// Entity: User
@Entity
@Table(name = "usuarios")
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  private String registrationNumber;
  private LocalDate birthDate;
  private String email;

  // Getters and Setters
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

