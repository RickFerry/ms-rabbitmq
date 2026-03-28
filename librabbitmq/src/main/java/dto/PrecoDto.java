package dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record PrecoDto(String codigoproduto, BigDecimal preco) implements Serializable {
}