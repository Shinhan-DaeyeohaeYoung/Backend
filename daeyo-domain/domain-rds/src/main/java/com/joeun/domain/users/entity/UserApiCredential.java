package com.joeun.domain.users.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_api_credentials")
@Getter
@Setter
@NoArgsConstructor
public class UserApiCredential {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // FK
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // 평문 저장 (암복호 제거)
  @Column(name = "api_key", nullable = false, length = 200)
  private String apiKey;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "rotated_at")
  private LocalDateTime rotatedAt;

}
