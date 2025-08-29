package com.joeun.api.ssafyAPI;

import com.joeun.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="ssafyBank", url="${ssafy.base-url}", configuration= FeignConfig.class)
public interface SsafyBankClient {
  // 예: 이체/계좌조회 등도 같은 패턴으로 추가
  // @PostMapping("/ssafy/api/v1/edu/bank/transfer")
  // TransferResponse transfer(@RequestBody TransferRequest req);

}