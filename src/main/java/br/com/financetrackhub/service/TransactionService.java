package br.com.financetrackhub.service;

import br.com.financetrackhub.dto.DashboardResponse;
import br.com.financetrackhub.dto.PageResponse;
import br.com.financetrackhub.dto.TransactionRequest;
import br.com.financetrackhub.dto.TransactionResponse;
import br.com.financetrackhub.entity.Category;
import br.com.financetrackhub.entity.Transaction;
import br.com.financetrackhub.entity.User;
import br.com.financetrackhub.exception.BadRequestException;
import br.com.financetrackhub.repository.CategoryRepository;
import br.com.financetrackhub.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> findAllByUserPaginated(
            String userEmail, 
            int page, 
            int size,
            String type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate) {
        User user = userService.findByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);
        
        Transaction.TransactionType transactionType = null;
        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("todos")) {
            try {
                transactionType = Transaction.TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Tipo de transação inválido: " + type);
            }
        }
        
        Page<Transaction> transactionPage;
        
        if (transactionType == null && categoryId == null && startDate == null && endDate == null) {
            transactionPage = transactionRepository.findByUserOrderByIdDesc(user, pageable);
        } else {
            transactionPage = transactionRepository.findByUserWithFilters(
                    user, transactionType, categoryId, startDate, endDate, pageable);
        }
        
        transactionPage.getContent().forEach(t -> {
            Hibernate.initialize(t.getCategory());
        });
        
        return new PageResponse<>(
                transactionPage.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()),
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements(),
                transactionPage.getTotalPages(),
                transactionPage.isFirst(),
                transactionPage.isLast()
        );
    }
    
    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BadRequestException("Transação não encontrada"));
        Hibernate.initialize(transaction.getCategory());
        return toResponse(transaction);
    }
    
    @Transactional
    public TransactionResponse create(TransactionRequest request, String userEmail) {
        User user = userService.findByEmail(userEmail);
        
        Transaction.TransactionType transactionType;
        try {
            transactionType = Transaction.TransactionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de transação inválido. Use 'INCOME' ou 'EXPENSE'");
        }
        
        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                .orElseThrow(() -> new BadRequestException("Categoria não encontrada ou não pertence ao usuário"));
        
        Transaction transaction = new Transaction();
        transaction.setType(transactionType);
        transaction.setValue(request.getValue());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setCategory(category);
        transaction.setUser(user);
        
        transaction = transactionRepository.save(transaction);
        return toResponse(transaction);
    }
    
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BadRequestException("Transação não encontrada"));
        
        Transaction.TransactionType transactionType;
        try {
            transactionType = Transaction.TransactionType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Tipo de transação inválido. Use 'INCOME' ou 'EXPENSE'");
        }
        
        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                .orElseThrow(() -> new BadRequestException("Categoria não encontrada ou não pertence ao usuário"));
        
        transaction.setType(transactionType);
        transaction.setValue(request.getValue());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        transaction.setCategory(category);
        
        transaction = transactionRepository.save(transaction);
        return toResponse(transaction);
    }
    
    @Transactional
    public void delete(Long id, String userEmail) {
        User user = userService.findByEmail(userEmail);
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new BadRequestException("Transação não encontrada"));
        transactionRepository.delete(transaction);
    }
    
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(String userEmail) {
        User user = userService.findByEmail(userEmail);
        List<Transaction> transactions = transactionRepository.findByUserOrderByDateAsc(user);
        
        transactions.forEach(t -> Hibernate.initialize(t.getCategory()));
        
        BigDecimal income = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal expenses = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal balance = income.subtract(expenses);
        
        DashboardResponse.Summary summary = new DashboardResponse.Summary(income, expenses, balance);
        
        Map<String, BigDecimal> expensesByCategory = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getValue, BigDecimal::add)
                ));
        
        List<DashboardResponse.CategoryData> categoryData = expensesByCategory.entrySet().stream()
                .map(entry -> new DashboardResponse.CategoryData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        BigDecimal initialBalance = transactions.stream()
                .filter(t -> t.getDate().isBefore(firstDayOfMonth))
                .map(t -> t.getType() == Transaction.TransactionType.INCOME 
                        ? t.getValue() 
                        : t.getValue().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<Transaction> currentMonthTransactions = transactions.stream()
                .filter(t -> !t.getDate().isBefore(firstDayOfMonth) && !t.getDate().isAfter(lastDayOfMonth))
                .collect(Collectors.toList());
        
        if (currentMonthTransactions.isEmpty()) {
            return new DashboardResponse(summary, categoryData, new ArrayList<>());
        }
        
        LocalDate lastTransactionDate = currentMonthTransactions.stream()
                .map(Transaction::getDate)
                .max(LocalDate::compareTo)
                .orElse(firstDayOfMonth);
        
        LocalDate endDate = lastTransactionDate;
        if (endDate.isAfter(now)) {
            endDate = now;
        }
        
        Map<LocalDate, BigDecimal> transactionsByDate = currentMonthTransactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getDate,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                t -> t.getType() == Transaction.TransactionType.INCOME 
                                        ? t.getValue() 
                                        : t.getValue().negate(),
                                BigDecimal::add
                        )
                ));
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        List<DashboardResponse.MonthlyData> monthlyData = new ArrayList<>();
        BigDecimal runningBalance = initialBalance;
        
        LocalDate currentDate = firstDayOfMonth;
        while (currentDate.isBefore(endDate) || currentDate.isEqual(endDate)) {
            BigDecimal dayValue = transactionsByDate.getOrDefault(currentDate, BigDecimal.ZERO);
            runningBalance = runningBalance.add(dayValue);
            
            monthlyData.add(new DashboardResponse.MonthlyData(
                    currentDate.format(formatter),
                    runningBalance
            ));
            
            currentDate = currentDate.plusDays(1);
            
            if (currentDate.isAfter(endDate)) {
                break;
            }
        }
        
        return new DashboardResponse(summary, categoryData, monthlyData);
    }
    
    private TransactionResponse toResponse(Transaction transaction) {
        Category category = transaction.getCategory();
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType().name(),
                transaction.getValue(),
                transaction.getDescription(),
                transaction.getDate(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}

