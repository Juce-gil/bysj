package com.kecheng.market.category.service.impl;

import com.kecheng.market.category.service.CategoryService;
import com.kecheng.market.category.vo.CategoryVo;
import com.kecheng.market.common.store.MarketStore;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final MarketStore marketStore;

    public CategoryServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public List<CategoryVo> list() {
        return marketStore.listCategories();
    }
}
