package com.microservico.estoquepreco.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitmqService {
    private final Logger logger = LogManager.getLogger(RabbitmqService.class);
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitmqService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void enviaMensagem(String nomeFila, Object mensagem) {
        logger.info("Enviando mensagem para fila '{}': {}", nomeFila, mensagem);
        try {
            String mensagemJson = this.objectMapper.writeValueAsString(mensagem);
            this.rabbitTemplate.convertAndSend(nomeFila, mensagemJson);
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem para fila '{}': {}", nomeFila, e.getMessage());
        }
    }
}
