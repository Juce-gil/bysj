package com.kecheng.market.comment.service.impl;

import com.kecheng.market.comment.service.CommentService;
import com.kecheng.market.comment.vo.CommentVo;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

    private final StorageAccessSupport storage;

    public CommentServiceImpl(
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
    public List<CommentVo> list(Long goodsId) {
        return storage.route(
                () -> storage.mysqlStore().listComments(goodsId),
                () -> storage.memoryStore().listComments(goodsId)
        );
    }

    @Override
    public CommentVo add(Long userId, Long goodsId, String content) {
        return storage.route(
                () -> storage.mysqlStore().addComment(goodsId, userId, content),
                () -> storage.memoryStore().addComment(goodsId, userId, content)
        );
    }
}
