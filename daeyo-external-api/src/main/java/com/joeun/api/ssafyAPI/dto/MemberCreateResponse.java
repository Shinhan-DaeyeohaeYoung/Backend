package com.joeun.api.ssafyAPI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MemberCreateResponse {
  @JsonProperty("userId")
  private String userId;
  @JsonProperty("userName")
  private String userName;
  @JsonProperty("institutionCode")
  private String institutionCode; // "00100"
  @JsonProperty("userKey")
  private String userKey;         // ✅ 저장 대상
  @JsonProperty("created")
  private String created;
  @JsonProperty("modified")
  private String modified;
}