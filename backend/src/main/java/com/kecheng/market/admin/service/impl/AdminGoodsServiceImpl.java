package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminGoodsQuery;
import com.kecheng.market.admin.service.AdminGoodsService;
import com.kecheng.market.admin.vo.AdminGoodsDetailVo;
import com.kecheng.market.admin.vo.AdminGoodsListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketStore;
import org.springframework.stereotype.Service;

@Service
public class AdminGoodsServiceImpl implements AdminGoodsService {

    private final MarketStore marketStore;

    public AdminGoodsServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public PageResult<AdminGoodsListItemVo> page(AdminGoodsQuery query) {
        return marketStore.pageAdminGoods(query.getKeyword(), query.getStatus(), query.getCategory(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public AdminGoodsDetailVo detail(Long id) {
        return marketStore.getAdminGoodsDetail(id);
    }

    @Override
    public AdminGoodsDetailVo offShelf(Long id) {
        return marketStore.offShelfAdminGoods(id);
    }

    @Override
    public AdminGoodsDetailVo relist(Long id) {
        return marketStore.relistAdminGoods(id);
    }

    @Override
    public void delete(Long id) {
        marketStore.deleteAdminGoods(id);
    }
}
