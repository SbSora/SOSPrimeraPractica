package com.biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity(name = "prestamos")
@Table(name = "prestamos")
public class Prestamo {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = true)
    private Libro libro;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "penalty_until")
    private LocalDate penaltyUntil;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public LocalDate getPenaltyUntil() { return penaltyUntil; }
    public void setPenaltyUntil(LocalDate penaltyUntil) { this.penaltyUntil = penaltyUntil; }
}