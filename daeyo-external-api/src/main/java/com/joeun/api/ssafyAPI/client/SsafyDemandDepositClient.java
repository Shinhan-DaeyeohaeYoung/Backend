package com.joeun.api.ssafyAPI.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.joeun.api.ssafyAPI.dto.CreateDemandDepositAccountRequest;
import com.joeun.api.ssafyAPI.dto.CreateDemandDepositAccountResponse;
import com.joeun.api.ssafyAPI.dto.InquireDemandDepositAccountBalanceRequest;
import com.joeun.api.ssafyAPI.dto.InquireDemandDepositAccountBalanceResponse;
import com.joeun.api.ssafyAPI.dto.UpdateDemandDepositAccountTransferRequest;
import com.joeun.api.ssafyAPI.dto.UpdateDemandDepositAccountTransferResponse;
import com.joeun.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "ssafyDemandDeposit",
    url  = "${ssafy.base-url}",
    configuration = FeignConfig.class
)
public interface SsafyDemandDepositClient {

  @PostMapping("/ssafy/api/v1/edu/demandDeposit/createDemandDepositAccount")
  CreateDemandDepositAccountResponse createDemandDepositAccount(@RequestBody CreateDemandDepositAccountRequest request);

  // 잔액 조회
  @PostMapping("/ssafy/api/v1/edu/demandDeposit/inquireDemandDepositAccountBalance")
  InquireDemandDepositAccountBalanceResponse inquireDemandDepositAccountBalance(
      @RequestBody InquireDemandDepositAccountBalanceRequest request);

  // 개인 -> 조직 계좌 이체
  @PostMapping("/ssafy/api/v1/edu/demandDeposit/updateDemandDepositAccountTransfer")
  UpdateDemandDepositAccountTransferResponse updateDemandDepositAccountTransfer(
      @RequestBody UpdateDemandDepositAccountTransferRequest request);

}