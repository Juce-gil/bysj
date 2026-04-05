package com.kecheng.market.home.service.impl;

import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.home.service.HomeService;
import com.kecheng.market.home.vo.HomeDataVo;
import org.springframework.stereotype.Service;

@Service
public class HomeServiceImpl implements HomeService {

    private final MarketStore marketStore;

    public HomeServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public HomeDataVo homeData(Long userId) {
        return marketStore.getHomeData(userId);
    }
}
