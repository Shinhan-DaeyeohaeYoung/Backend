package com.joeun.global.config;

import com.joeun.api.ssafyAPI.SsafyErrorDecoder;
import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

  @Bean public feign.Logger.Level feignLoggerLevel() { return feign.Logger.Level.FULL; }

  @Bean public Request.Options feignOptions() { return new Request.Options(5000, 10000); } // conn, read(ms)

  @Bean
  public feign.codec.ErrorDecoder errorDecoder() {
    return new SsafyErrorDecoder();
  }


}