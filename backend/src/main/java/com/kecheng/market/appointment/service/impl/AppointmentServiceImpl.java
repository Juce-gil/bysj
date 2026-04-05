package com.kecheng.market.appointment.service.impl;

import com.kecheng.market.appointment.service.AppointmentService;
import com.kecheng.market.appointment.vo.AppointmentVo;
import com.kecheng.market.common.store.MarketStore;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final MarketStore marketStore;

    public AppointmentServiceImpl(MarketStore marketStore) {
        this.marketStore = marketStore;
    }

    @Override
    public AppointmentVo create(Long userId, Long goodsId, LocalDateTime intendedTime, String intendedLocation, String remark) {
        return marketStore.createAppointment(userId, goodsId, intendedTime, intendedLocation, remark);
    }

    @Override
    public List<AppointmentVo> myList(Long userId) {
        return marketStore.listMyAppointments(userId);
    }

    @Override
    public AppointmentVo cancel(Long userId, Long appointmentId) {
        return marketStore.cancelAppointment(userId, appointmentId);
    }
}
