package com.kecheng.market.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(
        @NotNull(message = "商品ID不能为空") Long goodsId,
        @NotBlank(message = "评论内容不能为空") String content
) {
}
