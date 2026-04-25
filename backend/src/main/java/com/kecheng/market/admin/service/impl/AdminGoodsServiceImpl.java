package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminGoodsQuery;
import com.kecheng.market.admin.service.AdminGoodsService;
import com.kecheng.market.admin.vo.AdminGoodsDetailVo;
import com.kecheng.market.admin.vo.AdminGoodsListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminGoodsServiceImpl implements AdminGoodsService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public AdminGoodsServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public PageResult<AdminGoodsListItemVo> page(AdminGoodsQuery query) {
        return useMysql()
                ? requirePersistence().pageAdminGoods(query)
                : requireStore().pageAdminGoods(query.getKeyword(), query.getStatus(), query.getCategory(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public AdminGoodsDetailVo detail(Long id) {
        return useMysql() ? requirePersistence().getAdminGoodsDetail(id) : requireStore().getAdminGoodsDetail(id);
    }

    @Override
    public AdminGoodsDetailVo offShelf(Long id) {
        return useMysql() ? requirePersistence().offShelfAdminGoods(id) : requireStore().offShelfAdminGoods(id);
    }

    @Override
    public AdminGoodsDetailVo relist(Long id) {
        return useMysql() ? requirePersistence().relistAdminGoods(id) : requireStore().relistAdminGoods(id);
    }

    @Override
    public void delete(Long id) {
        if (useMysql()) {
            requirePersistence().deleteAdminGoods(id);
            return;
        }
        requireStore().deleteAdminGoods(id);
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
