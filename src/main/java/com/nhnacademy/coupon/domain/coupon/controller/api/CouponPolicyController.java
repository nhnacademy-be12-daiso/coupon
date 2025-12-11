package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.issue.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.policy.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Coupon Policy", description = "쿠폰 정책 관리 (관리자용)")
@RestController
@RequestMapping("/api/coupons")
public class CouponPolicyController {

    private final CouponPolicyService couponPolicyService;

    public CouponPolicyController(CouponPolicyService couponPolicyService) {
        this.couponPolicyService = couponPolicyService;
    }

    @Operation(summary = "쿠폰 정책 생성", description = "새로운 쿠폰 정책을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<CouponPolicyResponse> createCoupon(
            @RequestHeader(value = "X-Gateway-Pass", required = false)
            @Valid @RequestBody CouponPolicyCreateRequest request) {
        CouponPolicyResponse response = couponPolicyService.createCouponPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "쿠폰 정책 조회", description = "쿠폰 정책을 모두 조회합니다")
    @GetMapping("/policies")
    public ResponseEntity<List<CouponPolicyResponse>> getPolicies(){
        List<CouponPolicyResponse> couponPolicies = couponPolicyService.couponPolices();
        return ResponseEntity.ok(couponPolicies);
    }

    @Operation(summary = "쿠폰 정책 단일 조회", description = "쿠폰 정책을 모두 조회합니다")
    @GetMapping("/policies/{id}")
    public ResponseEntity<CouponPolicyResponse> getPolicyDetail(@PathVariable Long id){
        CouponPolicyResponse couponPolicy = couponPolicyService.couponPolicyDetail(id);
        return ResponseEntity.ok(couponPolicy);
    }

    @Operation(summary = "쿠폰 정책 수정", description = "이미 발급된 쿠폰일시 쿠폰 활성화,비활성화만 가능")
    @PutMapping("/policies/{id}")
    public ResponseEntity<CouponPolicyResponse> updatePolicy(
            @PathVariable Long id, @Valid @RequestBody CouponPolicyUpdateRequest request){
        CouponPolicyResponse updated = couponPolicyService.updateCouponPolicy(id,request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "쿠폰 발급", description = "사용자에게 쿠폰을 발급합니다.")
    @PostMapping
    public ResponseEntity<UserCouponResponse> issueCoupon(
            @RequestHeader("X-User-Id") Long userCreatedId,  // 토큰에서 검증된 진짜 ID
            @Valid @RequestBody UserCouponIssueRequest request) {
        UserCouponResponse response = couponPolicyService.issueCoupon(userCreatedId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
