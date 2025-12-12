package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.query.BookCouponQuery;
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
    private final CouponBookRepository couponBookRepository;


    // 쿠폰 정책 생성
    @Transactional
    public CouponPolicyResponse createCouponPolicy(CouponPolicyCreateRequest request) {
        // 1. 정책 저장
        CouponPolicy policy = couponPolicyRepository.save(request.toEntity());

        // 2-1. CATEGORY 타입이면 categoryIds를 coupon_categories에 저장
        if (policy.getCouponType() == CouponType.CATEGORY &&
                request.getCategoryIds() != null &&
                !request.getCategoryIds().isEmpty()) {

            List<CouponCategory> mappings = request.getCategoryIds().stream()
                    .distinct()  // 같은 카테고리 중복 선택 방지
                    .map(categoryId -> CouponCategory.of(policy, categoryId))
                    .toList();

            couponCategoryRepository.saveAll(mappings);
        }
        // 2-2 BOOK 타입이면 bookIDs를 coupon_books에 저장
        if(policy.getCouponType() == CouponType.BOOKS &&
                request.getBookIds() != null &&
                !request.getBookIds().isEmpty()){
            List<CouponBook> bookMappings = request.getBookIds().stream()
                    .distinct()
                    .map(bookId -> CouponBook.of(policy, bookId))
                    .toList();
            couponBookRepository.saveAll(bookMappings);
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
    public List<CategoryCouponResponse> getAvailableCouponsForBook(BookCouponQuery query) {

        Long userId = query.getUserId();
        Long bookId = query.getBookId();
        Long primaryCategoryId = query.getPrimaryCategoryId();
        Long secondaryCategoryId = query.getSecondaryCategoryId();

        LocalDateTime now = LocalDateTime.now();

        log.info("▶ getAvailableCouponsForBook(userId={}, primary={}, secondary={})",
                userId, primaryCategoryId, secondaryCategoryId);

        // 1. 현재 유효한 정책 전체
        List<CouponPolicy> policies =
                couponPolicyRepository.findAllAvailable(CouponPolicyStatus.ACTIVE, now);

        // 2. 유저가 이미 가진 쿠폰 정책 id
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);
        Set<Long> downloadedPolicyIds = userCoupons.stream()
                .map(uc -> uc.getCouponPolicy().getCouponPolicyId())
                .collect(Collectors.toSet());


        // 3. 이 책의 카테고리(1단계 + 2단계) 모으기
        List<Long> categoryIds = new ArrayList<>();
        if (primaryCategoryId != null) { categoryIds.add(primaryCategoryId); }
        if (secondaryCategoryId != null) { categoryIds.add(secondaryCategoryId); }

        // 카테고리 정보가 아예 없으면 바로 빈 리스트 반환
        Map<Long, Set<Long>> policyIdToCategoryIds;

        if (!categoryIds.isEmpty()) {
            List<CouponCategory> mappings =
                    couponCategoryRepository.findByCategoryIdIn(categoryIds);

            policyIdToCategoryIds = mappings.stream()
                    .collect(Collectors.groupingBy(
                            cc -> cc.getCouponPolicy().getCouponPolicyId(),
                            Collectors.mapping(CouponCategory::getCategoryId, Collectors.toSet())
                    ));
        } else {
            policyIdToCategoryIds = Map.of();
        }

        Set<Long> matchingCategoryPolicyIds = policyIdToCategoryIds.keySet();

        // 3-2. BOOKS 매핑: 이 bookId에 붙은 정책들
        List<CouponBook> bookMappings = couponBookRepository.findByBookId(bookId);
        Set<Long> bookPolicyIds = bookMappings.stream()
                .map(cb -> cb.getCouponPolicy().getCouponPolicyId())
                .collect(Collectors.toSet());


        // 4. 필터링 + DTO 변환
        return policies.stream()
                // 이미 가진 건 제외
                .filter(policy -> !downloadedPolicyIds.contains(policy.getCouponPolicyId()))
                // 이 책에서 쓸 수 있는 타입만 남기기
                .filter(policy -> {
                    CouponType type = policy.getCouponType();
                    Long pid = policy.getCouponPolicyId();

                    if (type == CouponType.CATEGORY) {
                        return matchingCategoryPolicyIds.contains(pid);
                    }
                    if (type == CouponType.BOOKS) {
                        return bookPolicyIds.contains(pid);
                    }
                    return false; // GENERAL / WELCOME / BIRTHDAY 등은 도서 상세에서는 제외
                })
                .map(policy -> {
                    Long policyId = policy.getCouponPolicyId();
                    Long categoryIdForThisBook = null;

                    if (policy.getCouponType() == CouponType.CATEGORY) {
                        Set<Long> mappedCategoryIds = policyIdToCategoryIds.get(policyId);
                        if (mappedCategoryIds != null) {
                            if (secondaryCategoryId != null && mappedCategoryIds.contains(secondaryCategoryId)) {
                                categoryIdForThisBook = secondaryCategoryId;  // 2단계 우선
                            } else if (primaryCategoryId != null && mappedCategoryIds.contains(primaryCategoryId)) {
                                categoryIdForThisBook = primaryCategoryId;    // 아니면 1단계
                            }
                        }
                    }

                    return CategoryCouponResponse.of(policy, categoryIdForThisBook);
                })
                .toList();
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
