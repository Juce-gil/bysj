package com.kecheng.market.announcement.service.impl;

import com.kecheng.market.announcement.service.AnnouncementService;
import com.kecheng.market.announcement.vo.AnnouncementVo;
import com.kecheng.market.common.store.MarketStore;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    private final MarketStore marketStore;

    public AnnouncementServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public List<AnnouncementVo> list() {
        return marketStore.listAnnouncements();
    }

    @Override
    public AnnouncementVo detail(Long id) {
        return marketStore.getAnnouncementDetail(id);
    }
}
