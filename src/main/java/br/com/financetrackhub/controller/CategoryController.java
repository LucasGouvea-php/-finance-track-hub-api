package br.com.financetrackhub.controller;

import br.com.financetrackhub.dto.CategoryRequest;
import br.com.financetrackhub.dto.CategoryResponse;
import br.com.financetrackhub.dto.PageResponse;
import br.com.financetrackhub.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        String userEmail = getCurrentUserEmail();
        PageResponse<CategoryResponse> categories = categoryService.findAllByUserPaginated(userEmail, page, size);
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        String userEmail = getCurrentUserEmail();
        CategoryResponse category = categoryService.findById(id, userEmail);
        return ResponseEntity.ok(category);
    }
    
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        String userEmail = getCurrentUserEmail();
        CategoryResponse category = categoryService.create(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        String userEmail = getCurrentUserEmail();
        CategoryResponse category = categoryService.update(id, request, userEmail);
        return ResponseEntity.ok(category);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        String userEmail = getCurrentUserEmail();
        categoryService.delete(id, userEmail);
        return ResponseEntity.noContent().build();
    }
    
    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}

