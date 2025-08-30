// daeyo-external-api/src/main/java/com/joeun/api/returnrequest/ReturnRequestDamageController.java
package com.joeun.api.returnrequest.controller;

import com.joeun.api.gpt.dto.DamageSuggestionResult;
import com.joeun.api.gpt.service.DamageAssessmentService;
import com.joeun.global.file.FileUrlResolver;

import com.joeun.service.returnrequest.ReturnRequestQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Authorization")
@RequestMapping("/api")
public class ReturnRequestDamageController {

    private final ReturnRequestQueryService queryService;
    private final DamageAssessmentService damageSvc;
    private final FileUrlResolver fileUrlResolver;

    @GetMapping("/admin/return-requests/{id}/damage/suggestions")
    public ResponseEntity<DamageSuggestionResult> suggestions(@PathVariable Long id) throws IOException {
        var result = damageSvc.assess(id);
        return ResponseEntity.ok(result);
    }
}
