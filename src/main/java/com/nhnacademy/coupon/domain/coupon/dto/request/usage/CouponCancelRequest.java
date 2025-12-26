package com.nhnacademy.coupon.domain.coupon.dto.request.usage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "쿠폰 사용 취소 요청 (주문 취소/환불)")
public class CouponCancelRequest {

    @NotNull(message = "주문 ID는 필수입니다.")
    @Schema(description = "취소된 주문 ID", example = "20240101-123456")
    private Long orderId;

    @Schema(description = "취소 사유", example = "단순 변심")
    private String cancelReason;

    @NotNull(message = "취소할 쿠폰 ID 리스트는 필수입니다.")
    @Schema(description = "취소(복구)할 쿠폰 ID 목록", example = "[10, 12]")
    private List<Long> userCouponIds; // 일괄 처리를 위해 리스트로 받음
}