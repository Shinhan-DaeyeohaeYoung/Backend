package com.joeun.domain.students.entity;

import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.users.types.UserOrgRole;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "student_org_affiliation",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_student_org_role",
            columnNames = {"student_id", "organization_id", "org_role"})
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class StudentOrgAffiliation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id",
      foreignKey = @ForeignKey(name = "fk_soa_student"))
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id",
      foreignKey = @ForeignKey(name = "fk_soa_organization"))
  private Organization organization;

  @Enumerated(EnumType.STRING)
  @Column(name = "org_role", nullable = false, length = 20)
  private UserOrgRole orgRole;
}
