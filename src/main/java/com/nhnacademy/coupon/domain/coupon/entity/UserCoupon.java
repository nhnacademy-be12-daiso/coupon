package com.nhnacademy.coupon.domain.coupon.entity;

import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long userCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_policy_id", nullable = false) // FK
    private CouponPolicy couponPolicy;

    // User 엔티티 pk 타입이 Long 이라고 가정
    @Column(name = "user_created_id",nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CouponStatus status; // ISSUED, USED, EXPIRED 등

    @Column(name = "used_at") // 쿠폰 사용일
    private LocalDateTime usedAt;

    @Column(name = "issue_at", nullable = false) // 쿠폰 발급일
    private LocalDateTime issuedAt;

    @Column(name = "expiry_at", nullable = false) // 쿠폰 만료일
    private LocalDateTime expiryAt;


    /**
     * 쿠폰 사용 처리
     * ISSUED 또는 CANCELED 상태에서만 사용 가능
     */
    public void use() {
        // 사용 가능한 상태 확인
        if (this.status != CouponStatus.ISSUED && this.status != CouponStatus.CANCELED) {
            throw new IllegalStateException(
                    "쿠폰을 사용할 수 없는 상태입니다. 현재 상태: " + this.status
            );
        }

        // 만료 확인
        if (LocalDateTime.now().isAfter(this.expiryAt)) { // 메서드 호출 시간보다 만료시간이 더 뒤인가?
            throw new IllegalStateException("만료된 쿠폰입니다.");
        }

        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 쿠폰 만료 처리
     * ISSUED 상태의 쿠폰만 만료 처리
     */
    public void expire() {
        if (this.status == CouponStatus.ISSUED) {
            this.status = CouponStatus.EXPIRED;
        }
    }

    /**
     * 주문 취소로 인한 쿠폰 복구
     * USED 상태에서만 CANCELED로 변경 가능
     */
    public void cancel() {
        if (this.status != CouponStatus.USED) {
            throw new IllegalStateException(
                    "사용된 쿠폰만 취소할 수 있습니다. 현재 상태: " + this.status
            );
        }

        // 만료 확인 (만료된 쿠폰은 복구 불가)
        if (LocalDateTime.now().isAfter(this.expiryAt)) {
            throw new IllegalStateException(
                    "이미 만료된 쿠폰은 복구할 수 없습니다."
            );
        }

        this.status = CouponStatus.CANCELED;
        this.usedAt = null;  // 사용 시간 초기화
    }

    /**
     * 쿠폰 사용 가능 여부 확인
     */
    public boolean isAvailable() {
        return (this.status == CouponStatus.ISSUED || this.status == CouponStatus.CANCELED)
                && LocalDateTime.now().isBefore(this.expiryAt);
    }



}
