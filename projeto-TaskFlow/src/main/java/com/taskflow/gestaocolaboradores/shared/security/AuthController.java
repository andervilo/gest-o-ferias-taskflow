package com.taskflow.gestaocolaboradores.shared.security;

import com.taskflow.gestaocolaboradores.shared.security.dto.LoginRequest;
import com.taskflow.gestaocolaboradores.shared.security.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e dados do usuário autenticado")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Login — retorna JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtService.generateToken(principal);
        return new LoginResponse(token, principal.getId(), principal.getFullName(),
                principal.getUsername(), principal.getRole());
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado")
    public LoginResponse me() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return new LoginResponse(null, principal.getId(), principal.getFullName(),
                principal.getUsername(), principal.getRole());
    }
}
