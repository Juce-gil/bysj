package com.kecheng.market.home.vo;

import com.kecheng.market.announcement.vo.AnnouncementVo;
import com.kecheng.market.banner.vo.BannerVo;
import com.kecheng.market.goods.vo.GoodsVo;
import com.kecheng.market.wanted.vo.WantedVo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "首页聚合数据")
public record HomeDataVo(
        @Schema(description = "首页统计卡片") List<HomeStatVo> stats,
        @Schema(description = "精选商品") List<GoodsVo> featuredGoods,
        @Schema(description = "热门求购") List<WantedVo> hotWanted,
        @Schema(description = "最新公告") List<AnnouncementVo> latestAnnouncements,
        @Schema(description = "首页轮播图") List<BannerVo> banners
) {
}
