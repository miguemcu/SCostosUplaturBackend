package com.miguel_mejia.fincostos_backend.repository;

import java.math.BigDecimal;

public interface MonthlyExpenseAggregate {

    Integer getMes();

    BigDecimal getTotal();
}