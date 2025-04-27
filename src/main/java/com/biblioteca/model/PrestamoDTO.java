package com.biblioteca.model;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public class PrestamoDTO {
    private Long id;

    @NotNull(message = "El ID del usuario no puede ser nulo")
    private Long userId;

    @NotNull(message = "El ID del libro no puede ser nulo")
    private Long bookId;

    private LocalDate loanDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private LocalDate penaltyUntil;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public LocalDate getPenaltyUntil() {
        return penaltyUntil;
    }

    public void setPenaltyUntil(LocalDate penaltyUntil) {
        this.penaltyUntil = penaltyUntil;
    }
}