package com.kecheng.market.admin.service;

import com.kecheng.market.admin.dto.AdminAnnouncementQuery;
import com.kecheng.market.admin.dto.AdminAnnouncementSaveRequest;
import com.kecheng.market.admin.vo.AdminAnnouncementDetailVo;
import com.kecheng.market.admin.vo.AdminAnnouncementListItemVo;
import com.kecheng.market.common.PageResult;

public interface AdminAnnouncementService {
    PageResult<AdminAnnouncementListItemVo> page(AdminAnnouncementQuery query);

    AdminAnnouncementDetailVo detail(Long id);

    AdminAnnouncementDetailVo create(AdminAnnouncementSaveRequest request);

    AdminAnnouncementDetailVo update(Long id, AdminAnnouncementSaveRequest request);

    AdminAnnouncementDetailVo publish(Long id);

    AdminAnnouncementDetailVo offline(Long id);

    void delete(Long id);
}
