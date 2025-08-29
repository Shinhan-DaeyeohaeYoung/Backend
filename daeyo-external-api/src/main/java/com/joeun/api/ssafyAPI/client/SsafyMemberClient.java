package com.joeun.api.ssafyAPI.client;

import com.joeun.api.ssafyAPI.dto.MemberCreateRequest;
import com.joeun.api.ssafyAPI.dto.MemberCreateResponse;
import com.joeun.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "ssafyMember",
    url = "${ssafy.base-url}",
    configuration = FeignConfig.class
)
public interface SsafyMemberClient {

  @PostMapping("/ssafy/api/v1/member")
  MemberCreateResponse createMember(@RequestBody MemberCreateRequest request);
}