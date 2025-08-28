package com.joeun.api.deposit.dto;

import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.entity.DepositEvent;
import com.joeun.domain.deposit.types.DepositEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositResponse {

  private Long id;                      // deposit_event.id (혹은 deposit.id 원하면 변경 가능)
  private BigDecimal amount;
  private String status;                // CREATED -> "예치", REFUNDED -> "환불", FORFEITED -> "몰수"
  private LocalDateTime created_updated_at; // occurred_at 매핑
  private String organization_name;

  public static DepositResponse from(DepositEvent e, String organizationName) {
    return DepositResponse.builder()
        .id(e.getId())  // 이벤트 id 사용. 만약 기존처럼 deposit id를 원하면 e.getDeposit().getId()
        .amount(e.getAmount())
        .status(mapToKoreanStatus(e.getEventType()))
        .created_updated_at(e.getOccurredAt())
        .organization_name(organizationName)
        .build();
  }

  private static String mapToKoreanStatus(DepositEventType t) {
    return switch (t) {
      case CREATED -> "예치";
      case REFUNDED -> "환불";
      case FORFEITED -> "몰수";
    };
  }

  public static DepositResponse from(
      Long eventId,
      BigDecimal amount,
      com.joeun.domain.deposit.types.DepositEventType eventType,
      LocalDateTime occurredAt,
      String organizationName
  ) {
    return DepositResponse.builder()
        .id(eventId)
        .amount(amount)
        .status(mapToKoreanStatus(eventType))
        .created_updated_at(occurredAt)
        .organization_name(organizationName)
        .build();
  }
}
