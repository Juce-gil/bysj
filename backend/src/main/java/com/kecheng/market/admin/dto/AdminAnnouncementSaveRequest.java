package com.kecheng.market.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Admin announcement save request")
public record AdminAnnouncementSaveRequest(
        @Schema(description = "Announcement title")
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 100, message = "Title length must be at most 100 characters")
        String title,

        @Schema(description = "Announcement summary")
        @Size(max = 255, message = "Summary length must be at most 255 characters")
        String summary,

        @Schema(description = "Announcement content")
        @NotBlank(message = "Content cannot be blank")
        @Size(max = 10000, message = "Content length must be at most 10000 characters")
        String content,

        @Schema(description = "Announcement level")
        @Size(max = 30, message = "Level length must be at most 30 characters")
        String level,

        @Schema(description = "Whether the announcement is pinned")
        Boolean top,

        @Schema(description = "Whether the announcement is published")
        Boolean published
) {
}
