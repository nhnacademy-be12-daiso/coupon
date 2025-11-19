package com.nhnacademy.coupon.controller;

import com.nhnacademy.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.dto.response.CouponApplyResponse;
import com.nhnacademy.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.service.CouponServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "User Coupon", description = "사용자 쿠폰 관리 (발급 및 사용)")
@RestController
@RequestMapping("/api/user-coupons")
public class UserCouponController {

    private final CouponServiceImpl couponServiceImpl;

    public UserCouponController(CouponServiceImpl couponServiceImpl) {
        this.couponServiceImpl = couponServiceImpl;
    }

    @Operation(summary = "쿠폰 발급", description = "사용자에게 쿠폰을 발급합니다.")
    @PostMapping
    public ResponseEntity<UserCouponResponse> issueCoupon(@Valid @RequestBody UserCouponIssueRequest request) {
        UserCouponResponse response = couponServiceImpl.issueCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 쿠폰 목록 조회")
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<UserCouponResponse>> getUserCoupons(@PathVariable Long userId, Pageable pageable) {
        Page<UserCouponResponse> response = couponServiceImpl.getUserCoupons(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용 가능한 쿠폰 조회")
    @GetMapping("/users/{userId}/available")
    public ResponseEntity<List<UserCouponResponse>> getAvailableCoupons(@PathVariable Long userId) {
        List<UserCouponResponse> response = couponServiceImpl.getAvailableCoupons(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "할인 금액 미리보기", description = "이 쿠폰을 썼을 때 얼마가 할인되는지 계산합니다.")
    @GetMapping("/{userCouponId}/calculation")
    public ResponseEntity<CouponApplyResponse> calculateDiscount(
            @PathVariable Long userCouponId,
            @RequestParam BigDecimal price) {

        CouponApplyResponse response = couponServiceImpl.applyCoupon(userCouponId, price);
        return ResponseEntity.ok(response);
    }
}
