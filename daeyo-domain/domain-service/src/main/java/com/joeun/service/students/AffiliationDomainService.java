package com.joeun.service.students;

import com.joeun.domain.students.entity.StudentOrgAffiliation;
import com.joeun.domain.students.repository.AffiliationRepository;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.domain.users.repository.UserOrgMembershipRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AffiliationDomainService {
  private final AffiliationRepository affilRepo;
  private final UserOrgMembershipRepository uomRepo;

  @Transactional
  public List<StudentOrgAffiliation> findAllByStudentId(Long studentId) {
    return affilRepo.findAllByStudent_Id(studentId);
  }

  @Transactional
  public List<UserOrgMembership> migrateToUserMemberships(User user, List<StudentOrgAffiliation> affs) {
    List<UserOrgMembership> out = new ArrayList<>();
    for (StudentOrgAffiliation a : affs) {
      // upsert: (user, org, role) 유니크 보장
      if (!uomRepo.existsByUser_IdAndOrganization_IdAndRole(user.getId(), a.getOrganization().getId(), a.getOrgRole())) {
        UserOrgMembership m = UserOrgMembership.builder()
            .user(user)
            .organization(a.getOrganization()) // affiliation이 이미 Organization을 들고 있음
            .role(a.getOrgRole())           // UserOrgRole enum 공유
            .build();
        out.add(uomRepo.save(m));
      }
    }
    return out;
  }
}