package com.joeun.api.deposit.dto;

import com.joeun.domain.deposit.entity.DepositEvent;
import com.joeun.domain.deposit.types.DepositEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrgDepositResponse {

  private Long id;                      // deposit_event.id
  private BigDecimal amount;
  private String status;                // "예치" / "환불" / "몰수"
  private LocalDateTime created_updated_at;
  private String user_name;             // 사용자 이름

  public static OrgDepositResponse from(DepositEvent e, String userName) {
    return OrgDepositResponse.builder()
        .id(e.getId())
        .amount(e.getAmount())
        .status(mapToKoreanStatus(e.getEventType()))
        .created_updated_at(e.getOccurredAt())
        .user_name(userName)
        .build();
  }

  public static OrgDepositResponse from(
      Long eventId,
      BigDecimal amount,
      DepositEventType eventType,
      LocalDateTime occurredAt,
      String userName
  ) {
    return OrgDepositResponse.builder()
        .id(eventId)
        .amount(amount)
        .status(mapToKoreanStatus(eventType))
        .created_updated_at(occurredAt)
        .user_name(userName)
        .build();
  }

  private static String mapToKoreanStatus(DepositEventType t) {
    return switch (t) {
      case CREATED   -> "예치";
      case REFUNDED  -> "환불";
      case FORFEITED -> "몰수";
    };
  }
}