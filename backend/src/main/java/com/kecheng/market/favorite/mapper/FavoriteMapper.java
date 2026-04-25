package com.kecheng.market.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kecheng.market.favorite.entity.FavoriteEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FavoriteMapper extends BaseMapper<FavoriteEntity> {

    @Select("SELECT id, user_id, goods_id, create_time, update_time, deleted FROM favorite WHERE user_id = #{userId} AND goods_id = #{goodsId} ORDER BY id DESC LIMIT 1")
    FavoriteEntity selectAnyByUserIdAndGoodsId(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    @Update("UPDATE favorite SET deleted = #{deleted}, update_time = NOW() WHERE id = #{id}")
    int updateDeletedFlag(@Param("id") Long id, @Param("deleted") Integer deleted);
}
