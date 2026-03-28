# 🚀 MS-RabbitMQ: Microserviços com RabbitMQ

> **Arquitetura de Microserviços com Spring Boot e RabbitMQ para processamento de mensagens de Estoque e Preço**

---

## 📋 Visão Geral

Este projeto consiste em uma arquitetura de **3 componentes integrados** que trabalham juntos para processar atualizações de **Estoque** e **Preço** através de um sistema de **fila de mensagens** com RabbitMQ.

### 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                                                                 │
│  PRODUTOR (produtor-fila-mensagem)                              │
│  ├─ API REST (Port 8080)                                       │
│  │  ├─ PUT /estoque - Envia atualização de estoque             │
│  │  └─ PUT /preco   - Envia atualização de preço               │
│  │                                                              │
│  └─ RabbitMQ Service                                            │
│     └─ Publica mensagens em JSON para filas                   │
│                                                                 │
│                          RabbitMQ                               │
│                      (Message Broker)                           │
│                     ┌─────────────┐                             │
│                     │   ESTOQUE   │                             │
│                     │   PRECO     │                             │
│                     └─────────────┘                             │
│                                                                 │
│  CONSUMIDOR (consumidor-estoque-java)                           │
│  └─ @RabbitListener                                             │
│     └─ Processa mensagens de estoque em tempo real             │
│                                                                 │
│                                                                 │
│  LIB COMPARTILHADA (librabbitmq)                                │
│  ├─ RabbitmqConstantes (FILA_ESTOQUE, FILA_PRECO)             │
│  ├─ EstoqueDto (record com codigoproduto, quantidade)         │
│  └─ PrecoDto (record com codigoproduto, preco)                │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📂 Estrutura do Projeto

```
ms-rabbitmq/
├── librabbitmq/                              # Biblioteca compartilhada
│   ├── pom.xml
│   └── src/main/java/
│       ├── constantes/RabbitmqConstantes.java
│       └── dto/
│           ├── EstoqueDto.java
│           └── PrecoDto.java
│
├── produtor-fila-mensagem/                   # API Produtora
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.properties
│   └── src/main/java/com/microservico/estoquepreco/
│       ├── Application.java
│       ├── config/RabbitMQConfiguration.java
│       ├── controller/
│       │   ├── EstoqueController.java
│       │   └── PrecoController.java
│       └── service/RabbitmqService.java
│
├── consumidor-estoque-java/                  # Consumidor de Mensagens
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml
│   └── src/main/java/com/microsservico/consumidorestoque/
│       ├── Application.java
│       ├── config/RabbitmqConfig.java
│       └── consumer/
│           ├── EstoqueConsumer.java
│           └── CustomErrorStrategy.java
│
└── README.md                                 # Este arquivo
```

---

## 🔧 Componentes Detalhados

### 1. **librabbitmq** - Biblioteca Compartilhada

Biblioteca JAR que define as **constantes e DTOs** utilizados por todas as aplicações.

#### ✨ Funcionalidades

- **RabbitmqConstantes**: Define nomes das filas
  ```java
  public static final String FILA_ESTOQUE = "ESTOQUE";
  public static final String FILA_PRECO = "PRECO";
  ```

- **EstoqueDto**: Record para dados de estoque
  ```java
  public record EstoqueDto(String codigoproduto, Integer quantidade)
  ```

- **PrecoDto**: Record para dados de preço
  ```java
  public record PrecoDto(String codigoproduto, BigDecimal preco)
  ```

#### 📦 Dependências

```xml
<dependency>
  <groupId>org.librabbitmq</groupId>
  <artifactId>librabbitmq</artifactId>
  <version>1.0.1-SNAPSHOT</version>
</dependency>
```

#### 🚀 Como Usar

```bash
# Compilar e instalar no repositório local
cd librabbitmq
mvn clean install
```

---

### 2. **produtor-fila-mensagem** - API Produtora

API REST que **produz mensagens** para as filas de RabbitMQ. Responsável por enviar atualizações de **Estoque** e **Preço**.

#### ✨ Funcionalidades

##### **Controllers**

- **EstoqueController** (`PUT /estoque`)
  ```bash
  curl -X PUT http://localhost:8080/estoque \
    -H "Content-Type: application/json" \
    -d '{"codigoproduto":"P001","quantidade":100}'
  ```

- **PrecoController** (`PUT /preco`)
  ```bash
  curl -X PUT http://localhost:8080/preco \
    -H "Content-Type: application/json" \
    -d '{"codigoproduto":"P001","preco":99.99}'
  ```

##### **RabbitMQConfiguration**

Define **3 Beans principais**:

1. **Filas**: `filaEstoque()` e `filaPreco()`
   - Configuradas como durable (persistem após reinicialização)

2. **Exchange**: `trocaDireta()`
   - DirectExchange para roteamento direto

3. **Bindings**: Conectam filas ao exchange
   - `bindingEstoque()` → ESTOQUE
   - `bindingPreco()` → PRECO

##### **RabbitmqService**

Responsável por enviar mensagens:
```java
public void enviaMensagem(String nomeFila, Object mensagem) {
    String mensagemJson = objectMapper.writeValueAsString(mensagem);
    rabbitTemplate.convertAndSend(nomeFila, mensagemJson);
}
```

#### 📊 Stack Técnico

- **Java**: 17
- **Spring Boot**: 2.5.5
- **Spring AMQP**: spring-rabbit
- **Logging**: Log4j

#### 🔧 Configuração

**application.properties** (padrão):
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=123456

rabbitmq.queues.estoque=ESTOQUE
rabbitmq.queues.preco=PRECO

server.port=8080
```

#### 🚀 Como Executar

```bash
cd produtor-fila-mensagem

# Compilar
mvn clean compile

# Executar
mvn spring-boot:run

# API estará disponível em: http://localhost:8080
```

---

### 3. **consumidor-estoque-java** - Consumidor de Mensagens

Aplicação que **consome e processa mensagens** da fila ESTOQUE em tempo real.

#### ✨ Funcionalidades

##### **EstoqueConsumer**

Listener que processa mensagens da fila ESTOQUE:
```java
@RabbitListener(queues = RabbitmqConstantes.FILA_ESTOQUE)
public void consumidor(String mensagem) throws JsonProcessingException {
    EstoqueDto estoqueDto = objectMapper.readValue(mensagem, EstoqueDto.class);
    log.info("Mensagem recebida - Produto: {}, Quantidade: {}",
             estoqueDto.codigoproduto(), estoqueDto.quantidade());
    // Implementar lógica de negócio aqui
}
```

##### **RabbitmqConfig**

Configuração centralizada com **4 Beans**:

1. **rabbitListenerContainerFactory**: Factory para listeners
   - Prefetch: 4 mensagens
   - Acknowledge Mode: AUTO
   - Type: DIRECT

2. **customErrorStrategy**: Estratégia de erro customizada
   - Define exceções fatais vs retentáveis
   - IllegalArgumentException = Fatal

3. **errorHandler**: Handler de erro condicional
   - Rejeita permanentemente se fatal
   - Fila normalmente se recuperável

4. **objectMapper**: Para desserialização JSON

##### **CustomErrorStrategy**

Define qual exceção é fatal (não retorna à fila):
```java
@Override
public boolean isFatal(Throwable t) {
    if (t instanceof ListenerExecutionFailedException) {
        ListenerExecutionFailedException ex = (ListenerExecutionFailedException) t;
        log.error("Erro ao processar: {}", ex.getCause().getMessage(), ex);
        return ex.getCause() instanceof IllegalArgumentException;
    }
    return false;
}
```

#### 📊 Stack Técnico

- **Java**: 17
- **Spring Boot**: 2.5.5
- **Spring AMQP**: spring-boot-starter-amqp
- **Lombok**: Para @Slf4j
- **Logging**: SLF4J (Log4j backend via Spring)

#### 🔧 Configuração

**application.yml**:
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: ${RABBITMQ_PASSWORD:123456}
    listener:
      type: direct
      direct:
        prefetch: 4
        acknowledge-mode: auto
    connection-timeout: 5000ms
    requested-heartbeat: 60s

server:
  port: 8081

logging:
  level:
    root: INFO
    com.microsservico: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

#### 🚀 Como Executar

```bash
cd consumidor-estoque-java

# Compilar
mvn clean compile

# Executar
mvn spring-boot:run

# Logs aparecerão em tempo real
# [INFO] RabbitMQ Listener Container configurado com prefetch=4
```

---

## 🔄 Fluxo de Funcionamento

### 1️⃣ **Enviar Mensagem**
```bash
# Client envia PUT request para API Produtora
curl -X PUT http://localhost:8080/estoque \
  -H "Content-Type: application/json" \
  -d '{"codigoproduto":"P001","quantidade":100}'
```

### 2️⃣ **Processar no Produtor**
- EstoqueController recebe request
- Valida dados
- Chama RabbitmqService.enviaMensagem()

### 3️⃣ **Publicar em RabbitMQ**
- RabbitmqService serializa DTO para JSON
- RabbitTemplate envia para fila ESTOQUE
- Mensagem armazenada persistentemente

### 4️⃣ **Consumir no Consumer**
- EstoqueConsumer monitora fila ESTOQUE
- RabbitListener recebe mensagem automaticamente
- Desserializa JSON para EstoqueDto
- Processa logicamente
- Confirma processamento (ACK)

### 5️⃣ **Tratamento de Erro**
- Se erro recuperável: volta para fila
- Se erro fatal (IllegalArgumentException): vai para Dead Letter Queue
- Logs estruturados registram tudo

---

## 🐳 Pré-requisitos

### Instalação Local

1. **Java 17+**
   ```bash
   java -version
   # openjdk version "17.x.x"
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   # Apache Maven 3.8.x
   ```

3. **RabbitMQ**
   ```bash
   # Opção 1: Docker (Recomendado)
   docker run -d \
     --name rabbitmq \
     -p 5672:5672 \
     -p 15672:15672 \
     -e RABBITMQ_DEFAULT_USER=admin \
     -e RABBITMQ_DEFAULT_PASS=123456 \
     rabbitmq:3-management

   # Acessar Management UI: http://localhost:15672
   # Credentials: admin / 123456
   ```

   ```bash
   # Opção 2: Instalação Manual
   # macOS
   brew install rabbitmq
   brew services start rabbitmq
   
   # Linux (Ubuntu/Debian)
   sudo apt-get install rabbitmq-server
   sudo systemctl start rabbitmq-server
   ```

---

## 🚀 Instalação & Execução

### Passo 1: Clonar/Preparar Repositório

```bash
cd /home/ricardo/Documents/projects/ms-rabbitmq
```

### Passo 2: Compilar Biblioteca

```bash
cd librabbitmq
mvn clean install
cd ..
```

### Passo 3: Iniciar RabbitMQ

```bash
# Se usando Docker
docker start rabbitmq

# Ou verificar se está rodando
netstat -an | grep 5672
```

### Passo 4: Executar Produtor (Terminal 1)

```bash
cd produtor-fila-mensagem
mvn spring-boot:run

# Saída esperada:
# [INFO] Started Application in 2.5 seconds
# API pronta em http://localhost:8080
```

### Passo 5: Executar Consumidor (Terminal 2)

```bash
cd consumidor-estoque-java
mvn spring-boot:run

# Saída esperada:
# [INFO] RabbitMQ Listener Container configurado com prefetch=4
# [INFO] Listener aguardando mensagens...
```

### Passo 6: Testar Integração (Terminal 3)

```bash
# Teste 1: Enviar atualização de ESTOQUE
curl -X PUT http://localhost:8080/estoque \
  -H "Content-Type: application/json" \
  -d '{"codigoproduto":"P001","quantidade":100}'

# Esperado no Terminal 2:
# [INFO] Mensagem recebida da fila ESTOQUE - Produto: P001, Quantidade: 100

# Teste 2: Enviar atualização de PREÇO
curl -X PUT http://localhost:8080/preco \
  -H "Content-Type: application/json" \
  -d '{"codigoproduto":"P001","preco":99.99}'
```

---

## 📊 Monitoramento

### RabbitMQ Management UI

Acesse: **http://localhost:15672**
- **User**: admin
- **Password**: 123456

#### Visualizações Disponíveis:

1. **Queues**: Ver filas, mensagens enfileiradas, taxa de consumo
2. **Connections**: Conexões ativas
3. **Channels**: Canais abertos
4. **Nodes**: Saúde do servidor

### Logs em Tempo Real

**Produtor**:
```
[INFO] Alterando estoque do produto: P001
[INFO] Enviando mensagem para fila 'ESTOQUE': EstoqueDto[...]
```

**Consumidor**:
```
[INFO] RabbitMQ Listener Container configurado com prefetch=4
[INFO] Mensagem recebida da fila ESTOQUE - Produto: P001, Quantidade: 100
```

---

## 🔒 Tratamento de Erros

### Estratégia de Erro (CustomErrorStrategy)

| Tipo de Erro | Ação | Resultado |
|---|---|---|
| **IllegalArgumentException** | Fatal | ❌ Rejeita permanentemente |
| **JsonProcessingException** | Recuperável | 🔄 Retorna à fila |
| **RuntimeException** | Recuperável | 🔄 Retorna à fila |
| **Outros** | Recuperável | 🔄 Retorna à fila |

### Configuração de Retry

**application.yml do Consumidor**:
```yaml
spring:
  rabbitmq:
    listener:
      direct:
        prefetch: 4              # Processa 4 msgs simultaneamente
        acknowledge-mode: auto   # Confirma após sucesso
```

### Dead Letter Queue (DLQ)

Para mensagens que falham permanentemente (implementação futura):
```yaml
spring:
  rabbitmq:
    listener:
      direct:
        retry:
          enabled: true
          initial-interval: 1000ms
          max-attempts: 3
          max-interval: 10000ms
```

---

## 🏗️ Arquitetura de Dados

### Fluxo de Dados

```
┌─────────────────────────────────────────────────┐
│ Client HTTP Request                             │
│ {                                               │
│   "codigoproduto": "P001",                       │
│   "quantidade": 100                             │
│ }                                               │
└──────────────────┬──────────────────────────────┘
                   │
                   ▼
         ┌─────────────────────┐
         │ EstoqueController   │
         │ - Validação         │
         │ - Logging           │
         └──────────┬──────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │ RabbitmqService     │
         │ - Serialização JSON │
         │ - Send to Queue     │
         └──────────┬──────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │ RabbitMQ (ESTOQUE)  │
         │ Fila de Mensagens   │
         │ (Persistent)        │
         └──────────┬──────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │ EstoqueConsumer     │
         │ - RabbitListener    │
         │ - Desserialização   │
         │ - Processamento     │
         └──────────┬──────────┘
                    │
                    ▼
         ┌─────────────────────┐
         │ Lógica de Negócio   │
         │ (Banco, Cache, etc) │
         └─────────────────────┘
```

### Formato de Mensagem

```json
{
  "codigoproduto": "P001",
  "quantidade": 100
}
```

---

## 📈 Performance & Escalabilidade

### Configurações Recomendadas

| Parâmetro | Valor | Descrição |
|---|---|---|
| **prefetch** | 4 | Mensagens processadas por consumer |
| **connection-timeout** | 5000ms | Timeout de conexão |
| **heartbeat** | 60s | Keep-alive interval |
| **acknowledge-mode** | auto | Confirmação automática após sucesso |

### Métricas a Monitorar

1. **Taxa de Publicação**: Msgs/segundo produzidas
2. **Taxa de Consumo**: Msgs/segundo consumidas
3. **Latência**: Tempo de processamento por mensagem
4. **Tamanho da Fila**: Mensagens enfileiradas
5. **Taxa de Erro**: % de mensagens falhadas

---

## 🧪 Testes

### Teste de Carga

```bash
# Enviar 100 mensagens rapidamente
for i in {1..100}; do
  curl -X PUT http://localhost:8080/estoque \
    -H "Content-Type: application/json" \
    -d "{\"codigoproduto\":\"P$i\",\"quantidade\":$((RANDOM % 1000))}"
done
```

### Teste de Erro

```bash
# Enviar JSON inválido (vai falhar)
curl -X PUT http://localhost:8080/estoque \
  -H "Content-Type: application/json" \
  -d '{"codigoproduto":"P001"}'  # Falta quantity
```

### Teste de Reconexão

```bash
# Terminal 1: Parar RabbitMQ
docker stop rabbitmq

# Terminal 2: Consumidor tentará reconectar
# [ERROR] Failed to connect to RabbitMQ
# [INFO] Retrying connection...

# Terminal 1: Reiniciar RabbitMQ
docker start rabbitmq

# Terminal 2: Reconexão bem-sucedida
# [INFO] RabbitMQ Listener Container configurado com prefetch=4
```

---

## 🔍 Troubleshooting

### Problema: Connection Refused

```
java.net.ConnectException: Connection refused
```

**Solução**:
```bash
# Verificar se RabbitMQ está rodando
docker ps | grep rabbitmq
# ou
netstat -an | grep 5672

# Reiniciar RabbitMQ
docker restart rabbitmq
```

### Problema: No RabbitMQ Bean Found

```
No qualifying bean of type 'com.rabbitmq.client.Channel'
```

**Solução**:
- Adicionar `@Bean public ObjectMapper objectMapper()` em RabbitmqConfig
- Verificar imports no application

### Problema: Fila Vazia

```
[DEBUG] No messages received
```

**Verificar**:
- Consumidor está rodando?
- Produtor consegue publicar?
- RabbitMQ Management UI mostra mensagens?

### Problema: Mensagens Duplicadas

**Causa**: ACK manual falhando ou reconexão

**Solução**:
```yaml
spring:
  rabbitmq:
    listener:
      direct:
        acknowledge-mode: auto  # Usar AUTO em produção
```

---

## 📚 Documentação Adicional

### Arquivos Importantes

- **[produtor-fila-mensagem/README.md](./produtor-fila-mensagem/)**: Docs específicas do Produtor
- **[consumidor-estoque-java/README.md](./consumidor-estoque-java/)**: Docs específicas do Consumidor
- **[librabbitmq/README.md](./librabbitmq/)**: Docs específicas da Lib

### Referências

- [Spring AMQP Documentation](https://spring.io/projects/spring-amqp)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [Spring Boot with RabbitMQ](https://spring.io/guides/gs/messaging-rabbitmq/)

---

## 🤝 Contribuindo

Para adicionar novos recursos:

1. **Nova Fila**: Adicionar em `RabbitmqConstantes` + DTOs em `librabbitmq`
2. **Novo Endpoint**: Criar Controller em `produtor-fila-mensagem`
3. **Novo Consumer**: Criar `@RabbitListener` em `consumidor-estoque-java`
4. **Testes**: Adicionar em `src/test/java`

---

## 📄 Versões & Dependências

| Componente | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 2.5.5 |
| Spring AMQP | Included |
| RabbitMQ | 3.8+ |
| Maven | 3.6+ |
| Lombok | 1.18+ |

---

## 🎯 Próximas Melhorias

- [ ] Implementar Dead Letter Queue (DLQ)
- [ ] Adicionar retry automático configurável
- [ ] Circuit breaker pattern
- [ ] Métricas com Micrometer/Prometheus
- [ ] Health checks e liveness probes
- [ ] Integração com ELK Stack
- [ ] Testes integrados automatizados
- [ ] Documentação OpenAPI/Swagger

---

## 📞 Suporte

Para dúvidas ou problemas:

1. Verificar logs estruturados com timestamp
2. Acessar RabbitMQ Management UI (http://localhost:15672)
3. Consultar Spring AMQP documentation
4. Verificar conectividade de rede

---

## 📝 Licença

Projeto interno de microserviços - Direitos reservados.

---

## ✨ Resumo

| Aspecto | Status |
|---|---|
| **Produtor** | ✅ Funcional (Port 8080) |
| **Consumidor** | ✅ Funcional (Port 8080) |
| **Lib Compartilhada** | ✅ Instalável |
| **Integração** | ✅ Completa |
| **Logging** | ✅ Estruturado (SLF4J) |
| **Tratamento Erro** | ✅ Customizado |
| **Documentação** | ✅ Completa |

---

**Última Atualização**: 28 de Março de 2026  
**Versão**: 1.0.0  
**Status**: Production Ready ✅


