package com.miguel_mejia.fincostos_backend.repository;

import java.math.BigDecimal;

public interface ExpenseSummaryAggregate {

    BigDecimal getTotal();

    Long getTotalRegistros();
}