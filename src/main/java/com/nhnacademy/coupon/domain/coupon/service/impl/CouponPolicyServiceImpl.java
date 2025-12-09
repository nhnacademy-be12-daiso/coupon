package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.*;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyNotFoundException;
import com.nhnacademy.coupon.domain.coupon.exception.DuplicateCouponException;
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
    public CouponPolicyResponse createCouponPolicy(CouponPolicyCreateRequest request) {
        CouponPolicy saved = couponPolicyRepository.save(request.toEntity());
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
        return convertToResponse(policy);
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

    /**
     * 통합된 쿠폰 발급 메서드
     * - Welcome 쿠폰, 생일 쿠폰: targetId = null
     * - 카테고리 쿠폰: targetId = 카테고리ID
     * - 도서 쿠폰: targetId = 도서ID
     *
     * @param userId 사용자 ID
     * @param request 쿠폰 발급 요청 (couponPolicyId, targetId)
     * @return 발급된 사용자 쿠폰 정보
     */
    @Override
    @Transactional
    public UserCouponResponse issueCoupon(Long userId, UserCouponIssueRequest request){
        // 1. 쿠폰 정책 조회
        CouponPolicy policy = couponPolicyRepository.findById(request.getCouponPolicyId())
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));

        // 2. targetId 필수 여부 검증
        validateTargetId(policy.getCouponType(), request.getTargetId());

        // 3. 중복 발급 체크 (타입별로 다르게 처리)
        checkDuplicateIssuance(userId, policy, request.getTargetId());

        // 4. 수량 차감
        policy.decreaseQuantity();

        // 5. 만료일 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryAt = calculateExpiryDate(policy, now);

        // 6. UserCoupon 생성 및 저장
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponPolicy(policy)
                .targetId(request.getTargetId())
                .status(CouponStatus.ISSUED)
                .issuedAt(now)
                .expiryAt(expiryAt)
                .build();

        UserCoupon saved = userCouponRepository.save(userCoupon);
        log.info("쿠폰 발급 완료: userId={}, policyId={}, type={}, targetId={}",
                userId, policy.getCouponPolicyId(), policy.getCouponType(), request.getTargetId());

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

    // ========== Private Helper Methods ==========

    /**
     * targetId 필수 여부 검증
     * - CATEGORY, BOOKS 타입: targetId 필수
     * - WELCOME, BIRTHDAY 등: targetId 불필요
     */
    private void validateTargetId(CouponType couponType, Long targetId) {
        if ((couponType == CouponType.CATEGORY || couponType == CouponType.BOOKS)
                && targetId == null) {
            throw new InvalidCouponException(
                    "카테고리/도서 쿠폰은 적용 대상(targetId)이 반드시 필요합니다.");
        }
    }

    /**
     * 중복 발급 체크
     *
     * 쿠폰 타입별 중복 체크 로직:
     * 1. CATEGORY/BOOKS 쿠폰: 정책ID + targetId 조합으로 체크
     *    - 같은 카테고리/도서에 대해 같은 정책의 쿠폰은 1번만 발급
     *    - 예: "소설 카테고리 10% 할인" 쿠폰을 이미 받았으면 재발급 불가
     *
     * 2. WELCOME/BIRTHDAY 등 일반 쿠폰: 정책ID만으로 체크
     *    - targetId가 없으므로 정책 단위로만 체크
     *    - 예: "Welcome 쿠폰"은 회원당 1번만 발급
     *
     * @param userId 사용자 ID
     * @param policy 쿠폰 정책
     * @param targetId 적용 대상 ID (null 가능)
     */
    private void checkDuplicateIssuance(Long userId, CouponPolicy policy, Long targetId) {
        boolean isDuplicate;

        if (policy.getCouponType() == CouponType.CATEGORY ||
                policy.getCouponType() == CouponType.BOOKS) {
            // 카테고리/도서 쿠폰: 정책 + targetId 조합으로 체크
            isDuplicate = userCouponRepository
                    .existsByUserIdAndCouponPolicy_CouponPolicyIdAndTargetId(
                            userId, policy.getCouponPolicyId(), targetId);

            if (isDuplicate) {
                throw new DuplicateCouponException(
                        String.format("이미 발급받은 쿠폰입니다. (정책ID: %d, 대상ID: %d)",
                                policy.getCouponPolicyId(), targetId));
            }
        } else {
            // Welcome, Birthday 등 일반 쿠폰: 정책만으로 체크
            isDuplicate = userCouponRepository
                    .existsByUserIdAndCouponPolicy_CouponPolicyId(
                            userId, policy.getCouponPolicyId());

            if (isDuplicate) {
                throw new DuplicateCouponException(
                        String.format("이미 발급받은 쿠폰입니다. (정책ID: %d)",
                                policy.getCouponPolicyId()));
            }
        }
    }

    /**
     * 쿠폰 만료일 계산
     * 우선순위: validDays > validEndDate > 기본값(1년)
     *
     * @param policy 쿠폰 정책
     * @param issueTime 발급 시각
     * @return 만료 일시
     */
    private LocalDateTime calculateExpiryDate(CouponPolicy policy, LocalDateTime issueTime) {
        // 상대적 유효기간 (예: 발급일로부터 30일)
        if (policy.getValidDays() != null) {
            return issueTime.plusDays(policy.getValidDays());
        }

        // 절대적 유효기간 (예: 2024-12-31까지)
        if (policy.getValidEndDate() != null) {
            return policy.getValidEndDate();
        }

        // 기본값: 1년
        return issueTime.plusYears(1);
    }

    // ========== Conversion Methods ==========

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
}
