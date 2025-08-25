package com.joeun.global.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AccountCipher {

  @Value("${crypto.key}") // Base64-encoded 32 bytes (AES-256)
  private String keyBase64;

  private SecretKeySpec key() {
    byte[] raw = Base64.getDecoder().decode(keyBase64);
    return new SecretKeySpec(raw, "AES");
  }

  public byte[] encrypt(String plain) {
    try {
      byte[] iv = new byte[12];
      new SecureRandom().nextBytes(iv);

      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
      byte[] ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));

      byte[] out = new byte[iv.length + ct.length];
      System.arraycopy(iv, 0, out, 0, iv.length);
      System.arraycopy(ct, 0, out, iv.length, ct.length);
      return out;
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Account encryption failed", e);
    }
  }

  public String decrypt(byte[] data) {
    try {
      byte[] iv = Arrays.copyOfRange(data, 0, 12);
      byte[] ct = Arrays.copyOfRange(data, 12, data.length);

      Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
      c.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
      return new String(c.doFinal(ct), StandardCharsets.UTF_8);
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("Account decryption failed", e);
    }
  }

  public String mask(String accNo) {
    String digits = accNo.replaceAll("\\D", "");
    if (digits.length() < 4) return "****";
    return "****" + digits.substring(digits.length() - 4);
  }
}
