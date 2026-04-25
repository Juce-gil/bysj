package com.kecheng.market.category.service.impl;

import com.kecheng.market.category.service.CategoryService;
import com.kecheng.market.category.vo.CategoryVo;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public CategoryServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public List<CategoryVo> list() {
        return useMysql() ? requirePersistence().listCategories() : requireStore().listCategories();
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
