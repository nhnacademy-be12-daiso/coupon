package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyCreateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.policy.CouponPolicyUpdateRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.issue.UserCouponIssueRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategoryCouponResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.policy.AvailableCouponResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.policy.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.*;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyNotFoundException;
import com.nhnacademy.coupon.domain.coupon.exception.DuplicateCouponException;
import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.repository.*;
import com.nhnacademy.coupon.domain.coupon.service.CouponPolicyService;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponPolicyServiceImpl implements CouponPolicyService {

    private final CouponPolicyRepository couponPolicyRepository;
    private final UserCouponRepository userCouponRepository;
    private final CouponCategoryRepository couponCategoryRepository;


    // 쿠폰 정책 생성
    @Transactional
    public CouponPolicyResponse createCouponPolicy(CouponPolicyCreateRequest request) {
        // 1. 정책 저장
        CouponPolicy policy = couponPolicyRepository.save(request.toEntity());

        // 2. CATEGORY 타입이면 categoryIds를 coupon_categories에 저장
        if (policy.getCouponType() == CouponType.CATEGORY &&
                request.getCategoryIds() != null &&
                !request.getCategoryIds().isEmpty()) {

            List<CouponCategory> mappings = request.getCategoryIds().stream()
                    .distinct()  // 같은 카테고리 중복 선택 방지
                    .map(categoryId -> CouponCategory.of(policy, categoryId))
                    .toList();

            couponCategoryRepository.saveAll(mappings);
        }

        return convertToResponse(policy);
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
        long issuedCount = userCouponRepository.countByCouponPolicy_CouponPolicyId(id);

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

    @Override
    @Transactional
    public UserCouponResponse issueCoupon(Long userId, UserCouponIssueRequest request) {
        // 1. 쿠폰 정책 조회
        CouponPolicy policy = couponPolicyRepository.findById(request.getCouponPolicyId())
                .orElseThrow(() -> new CouponPolicyNotFoundException("쿠폰 정책을 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();

        // 2. 정책 상태/기간 검증
        if (policy.getCouponPolicyStatus() != CouponPolicyStatus.ACTIVE) {
            throw new InvalidCouponException("발급 불가능한 쿠폰입니다. (비활성 정책)");
        }
        if (policy.getValidStartDate() != null && policy.getValidStartDate().isAfter(now)) {
            throw new InvalidCouponException("아직 발급 기간이 아닙니다.");
        }
        if (policy.getValidEndDate() != null && policy.getValidEndDate().isBefore(now)) {
            throw new InvalidCouponException("발급 기간이 지났습니다.");
        }

        // 3. 중복 발급 체크
        boolean alreadyHas =
                userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(
                        userId, policy.getCouponPolicyId());

        if (alreadyHas) {
            throw new DuplicateCouponException(
                    String.format("이미 발급받은 쿠폰입니다. (정책ID: %d)", policy.getCouponPolicyId()));
        }

        // 4. 수량 차감
        policy.decreaseQuantity();

        // 5. 만료일 계산
        LocalDateTime expiryAt = calculateExpiryDate(policy, now);

        // 6. UserCoupon 생성 (targetId 제거)
        UserCoupon userCoupon = UserCoupon.builder()
                .userId(userId)
                .couponPolicy(policy)
                .status(CouponStatus.ISSUED)
                .issuedAt(now)
                .expiryAt(expiryAt)
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

    @Override
    public List<CategoryCouponResponse> getAvailableCouponsForBook(
            Long userId,
            Long primaryCategoryId,
            Long secondaryCategoryId) {

        LocalDateTime now = LocalDateTime.now();

        log.info("▶ getAvailableCouponsForBook(userId={}, primary={}, secondary={})",
                userId, primaryCategoryId, secondaryCategoryId);

        // 1. 현재 유효한 정책 전체 (ACTIVE + 기간 유효)
        List<CouponPolicy> policies =
                couponPolicyRepository.findAllAvailable(CouponPolicyStatus.ACTIVE, now);

        log.info("✅ findAllAvailable -> policyIds = {}",
                policies.stream()
                        .map(CouponPolicy::getCouponPolicyId)
                        .toList());

        // 2. 유저가 이미 가진 쿠폰 정책 id
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        Set<Long> downloadedPolicyIds = userCoupons.stream()
                .map(uc -> uc.getCouponPolicy().getCouponPolicyId())
                .collect(Collectors.toSet());

        log.info("✅ userCoupons size = {}, downloadedPolicyIds = {}",
                userCoupons.size(), downloadedPolicyIds);

        // 3. 이 책의 카테고리(1단계 + 2단계) 모으기
        List<Long> categoryIds = new ArrayList<>();
        if (primaryCategoryId != null) {
            categoryIds.add(primaryCategoryId);
        }
        if (secondaryCategoryId != null) {
            categoryIds.add(secondaryCategoryId);
        }

        log.info("✅ categoryIds(1,2단계) = {}", categoryIds);

        // 카테고리 정보가 아예 없으면 바로 빈 리스트 반환
        if (categoryIds.isEmpty()) {
            log.info("⛔ categoryIds 비어있음 -> 빈 리스트 반환");
            return List.of();
        }

        // 4. 이 책의 카테고리들에 매핑된 CATEGORY 정책들 조회
        List<CouponCategory> mappings =
                couponCategoryRepository.findByCategoryIdIn(categoryIds);

        log.info("✅ couponCategory mappings(size={}) = {}",
                mappings.size(),
                mappings.stream()
                        .map(cc -> String.format("[cat=%d, policy=%d]",
                                cc.getCategoryId(),
                                cc.getCouponPolicy().getCouponPolicyId()))
                        .toList()
        );

        // policyId -> 이 책의 카테고리 ID들(1단계/2단계) 매핑
        Map<Long, Set<Long>> policyIdToCategoryIds = mappings.stream()
                .collect(Collectors.groupingBy(
                        cc -> cc.getCouponPolicy().getCouponPolicyId(),
                        Collectors.mapping(CouponCategory::getCategoryId, Collectors.toSet())
                ));

        Set<Long> matchingCategoryPolicyIds = policyIdToCategoryIds.keySet();

        log.info("✅ policyIdToCategoryIds = {}", policyIdToCategoryIds);
        log.info("✅ matchingCategoryPolicyIds = {}", matchingCategoryPolicyIds);

        // 5. 최종 필터링 & 응답 DTO 변환 (디버그용: 스트림 → for문)
        List<CategoryCouponResponse> result = new ArrayList<>();

        for (CouponPolicy policy : policies) {
            Long pid = policy.getCouponPolicyId();
            CouponType type = policy.getCouponType();

            log.info("➡️ candidate policy: id={}, type={}", pid, type);

            // 1) CATEGORY 아니면 스킵
            if (type != CouponType.CATEGORY) {
                log.info("   ❌ skip(id={}): not CATEGORY", pid);
                continue;
            }

            // 2) 이 책의 1/2단계 카테고리에 매핑된 정책인지
            if (!matchingCategoryPolicyIds.contains(pid)) {
                log.info("   ❌ skip(id={}): not matched category", pid);
                continue;
            }

            // 3) 이미 다운로드한 정책인지
            if (downloadedPolicyIds.contains(pid)) {
                log.info("   ❌ skip(id={}): already downloaded", pid);
                continue;
            }

            // 4) 이 정책이 이 책의 어떤 카테고리에 매핑됐는지 선택
            Set<Long> mappedCategoryIds = policyIdToCategoryIds.get(pid);
            log.info("   ✅ matched(id={}): mappedCategoryIds = {}", pid, mappedCategoryIds);

            Long categoryIdForThisBook = null;

            if (mappedCategoryIds != null) {
                // 2단계 우선
                if (secondaryCategoryId != null && mappedCategoryIds.contains(secondaryCategoryId)) {
                    categoryIdForThisBook = secondaryCategoryId;
                }
                // 아니면 1단계
                else if (primaryCategoryId != null && mappedCategoryIds.contains(primaryCategoryId)) {
                    categoryIdForThisBook = primaryCategoryId;
                }
            }

            log.info("   → chosen categoryIdForThisBook(id={}) = {}", pid, categoryIdForThisBook);

            result.add(CategoryCouponResponse.of(policy, categoryIdForThisBook));
        }

        log.info("🎯 final downloadable policyIds = {}",
                result.stream()
                        .map(r -> r.getPolicyInfo().getCouponPolicyId())
                        .toList());

        return result;
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
                .couponPolicy(convertToResponse(userCoupon.getCouponPolicy()))
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .expiryAt(userCoupon.getExpiryAt())
                .usedAt(userCoupon.getUsedAt())
                // itemName은 아직 없으니 null 또는 "" 로 두고,
                // 나중에 마이페이지 조회 서비스에서 채우는 걸로 하자.
                .itemName(null)
                .build();
    }


}
