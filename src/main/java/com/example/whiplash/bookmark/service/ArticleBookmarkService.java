package com.example.whiplash.bookmark.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.bookmark.dto.response.BookmarkStatusResDto;
import com.example.whiplash.bookmark.dto.response.BookmarkedArticleResDto;
import com.example.whiplash.bookmark.entity.ArticleBookmark;
import com.example.whiplash.bookmark.repository.ArticleBookmarkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleBookmarkService {

    private final ArticleBookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;

    @Transactional
    public void addBookmark(Long userId, String articleId) {

        log.info("📌 북마크 등록: userId={}, articleId={}", userId, articleId);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND));

        // 2. 이미 북마크되어 있는지 확인
        if (bookmarkRepository.existsByUserIdAndArticleId(userId, articleId)) {
            log.warn("⚠️ 이미 북마크됨: userId={}, articleId={}", userId, articleId);
            throw new WhiplashException(ErrorStatus.BOOKMARK_ALREADY_EXISTS);
        }

        ArticleBookmark bookmark = ArticleBookmark.builder()
                .userId(userId)
                .articleId(articleId)
                .build();

        bookmarkRepository.save(bookmark);
    }

    public Page<BookmarkedArticleResDto> getBookmarkedArticles(Long userId, Pageable pageable) {

        Page<ArticleBookmark> articleBookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<String> articleIdList = articleBookmarks.getContent().stream()
                .map(ArticleBookmark::getArticleId)
                .toList();

        List<Article> articles = articleRepository.findAllById(articleIdList);

        Page<BookmarkedArticleResDto> response = articleBookmarks.map(bookmark -> {
            Article article = articles.stream()
                    .filter(a -> a.getId().equals(bookmark.getArticleId()))
                    .findFirst()
                    .orElse(null);

            if (article == null) {
                log.warn("⚠️ 기사를 찾을 수 없음: articleId={}", bookmark.getArticleId());
                return null;
            }

            return BookmarkedArticleResDto.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .publishedAt(article.getPublishedAt())
                    .build();

        });

        log.info("✅ 북마크 목록 조회 완료: totalElements={}", response.getTotalElements());

        return response;
    }

    @Transactional
    public void deleteBookmark(Long userId, String articleId) {

        log.info("🗑️ 북마크 취소: userId={}, articleId={}", userId, articleId);

        if(!bookmarkRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND);
        }

        bookmarkRepository.deleteByUserIdAndArticleId(userId, articleId);

        log.info("✅ 북마크 취소 완료");
    }

    public BookmarkStatusResDto getBookmarkStatus(Long userId, String articleId) {

        log.info("🔍 북마크 상태 조회: userId={}, articleId={}", userId, articleId);

        ArticleBookmark bookmark = bookmarkRepository
                .findByUserIdAndArticleId(userId, articleId)
                .orElse(null);

        boolean isBookmarked = bookmark != null;
        Long bookmarkId = isBookmarked ? bookmark.getId() : null;

        return BookmarkStatusResDto.builder()
                .isBookmarked(isBookmarked)
                .bookmarkId(bookmarkId)
                .build();
    }
}

