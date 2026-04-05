package com.kecheng.market.appointment.service;

import com.kecheng.market.appointment.vo.AppointmentVo;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {
    AppointmentVo create(Long userId, Long goodsId, LocalDateTime intendedTime, String intendedLocation, String remark);
    List<AppointmentVo> myList(Long userId);
    AppointmentVo cancel(Long userId, Long appointmentId);
}
