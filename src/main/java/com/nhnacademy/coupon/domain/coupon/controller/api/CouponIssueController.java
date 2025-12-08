package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.CategoryCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.impl.CouponIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponIssueController {

    private final CouponIssueService couponIssueService;

    @PostMapping("/issue")
    public ResponseEntity<UserCouponResponse> issueCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CategoryCouponIssueRequest request){
        UserCouponResponse response = couponIssueService.issueCoupon(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
