package com.kecheng.market.appointment.vo;

public record AppointmentVo(Long id, Long goodsId, String goodsTitle, Long buyerId, String buyerName, Long sellerId,
                            String sellerName, String intendedTime, String intendedLocation, String remark, String status) {
}
