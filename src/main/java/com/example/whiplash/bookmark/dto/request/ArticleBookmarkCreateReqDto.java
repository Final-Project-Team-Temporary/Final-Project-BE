package com.example.whiplash.bookmark.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArticleBookmarkCreateReqDto {

    @NotBlank(message = "기사 ID는 필수입니다.")
    private String articleId;
}
