package com.miguel_mejia.fincostos_backend.repository;

import java.math.BigDecimal;

public interface SummaryAggregate {

    BigDecimal getTotal();

    BigDecimal getTotalCajas();

    Long getTotalRegistros();
}
