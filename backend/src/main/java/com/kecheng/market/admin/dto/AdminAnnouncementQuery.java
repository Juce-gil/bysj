package com.kecheng.market.admin.dto;

import com.kecheng.market.common.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 后台公告分页查询参数。
 */
public class AdminAnnouncementQuery extends PageQuery {

    @Schema(description = "关键字：匹配标题、摘要、内容")
    private String keyword;

    @Schema(description = "是否已发布")
    private Boolean published;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }
}