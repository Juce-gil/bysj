package com.kecheng.market.goods.service.impl;

import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import com.kecheng.market.goods.service.GoodsService;
import com.kecheng.market.goods.vo.GoodsVo;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl implements GoodsService {

    private final StorageAccessSupport storage;

    public GoodsServiceImpl(
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
    public List<GoodsVo> list(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().listGoods(userId),
                () -> storage.memoryStore().listGoods(userId)
        );
    }

    @Override
    public List<GoodsVo> myGoods(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().listMyGoods(userId),
                () -> storage.memoryStore().listMyGoods(userId)
        );
    }

    @Override
    public GoodsVo detail(Long id, Long userId) {
        return storage.route(
                () -> storage.mysqlStore().getGoodsDetail(id, userId),
                () -> storage.memoryStore().getGoodsDetail(id, userId)
        );
    }

    @Override
    public boolean toggleFavorite(Long userId, Long goodsId) {
        return storage.route(
                () -> storage.mysqlStore().toggleFavorite(userId, goodsId),
                () -> storage.memoryStore().toggleFavorite(userId, goodsId)
        );
    }

    @Override
    public List<GoodsVo> myFavorites(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().listMyFavorites(userId),
                () -> storage.memoryStore().listMyFavorites(userId)
        );
    }

    @Override
    public GoodsVo create(Long userId, String title, BigDecimal price, BigDecimal originalPrice, String category, String campus,
                          String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        return storage.route(
                () -> storage.mysqlStore().createGoods(userId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls),
                () -> storage.memoryStore().createGoods(userId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls)
        );
    }

    @Override
    public GoodsVo update(Long userId, Long goodsId, String title, BigDecimal price, BigDecimal originalPrice, String category,
                          String campus, String condition, String intro, String description, List<String> tags, List<String> imageUrls) {
        return storage.route(
                () -> storage.mysqlStore().updateGoods(userId, goodsId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls),
                () -> storage.memoryStore().updateGoods(userId, goodsId, title, price, originalPrice, category, campus, condition, intro, description, tags, imageUrls)
        );
    }

    @Override
    public GoodsVo offShelf(Long userId, Long goodsId) {
        return storage.route(
                () -> storage.mysqlStore().offShelfGoods(userId, goodsId),
                () -> storage.memoryStore().offShelfGoods(userId, goodsId)
        );
    }

    @Override
    public GoodsVo relist(Long userId, Long goodsId) {
        return storage.route(
                () -> storage.mysqlStore().relistGoods(userId, goodsId),
                () -> storage.memoryStore().relistGoods(userId, goodsId)
        );
    }

    @Override
    public void delete(Long userId, Long goodsId) {
        storage.route(
                () -> storage.mysqlStore().deleteGoods(userId, goodsId),
                () -> storage.memoryStore().deleteGoods(userId, goodsId)
        );
    }
}
