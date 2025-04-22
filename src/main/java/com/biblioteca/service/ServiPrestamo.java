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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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

    @Transactional
    public Prestamo borrowBook(Long userId, Long bookId) {
        Usuario usuario = repoUsuario.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Libro libro = repoLibro.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        // Verificar si el usuario tiene penalizaciones activas
        Page<Prestamo> activeLoans = repoPrestamo.findByUserIdAndReturnDateIsNull(userId, Pageable.unpaged());
        activeLoans.getContent().stream()
                .filter(prestamo -> prestamo.getPenaltyUntil() != null && prestamo.getPenaltyUntil().isAfter(LocalDate.now()))
                .findFirst()
                .ifPresent(prestamo -> {
                    throw new BadRequestException("El usuario tiene una penalización activa hasta " + prestamo.getPenaltyUntil());
                });

        // Verificar si el libro está disponible
        if (!libro.isAvailable()) {
            throw new BadRequestException("El libro no está disponible");
        }

        // Crear el préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setUsuario(usuario);
        prestamo.setLibro(libro);
        prestamo.setLoanDate(LocalDate.now());
        prestamo.setDueDate(LocalDate.now().plusWeeks(2));
        prestamo.setReturnDate(null);
        prestamo.setPenaltyUntil(null);

        // Assign the lowest available ID
        prestamo.setId(getNextAvailableId());

        libro.setAvailable(false);
        repoLibro.save(libro);
        return repoPrestamo.save(prestamo);
    }

    private Long getNextAvailableId() {
        List<Long> existingIds = repoPrestamo.findAllIds();
        long nextId = 1;
        for (Long id : existingIds) {
            if (id > nextId) {
                break;
            }
            nextId = id + 1;
        }
        return nextId;
    }

    @Transactional
    public Prestamo returnBook(Long loanId) {
        Prestamo prestamo = repoPrestamo.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado"));

        if (prestamo.getReturnDate() != null) {
            throw new BadRequestException("El libro ya fue devuelto");
        }

        // Marcar el libro como devuelto
        prestamo.setReturnDate(LocalDate.now());
        Libro libro = prestamo.getLibro();
        libro.setAvailable(true);

        // Verificar si la devolución fue tardía
        if (prestamo.getDueDate().isBefore(LocalDate.now())) {
            prestamo.setPenaltyUntil(LocalDate.now().plusWeeks(1));
        }

        repoLibro.save(libro);
        return repoPrestamo.save(prestamo);
    }

    @Transactional
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