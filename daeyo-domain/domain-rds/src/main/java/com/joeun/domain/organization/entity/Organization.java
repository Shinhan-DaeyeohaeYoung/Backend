package com.joeun.domain.organization.entity;

import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.organization.types.OrganizationType;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.users.entity.UserOrgMembership;
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
    name = "organization",
    indexes = {
        @Index(name = "idx_org_univ_parent", columnList = "university_id, parent_organization_id"),
        @Index(name = "idx_org_id_univ", columnList = "id, university_id") // 같은 대학 강제용 보조 인덱스(복합 FK 타깃)
    }
)
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "bigint")
  private Long id;

  /* ===== 소속 대학 ===== */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "university_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_organization_university")
  )
  private University university;

  /* ===== 기본 정보 ===== */
  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private OrganizationType type; // UNIVERSITY / COLLEGE / DEPARTMENT / LAB / CENTER / CLUB

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /* ===== 자기참조(조직 트리) ===== */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "parent_organization_id",
      foreignKey = @ForeignKey(name = "fk_organization_parent")
  )
  private Organization parent; // 최상위는 null

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  private List<Organization> children = new ArrayList<>();

  /* ===== 연관관계(읽기 위주 컬렉션) ===== */
  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private List<UserOrgMembership> memberships = new ArrayList<>();

  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private List<Deposit> deposits = new ArrayList<>();

  /*
  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private List<Item> items = new ArrayList<>();

  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private List<Holding> holdings = new ArrayList<>();

  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private List<Rental> rentals = new ArrayList<>();

  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private List<ReturnPhoto> returnPhotos = new ArrayList<>();


  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private List<ReturnRequest> returnRequests = new ArrayList<>();

  @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
  private List<ItemRequest> itemRequests = new ArrayList<>();

  */
}