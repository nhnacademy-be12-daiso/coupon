package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.*;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyNotFoundException;
import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class CouponPolicyServiceImpl implements CouponPolicyService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;

    public CouponPolicyServiceImpl(CouponPolicyRepository couponPolicyRepository, UserCouponRepository userCouponRepository) {
        this.couponPolicyRepository = couponPolicyRepository;
        this.userCouponRepository = userCouponRepository;
    }

    // 쿠폰 정책 생성
    @Transactional
    public CouponPolicyResponse createCoupon(CouponPolicyCreateRequest request){
        // dto -> entity
        CouponPolicy saved = couponPolicyRepository.save(request.toEntity());
        return convertToResponse(saved);
    }
    // 쿠폰 정책 전체 조회
    @Override
    public List<CouponPolicyResponse> couponPolices() {
        List<CouponPolicy> policies = couponPolicyRepository.findAll();
        ArrayList<CouponPolicyResponse> responses = new ArrayList<>();
        for (CouponPolicy policy : policies) {
            responses.add(convertToResponse(policy));
        }
        return responses;
    }
    // 쿠폰 정책 단일 조회
    @Override
    public CouponPolicyResponse couponPolicyDetail(Long id) {
        CouponPolicy policy = couponPolicyRepository.findById(id)
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));
        CouponPolicyResponse response = convertToResponse(policy);
        return response;
    }

    // 쿠폰 정책 수정
    @Override
    public CouponPolicyResponse updateCouponPolicy(Long id, CouponPolicyUpdateRequest request) {
        CouponPolicy policy = couponPolicyRepository.findById(id)
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));
        // 발급된 쿠폰 개수 확인
        long issuedCount = userCouponRepository.countByCouponPolicyCouponPolicyId(id);

        if(issuedCount > 0){
            // 발급 후에는 상태만 변경 가능
            if (!isSameExceptStatus(policy, request)) {
                throw new IllegalStateException(
                        "이미 " + issuedCount + "개의 쿠폰이 발급되어 할인 조건을 수정할 수 없습니다. " +
                                "상태만 변경할 수 있습니다."
                );
            }
            // 상태만 변경
            policy.updateStatus(request.getPolicyStatus());
        } else {
            policy.update(request);
        }

        CouponPolicy saved = couponPolicyRepository.save(policy);
        return convertToResponse(saved);
    }

    // 사용자에게 쿠폰 발급
    @Override
    @Transactional
    public UserCouponResponse issueCoupon(Long userId, UserCouponIssueRequest request){
        // 쿠폰 정책 조회
        CouponPolicy couponPolicy = couponPolicyRepository.findById(request.getCouponPolicyId())
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));

        if(couponPolicy.getQuantity() != null){
            if(couponPolicy.getQuantity() <= 0){
                throw new IllegalStateException("발급 가능한 쿠폰이 모두 소진되었습니다.");
            }
            couponPolicy.decreaseQuantity();
        }
        // 만료일 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryAt;

        if(couponPolicy.getValidDays() != null){
            expiryAt = now.plusDays(couponPolicy.getValidDays());
        } else if(couponPolicy.getValidEndDate() != null){ // 고정 유효기간 일떄
            expiryAt = couponPolicy.getValidEndDate();
        } else { // 기본값 처리 (1년 추가)
            expiryAt = now.plusYears(1);
        }
        // 사용자 쿠폰 생성(여기서 매겨변수 userId를 사용)
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponPolicy(couponPolicy)
                .status(CouponStatus.ISSUED)
                .issuedAt(now) // 발급일시
                .expiryAt(expiryAt) // 만료일시
                .build();

        UserCoupon saved = userCouponRepository.save(userCoupon);
        return convertToUserCouponResponse(saved);

    }
    // Welcome 쿠폰 발급
    @Override
    @Transactional
    public void issueWelcomeCoupon(Long userId){
        List<CouponPolicy> welcomePolicies = couponPolicyRepository.findByCouponType(CouponType.WELCOME);

        if(welcomePolicies.isEmpty()){
            log.warn("Welcome 쿠폰 정책이 없습니다.");
            return;
        }
        // Welcome 쿠폰 발급 (정책: 50,000 이상 구매 시 10.000 할인, 30일)
        for (CouponPolicy policy : welcomePolicies) {
            try{

                boolean alreadyHas = userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(userId, policy.getCouponPolicyId());

                if(alreadyHas){
                    log.warn("이미 지급된 Welcome 쿠폰입니다: userId={}, couponId={}",userId, policy.getCouponPolicyId());
                    continue;
                }
                UserCouponIssueRequest request = new UserCouponIssueRequest(policy.getCouponPolicyId());
                issueCoupon(userId,request);

                log.info("Welcome 쿠폰 발급 성공: userId={}, couponId={}", userId, policy.getCouponPolicyId());
            } catch (Exception e){
                // 쿠폰 발급 실패해도 회원가입은 정상 처리
                log.error("Welcome 쿠폰 발급 실패: userId={}, error={}", userId, e.getMessage());

                // TODO: 실패 이벤트 발행 또는 재시도 큐에 추가
            }
        }

    }


    // 쿠폰 사용 (주문 시)
    @Transactional
    public CouponApplyResponse applyCoupon(Long userCouponId, BigDecimal orderAmount){
        UserCoupon userCoupon = userCouponRepository.findById(userCouponId)
                .orElseThrow(() -> new CouponPolicyNotFoundException("보유한 쿠폰을 찾을 수 없습니다."));

        CouponPolicy couponPolicy = userCoupon.getCouponPolicy();

        // 최소 주문 금액 체크
        if(couponPolicy.getMinOrderAmount() != null &&
            orderAmount.compareTo(BigDecimal.valueOf(couponPolicy.getMinOrderAmount())) < 0){ // 주문 금액이 쿠폰 최소 금액 보다 작으면 예외 처리
            throw new InvalidCouponException(
                    "최소 주문 금액(" + couponPolicy.getMinOrderAmount() + ")을 충족하지 않습니다.");        }

        // 할인 금액 계산
        BigDecimal discountAmount = calculateDiscount(couponPolicy, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount); // 할인 된 최종 금액

        // 쿠폰 사용 처리
        userCoupon.use();


        return CouponApplyResponse.builder()
                .userCouponId(userCouponId)
                .couponName(couponPolicy.getCouponPolicyName()) // 정책 이름 사용
                .originalAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    public BigDecimal calculateDiscount(CouponPolicy couponPolicy, BigDecimal orderAmount) {
        BigDecimal discountAmount;

        if(couponPolicy.getDiscountWay() == DiscountWay.FIXED){
            // 고정 할인 금액
             discountAmount = couponPolicy.getDiscountAmount();
        } else {
            // 퍼센트 할인
            discountAmount = orderAmount
                    .multiply(couponPolicy.getDiscountAmount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // 최대 할인 금액 제한
        if(couponPolicy.getMaxDiscountAmount() != null){
            BigDecimal maxDiscount = BigDecimal.valueOf(couponPolicy.getMaxDiscountAmount());
            if(discountAmount.compareTo(maxDiscount) > 0){ // 할인 금액이 최대 할인 금액보다 크면 최대 할인 금액으로 할인 금액 Fix!
                discountAmount = maxDiscount;
            }
        }

        return discountAmount;
    }


    // 사용자 쿠폰 목록 조회
    public List<UserCouponResponse> getUserCoupons(Long userId){
        List<UserCouponResponse> userCouponResponses = userCouponRepository.findByUserId(userId).stream()
                .map(this::convertToUserCouponResponse).toList();
        return userCouponResponses;

    }

    // 사용 가능한 쿠폰 조회
    public List<UserCouponResponse> getAvailableCoupons(Long userId){
        List<UserCoupon> coupons = userCouponRepository
                .findByUserIdAndStatus(userId, CouponStatus.ISSUED);

        return coupons.stream()
                .map(this::convertToUserCouponResponse)
                .toList();
    }

    @Transactional
    public void expireCoupons(){
        int count = userCouponRepository.bulkExpireCoupons(LocalDateTime.now());
        log.info("만료 처리된 쿠폰 개수: {}", count);
    }




    private CouponPolicyResponse convertToResponse(CouponPolicy policy) {
        return CouponPolicyResponse.builder()
                .couponPolicyId(policy.getCouponPolicyId())
                .couponPolicyName(policy.getCouponPolicyName())
                .couponType(policy.getCouponType())
                .discountWay(policy.getDiscountWay())
                .discountAmount(policy.getDiscountAmount())
                .minOrderAmount(policy.getMinOrderAmount())
                .maxDiscountAmount(policy.getMaxDiscountAmount())
                .validDays(policy.getValidDays())
                .validStartDate(policy.getValidStartDate())
                .validEndDate(policy.getValidEndDate())
                .policyStatus(policy.getCouponPolicyStatus())
                .quantity(policy.getQuantity())
                .build();
    }
    private UserCouponResponse convertToUserCouponResponse(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .userCouponId(userCoupon.getUserCouponId())
                .userId(userCoupon.getUserId())
                .couponPolicy(convertToResponse(userCoupon.getCouponPolicy())) // coupon -> couponPolicy
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .expiryAt(userCoupon.getExpiryAt()) // expiryAt으로 통일
                .usedAt(userCoupon.getUsedAt())
                .build();
    }

    private boolean isSameExceptStatus(CouponPolicy policy, CouponPolicyUpdateRequest request) {
        return policy.getCouponPolicyName().equals(request.getCouponPolicyName())
                && policy.getCouponType().equals(request.getCouponType())
                && policy.getDiscountWay().equals(request.getDiscountWay())
                && policy.getDiscountAmount().equals(request.getDiscountAmount())
                && Objects.equals(policy.getMinOrderAmount(), request.getMinOrderAmount())
                && Objects.equals(policy.getMaxDiscountAmount(), request.getMaxDiscountAmount())
                && Objects.equals(policy.getValidDays(), request.getValidDays())
                && Objects.equals(policy.getQuantity(), request.getQuantity());
    }
}
