package com.kecheng.market.comment.service.impl;

import com.kecheng.market.comment.service.CommentService;
import com.kecheng.market.comment.vo.CommentVo;
import com.kecheng.market.common.store.MarketStore;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

    private final MarketStore marketStore;

    public CommentServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public List<CommentVo> list(Long goodsId) {
        return marketStore.listComments(goodsId);
    }

    @Override
    public CommentVo add(Long userId, Long goodsId, String content) {
        return marketStore.addComment(goodsId, userId, content);
    }
}
