package com.biblioteca.model;

import jakarta.persistence.*;
import java.time.LocalDate;


@Entity(name = "prestamos")
@Table(name = "prestamos")
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private Usuario user;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Libro book;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LocalDate penaltyUntil;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }
    public Libro getBook() { return book; }
    public void setBook(Libro book) { this.book = book; }
    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public LocalDate getPenaltyUntil() { return penaltyUntil; }
    public void setPenaltyUntil(LocalDate penaltyUntil) { this.penaltyUntil = penaltyUntil; }
    public void setLibro(Libro libro) { this.book = libro; }
    public Libro getLibro() { return book; }
    
}

