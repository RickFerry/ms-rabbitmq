package com.microservico.estoquepreco.controller;

import com.microservico.estoquepreco.service.RabbitmqService;
import constantes.RabbitmqConstantes;
import dto.EstoqueDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {
    private final Logger logger = LogManager.getLogger(EstoqueController.class);
    private final RabbitmqService rabbitmqService;

    public EstoqueController(RabbitmqService rabbitmqService) {
        this.rabbitmqService = rabbitmqService;
    }

    @PutMapping
    public ResponseEntity<Void> alteraEstoque(@RequestBody EstoqueDto estoqueDto) {
        logger.info("Alterando estoque do produto: {}", estoqueDto.codigoproduto());
        this.rabbitmqService.enviaMensagem(RabbitmqConstantes.FILA_ESTOQUE, estoqueDto);

        return ResponseEntity.ok().build();
    }
}
