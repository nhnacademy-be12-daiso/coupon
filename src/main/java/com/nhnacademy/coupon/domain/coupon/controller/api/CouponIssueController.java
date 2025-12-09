package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.CategoryCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.impl.CouponIssueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponIssueController {

    private CouponIssueService couponIssueService;

    public CouponIssueController(CouponIssueService couponIssueService) {
        this.couponIssueService = couponIssueService;
    }

    @PostMapping("/issue")
    public ResponseEntity<UserCouponResponse> issueCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CategoryCouponIssueRequest request){
        UserCouponResponse response = couponIssueService.issueCoupon(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
