package com.renlip.fiis.controller;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller de verificação de saúde da API.
 *
 * @RestController: combina @Controller + @ResponseBody.
 *   Significa que todo método retorna JSON direto (sem passar por view/template).
 *
 * @RequestMapping("/api/health"): define o caminho base deste controller.
 *   Todos os endpoints aqui começam com /api/health.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Endpoint: GET /api/health
     * Retorna um JSON indicando que a API está funcionando.
     *
     * Usamos um "record" (Java 14+) como DTO de resposta.
     * Records são classes imutáveis com getters, equals, hashCode
     * e toString gerados automaticamente. Muito menos verbose que POJOs.
     */
    @GetMapping
    public HealthResponse check() {
        return new HealthResponse(
            "UP",
            "FIIs API está no ar!",
            LocalDateTime.now()
        );
    }

    /**
     * DTO de resposta usando record.
     * O Spring serializa automaticamente para JSON.
     */
    public record HealthResponse(
        String status,
        String message,
        LocalDateTime timestamp
    ) {}
}
