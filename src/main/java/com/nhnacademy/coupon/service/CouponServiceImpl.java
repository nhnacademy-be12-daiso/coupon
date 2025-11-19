package com.nhnacademy.coupon.service;

import com.nhnacademy.coupon.dto.request.CouponCreateRequest;
import com.nhnacademy.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.dto.response.CouponApplyResponse;
import com.nhnacademy.coupon.dto.response.CouponResponse;
import com.nhnacademy.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.entity.Coupon;
import com.nhnacademy.coupon.entity.CouponStatus;
import com.nhnacademy.coupon.entity.DiscountWay;
import com.nhnacademy.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.exception.CouponNotFoundException;
import com.nhnacademy.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.repository.CouponRepository;
import com.nhnacademy.coupon.repository.UserCouponRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;

    public CouponServiceImpl(CouponRepository couponRepository, UserCouponRepository userCouponRepository) {
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
    }

    // 쿠폰 정책 생성
    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request){
        Coupon coupon = Coupon.builder()
                .couponName(request.getCouponName())
                .discountWay(request.getDiscountWay())
                .discount(request.getDiscount())
                .categoryId(request.getCategoryId())
                .targetBookId(request.getTargetBookId())
                .isBirthday(request.isBirthday())
                .isWelcome(request.isWelcome())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .availabilityDays(request.getAvailabilityDays())
                .build();

        Coupon saved = couponRepository.save(coupon);
        return convertToResponse(saved);
    }
    // 사용자에게 쿠폰 발급
    @Transactional
    public UserCouponResponse issueCoupon(UserCouponIssueRequest request){
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryAt = now.plusDays(coupon.getAvailabilityDays());

        UserCoupon userCoupon = UserCoupon.builder()
                .userId(request.getUserId())
                .coupon(coupon)
                .status(CouponStatus.ISSUED)
                .expiryAt(expiryAt)
                .build();

        UserCoupon saved = userCouponRepository.save(userCoupon);
        return convertToUserCouponResponse(saved);

    }
    // Welcome 쿠폰 발급
    @Transactional
    public void issueWelcomCoupon(Long userId){
        List<Coupon> welcomeCoupons = couponRepository.findByIsWelcomeTrue();

        if(welcomeCoupons.isEmpty()){
            log.warn("Welcome 쿠폰 정책이 없습니다.");
            return;
        }
        // Welcome 쿠폰 발급 (정책: 50,000 이상 구매 시 10.000 할인, 30일)
        for (Coupon coupon : welcomeCoupons) {
            try{
                UserCouponIssueRequest request = new UserCouponIssueRequest();
                request.setUserId(userId);
                request.setCouponId(coupon.getCouponId());

                issueCoupon(request);
                log.info("Welcome 쿠폰 발급 성공: userId={}, couponId={}", userId, coupon.getCouponId());
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
                .orElseThrow(() -> new CouponNotFoundException("쿠폰을 찾을 수 없습니다."));

        Coupon coupon = userCoupon.getCoupon();

        // 최소 주문 금액 체크
        if(coupon.getMinOrderAmount() != null &&
            orderAmount.compareTo(BigDecimal.valueOf(coupon.getMinOrderAmount())) < 0){ // 주문 금액이 쿠폰 최소 금액 보다 작으면 예외 처리
            throw new InvalidCouponException(
                    "최소 주문 금액(" + coupon.getMinOrderAmount() + ")을 충족하지 않습니다.");
        }

        // 할인 금액 계산
        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount); // 할인 된 최종 금액

        // 쿠폰 사용 처리
        userCoupon.use();


        return CouponApplyResponse.builder()
                .userCouponId(userCouponId)
                .couponName(coupon.getCouponName())
                .originalAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .build();
    }

    public BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discountAmount;

        if(coupon.getDiscountWay() == DiscountWay.FIXED_AMOUNT){
            // 고정 할인 금액
             discountAmount = coupon.getDiscount();
        } else{
            // 퍼센트 할인
            discountAmount = orderAmount
                    .multiply(coupon.getDiscount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // 최대 할인 금액 제한
        if(coupon.getMaxDiscountAmount() != null){
            BigDecimal maxDiscount = BigDecimal.valueOf(coupon.getMaxDiscountAmount());
            if(discountAmount.compareTo(maxDiscount) > 0){ // 할인 금액이 최대 할인 금액보다 크면 최대 할인 금액으로 할인 금액 Fix!
                discountAmount = maxDiscount;
            }
        }

        return discountAmount;
    }

    // 사용자 쿠폰 목록 조회
    public Page<UserCouponResponse> getUserCoupons(Long userId, Pageable pageable){
        return userCouponRepository.findByUserId(userId,pageable)
                .map(this::convertToUserCouponResponse);
    }

    //  사용 가능한 쿠폰 조회
    public List<UserCouponResponse> getAvailableCoupons(Long userId){
        List<UserCoupon> coupons = userCouponRepository
                .findByUserIdAndStatus(userId, CouponStatus.ISSUED);

        return coupons.stream()
                .map(this::convertToUserCouponResponse)
                .toList();
    }

    // 만료된 쿠폰 처리 (배치)
    @Transactional
    public void expireCoupons(){
        List<UserCoupon> expiredCoupons = userCouponRepository
                .findAllExpiredCoupons(LocalDateTime.now());

        for (UserCoupon coupon : expiredCoupons) {
            coupon.expire();
            log.info("쿠폰 만료 처리: userCouponId={}", coupon.getUserCouponId());
        }
    }



    private CouponResponse convertToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .discountWay(coupon.getDiscountWay())
                .discount(coupon.getDiscount())
                .categoryId(coupon.getCategoryId())
                .targetBookId(coupon.getTargetBookId())
                .isBirthday(coupon.isBirthday())
                .isWelcome(coupon.isWelcome())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .availabilityDays(coupon.getAvailabilityDays())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
    private UserCouponResponse convertToUserCouponResponse(UserCoupon userCoupon) {
        return UserCouponResponse.builder()
                .userCouponId(userCoupon.getUserCouponId())
                .userId(userCoupon.getUserId())
                .coupon(convertToResponse(userCoupon.getCoupon()))
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .expiryAt(userCoupon.getExpiryAt())
                .usedAt(userCoupon.getUsedAt())
                .build();
    }
}
