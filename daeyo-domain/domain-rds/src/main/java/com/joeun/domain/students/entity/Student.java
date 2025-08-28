package com.joeun.domain.students.entity;

import com.joeun.domain.students.types.SignupStatus;
import com.joeun.domain.university.entity.University;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_student_univ_no", columnNames = {"university_id", "student_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "university_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_student_univ"))
  private University university;

  @Column(name = "student_no", length = 50, nullable = false)
  private String studentNo;

  @Column(name = "name", length = 100, nullable = false)
  private String name;

  @Column(name = "email", length = 120, nullable = false)
  private String email;

  @Column(name = "password_hash", length = 255, nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "signup_status", nullable = false, length = 20)
  private SignupStatus signupStatus = SignupStatus.PENDING;
}
