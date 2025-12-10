package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.CouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.CouponUseResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Tag(name = "User Coupon", description = "사용자 쿠폰 관리 (발급 및 사용)")
@RestController
@RequestMapping("/api/coupons")
public class UserCouponController {

    private final UserCouponService userCouponService;

    public UserCouponController(UserCouponService userCouponService) {
        this.userCouponService = userCouponService;
    }

    @Operation(summary = "내 쿠폰 목록 조회")
    @GetMapping("/users")
    public ResponseEntity<List<UserCouponResponse>> getUserCoupons(@RequestHeader("X-User-Id") Long userCreatedId) {
        List<UserCouponResponse> userCoupons = userCouponService.getUserCoupons(userCreatedId);
        return ResponseEntity.ok(userCoupons);
    }

    @Operation(summary = "주문시 사용 가능한 유저 쿠폰 조회")
    @GetMapping("/available")
    public ResponseEntity<List<UserCouponResponse>> getAvailableCoupons(@RequestHeader("X-User-Id") Long userId,
                                                                        @RequestParam(name = "bookId",required = false) Long bookId) {

        List<UserCouponResponse> response = userCouponService.getAvailableCoupons(userId, bookId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "쿠폰 사용 처리")
    @PostMapping("/{userCouponId}/use")
    public CouponUseResponse useCoupon(
            @PathVariable Long userCouponId, @RequestBody CouponUseRequest request){

        return null;
    }

    @Operation(summary = "할인 금액 미리보기", description = "이 쿠폰을 썼을 때 얼마가 할인되는지 계산합니다.")
    @GetMapping("/{userCouponId}/calculation")
    public ResponseEntity<CouponApplyResponse> calculateDiscount(
            @PathVariable Long userCouponId,
            @RequestParam BigDecimal price,

            @RequestParam List<Long> targetIds) {

        CouponApplyResponse response = userCouponService.applyCoupon(userCouponId, price,targetIds);
        return ResponseEntity.ok(response);

    }
}
