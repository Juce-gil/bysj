package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminGoodsQuery;
import com.kecheng.market.admin.service.AdminGoodsService;
import com.kecheng.market.admin.vo.AdminGoodsDetailVo;
import com.kecheng.market.admin.vo.AdminGoodsListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminGoodsServiceImpl implements AdminGoodsService {

    private final StorageAccessSupport storage;

    public AdminGoodsServiceImpl(
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
    public PageResult<AdminGoodsListItemVo> page(AdminGoodsQuery query) {
        return storage.route(
                () -> storage.mysqlStore().pageAdminGoods(query),
                () -> storage.memoryStore().pageAdminGoods(query.getKeyword(), query.getStatus(), query.getCategory(), query.getPageNum(), query.getPageSize())
        );
    }

    @Override
    public AdminGoodsDetailVo detail(Long id) {
        return storage.route(
                () -> storage.mysqlStore().getAdminGoodsDetail(id),
                () -> storage.memoryStore().getAdminGoodsDetail(id)
        );
    }

    @Override
    public AdminGoodsDetailVo offShelf(Long id) {
        return storage.route(
                () -> storage.mysqlStore().offShelfAdminGoods(id),
                () -> storage.memoryStore().offShelfAdminGoods(id)
        );
    }

    @Override
    public AdminGoodsDetailVo relist(Long id) {
        return storage.route(
                () -> storage.mysqlStore().relistAdminGoods(id),
                () -> storage.memoryStore().relistAdminGoods(id)
        );
    }

    @Override
    public void delete(Long id) {
        storage.route(
                () -> storage.mysqlStore().deleteAdminGoods(id),
                () -> storage.memoryStore().deleteAdminGoods(id)
        );
    }
}
