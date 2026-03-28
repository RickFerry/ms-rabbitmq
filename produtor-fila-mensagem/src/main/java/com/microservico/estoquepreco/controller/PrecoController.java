package com.microservico.estoquepreco.controller;

import com.microservico.estoquepreco.service.RabbitmqService;
import constantes.RabbitmqConstantes;
import dto.PrecoDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "preco")
public class PrecoController {
    private final Logger logger = LogManager.getLogger(PrecoController.class);
    private final RabbitmqService rabbitmqService;

    public PrecoController(RabbitmqService rabbitmqService) {
        this.rabbitmqService = rabbitmqService;
    }

    @PutMapping
    public ResponseEntity<Void> alteraPreco(@RequestBody PrecoDto precoDto) {
        logger.info("Alterando preço do produto: {}", precoDto.codigoproduto());
        this.rabbitmqService.enviaMensagem(RabbitmqConstantes.FILA_PRECO, precoDto);

        return ResponseEntity.ok().build();
    }
}
