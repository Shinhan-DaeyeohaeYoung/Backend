package com.joeun.domain.users.entity;

import com.joeun.domain.university.entity.University;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "`user`",
    indexes = {
        @Index(name = "idx_user_university_id", columnList = "university_id"),
        @Index(name = "idx_user_email", columnList = "email", unique = true)
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

  /* ===== 기본 컬럼 ===== */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "bigint")
  private Long id;

  // FK: university.id
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "university_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_user_university")
  )
  private University university;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "email", nullable = false, unique = true, length = 120)
  private String email;

  @Column(name = "student_id", length = 64)
  private String studentId;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  // ERD: varchar(32) note: '예: USER/ADMIN'
  // 굳이 Enum 강제하지 않고 문자열로 유지(운영 유연성)
  @Column(name = "role", length = 32)
  private String role;

  @Column(name = "point", nullable = false)
  private long point;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserApiCredential> apiCredentials = new ArrayList<>();

  // 헬퍼 메서드 (연관관계 편의)
  public void addApiCredential(UserApiCredential credential) {
    apiCredentials.add(credential);
    credential.setUser(this);
  }

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<UserOrgMembership> memberships = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<UserBankAccount> bankAccounts = new ArrayList<>();

  /*
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<Rental> rentals = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<Deposit> deposits = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<WaitlistEntry> waitlistEntries = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<Holding> holdings = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<ReturnRequest> returnRequests = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<ItemRequest> itemRequests = new ArrayList<>();

 */

}
