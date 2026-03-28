package com.microsservico.consumidorestoque.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsservico.consumidorestoque.consumer.CustomErrorStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

/**
 * Configuração centralizada do RabbitMQ para processamento de mensagens
 * Define o listener container factory com tratamento robusto de erros
 */
@Slf4j
@Configuration
public class RabbitmqConfig {

    @Value("${rabbitmq.listener.prefetch:4}")
    private int prefetchCount;

    /**
     * Configura a factory para listeners RabbitMQ com tratamento de erro customizado
     * Usa DirectMessageListenerContainer para melhor performance
     *
     * @param connectionFactory factory de conexão do Spring AMQP
     * @return RabbitListenerContainerFactory configurado
     */
    @Bean
    public RabbitListenerContainerFactory<DirectMessageListenerContainer> rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        DirectRabbitListenerContainerFactory factory = new DirectRabbitListenerContainerFactory();

        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setPrefetchCount(prefetchCount);
        factory.setErrorHandler(errorHandler());

        log.info("RabbitMQ Listener Container configurado com prefetch={}", prefetchCount);
        return factory;
    }

    /**
     * Define a estratégia customizada de tratamento de erros fatais
     * Determina quais exceções devem resultar em rejeição permanente da mensagem
     *
     * @return FatalExceptionStrategy customizada
     */
    @Bean
    public FatalExceptionStrategy customErrorStrategy() {
        return new CustomErrorStrategy();
    }

    /**
     * Cria o handler de erro condicionado pela estratégia fatal
     * Rejeita mensagens se a estratégia considerar como fatal
     *
     * @return ErrorHandler configurado
     */
    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(customErrorStrategy());
    }

    /**
     * Fornece ObjectMapper para desserialização JSON
     * Utilizado pelos consumers para converter mensagens JSON em objetos Java
     *
     * @return ObjectMapper configurado
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
