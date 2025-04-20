package com.biblioteca.service;

import com.biblioteca.exception.BadRequestException;
import com.biblioteca.exception.ResourceNotFoundException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Usuario;
import com.biblioteca.repository.RepoLibro;
import com.biblioteca.repository.RepoPrestamo;
import com.biblioteca.repository.RepoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ServiPrestamo {
    private final RepoPrestamo repoPrestamo;
    private final RepoUsuario repoUsuario;
    private final RepoLibro repoLibro;

    public ServiPrestamo(RepoPrestamo repoPrestamo, RepoUsuario repoUsuario, RepoLibro repoLibro) {
        this.repoPrestamo = repoPrestamo;
        this.repoUsuario = repoUsuario;
        this.repoLibro = repoLibro;
    }

    public Prestamo getLoan(Long id) {
        return repoPrestamo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));
    }

    public Prestamo borrowBook(Long userId, Long bookId) {
        Usuario usuario = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Libro libro = repoLibro.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        // Verificar si el usuario tiene penalizaciones activas
        Optional<Prestamo> penalizacionActiva = repoPrestamo.findByUserIdAndReturnDateIsNull(userId, Pageable.unpaged())
                .getContent().stream()
                .filter(prestamo -> prestamo.getPenaltyUntil() != null && prestamo.getPenaltyUntil().isAfter(LocalDate.now()))
                .findFirst();
        if (penalizacionActiva.isPresent()) {
            throw new BadRequestException("El usuario tiene una penalización activa hasta " + penalizacionActiva.get().getPenaltyUntil());
        }

        // Verificar si el libro está disponible
        if (!libro.isAvailable()) {
            throw new BadRequestException("El libro no está disponible");
        }

        // Crear el préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setUser(usuario);
        prestamo.setBook(libro);
        prestamo.setLoanDate(LocalDate.now());
        prestamo.setDueDate(LocalDate.now().plusWeeks(2));
        prestamo.setReturnDate(null);
        prestamo.setPenaltyUntil(null);
        libro.setAvailable(false);

        repoLibro.save(libro);
        return repoPrestamo.save(prestamo);
    }

    public Prestamo returnBook(Long loanId) {
        Prestamo prestamo = repoPrestamo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));

        if (prestamo.getReturnDate() != null) {
            throw new BadRequestException("El libro ya fue devuelto");
        }

        // Marcar el libro como devuelto
        prestamo.setReturnDate(LocalDate.now());
        Libro libro = prestamo.getBook();
        libro.setAvailable(true);

        // Verificar si la devolución fue tardía
        if (prestamo.getDueDate().isBefore(LocalDate.now())) {
            prestamo.setPenaltyUntil(LocalDate.now().plusWeeks(1));
        }

        repoLibro.save(libro);
        return repoPrestamo.save(prestamo);
    }

    public Prestamo extendLoan(Long loanId) {
        Prestamo prestamo = repoPrestamo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));

        if (prestamo.getReturnDate() != null) {
            throw new BadRequestException("No se puede extender un préstamo ya devuelto");
        }

        if (prestamo.getDueDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("No se puede extender un préstamo vencido");
        }

        // Extender la fecha de vencimiento por dos semanas
        prestamo.setDueDate(prestamo.getDueDate().plusWeeks(2));
        return repoPrestamo.save(prestamo);
    }

    public Page<Prestamo> listCurrentLoans(Long userId, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return repoPrestamo.findByUserIdAndReturnDateIsNull(userId, pageable);
    }

    public Page<Prestamo> listHistoricalLoans(Long userId, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return repoPrestamo.findByUserIdAndReturnDateIsNotNull(userId, pageable);
    }

    public Page<Prestamo> listLoansByDateRange(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (!repoUsuario.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return repoPrestamo.findByUserIdAndLoanDateBetween(userId, startDate, endDate, pageable);
    }
}