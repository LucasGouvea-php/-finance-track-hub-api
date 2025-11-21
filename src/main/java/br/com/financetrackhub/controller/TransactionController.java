package br.com.financetrackhub.controller;

import br.com.financetrackhub.dto.PageResponse;
import br.com.financetrackhub.dto.TransactionRequest;
import br.com.financetrackhub.dto.TransactionResponse;
import br.com.financetrackhub.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {
    
    private final TransactionService transactionService;
    
    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userEmail = getCurrentUserEmail();
        PageResponse<TransactionResponse> transactions = transactionService.findAllByUserPaginated(
                userEmail, page, size, type, categoryId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
        String userEmail = getCurrentUserEmail();
        TransactionResponse transaction = transactionService.findById(id, userEmail);
        return ResponseEntity.ok(transaction);
    }
    
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        String userEmail = getCurrentUserEmail();
        TransactionResponse transaction = transactionService.create(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        String userEmail = getCurrentUserEmail();
        TransactionResponse transaction = transactionService.update(id, request, userEmail);
        return ResponseEntity.ok(transaction);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        String userEmail = getCurrentUserEmail();
        transactionService.delete(id, userEmail);
        return ResponseEntity.noContent().build();
    }
    
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

