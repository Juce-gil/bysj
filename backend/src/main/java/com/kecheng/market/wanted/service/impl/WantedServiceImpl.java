package com.kecheng.market.wanted.service.impl;

import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.wanted.service.WantedService;
import com.kecheng.market.wanted.vo.WantedVo;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WantedServiceImpl implements WantedService {

    private final MarketStore marketStore;

    public WantedServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public List<WantedVo> list(Long userId) {
        return marketStore.listWanted(userId);
    }

    @Override
    public List<WantedVo> myWanted(Long userId) {
        return marketStore.listMyWanted(userId);
    }

    @Override
    public WantedVo detail(Long id, boolean loggedIn) {
        return marketStore.getWantedDetail(id, loggedIn);
    }

    @Override
    public WantedVo create(Long userId, String title, String budget, String category, String campus, String deadline,
                           String intro, String description, List<String> tags, List<String> imageUrls) {
        return marketStore.createWanted(userId, title, budget, category, campus, deadline, intro, description, tags, imageUrls);
    }

    @Override
    public WantedVo update(Long userId, Long wantedId, String title, String budget, String category, String campus, String deadline,
                           String intro, String description, List<String> tags, List<String> imageUrls) {
        return marketStore.updateWanted(userId, wantedId, title, budget, category, campus, deadline, intro, description, tags, imageUrls);
    }

    @Override
    public WantedVo close(Long userId, Long wantedId) {
        return marketStore.closeWanted(userId, wantedId);
    }

    @Override
    public WantedVo reopen(Long userId, Long wantedId) {
        return marketStore.reopenWanted(userId, wantedId);
    }

    @Override
    public void delete(Long userId, Long wantedId) {
        marketStore.deleteWanted(userId, wantedId);
    }
}