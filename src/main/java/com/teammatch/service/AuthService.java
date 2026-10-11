package com.teammatch.service;

import com.teammatch.dto.AuthResponse;
import com.teammatch.dto.LoginRequest;
import com.teammatch.dto.RegistrationRequest;
import com.teammatch.entity.User;
import com.teammatch.entity.UserStatus;
import com.teammatch.repository.UserRepository;
import com.teammatch.security.CustomUserDetails;
import com.teammatch.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCollege(request.getCollege());
        user.setCourse(request.getCourse());
        user.setYear(request.getYear());
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        String jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        return new AuthResponse(jwtToken, user.getEmail(), user.getFullName(), user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        String jwtToken = jwtService.generateToken(new CustomUserDetails(user));
        return new AuthResponse(jwtToken, user.getEmail(), user.getFullName(), user.getId());
    }
}
