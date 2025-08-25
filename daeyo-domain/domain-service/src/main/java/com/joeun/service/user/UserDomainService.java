package com.joeun.service.user;

import com.joeun.domain.university.entity.University;
import com.joeun.domain.university.repository.UniversityRepository;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDomainService {

  private final UserRepository userRepo;
  private final UniversityRepository univRepo;

  public void createUser(User user){
    userRepo.save(user);
  }

  public University getUniversity(Long univ_id){
    return univRepo.getReferenceById(univ_id);
  }

}
