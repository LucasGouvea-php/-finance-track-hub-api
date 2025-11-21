package br.com.financetrackhub.repository;

import br.com.financetrackhub.entity.Category;
import br.com.financetrackhub.entity.Transaction;
import br.com.financetrackhub.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserOrderByIdDesc(User user, Pageable pageable);
    
    Page<Transaction> findByUserAndTypeOrderByIdDesc(User user, Transaction.TransactionType type, Pageable pageable);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "ORDER BY t.id DESC")
    Page<Transaction> findByUserWithFilters(
            @Param("user") User user,
            @Param("type") Transaction.TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    
    List<Transaction> findByUserOrderByDateAsc(User user);
    
    Optional<Transaction> findByIdAndUser(Long id, User user);
}

