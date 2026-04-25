package com.kecheng.market.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kecheng.market.common.entity.BaseEntity;
import java.time.LocalDateTime;

@TableName("appointment")
public class AppointmentEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("goods_id")
    private Long goodsId;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("intended_time")
    private LocalDateTime intendedTime;

    @TableField("intended_location")
    private String intendedLocation;

    private String remark;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("handle_time")
    private LocalDateTime handleTime;

    private String status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public LocalDateTime getIntendedTime() {
        return intendedTime;
    }

    public void setIntendedTime(LocalDateTime intendedTime) {
        this.intendedTime = intendedTime;
    }

    public String getIntendedLocation() {
        return intendedLocation;
    }

    public void setIntendedLocation(String intendedLocation) {
        this.intendedLocation = intendedLocation;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
