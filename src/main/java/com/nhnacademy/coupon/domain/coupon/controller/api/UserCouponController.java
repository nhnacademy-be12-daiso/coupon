package com.nhnacademy.coupon.domain.coupon.controller.api;

import com.nhnacademy.coupon.domain.coupon.dto.request.usage.BatchCouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponCancelRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.SingleCouponApplyRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.CouponCancelResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.SingleCouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.service.UserCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "단일 도서에 쿠폰 적용 계산",description = "특정 도서에 쿠폰을 적용했을 때 할인 금액을 실시간으로 계산합니다. 실제 사용하지는 않습니다.")
    @PostMapping("/calculate")
    public ResponseEntity<SingleCouponApplyResponse> calculateSingleCoupon(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SingleCouponApplyRequest request){

        log.info("단일 쿠폰 계산 요청: userId={}, bookId={}, couponId={}",
                userId, request.getBookId(), request.getUserCouponId());

        SingleCouponApplyResponse response = userCouponService.calculateSingleCoupon(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "쿠폰 일괄 사용 처리", description = "주문 완료 시 여러개의 쿠폰을 사용 처리합니다.")
    @PostMapping("/use-batch")
    public ResponseEntity<Void> useCouponsBatch(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid BatchCouponUseRequest request){

        log.info("쿠폰 일괄 사용 요청: userId={}, orderId={}, couponIds={}",
                userId, request.getOrderId(), request.getUserCouponIds());

        userCouponService.useCoupons(userId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "쿠폰 사용 취소", description = "주문취소를 처리합니다.")
    @PostMapping("/use-cancel")
    public ResponseEntity<Void> cancelCoupons(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody @Valid CouponCancelRequest request){

        log.info("쿠폰 사용 취소 요청: userId={}, orderId={}", userId, request.getOrderId());

        userCouponService.cancelCouponUsage(userId, request);

        return ResponseEntity.ok().build();
    }

}
