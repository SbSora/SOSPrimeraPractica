package com.biblioteca.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;

import com.biblioteca.model.Prestamo;

public interface RepoPrestamo extends JpaRepository<Prestamo, Long> {
    Page<Prestamo> findByUserIdAndReturnDateIsNull(Long userId, Pageable pageable);
    Page<Prestamo> findByUserIdAndReturnDateIsNotNull(Long userId, Pageable pageable);
    @Query("SELECT l FROM prestamos l WHERE l.user.id = :userId AND l.loanDate BETWEEN :startDate AND :endDate")
    Page<Prestamo> findByUserIdAndLoanDateBetween(Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}