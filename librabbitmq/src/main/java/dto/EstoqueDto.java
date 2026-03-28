package dto;

import java.io.Serializable;

public record EstoqueDto(String codigoproduto, Integer quantidade) implements Serializable {
}
