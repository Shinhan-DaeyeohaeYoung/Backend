package com.joeun.service.returnrequest;

import com.joeun.domain.item.repository.UnitPhotoRepository;
import com.joeun.domain.returnrequest.entity.ReturnRequest;
import com.joeun.domain.returnrequest.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReturnRequestQueryService {

    private final ReturnRequestRepository rrRepo;
    private final UnitPhotoRepository unitPhotoRepo;

    public BeforeAfterKeys getBeforeAfterKeys(Long rrId) {
        ReturnRequest rr = rrRepo.findById(rrId)
                .orElseThrow(() -> new IllegalArgumentException("ReturnRequest not found: " + rrId));

        Long unitId = rr.getRental().getUnit().getId();

        String beforeKey = unitPhotoRepo.findByUnit_Id(unitId)
                .orElseThrow(() -> new IllegalArgumentException("unit photo not found: unitId=" + unitId))
                .getImageKey();

        String afterKey = rr.getSubmittedImageKey();
        if (afterKey == null || afterKey.isBlank()) {
            throw new IllegalStateException("Return photo is not uploaded yet");
        }

        return new BeforeAfterKeys(beforeKey, afterKey);
    }

    public record BeforeAfterKeys(String beforeKey, String afterKey) {}
}
