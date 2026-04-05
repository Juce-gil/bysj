package com.kecheng.market.admin.dto;

import com.kecheng.market.common.dto.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 后台用户分页查询参数。
 */
public class AdminUserQuery extends PageQuery {

    @Schema(description = "关键词：匹配用户名、昵称、学号、手机号")
    private String keyword;

    @Schema(description = "角色过滤", example = "user")
    private String role;

    @Schema(description = "是否禁用")
    private Boolean disabled;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
