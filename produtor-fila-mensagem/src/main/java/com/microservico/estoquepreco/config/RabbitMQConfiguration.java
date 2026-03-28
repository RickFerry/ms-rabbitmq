package com.microservico.estoquepreco.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração centralizada para RabbitMQ com Spring Beans
 * Define as filas, exchange e bindings de forma declarativa
 */
@Configuration
public class RabbitMQConfiguration {

    private static final String NOME_EXCHANGE = "amq.direct";

    // Valores das filas podem ser customizados via properties
    @Value("${rabbitmq.queues.estoque:ESTOQUE}")
    private String nomeFilaEstoque;

    @Value("${rabbitmq.queues.preco:PRECO}")
    private String nomeFilaPreco;

    // ============ BEANS DAS FILAS ============

    /**
     * Cria a fila de ESTOQUE
     *
     * @return Queue configurada
     */
    @Bean
    public Queue filaEstoque() {
        return new Queue(nomeFilaEstoque, true, false, false);
    }

    /**
     * Cria a fila de PREÇO
     *
     * @return Queue configurada
     */
    @Bean
    public Queue filaPreco() {
        return new Queue(nomeFilaPreco, true, false, false);
    }

    // ============ BEAN DO EXCHANGE ============

    /**
     * Cria o DirectExchange para roteamento direto
     *
     * @return DirectExchange configurado
     */
    @Bean
    public DirectExchange trocaDireta() {
        return new DirectExchange(NOME_EXCHANGE);
    }

    // ============ BEANS DOS BINDINGS ============

    /**
     * Binding entre a fila de ESTOQUE e o exchange
     *
     * @param filaEstoque fila injetada
     * @param trocaDireta exchange injetado
     * @return Binding configurado
     */
    @Bean
    public Binding bindingEstoque(Queue filaEstoque, DirectExchange trocaDireta) {
        return BindingBuilder.bind(filaEstoque)
                .to(trocaDireta)
                .with(filaEstoque.getName());
    }

    /**
     * Binding entre a fila de PREÇO e o exchange
     *
     * @param filaPreco   fila injetada
     * @param trocaDireta exchange injetado
     * @return Binding configurado
     */
    @Bean
    public Binding bindingPreco(Queue filaPreco, DirectExchange trocaDireta) {
        return BindingBuilder.bind(filaPreco)
                .to(trocaDireta)
                .with(filaPreco.getName());
    }
}

