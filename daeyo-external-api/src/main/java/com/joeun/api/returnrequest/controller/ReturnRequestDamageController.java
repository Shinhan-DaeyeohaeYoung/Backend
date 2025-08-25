// daeyo-external-api/src/main/java/com/joeun/api/returnrequest/ReturnRequestDamageController.java
package com.joeun.api.returnrequest.controller;

import com.joeun.api.gpt.dto.DamageSuggestionResult;
import com.joeun.api.gpt.service.DamageAssessmentService;
import com.joeun.global.file.FileUrlResolver;

import com.joeun.service.returnrequest.ReturnRequestQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReturnRequestDamageController {

    private final ReturnRequestQueryService queryService;
    private final DamageAssessmentService damageSvc;
    private final FileUrlResolver fileUrlResolver;

    @GetMapping("/admin/return-requests/{id}/damage/suggestions")
    public ResponseEntity<DamageSuggestionResult> suggestions(@PathVariable Long id) {
        var keys = queryService.getBeforeAfterKeys(id);
        String beforeUrl = fileUrlResolver.toPublicUrl(keys.beforeKey());
        String afterUrl  = fileUrlResolver.toPublicUrl(keys.afterKey());
        var result = damageSvc.assess(beforeUrl, afterUrl);
        return ResponseEntity.ok(result);
    }
}
