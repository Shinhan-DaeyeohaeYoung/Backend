package com.joeun.domain.university.entity;

import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "university",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_university_code", columnNames = "code")
    }
)
@Data
@NoArgsConstructor
public class University {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "bigint")
  private Long id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 64)
  private String code;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /* ===== 연관관계 ===== */

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<Organization> organizations = new ArrayList<>();

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<User> users = new ArrayList<>();

  /*
  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<Item> items = new ArrayList<>();

  // 포인트 이력/스냅샷 성격으로 다건 가정 (단건이면 OneToOne로 변경)
  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<UniversityPoint> points = new ArrayList<>();

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<WaitlistEntry> waitlistEntries = new ArrayList<>();

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<Holding> holdings = new ArrayList<>();

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<Rental> rentals = new ArrayList<>();

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<ReturnPhoto> returnPhotos = new ArrayList<>();

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<Deposit> deposits = new ArrayList<>();

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<ReturnRequest> returnRequests = new ArrayList<>();

  @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
  private List<ItemRequest> itemRequests = new ArrayList<>();

   */
}