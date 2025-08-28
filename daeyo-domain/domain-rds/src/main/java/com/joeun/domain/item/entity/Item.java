package com.joeun.domain.item.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "Item")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE) @Builder(toBuilder = true)
public class Item {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false) private Long universityId;
    @Column(nullable = false) private Long organizationId;

    @Column(nullable = false, length = 160) private String name;
    @Lob private String description;

    private Long deposit;
    private Integer maxRentalDays;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Boolean isActive;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Item activate() { this.isActive = true; return this; }
    public Item deactivate() { this.isActive = false; return this; }

    /** 총수량/대여가능 수량 동시 변경 (음수 방지) */
    public Item increaseStock(int by) {
        if (by < 0) throw new IllegalArgumentException("by must be >= 0");
        this.totalQuantity += by;
        this.availableQuantity += by;
        return this;
    }

    public Item decreaseStock(int by) {
        if (by < 0) throw new IllegalArgumentException("by must be >= 0");
        if (this.totalQuantity - by < 0 || this.availableQuantity - by < 0)
            throw new IllegalStateException("stock cannot be negative");
        this.totalQuantity -= by;
        this.availableQuantity -= by;
        return this;
    }

    /** 대여 시작/반납 처리 시 사용 */
    public Item rentOne() {
        if (this.availableQuantity <= 0) throw new IllegalStateException("no available stock");
        this.availableQuantity -= 1;
        return this;
    }

    public Item returnOne() {
        if (this.availableQuantity + 1 > this.totalQuantity)
            throw new IllegalStateException("available exceeds total");
        this.availableQuantity += 1;
        return this;
    }

    /** 일부 필드만 PATCH 반영 (null 무시) */
    public Item patch(String name, String description, Long deposit,
                      Integer maxRentalDays, Boolean isActive) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (deposit != null) this.deposit = deposit;
        if (maxRentalDays != null) this.maxRentalDays = maxRentalDays;
        if (isActive != null) this.isActive = isActive;
        return this;
    }

    //삭제 시 사용
    public void setTotalQuantity(int total) {
        if (total < 0) {
            throw new IllegalArgumentException("totalQuantity cannot be negative");
        }
        this.totalQuantity = total;

        // 가용 수량은 총수량을 초과할 수 없음
        if (this.availableQuantity > total) {
            this.availableQuantity = total;
        }
    }


    public void activeOn() {
        this.isActive = true;
    }
}
