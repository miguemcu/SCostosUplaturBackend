package com.miguel_mejia.fincostos_backend.repository;

import java.math.BigDecimal;

public interface MonthlySalesAggregate {

    Integer getMes();

    BigDecimal getTotal();

    BigDecimal getTotalCajas();
}