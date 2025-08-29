package com.joeun.domain.rental.dto;

public interface RentalCountProjection {
    Long getItemId();
    String getItemName();
    Integer getRentalCount();
    Long getOrganizationId();
    Integer getOverdueCount();
}

