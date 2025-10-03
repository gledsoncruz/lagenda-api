package com.lasystems.lagenda.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de cache para melhorar performance.
 * Usa Caffeine como provider de cache.
 *
 * IMPORTANTE: Certifique-se de ter as seguintes dependências no pom.xml:
 * - spring-boot-starter-cache
 * - caffeine (com.github.ben-manes.caffeine)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura o gerenciador de cache com Caffeine.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "businessHours",
                "companyClosures",
                "providers",
                "specialties"
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());

        return cacheManager;
    }

    /**
     * Configura o builder do Caffeine com políticas de expiração.
     */
    Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats();
    }
}