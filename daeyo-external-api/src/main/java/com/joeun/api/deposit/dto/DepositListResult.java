package com.joeun.api.deposit.dto;

import java.util.List;

public record DepositListResult(
    List<DepositResponse> content,
    long totalElements
) {}