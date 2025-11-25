package com.nhnacademy.coupon.dto.request;

import com.nhnacademy.coupon.entity.CouponPolicyStatus;
import com.nhnacademy.coupon.entity.CouponType;
import com.nhnacademy.coupon.entity.DiscountWay;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "쿠폰 생성 요청")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreateRequest {

    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    @Size(max = 100)
    private String couponPolicyName;

    @NotNull(message = "쿠폰 정책 종류는 필수입니다.")
    private CouponType couponType;

    @NotNull(message = "할인 방식은 필수입니다.")
    private DiscountWay discountWay;

    @NotNull(message = "할인 금액/비율은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal discountAmount;

    @Min(value = 0, message = "최소 주문 금액은 0 이상이어야 합니다.")
    private Long minOrderAmount;

    private Long maxDiscountAmount;

    private Integer validDays; // 쿠폰 상대 유효 일수

    private LocalDateTime validStartDate; // 쿠폰 고정 유효기간 시작일

    private LocalDateTime validEndDate; // 쿠폰 고정 유효기간 끝나는일

    private Integer quantity; // 수량

    @Enumerated(EnumType.STRING)
    private CouponPolicyStatus couponPolicyStatus;


}
