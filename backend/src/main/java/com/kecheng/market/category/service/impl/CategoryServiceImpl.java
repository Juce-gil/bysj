package com.kecheng.market.category.service.impl;

import com.kecheng.market.category.service.CategoryService;
import com.kecheng.market.category.vo.CategoryVo;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final StorageAccessSupport storage;

    public CategoryServiceImpl(
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
    public List<CategoryVo> list() {
        return storage.route(
                () -> storage.mysqlStore().listCategories(),
                () -> storage.memoryStore().listCategories()
        );
    }
}
