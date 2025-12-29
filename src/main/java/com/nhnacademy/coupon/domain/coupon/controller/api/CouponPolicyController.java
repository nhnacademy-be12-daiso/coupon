package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.policy.CouponPolicyResponse;
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
@RequestMapping("/api/coupons/policies")
public class CouponPolicyController {

    private final CouponPolicyService couponPolicyService;

    public CouponPolicyController(CouponPolicyService couponPolicyService) {
        this.couponPolicyService = couponPolicyService;
    }

    @Operation(summary = "쿠폰 정책 생성", description = "새로운 쿠폰 정책을 생성합니다.")
    @PostMapping
    public ResponseEntity<CouponPolicyResponse> createCoupon(
            @Valid @RequestBody CouponPolicyCreateRequest request) {
        CouponPolicyResponse response = couponPolicyService.createCouponPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "쿠폰 정책 조회", description = "쿠폰 정책을 모두 조회합니다")
    @GetMapping
    public ResponseEntity<List<CouponPolicyResponse>> getPolicies(){
        List<CouponPolicyResponse> couponPolicies = couponPolicyService.couponPolices();
        return ResponseEntity.ok(couponPolicies);
    }

    @Operation(summary = "쿠폰 정책 단일 조회", description = "쿠폰 정책을 모두 조회합니다")
    @GetMapping("/{id}")
    public ResponseEntity<CouponPolicyResponse> getPolicyDetail(@PathVariable Long id){
        CouponPolicyResponse couponPolicy = couponPolicyService.couponPolicyDetail(id);
        return ResponseEntity.ok(couponPolicy);
    }

    @Operation(summary = "쿠폰 정책 수정", description = "이미 발급된 쿠폰일시 쿠폰 활성화,비활성화만 가능")
    @PutMapping("/{id}")
    public ResponseEntity<CouponPolicyResponse> updatePolicy(
            @PathVariable Long id, @Valid @RequestBody CouponPolicyUpdateRequest request){
        CouponPolicyResponse updated = couponPolicyService.updateCouponPolicy(id,request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "쿠폰 정책 삭제", description = "쿠폰 정책을 삭제합니다. (이미 발급된 쿠폰이 있으면 삭제 불가)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePolicy(@PathVariable Long id) {
        couponPolicyService.deleteCouponPolicy(id);
        return ResponseEntity.noContent().build(); // 204
    }


}
