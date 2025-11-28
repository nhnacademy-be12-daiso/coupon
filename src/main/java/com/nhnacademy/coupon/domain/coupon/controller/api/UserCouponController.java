package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.nhnacademy.coupon.global.annotation.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Tag(name = "User Coupon", description = "사용자 쿠폰 관리 (발급 및 사용)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupons")
public class UserCouponController {

    private final CouponPolicyService couponPolicyService;

    @GetMapping("test/auth")
    public String testAuth(@RequestHeader(value = "X-User-Id", required = false) String userId) {

        log.info("========================================");
        log.info("[Coupon Server] 인증된 User ID: {}", userId);
        log.info("========================================");

        if (userId == null) {
            return "실패: 인증 헤더(X-User-Id)가 없습니다!";
        }
        return "성공: 당신의 ID는 " + userId + "입니다.";
    }

    @Operation(summary = "Welcome 쿠폰 발급", description = "회원가입 완료 시 시스템이 자동으로 호출하는 API입니다.")
    @PostMapping("/welcome/{userId}")
    public ResponseEntity<Void> issueWelcomeCoupon(@PathVariable Long userId) {
        couponPolicyService.issueWelcomeCoupon(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "쿠폰 발급", description = "사용자에게 쿠폰을 발급합니다.")
    @PostMapping
    public ResponseEntity<UserCouponResponse> issueCoupon(
            @CurrentUserId Long userId,  // 토큰에서 검증된 진짜 ID
            @Valid @RequestBody UserCouponIssueRequest request) {
        UserCouponResponse response = couponPolicyService.issueCoupon(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 쿠폰 목록 조회")
    @GetMapping("/users")
    public ResponseEntity<List<UserCouponResponse>> getUserCoupons(@RequestHeader("X-User-Id") Long userCreatedId) {
        List<UserCouponResponse> userCoupons = couponPolicyService.getUserCoupons(userCreatedId);
        return ResponseEntity.ok(userCoupons);
    }

    @Operation(summary = "사용 가능한 쿠폰 조회")
    @GetMapping("/users/{userId}/available")
    public ResponseEntity<List<UserCouponResponse>> getAvailableCoupons(@PathVariable Long userId) {
        List<UserCouponResponse> response = couponPolicyService.getAvailableCoupons(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할인 금액 미리보기", description = "이 쿠폰을 썼을 때 얼마가 할인되는지 계산합니다.")
    @GetMapping("/{userCouponId}/calculation")
    public ResponseEntity<CouponApplyResponse> calculateDiscount(
            @PathVariable Long userCouponId,
            @RequestParam BigDecimal price) {

        CouponApplyResponse response = couponPolicyService.applyCoupon(userCouponId, price);
        return ResponseEntity.ok(response);
    }
}
