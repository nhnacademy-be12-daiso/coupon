package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.issue.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponIssueController {

    private final CouponPolicyService couponIssueService;

    public CouponIssueController(CouponPolicyService couponIssueService) {
        this.couponIssueService = couponIssueService;
    }

    @PostMapping("/issue")
    public ResponseEntity<UserCouponResponse> issueCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserCouponIssueRequest request){
        UserCouponResponse response = couponIssueService.issueCoupon(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{couponPolicyId}/download")
    public ResponseEntity<UserCouponResponse> downloadCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long couponPolicyId) {

        UserCouponIssueRequest request = new UserCouponIssueRequest(couponPolicyId);

        UserCouponResponse response = couponIssueService.issueCoupon(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
