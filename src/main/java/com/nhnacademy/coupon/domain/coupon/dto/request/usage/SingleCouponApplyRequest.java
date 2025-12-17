package com.nhnacademy.coupon.domain.coupon.dto.request.usage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "단일 도서에 쿠폰 적용 계산 요청")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SingleCouponApplyRequest {

    @NotNull(message = "도서 ID는 필수입니다.")
    @Schema(description = "도서 ID", example = "1")
    private Long bookId;

    @NotNull(message = "도서 가격은 필수입니다.")
    @Schema(description = "도서 가격", example = "10000")
    private BigDecimal bookPrice;

    @NotNull(message = "수량은 필수입니다.")
    @Schema(description = "구매 수량", example = "2")
    private Integer quantity;

    @NotNull(message = "쿠폰 ID는 필수입니다.")
    @Schema(description = "적용할 쿠폰 ID", example = "7")
    private Long userCouponId;
}
