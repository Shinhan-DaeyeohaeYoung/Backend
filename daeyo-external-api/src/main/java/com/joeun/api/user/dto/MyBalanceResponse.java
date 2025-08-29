package com.joeun.api.user.dto;

import java.math.BigDecimal;

public record MyBalanceResponse(
    BigDecimal accountBalance
) {}