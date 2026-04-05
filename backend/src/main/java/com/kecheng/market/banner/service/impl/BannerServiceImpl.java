package com.kecheng.market.banner.service.impl;

import com.kecheng.market.banner.service.BannerService;
import com.kecheng.market.banner.vo.BannerVo;
import com.kecheng.market.common.store.MarketStore;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BannerServiceImpl implements BannerService {

    private final MarketStore marketStore;

    public BannerServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public List<BannerVo> list() {
        return marketStore.listBanners();
    }
}
