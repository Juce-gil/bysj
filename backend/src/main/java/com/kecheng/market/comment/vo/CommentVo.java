package com.kecheng.market.comment.vo;

public record CommentVo(Long id, Long goodsId, Long userId, String userName, String content, String createTime) {
}
