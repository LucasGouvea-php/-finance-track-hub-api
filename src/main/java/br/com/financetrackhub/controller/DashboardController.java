package br.com.financetrackhub.controller;

import br.com.financetrackhub.dto.DashboardResponse;
import br.com.financetrackhub.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private final TransactionService transactionService;
    
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        String userEmail = getCurrentUserEmail();
        DashboardResponse dashboard = transactionService.getDashboardData(userEmail);
        return ResponseEntity.ok(dashboard);
    }
    
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

