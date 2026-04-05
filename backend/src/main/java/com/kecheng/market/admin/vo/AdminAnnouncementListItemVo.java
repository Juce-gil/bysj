package com.kecheng.market.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "后台公告列表项")
public record AdminAnnouncementListItemVo(
        @Schema(description = "公告ID") Long id,
        @Schema(description = "标题") String title,
        @Schema(description = "摘要") String summary,
        @Schema(description = "公告级别") String level,
        @Schema(description = "是否置顶") Boolean top,
        @Schema(description = "是否已发布") Boolean published,
        @Schema(description = "发布时间") String publishedAt
) {
}