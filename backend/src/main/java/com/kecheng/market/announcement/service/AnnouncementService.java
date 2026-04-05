package com.kecheng.market.announcement.service;

import com.kecheng.market.announcement.vo.AnnouncementVo;
import java.util.List;

public interface AnnouncementService {
    List<AnnouncementVo> list();
    AnnouncementVo detail(Long id);
}
