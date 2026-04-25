package com.kecheng.market.banner.service.impl;

import com.kecheng.market.banner.service.BannerService;
import com.kecheng.market.banner.vo.BannerVo;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl implements BannerService {

    private final StorageAccessSupport storage;

    public BannerServiceImpl(
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
    public List<BannerVo> list() {
        return storage.route(
                () -> storage.mysqlStore().listBanners(),
                () -> storage.memoryStore().listBanners()
        );
    }
}
