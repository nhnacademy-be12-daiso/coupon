package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.*;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyNotFoundException;
import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.repository.BookCouponRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CategoryCouponRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class CouponPolicyServiceImpl implements CouponPolicyService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;
    private final CategoryCouponRepository categoryCouponRepository;
    private final BookCouponRepository bookCouponRepository;

    public CouponPolicyServiceImpl(
            CouponPolicyRepository couponPolicyRepository,
            UserCouponRepository userCouponRepository,
            CategoryCouponRepository categoryCouponRepository,
            BookCouponRepository bookCouponRepository) {
        this.couponPolicyRepository = couponPolicyRepository;
        this.userCouponRepository = userCouponRepository;
        this.categoryCouponRepository = categoryCouponRepository;
        this.bookCouponRepository = bookCouponRepository;
    }

    // 쿠폰 정책 생성
    @Transactional
    public CouponPolicyResponse createCouponPolicy(CouponPolicyCreateRequest request){
        // 1. 쿠폰 정책 저장
        CouponPolicy saved = couponPolicyRepository.save(request.toEntity());

//        // 2. 타입별 매핑 테이블 저장
//        if(request.getCouponType() == CouponType.CATEGORY){
//            if(request.getTargetCategoryIds() != null && !request.getTargetCategoryIds().isEmpty()){
//                for (Long categoryId : request.getTargetCategoryIds()) {
//                    CategoryCoupon categoryCoupon = new CategoryCoupon(saved, categoryId); // (쿠폰 정책 Pk, 카테고리Pk)
//                    categoryCouponRepository.save(categoryCoupon);
//                }
//                log.info("카테고리 쿠폰 매핑 완료: policyId={}, categories={}",
//                        saved.getCouponPolicyId(), request.getTargetCategoryIds());
//            }
//        } else if (request.getCouponType() == CouponType.BOOKS) {
//            // 특정 도서 쿠폰
//            if (request.getTargetBookIds() != null && !request.getTargetBookIds().isEmpty()) {
//                for (Long bookId : request.getTargetBookIds()) {
//                    BookCoupon bookCoupon = new BookCoupon(saved, bookId);
//                    bookCouponRepository.save(bookCoupon);
//                }
//                log.info("도서 쿠폰 매핑 완료: policyId={}, books={}",
//                        saved.getCouponPolicyId(), request.getTargetBookIds());
//            }
//        }
        return convertToResponse(saved);
    }
    // 쿠폰 정책 전체 조회
    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public CouponPolicyResponse couponPolicyDetail(Long id) {
        CouponPolicy policy = couponPolicyRepository.findById(id)
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));
        CouponPolicyResponse response = convertToResponse(policy);
        return response;
    }

    // 쿠폰 정책 수정
    @Override
    @Transactional
    public CouponPolicyResponse updateCouponPolicy(Long id, CouponPolicyUpdateRequest request) {
        CouponPolicy policy = couponPolicyRepository.findById(id)
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));
        // 발급된 쿠폰 개수 확인
        long issuedCount = userCouponRepository.countByCouponPolicyCouponPolicyId(id);

        if(issuedCount > 0){
            // 발급 후에는 상태만 변경 (검증 제거)
            log.info("이미 {}개의 쿠폰이 발급되어 상태만 변경합니다.", issuedCount);
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

        CouponType type = couponPolicy.getCouponType();
        if ((type == CouponType.CATEGORY || type == CouponType.BOOKS) && request.getTargetId() == null) {
            throw new InvalidCouponException("이 쿠폰은 적용 대상(targetId)이 반드시 지정되어야 합니다.");
        }

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
                .targetId(request.getTargetId())
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
                UserCouponIssueRequest request = new UserCouponIssueRequest(policy.getCouponPolicyId(),null);
                issueCoupon(userId,request);

                log.info("Welcome 쿠폰 발급 성공: userId={}, couponId={}", userId, policy.getCouponPolicyId());
            } catch (Exception e){
                // 쿠폰 발급 실패해도 회원가입은 정상 처리
                log.error("Welcome 쿠폰 발급 실패: userId={}, error={}", userId, e.getMessage());

                // TODO: 실패 이벤트 발행 또는 재시도 큐에 추가
            }
        }

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
