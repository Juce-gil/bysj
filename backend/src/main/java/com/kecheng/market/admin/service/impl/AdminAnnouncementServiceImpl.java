package com.kecheng.market.admin.service.impl;

import com.kecheng.market.admin.dto.AdminAnnouncementQuery;
import com.kecheng.market.admin.dto.AdminAnnouncementSaveRequest;
import com.kecheng.market.admin.service.AdminAnnouncementService;
import com.kecheng.market.admin.vo.AdminAnnouncementDetailVo;
import com.kecheng.market.admin.vo.AdminAnnouncementListItemVo;
import com.kecheng.market.common.PageResult;
import com.kecheng.market.common.store.MarketStore;
import org.springframework.stereotype.Service;

@Service
public class AdminAnnouncementServiceImpl implements AdminAnnouncementService {

    private final MarketStore marketStore;

    public AdminAnnouncementServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public PageResult<AdminAnnouncementListItemVo> page(AdminAnnouncementQuery query) {
        return marketStore.pageAdminAnnouncements(query.getKeyword(), query.getPublished(), query.getPageNum(), query.getPageSize());
    }

    @Override
    public AdminAnnouncementDetailVo detail(Long id) {
        return marketStore.getAdminAnnouncementDetail(id);
    }

    @Override
    public AdminAnnouncementDetailVo create(AdminAnnouncementSaveRequest request) {
        return marketStore.createAdminAnnouncement(
                request.title(),
                request.summary(),
                request.content(),
                request.level(),
                request.top(),
                request.published());
    }

    @Override
    public AdminAnnouncementDetailVo update(Long id, AdminAnnouncementSaveRequest request) {
        return marketStore.updateAdminAnnouncement(
                id,
                request.title(),
                request.summary(),
                request.content(),
                request.level(),
                request.top(),
                request.published());
    }

    @Override
    public AdminAnnouncementDetailVo publish(Long id) {
        return marketStore.publishAdminAnnouncement(id);
    }

    @Override
    public AdminAnnouncementDetailVo offline(Long id) {
        return marketStore.offlineAdminAnnouncement(id);
    }

    @Override
    public void delete(Long id) {
        marketStore.deleteAdminAnnouncement(id);
    }
}
