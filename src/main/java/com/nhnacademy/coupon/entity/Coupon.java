package com.nhnacademy.coupon.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_policy_id")
    private CouponPolicy couponPolicy;

    @Column(name = "coupon_name", nullable = false) // 이름 값 필수
    private String couponName;

    @Column(name = "issue_limit") // 쿠폰 발급 한도
    private Long issueLimit;

    @Column(name = "issue_count", nullable = false) // 쿠폰 현재 발급 수
    private long issueCount = 0;

    @Column(name = "valid_days") // 쿠폰 유효일수
    private Integer validDays;

    @Column(name = "valid_start_date") // 쿠폰 유효기간 시작일
    private LocalDateTime validStartDate;

    @Column(name = "valid_end_date") // 쿠폰 유효기간 끝나는일
    private LocalDateTime validEndDate;

}
