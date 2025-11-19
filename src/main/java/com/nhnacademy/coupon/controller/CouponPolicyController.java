package com.nhnacademy.coupon.controller;

import com.nhnacademy.coupon.dto.request.CouponCreateRequest;
import com.nhnacademy.coupon.dto.response.CouponResponse;
import com.nhnacademy.coupon.service.CouponServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Coupon Policy", description = "쿠폰 정책 관리 (관리자용)")
@RestController
@RequestMapping("api/coupons")
public class CouponPolicyController {

    private final CouponServiceImpl couponServiceImpl;

    public CouponPolicyController(CouponServiceImpl couponServiceImpl) {
        this.couponServiceImpl = couponServiceImpl;
    }

    @Operation(summary = "쿠폰 정책 생성", description = "새로운 쿠폰 정책을 생성합니다.")
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CouponCreateRequest request) {
        CouponResponse response = couponServiceImpl.createCoupon(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
