package com.joeun.api.waitlist.service;

import com.joeun.domain.item.entity.Item;
import com.joeun.domain.item.service.ItemDomainService;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.waitlist.entity.Waitlist;
import com.joeun.domain.waitlist.entity.WaitlistStatus;
import com.joeun.global.config.LoginUser;
import com.joeun.service.university.UniversityDomainService;
import com.joeun.service.user.UserDomainService;
import com.joeun.service.waitlist.WaitlistDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistDomainService waitlistDomainService;
    private final ItemDomainService itemDomainService;
    private final UserDomainService userDomainService;

    public void joinWaitListByItemId(Long itemId, LoginUser loginUser) {
        Item item = itemDomainService.getItemById(itemId);
        int currentWaitlistCount = waitlistDomainService.getWaitListCount(itemId);

        if (currentWaitlistCount >= item.getTotalQuantity()) {
            throw new IllegalStateException("Waitlist is full for this item");
        }

        User user = userDomainService.findById(loginUser.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Waitlist waitlist = Waitlist.builder()
                .universityId(loginUser.universityId())
                .item(item)
                .user(user)
                .priority(0)
                .status(WaitlistStatus.WAITING)
                .joinedAt(LocalDateTime.now())
                .build();

        waitlistDomainService.joinWaitlist(waitlist);
    }
}
