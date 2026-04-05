package com.kecheng.market.admin.service;

import com.kecheng.market.admin.dto.AdminGoodsQuery;
import com.kecheng.market.admin.vo.AdminGoodsDetailVo;
import com.kecheng.market.admin.vo.AdminGoodsListItemVo;
import com.kecheng.market.common.PageResult;

public interface AdminGoodsService {
    PageResult<AdminGoodsListItemVo> page(AdminGoodsQuery query);

    AdminGoodsDetailVo detail(Long id);

    AdminGoodsDetailVo offShelf(Long id);

    AdminGoodsDetailVo relist(Long id);

    void delete(Long id);
}
