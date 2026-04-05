package com.kecheng.market.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Admin announcement detail")
public record AdminAnnouncementDetailVo(
        @Schema(description = "Announcement id") Long id,
        @Schema(description = "Announcement title") String title,
        @Schema(description = "Announcement summary") String summary,
        @Schema(description = "Announcement content") String content,
        @Schema(description = "Announcement level") String level,
        @Schema(description = "Whether the announcement is pinned") Boolean top,
        @Schema(description = "Whether the announcement is published") Boolean published,
        @Schema(description = "Announcement published time") String publishedAt
) {
}
