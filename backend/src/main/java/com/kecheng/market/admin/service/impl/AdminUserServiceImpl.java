package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminUserQuery;
import com.kecheng.market.admin.service.AdminUserService;
import com.kecheng.market.admin.vo.AdminUserListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketStore;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final MarketStore marketStore;

    public AdminUserServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public PageResult<AdminUserListItemVo> page(AdminUserQuery query) {
        return marketStore.pageAdminUsers(query.getKeyword(), query.getRole(), query.getDisabled(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public AdminUserListItemVo updateStatus(Long currentAdminId, Long targetUserId, Boolean disabled) {
        return marketStore.updateAdminUserStatus(currentAdminId, targetUserId, Boolean.TRUE.equals(disabled));
    }
}