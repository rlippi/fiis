package com.renlip.fiis.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.renlip.fiis.domain.dto.ResumoJobResponse;
import com.renlip.fiis.support.AtualizarCotacoesJob;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

/**
 * Endpoints de disparo manual de jobs administrativos.
 *
 * <p>Restritos ao perfil {@code ADMIN} via {@code SecurityConfig}. Marcados com
 * {@link Hidden} para não aparecerem no Swagger público — são ferramentas
 * operacionais, não parte da API consumida pelo frontend. Servem de fallback
 * quando o agendamento interno não é suficiente (ex: Render free tier dormindo
 * no horário do cron) ou para disparar um ciclo extra sob demanda.</p>
 */
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Hidden
public class JobController {

    private final AtualizarCotacoesJob atualizarCotacoesJob;

    /**
     * Dispara o job de atualização de cotações imediatamente, ignorando a flag
     * {@code fiis.job.atualizar-cotacoes.enabled}.
     */
    @PostMapping("/atualizar-cotacoes")
    public ResponseEntity<ResumoJobResponse> atualizarCotacoes() {
        return ResponseEntity.ok(atualizarCotacoesJob.executar());
    }
}
