package com.kecheng.market.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateAppointmentRequest(
        @NotNull(message = "商品ID不能为空") Long goodsId,
        @NotNull(message = "意向时间不能为空") LocalDateTime intendedTime,
        @NotBlank(message = "意向地点不能为空") String intendedLocation,
        String remark
) {
}
