package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminUserQuery;
import com.kecheng.market.admin.service.AdminUserService;
import com.kecheng.market.admin.vo.AdminUserListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public AdminUserServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public PageResult<AdminUserListItemVo> page(AdminUserQuery query) {
        return useMysql()
                ? requirePersistence().pageAdminUsers(query)
                : requireStore().pageAdminUsers(query.getKeyword(), query.getRole(), query.getDisabled(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public AdminUserListItemVo updateStatus(Long currentAdminId, Long targetUserId, Boolean disabled) {
        return useMysql()
                ? requirePersistence().updateAdminUserStatus(currentAdminId, targetUserId, Boolean.TRUE.equals(disabled))
                : requireStore().updateAdminUserStatus(currentAdminId, targetUserId, Boolean.TRUE.equals(disabled));
    }

    private boolean useMysql() {
        return "mysql".equalsIgnoreCase(storageMode);
    }

    private MarketPersistenceService requirePersistence() {
        if (persistenceService == null) {
            throw new IllegalStateException("MySQL persistence service is not available");
        }
        return persistenceService;
    }

    private MarketStore requireStore() {
        if (marketStore == null) {
            throw new IllegalStateException("Memory store is not available");
        }
        return marketStore;
    }
}
