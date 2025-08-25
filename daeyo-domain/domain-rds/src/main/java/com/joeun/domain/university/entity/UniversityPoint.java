package com.joeun.domain.university.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "university_point")
@Data
@NoArgsConstructor
public class UniversityPoint {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "bigint")
  private Long id;

  /* ===== 소속 대학 ===== */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "university_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_university_point_university")
  )
  private University university;

  /* ===== 포인트 값 ===== */
  @Column(name = "point", nullable = false)
  private Long point;

  /* ===== 생성/수정 시각 ===== */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}