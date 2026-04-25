package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminUserQuery;
import com.kecheng.market.admin.service.AdminUserService;
import com.kecheng.market.admin.vo.AdminUserListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final StorageAccessSupport storage;

    public AdminUserServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storage = new StorageAccessSupport(
                storageMode,
                marketStoreProvider.getIfAvailable(),
                persistenceServiceProvider.getIfAvailable()
        );
    }

    @Override
    public PageResult<AdminUserListItemVo> page(AdminUserQuery query) {
        return storage.route(
                () -> storage.mysqlStore().pageAdminUsers(query),
                () -> storage.memoryStore().pageAdminUsers(query.getKeyword(), query.getRole(), query.getDisabled(), query.getPageNum(), query.getPageSize())
        );
    }

    @Override
    public AdminUserListItemVo updateStatus(Long currentAdminId, Long targetUserId, Boolean disabled) {
        return storage.route(
                () -> storage.mysqlStore().updateAdminUserStatus(currentAdminId, targetUserId, Boolean.TRUE.equals(disabled)),
                () -> storage.memoryStore().updateAdminUserStatus(currentAdminId, targetUserId, Boolean.TRUE.equals(disabled))
        );
    }
}
