package br.com.financetrackhub.service;

import br.com.financetrackhub.dto.CategoryRequest;
import br.com.financetrackhub.dto.CategoryResponse;
import br.com.financetrackhub.dto.PageResponse;
import br.com.financetrackhub.entity.Category;
import br.com.financetrackhub.entity.User;
import br.com.financetrackhub.exception.BadRequestException;
import br.com.financetrackhub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAllByUser(String userEmail) {
        User user = userService.findByEmail(userEmail);
        List<Category> categories = categoryRepository.findByUser(user);
        return categories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> findAllByUserPaginated(String userEmail, int page, int size) {
        User user = userService.findByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findByUserOrderByIdDesc(user, pageable);
        
        List<CategoryResponse> content = categoryPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                content,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isFirst(),
                categoryPage.isLast()
        );
    }
    
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BadRequestException("Categoria não encontrada"));
        return toResponse(category);
    }
    
    @Transactional
    public CategoryResponse create(CategoryRequest request, String userEmail) {
        User user = userService.findByEmail(userEmail);
        
        // Verifica se já existe uma categoria com o mesmo nome para o usuário
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new BadRequestException("Já existe uma categoria com este nome");
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setUser(user);
        
        category = categoryRepository.save(category);
        return toResponse(category);
    }
    
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BadRequestException("Categoria não encontrada"));
        
        // Verifica se já existe outra categoria com o mesmo nome (exceto a atual)
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new BadRequestException("Já existe uma categoria com este nome");
        }
        
        category.setName(request.getName());
        
        category = categoryRepository.save(category);
        return toResponse(category);
    }
    
    @Transactional
    public void delete(Long id, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BadRequestException("Categoria não encontrada"));
        categoryRepository.delete(category);
    }
    
    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}

