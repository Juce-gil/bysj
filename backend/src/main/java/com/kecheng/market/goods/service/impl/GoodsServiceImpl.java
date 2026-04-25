package com.kecheng.market.goods.service.impl;

import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.goods.service.GoodsService;
import com.kecheng.market.goods.vo.GoodsVo;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl implements GoodsService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public GoodsServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public List<GoodsVo> list(Long userId) {
        return useMysql() ? requirePersistence().listGoods(userId) : requireStore().listGoods(userId);
    }

    @Override
    public List<GoodsVo> myGoods(Long userId) {
        return useMysql() ? requirePersistence().listMyGoods(userId) : requireStore().listMyGoods(userId);
    }

    @Override
    public GoodsVo detail(Long id, Long userId) {
        return useMysql() ? requirePersistence().getGoodsDetail(id, userId) : requireStore().getGoodsDetail(id, userId);
    }

    @Override
    public boolean toggleFavorite(Long userId, Long goodsId) {
        return useMysql() ? requirePersistence().toggleFavorite(userId, goodsId) : requireStore().toggleFavorite(userId, goodsId);
    }

    @Override
    public List<GoodsVo> myFavorites(Long userId) {
        return useMysql() ? requirePersistence().listMyFavorites(userId) : requireStore().listMyFavorites(userId);
    }

    @Override
    public GoodsVo create(Long userId, String title, BigDecimal price, BigDecimal originalPrice, String category, String campus,
                          String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        return useMysql()
                ? requirePersistence().createGoods(userId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls)
                : requireStore().createGoods(userId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls);
    }

    @Override
    public GoodsVo update(Long userId, Long goodsId, String title, BigDecimal price, BigDecimal originalPrice, String category,
                          String campus, String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        return useMysql()
                ? requirePersistence().updateGoods(userId, goodsId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls)
                : requireStore().updateGoods(userId, goodsId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls);
    }

    @Override
    public GoodsVo offShelf(Long userId, Long goodsId) {
        return useMysql() ? requirePersistence().offShelfGoods(userId, goodsId) : requireStore().offShelfGoods(userId, goodsId);
    }

    @Override
    public GoodsVo relist(Long userId, Long goodsId) {
        return useMysql() ? requirePersistence().relistGoods(userId, goodsId) : requireStore().relistGoods(userId, goodsId);
    }

    @Override
    public void delete(Long userId, Long goodsId) {
        if (useMysql()) {
            requirePersistence().deleteGoods(userId, goodsId);
            return;
        }
        requireStore().deleteGoods(userId, goodsId);
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
