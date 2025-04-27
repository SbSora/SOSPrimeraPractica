package com.biblioteca.model;

import jakarta.validation.constraints.NotBlank;

public class LibroDTO {
    private Long id;

    @NotBlank(message = "El título no puede estar en blanco")
    private String title;

    @NotBlank(message = "Los autores no pueden estar en blanco")
    private String authors;

    @NotBlank(message = "La edición no puede estar en blanco")
    private String edition;

    @NotBlank(message = "El ISBN no puede estar en blanco")
    private String isbn;

    @NotBlank(message = "La editorial no puede estar en blanco")
    private String publisher;

    private boolean available;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }
    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}