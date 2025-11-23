package com.example.whiplash.bookmark.repository;

import com.example.whiplash.bookmark.entity.ArticleBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleBookmarkRepository extends JpaRepository<ArticleBookmark, Long> {

    /**
     * 사용자 + 기사로 북마크 조회
     * @param userId
     * @param articleId
     * @return
     */
    Optional<ArticleBookmark> findByUserIdAndArticleId(Long userId, String articleId);

    /**
     * 북마크 존재 여부 확인
     */
    boolean existsByUserIdAndArticleId(Long userId, String articleId);

    /**
     * 사용자의 북마크 기사 ID 목록 조회
     */
    @Query("SELECT ab.articleId FROM ArticleBookmark ab WHERE ab.userId = :userId ORDER BY ab.createdAt DESC")
    List<String> findArticleIdsByUserId(@Param("userId") Long userId);

    Page<ArticleBookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndArticleId(Long userId, String articleId);

    List<ArticleBookmark> findByUserIdAndArticleIdIn(Long userId, List<String> articleIds);
}
