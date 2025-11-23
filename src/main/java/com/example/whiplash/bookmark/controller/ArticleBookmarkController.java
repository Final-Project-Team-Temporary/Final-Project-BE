package com.example.whiplash.bookmark.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.bookmark.dto.request.ArticleBookmarkCreateReqDto;
import com.example.whiplash.bookmark.dto.response.BookmarkStatusResDto;
import com.example.whiplash.bookmark.dto.response.BookmarkedArticleResDto;
import com.example.whiplash.bookmark.service.ArticleBookmarkService;
import com.example.whiplash.config.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles/bookmarks")
@RequiredArgsConstructor
public class ArticleBookmarkController {

    private final ArticleBookmarkService articleBookmarkService;

    @PostMapping
    public ApiResponse<?> createBookmark(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ArticleBookmarkCreateReqDto reqDto
    ) {

        Long userId = principal.getUserId();

        articleBookmarkService.addBookmark(userId, reqDto.getArticleId());

        return ApiResponse.onCreated(reqDto);
    }

    @GetMapping
    public ApiResponse<?> getBookmarkedArticles(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Long userId = principal.getUserId();

        Pageable pageable = PageRequest.of(page, pageSize);

        Page<BookmarkedArticleResDto> bookmarkedArticles = articleBookmarkService.getBookmarkedArticles(userId, pageable);

        return ApiResponse.onSuccess(bookmarkedArticles);
    }

    @DeleteMapping("/{articleId}")
    public ApiResponse<?> deleteBookmark(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String articleId
    ) {
        Long userId = principal.getUserId();

        articleBookmarkService.deleteBookmark(userId, articleId);

        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/status/{articleId}")
    public ApiResponse<?> getBookmarkedStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String articleId
    ) {

        Long userId = principal.getUserId();

        BookmarkStatusResDto response = articleBookmarkService.getBookmarkStatus(userId, articleId);

        return ApiResponse.onSuccess(response);
    }

}

