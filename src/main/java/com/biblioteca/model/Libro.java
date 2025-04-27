package com.biblioteca.model;

import jakarta.persistence.*;

@Entity(name = "libros")
@Table(name = "libros")
public class Libro {

    @Id
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "authors", nullable = false)
    private String authors;

    @Column(name = "edition", nullable = false)
    private String edition;

    @Column(name = "isbn", nullable = false)
    private String isbn;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "available", nullable = false)
    private boolean available;

    // Getters and setters
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