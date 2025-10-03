package com.lasystems.lagenda.security;

import com.lasystems.lagenda.models.ApiKey;
import com.lasystems.lagenda.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Filtro para autenticação via API Key.
 * Intercepta requisições com header "X-API-Key" e valida a chave.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Se já está autenticado (ex: via JWT), pular
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Buscar API Key no header
        String apiKey = extractApiKey(request);

        if (apiKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Validar API Key
        Optional<ApiKey> validatedKey = apiKeyService.validateApiKey(apiKey);

        if (validatedKey.isEmpty()) {
            log.warn("API Key inválida recebida de IP: {}", request.getRemoteAddr());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Invalid API Key\"}");
            return;
        }

        ApiKey key = validatedKey.get();

        // Criar autenticação
        ApiKeyAuthenticationToken authentication = new ApiKeyAuthenticationToken(
                key,
                buildAuthorities(key)
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("API Key autenticada: {} para company: {}",
                key.getKeyPrefix(), key.getCompany().getId());

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai API Key do header da requisição.
     */
    private String extractApiKey(HttpServletRequest request) {
        // Tentar header X-API-Key
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey.trim();
        }

        // Tentar Authorization: Bearer <api-key>
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (token.startsWith("lag_")) {
                return token;
            }
        }

        return null;
    }

    /**
     * Constrói authorities baseado nas permissões da API Key.
     */
    private Collection<? extends GrantedAuthority> buildAuthorities(ApiKey apiKey) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Adicionar role base
        authorities.add(new SimpleGrantedAuthority("ROLE_API"));

        // Adicionar permissões específicas
        if (apiKey.getPermissions() != null) {
            for (String permission : apiKey.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }

        return authorities;
    }

    /**
     * Token de autenticação customizado para API Keys.
     * IMPORTANTE: O construtor já marca como authenticated=true automaticamente.
     */
    public static class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
        private final ApiKey apiKey;
        private final Object principal;

        public ApiKeyAuthenticationToken(
                ApiKey apiKey,
                Collection<? extends GrantedAuthority> authorities
        ) {
            super(authorities);
            this.apiKey = apiKey;
            this.principal = apiKey.getCompany().getId();
            super.setAuthenticated(true); // Marcar como autenticado
        }

        @Override
        public Object getCredentials() {
            return null; // Não expor credenciais
        }

        @Override
        public Object getPrincipal() {
            return principal;
        }

        public ApiKey getApiKey() {
            return apiKey;
        }

        /**
         * Previne mudança do estado de autenticação após construção.
         */
        @Override
        public void setAuthenticated(boolean authenticated) {
            if (!authenticated) {
                super.setAuthenticated(false);
            } else {
                throw new IllegalArgumentException(
                        "Cannot set this token to trusted - use constructor instead"
                );
            }
        }
    }
}