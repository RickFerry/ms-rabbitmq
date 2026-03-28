package com.microsservico.consumidorestoque.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import constantes.RabbitmqConstantes;
import dto.EstoqueDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de mensagens da fila de ESTOQUE
 * Processa atualizações de estoque recebidas via RabbitMQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EstoqueConsumer {
    private final ObjectMapper objectMapper;

    /**
     * Processa mensagem recebida da fila de ESTOQUE
     * A mensagem é deserializada de JSON para EstoqueDto
     *
     * @param mensagem conteúdo JSON da mensagem
     * @throws JsonProcessingException se houver erro ao deserializar JSON
     */
    @RabbitListener(queues = RabbitmqConstantes.FILA_ESTOQUE)
    public void consumidor(String mensagem) throws JsonProcessingException {
        try {
            EstoqueDto estoqueDto = objectMapper.readValue(mensagem, EstoqueDto.class);

            log.info("Mensagem recebida da fila ESTOQUE - Produto: {}, Quantidade: {}",
                    estoqueDto.codigoproduto(), estoqueDto.quantidade());

            // TODO: Implementar lógica de negócio aqui
            // Exemplo: salvar em banco de dados, atualizar cache, etc.

        } catch (JsonProcessingException e) {
            log.error("Erro ao deserializar mensagem JSON: {}", mensagem, e);
            throw e; // Re-lançar para processamento de erro do RabbitMQ
        } catch (Exception e) {
            log.error("Erro ao processar mensagem da fila de ESTOQUE: {}", mensagem, e);
            throw new RuntimeException("Erro ao processar estoque", e);
        }
    }
}
