package com.nhnacademy.coupon.controller.api;

import com.nhnacademy.coupon.dto.request.CouponCreateRequest;
import com.nhnacademy.coupon.dto.response.CouponResponse;
import com.nhnacademy.coupon.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Coupon Policy", description = "쿠폰 정책 관리 (관리자용)")
@RestController
@RequestMapping("/api/coupons/create")
public class CouponPolicyController {

    private final CouponService couponService;

    public CouponPolicyController(CouponService couponService) {
        this.couponService = couponService;
    }

    @Operation(summary = "쿠폰 정책 생성", description = "새로운 쿠폰 정책을 생성합니다.")
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(
            @RequestHeader(value = "X-Gateway-Pass", required = false) String gatewayPass,
            @Valid @RequestBody CouponCreateRequest request) {
        System.out.println(gatewayPass);
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
