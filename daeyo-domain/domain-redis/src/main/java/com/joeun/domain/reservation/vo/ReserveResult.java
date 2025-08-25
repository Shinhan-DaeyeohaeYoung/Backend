package com.joeun.domain.reservation.vo;

import lombok.Builder;

public record ReserveResult (
        boolean ok,
        String expireEpoch,
        String reason
){
    @Builder
    public ReserveResult (boolean ok, String expireEpoch, String reason) {
        this.ok = ok;
        this.expireEpoch = expireEpoch;
        this.reason = reason;
    }
}
