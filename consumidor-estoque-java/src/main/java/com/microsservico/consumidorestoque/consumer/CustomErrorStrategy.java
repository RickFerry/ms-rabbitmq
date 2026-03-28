package com.microsservico.consumidorestoque.consumer;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;

/**
 * Estratégia customizada para determinar quais exceções são consideradas fatais
 * Exceções fatais resultem em rejeição permanente da mensagem (sem retry)
 */
@Slf4j
public class CustomErrorStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

    /**
     * Determina se uma exceção deve ser tratada como fatal
     * Exceções fatais causam rejeição permanente da mensagem
     *
     * @param t exceção lançada durante processamento
     * @return true se exceção é fatal, false caso contrário
     */
    @Override
    public boolean isFatal(@NonNull Throwable t) {
        if (t instanceof ListenerExecutionFailedException listenerException) {
            String mensagem = new String(listenerException.getFailedMessage().getBody());
            String fila = listenerException.getFailedMessage().getMessageProperties().getConsumerQueue();

            log.error("Erro ao processar mensagem da fila {}. Mensagem: {}. Causa: {}",
                    fila, mensagem, listenerException.getCause().getMessage(), listenerException);

            // Considerar fatal apenas IllegalArgumentException
            // Outras exceções podem ter retry
            return listenerException.getCause() instanceof IllegalArgumentException;
        }

        log.warn("Erro não identificado como ListenerExecutionFailedException: {}", t.getClass().getName());
        return false;
    }
}
