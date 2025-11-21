package br.com.financetrackhub.service;

import br.com.financetrackhub.dto.AuthResponse;
import br.com.financetrackhub.dto.LoginRequest;
import br.com.financetrackhub.dto.RegisterRequest;
import br.com.financetrackhub.entity.User;
import br.com.financetrackhub.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email já está em uso");
        }
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        user = userService.save(user);
        
        String token = jwtService.generateToken(user.getEmail());
        
        AuthResponse.UserResponse userResponse = new AuthResponse.UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
        
        return new AuthResponse(token, userResponse);
    }
    
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        User user = userService.findByEmail(request.getEmail());
        String token = jwtService.generateToken(user.getEmail());
        
        AuthResponse.UserResponse userResponse = new AuthResponse.UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
        
        return new AuthResponse(token, userResponse);
    }
}

