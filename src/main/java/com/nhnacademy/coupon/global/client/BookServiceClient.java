package com.nhnacademy.coupon.global.client;

import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookCategoryResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.book.BookResponse;
import com.nhnacademy.coupon.domain.coupon.dto.response.categoryCoupon.CategorySimpleResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "TEAM3-BOOKSEARCH")
public interface BookServiceClient {

    /**
     * 전체 카테고리 목록 조회
     */
//    @GetMapping("/api/categories")
//    List<CategoryResponse> getAllCategories();

    /**
     * 특정 도서의 카테고리 조회 (쿠폰 검증용)
     */
    @GetMapping("/api/books/{bookId}/category")
    BookCategoryResponse getBookCategory(@PathVariable Long bookId);

    /**
     * 여러 도서의 카테고리 일괄 조회 (쿠폰 검증용)
     */
    @GetMapping("/api/books/categories")
    List<BookCategoryResponse> getBookCategories(@RequestParam List<Long> bookIds);

    /**
     * 특정 도서 정보 조회 (도서 쿠폰 검증용)
     */
    @GetMapping("/api/books/{bookId}")
    BookResponse getBook(@PathVariable Long bookId);

    @GetMapping("/api/books/categoriesIds")
    List<CategorySimpleResponse> getCategoriesByIds(
            @RequestParam("ids") List<Long> categoryIds
    );

}
