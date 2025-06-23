package com.biblioteca.repository;

import com.biblioteca.model.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RepoPrestamo extends JpaRepository<Prestamo, Long> {

    @Query("SELECT COUNT(p) > 0 FROM prestamos p WHERE p.usuario.id = :userId AND p.returnDate IS NULL")
    boolean existsByUserIdAndReturnDateIsNull(@Param("userId") Long userId);

    @Query("SELECT COUNT(p) > 0 FROM prestamos p WHERE p.libro.id = :bookId AND p.returnDate IS NULL")
    boolean existsByBookIdAndReturnDateIsNull(@Param("bookId") Long bookId);

    @Query("SELECT p.id FROM prestamos p ORDER BY p.id")
    List<Long> findAllIds();

    @Query("SELECT p FROM prestamos p WHERE p.usuario.id = :userId AND p.returnDate IS NULL")
    Page<Prestamo> findByUserIdAndReturnDateIsNull(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM prestamos p WHERE p.usuario.id = :userId AND p.returnDate IS NOT NULL")
    Page<Prestamo> findByUserIdAndReturnDateIsNotNull(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM prestamos p WHERE p.usuario.id = :userId AND p.loanDate BETWEEN :startDate AND :endDate")
    Page<Prestamo> findByUserIdAndLoanDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    @Query("SELECT p FROM prestamos p WHERE p.usuario.id = :userId AND p.returnDate IS NULL AND p.loanDate >= :desde")
    Page<Prestamo> findByUserIdAndReturnDateIsNullAndLoanDateGreaterThanEqual(@Param("userId") Long userId, @Param("desde") LocalDate desde, Pageable pageable);

    @Query("SELECT p FROM prestamos p WHERE p.usuario.id = :userId AND p.returnDate IS NOT NULL ORDER BY p.returnDate DESC")
    List<Prestamo> findTop5ByUserIdAndReturnDateIsNotNullOrderByReturnDateDesc(@Param("userId") Long userId);

    @Query("SELECT p FROM prestamos p WHERE p.usuario.id = :userId")
    List<Prestamo> findByUsuarioId(@Param("userId") Long userId);

    @Query("SELECT p FROM prestamos p WHERE p.libro.id = :bookId")
    List<Prestamo> findByLibroId(@Param("bookId") Long bookId);

    @Query("SELECT COUNT(p) > 0 FROM prestamos p WHERE p.usuario.id = :userId AND p.penaltyUntil >= :date")
    boolean hasActivePenalty(@Param("userId") Long userId, @Param("date") LocalDate date);
}