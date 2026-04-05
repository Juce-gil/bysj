package com.kecheng.market.admin.service;

import com.kecheng.market.admin.dto.AdminUserQuery;
import com.kecheng.market.admin.vo.AdminUserListItemVo;
import com.kecheng.market.common.PageResult;

public interface AdminUserService {
    PageResult<AdminUserListItemVo> page(AdminUserQuery query);

    AdminUserListItemVo updateStatus(Long currentAdminId, Long targetUserId, Boolean disabled);
}