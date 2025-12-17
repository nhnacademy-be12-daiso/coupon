package com.nhnacademy.coupon.domain.coupon.service.impl;

import com.nhnacademy.coupon.domain.coupon.dto.request.usage.BatchCouponUseRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponCancelRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.CouponUseItemRequest;
import com.nhnacademy.coupon.domain.coupon.dto.request.usage.SingleCouponApplyRequest;
import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookCategoryResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategorySimpleResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.CouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.policy.CouponPolicyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.usage.SingleCouponApplyResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.user.UserCouponResponse;
import com.nhnacademy.coupon.domain.coupon.entity.CouponPolicy;
import com.nhnacademy.coupon.domain.coupon.entity.UserCoupon;
import com.nhnacademy.coupon.domain.coupon.exception.CouponPolicyNotFoundException;
import com.nhnacademy.coupon.domain.coupon.exception.InvalidCouponException;
import com.nhnacademy.coupon.domain.coupon.exception.UserCouponNotFoundException;
import com.nhnacademy.coupon.domain.coupon.repository.CouponBookRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponCategoryRepository;
import com.nhnacademy.coupon.domain.coupon.repository.CouponPolicyRepository;
import com.nhnacademy.coupon.domain.coupon.repository.UserCouponRepository;
import com.nhnacademy.coupon.domain.coupon.service.UserCouponService;
import com.nhnacademy.coupon.domain.coupon.type.CouponPolicyStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponStatus;
import com.nhnacademy.coupon.domain.coupon.type.CouponType;
import com.nhnacademy.coupon.domain.coupon.type.DiscountWay;
import com.nhnacademy.coupon.global.client.BookServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCouponServiceImpl implements UserCouponService {

    private final UserCouponRepository userCouponRepository;
    private final BookServiceClient bookServiceClient;
    private final CouponPolicyRepository couponPolicyRepository;
    private final CouponCategoryRepository couponCategoryRepository;
    private final CouponBookRepository couponBookRepository;


    // 사용 가능한 쿠폰 조회
    @Transactional(readOnly = true)
    public List<UserCouponResponse> getAvailableCoupons(Long userId, Long bookId){
        // 유저의 ISSUED(보유 중) 상태인 모든 쿠폰 조회, 만료 쿠폰 제외
        LocalDateTime now = LocalDateTime.now();

        List<UserCoupon> myCoupons = userCouponRepository.findByUserIdAndStatus(userId, CouponStatus.ISSUED)
                .stream()
                .filter(c -> c.getExpiryAt() == null || c.getExpiryAt().isAfter(now))
                .toList();


        // bookId가 없으면(마이페이지 조회) 전체 반환
        if(bookId == null){
            return myCoupons.stream()
                    .map(this::convertToUserCouponResponse)
                    .toList();
        }
        // bookId가 있으면 책 정보(카테고리 포함) 조회
        BookCategoryResponse bookInfo = bookServiceClient.getBookCategory(bookId);

        // 필터링
        return myCoupons.stream()
                .filter(coupon -> isApplicableForBook(coupon, bookId, bookInfo))
                .map(this::convertToUserCouponResponse)
                .toList();
    }

    private boolean isApplicableForBook(UserCoupon coupon, Long bookId, BookCategoryResponse bookInfo) {
        CouponPolicy policy = coupon.getCouponPolicy();
        CouponType type = policy.getCouponType();

        // 1. 범용 쿠폰: 어떤 도서에도 적용 가능
        if (type == CouponType.WELCOME || type == CouponType.BIRTHDAY || type == CouponType.GENERAL) {
            return true;
        }

        // 2. 책의 카테고리 ID들 모으기
        List<Long> categoryIds = extractCategoryIds(bookInfo);

        // 카테고리 정보가 전혀 없으면 CATEGORY/BOOKS 적용 불가
        if (categoryIds.isEmpty() && (type == CouponType.CATEGORY || type == CouponType.BOOKS)) {
            return false;
        }

        // 3. 카테고리 쿠폰: 이 정책이 책의 카테고리 중 하나에 매핑되어 있으면 OK
        if (type == CouponType.CATEGORY) {
            Long policyId = policy.getCouponPolicyId();

            return couponCategoryRepository
                    .findByCouponPolicy_CouponPolicyId(policyId)
                    .stream()
                    .anyMatch(cc -> categoryIds.contains(cc.getCategoryId()));
        }

        // 4. 도서 쿠폰(BOOKS): 쿠폰 정책(policyId)가 bookId와 매핑되어 있으면 true
        if (type == CouponType.BOOKS) {
            return couponBookRepository.
                    existsByCouponPolicy_CouponPolicyIdAndBookId(policy.getCouponPolicyId(),bookId);
        }

        return false;
    }

    // 만료된 쿠폰 개수 반환
    @Transactional
    public void expireCoupons(){
        int count = userCouponRepository.bulkExpireCoupons(LocalDateTime.now());
        log.info("만료 처리된 쿠폰 개수: {}", count);
    }

    public BigDecimal calculateDiscount(CouponPolicy couponPolicy, BigDecimal orderAmount) {
        BigDecimal discountAmount;

        if(couponPolicy.getMinOrderAmount() != null &&
                orderAmount.compareTo(BigDecimal.valueOf(couponPolicy.getMinOrderAmount())) < 0){
            return BigDecimal.ZERO;
        }

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

    /**
     * 단일 도서에 단일 쿠폰 적용 계산(실시간)
     * 실제 사용하지 않고, 팔인 금액만 계산
     */
    @Override
    @Transactional(readOnly = true)
    public SingleCouponApplyResponse calculateSingleCoupon(Long userId, SingleCouponApplyRequest request) {
        try{
            // 1. 쿠폰 조회
            UserCoupon userCoupon = userCouponRepository.findById(request.getUserCouponId())
                    .orElseThrow(() -> new UserCouponNotFoundException(request.getUserCouponId()));

            CouponPolicy policy = userCoupon.getCouponPolicy();

            // 2. 가본 검증
            validateCouponOwnership(userCoupon, userId);
            validateCouponStatus(userCoupon);

            // 3. 도서 총 금액 계산
            BigDecimal itemTotal = request.getBookPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

            // 4. 최소 주문 금액 검증
            validateMinOrderAmount(policy,itemTotal);

            // 5. 카테고리/도서 쿠폰 검증
            BookCategoryResponse bookInfo = bookServiceClient.getBookCategory(request.getBookId());
            
            if(!isApplicableForBook(userCoupon, request.getBookId(), bookInfo)){
                throw new InvalidCouponException("이 도서(또는 카테고리)에는 적용할 수 없는 쿠폰입니다.");
            }
            // 6. 할인 금액 계산
            BigDecimal discountAmount = calculateDiscount(policy, itemTotal); // 최종 할인 금액
            BigDecimal finalAmount = itemTotal.subtract(discountAmount); // 할인 적용 후 최종 금액

            log.info("쿠폰 계산 성공: bookId={}, couponId={}, discount={}",
                    request.getBookId(), request.getUserCouponId(), discountAmount);

            return SingleCouponApplyResponse.builder()
                    .bookId(request.getBookId())
                    .userCouponId(request.getUserCouponId())
                    .couponName(policy.getCouponPolicyName())
                    .originalAmount(itemTotal)
                    .discountAmount(discountAmount)
                    .finalAmount(finalAmount)
                    .applicable(true)
                    .message("적용 가능")
                    .build();
        } catch (Exception e) {
            log.warn("쿠폰 계산 실패: bookId={}, couponId={}, error={}",
                    request.getBookId(), request.getUserCouponId(), e.getMessage());

            BigDecimal itemTotal = request.getBookPrice()
                    .multiply(BigDecimal.valueOf(request.getQuantity()));

            return SingleCouponApplyResponse.builder()
                    .bookId(request.getBookId())
                    .userCouponId(request.getUserCouponId())
                    .couponName(null)
                    .originalAmount(itemTotal)
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(itemTotal)
                    .applicable(false)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    @Transactional
    public void useCoupons(Long userId, BatchCouponUseRequest request) {

        Set<Long> distinctCouponIds = request.items().stream()
                .map(CouponUseItemRequest::userCouponId)
                .collect(Collectors.toSet());

        if (distinctCouponIds.size() != request.items().size()) {
            throw new InvalidCouponException("한 주문에서 동일 쿠폰을 여러 도서에 중복 적용할 수 없습니다.");
        }

        // 1) 쿠폰 일괄 조회
        List<Long> couponIds = request.items().stream()
                .map(CouponUseItemRequest::userCouponId)
                .toList();

        List<UserCoupon> coupons = userCouponRepository.findAllById(couponIds);

        if (coupons.size() != couponIds.size()) {
            throw new UserCouponNotFoundException(null);
        }

        Map<Long, UserCoupon> couponMap = coupons.stream()
                .collect(Collectors.toMap(UserCoupon::getUserCouponId, c -> c));

        LocalDateTime now = LocalDateTime.now();

        // 1. bookId 중복 제거
        Set<Long> bookIds = request.items().stream()
                .map(CouponUseItemRequest::bookId)
                .collect(Collectors.toSet());

        // 2. bookId별로 딱 1번만 호출해서 캐시
        Map<Long, BookCategoryResponse> bookInfoMap = new HashMap<>();
        for (Long bookId : bookIds) {
            bookInfoMap.put(bookId, bookServiceClient.getBookCategory(bookId));
        }

        // 2) 도서별 검증 후 사용처리
        for (CouponUseItemRequest item : request.items()) {
            UserCoupon coupon = couponMap.get(item.userCouponId());

            if (!coupon.getUserId().equals(userId)) {
                throw new InvalidCouponException("본인의 쿠폰만 사용할 수 있습니다. (couponId=" + coupon.getUserCouponId() + ")");
            }

            // 이미 같은 주문에서 USED면 멱등 처리(재시도 대비)
            if (coupon.getStatus() == CouponStatus.USED
                    && Objects.equals(coupon.getUsedOrderId(), request.orderId())) {
                continue;
            }

            if (coupon.getStatus() != CouponStatus.ISSUED) {
                throw new InvalidCouponException("사용할 수 없는 쿠폰입니다. (status=" + coupon.getStatus() + ")");
            }

            if (coupon.getExpiryAt() != null && !coupon.getExpiryAt().isAfter(now)) {
                throw new InvalidCouponException("만료된 쿠폰입니다. (couponId=" + coupon.getUserCouponId() + ")");
            }

            // 여기서부터는 map에서 꺼내 쓰기 (N번 호출 제거)
            BookCategoryResponse bookInfo = bookInfoMap.get(item.bookId());
            if (bookInfo == null) {
                throw new InvalidCouponException("도서 정보를 찾을 수 없습니다. (bookId=" + item.bookId() + ")");
            }

            if (!isApplicableForBook(coupon, item.bookId(), bookInfo)) {
                throw new InvalidCouponException(
                        "해당 도서에 적용할 수 없는 쿠폰입니다. (bookId=" + item.bookId()
                                + ", couponId=" + coupon.getUserCouponId() + ")"
                );
            }

            coupon.use(request.orderId());
        }
    }


    @Override
    @Transactional
    public void cancelCouponUsage(Long userId, CouponCancelRequest request) {
        List<Long> couponIds = request.getUserCouponIds();

        // 1. 쿠폰 조회
        List<UserCoupon> coupons = userCouponRepository.findAllById(couponIds);

        // 2. 개수 검증
        if(coupons.size() != couponIds.size()){
            throw new UserCouponNotFoundException(null);
        }

        for (UserCoupon coupon : coupons) {
            if (!coupon.getUserId().equals(userId)) {
                throw new InvalidCouponException("본인의 쿠폰만 취소할 수 있습니다.");
            }
            // 멱등 처리: 이미 취소(ISSUED) 상태면 성공으로 간주
            if (coupon.getStatus() == CouponStatus.ISSUED) {
                continue;
            }

            coupon.cancel(request.getOrderId()); // 주문ID 검증 포함
        }
    }
    @Override
    @Transactional
    public UserCouponResponse downloadCoupon(Long userId, Long couponPolicyId) {
        LocalDateTime now = LocalDateTime.now();

        // 1. 정책 조회 + 상태/기간 체크
        CouponPolicy policy = couponPolicyRepository.findById(couponPolicyId)
                .orElseThrow(() -> new CouponPolicyNotFoundException("존재하지 않는 쿠폰 정책입니다."));

        if (policy.getCouponPolicyStatus() != CouponPolicyStatus.ACTIVE) {
            throw new InvalidCouponException("발급 불가능한 쿠폰입니다.");
        }
        if (policy.getValidStartDate() != null && policy.getValidStartDate().isAfter(now)) {
            throw new InvalidCouponException("아직 발급 기간이 아닙니다.");
        }
        if (policy.getValidEndDate() != null && policy.getValidEndDate().isBefore(now)) {
            throw new InvalidCouponException("발급 기간이 지났습니다.");
        }

        // 2. 이미 발급된 쿠폰인지 체크 (1인 1장 기준)
        if (userCouponRepository.existsByUserIdAndCouponPolicy_CouponPolicyId(userId, couponPolicyId)) {
            // 이미 있다면 그냥 그걸 반환하거나, 예외를 던지거나 선택
            // 여기서는 예외로 처리
            throw new InvalidCouponException("이미 다운로드한 쿠폰입니다.");
        }

        // 3. 정책 수량 차감
        policy.decreaseQuantity();  // quantity가 null(무제한)이면 아무 일 안 함

        // 4. 만료일 계산 (상대일수 or 고정일자)
        LocalDateTime expiryAt;
        if (policy.getValidDays() != null) {
            expiryAt = now.plusDays(policy.getValidDays());
        } else {
            expiryAt = policy.getValidEndDate(); // null일 수도 있음 (무기한)
        }

        UserCoupon userCoupon = UserCoupon.builder()
                .couponPolicy(policy)
                .userId(userId)
                .status(CouponStatus.ISSUED)
                .issuedAt(now)
                .expiryAt(expiryAt)
                .build();

        UserCoupon saved = userCouponRepository.save(userCoupon);

        return UserCouponResponse.from(saved);
    }

    /**
     * 사용자 쿠폰 목록 조회 (카테고리/도서명 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserCouponResponse> getUserCoupons(Long userId) {
        // 1. 유저의 모든 쿠폰 조회 (couponPolicy fetch join 되어 있음)
        List<UserCoupon> userCoupons = userCouponRepository.findByUserId(userId);

        // 비어 있으면 바로 반환
        if (userCoupons.isEmpty()) {
            return List.of();
        }

        // 2. CATEGORY 타입 정책 id 모으기
        List<Long> categoryPolicyIds = userCoupons.stream()
                .map(UserCoupon::getCouponPolicy)
                .filter(policy -> policy.getCouponType() == CouponType.CATEGORY)
                .map(CouponPolicy::getCouponPolicyId)
                .distinct()
                .toList();
        // TODO: BOOK 타입 쿠폰 구현 예정
        // 정책ID -> 카테고리ID 리스트 매핑
        Map<Long, List<Long>> policyToCategoryIds = new HashMap<>();
        // 전체 카테고리 id 모음
        Set<Long> allCategoryIds = new java.util.HashSet<>();

        if (!categoryPolicyIds.isEmpty()) {
            couponCategoryRepository.findByCouponPolicy_CouponPolicyIdIn(categoryPolicyIds)
                    .forEach(cc -> {
                        Long policyId = cc.getCouponPolicy().getCouponPolicyId();
                        policyToCategoryIds
                                .computeIfAbsent(policyId, k -> new java.util.ArrayList<>())
                                .add(cc.getCategoryId());
                        allCategoryIds.add(cc.getCategoryId());
                    });
        }

        // 3. 카테고리 id -> 이름 맵핑 (Book 서버 Feign 호출)
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!allCategoryIds.isEmpty()) {
            List<Long> idList = allCategoryIds.stream().toList();

            List<CategorySimpleResponse> categoryInfos =
                    bookServiceClient.getCategoriesByIds(idList);

            for (CategorySimpleResponse c : categoryInfos) {
                categoryNameMap.put(c.categoryId(), c.categoryName());
            }
        }
        log.info("카테고리명 조회 완료: {}", categoryNameMap);


        // 4. 최종 응답 변환
        return userCoupons.stream()
                .map(uc -> {
                    CouponPolicy policy = uc.getCouponPolicy();
                    String itemName = null;

                    if (policy.getCouponType() == CouponType.CATEGORY) {
                        List<Long> catIds =
                                policyToCategoryIds.getOrDefault(policy.getCouponPolicyId(), List.of());

                        itemName = catIds.stream()
                                .map(categoryNameMap::get)
                                .filter(name -> name != null && !name.isBlank())
                                .collect(Collectors.joining(", "));
                    }
                    // BOOKS 타입은 나중에 BookCoupon 붙이면서 도서명 조회해서 채우기 (TODO)

                    return UserCouponResponse.builder()
                            .userCouponId(uc.getUserCouponId())
                            .userId(uc.getUserId())
                            .couponPolicy(convertToResponse(policy))
                            .status(uc.getStatus())
                            .issuedAt(uc.getIssuedAt())
                            .expiryAt(uc.getExpiryAt())
                            .usedAt(uc.getUsedAt())
                            .itemName(itemName)
                            .build();
                })
                .toList();
    }


    /**
     * 쿠폰 소유자 검증
     */
    private void validateCouponOwnership(UserCoupon userCoupon, Long userId) {
        if (!userCoupon.getUserId().equals(userId)) {
            throw new InvalidCouponException("본인의 쿠폰이 아닙니다.");
        }
    }

    /**
     * 쿠폰 상태 검증
     */
    private void validateCouponStatus(UserCoupon userCoupon) {
        if (!userCoupon.isAvailable()) {
            throw new InvalidCouponException(
                    "사용할 수 없는 쿠폰입니다. (상태: " + userCoupon.getStatus() + ")");
        }
    }

    /**
     * 최소 주문 금액 검증
     */
    private void validateMinOrderAmount(CouponPolicy policy, BigDecimal itemTotal) {
        if (policy.getMinOrderAmount() != null &&
                itemTotal.compareTo(BigDecimal.valueOf(policy.getMinOrderAmount())) < 0) {
            throw new InvalidCouponException(
                    "최소 주문 금액 미달입니다. (필요: " +
                            policy.getMinOrderAmount() + "원)");
        }
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

    private UserCouponResponse convertToResponseWithItemName(
            UserCoupon userCoupon,
            Map<Long, String> categoryNameMap) {

        // TODO: 향후 CouponCategory + BookService 연동으로 itemName 세팅
        String itemName = null;

        return UserCouponResponse.builder()
                .userCouponId(userCoupon.getUserCouponId())
                .userId(userCoupon.getUserId())
                .couponPolicy(convertToResponse(userCoupon.getCouponPolicy()))
                .status(userCoupon.getStatus())
                .issuedAt(userCoupon.getIssuedAt())
                .expiryAt(userCoupon.getExpiryAt())
                .usedAt(userCoupon.getUsedAt())
                .itemName(itemName)
                .build();
    }

    private List<Long> extractCategoryIds(BookCategoryResponse bookInfo) {
        List<Long> categoryIds = new ArrayList<>();
        if (bookInfo.getPrimaryCategoryId() != null) {
            categoryIds.add(bookInfo.getPrimaryCategoryId());
        }
        if (bookInfo.getSecondaryCategoryId() != null) {
            categoryIds.add(bookInfo.getSecondaryCategoryId());
        }
        return categoryIds;
    }



}
