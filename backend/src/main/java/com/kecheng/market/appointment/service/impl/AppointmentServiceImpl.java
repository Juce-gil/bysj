package com.kecheng.market.appointment.service.impl;

import com.kecheng.market.appointment.service.AppointmentService;
import com.kecheng.market.appointment.vo.AppointmentVo;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import com.kecheng.market.common.store.StorageAccessSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final StorageAccessSupport storage;

    public AppointmentServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storage = new StorageAccessSupport(
                storageMode,
                marketStoreProvider.getIfAvailable(),
                persistenceServiceProvider.getIfAvailable()
        );
    }

    @Override
    public AppointmentVo create(Long userId, Long goodsId, LocalDateTime intendedTime, String intendedLocation, String remark) {
        return storage.route(
                () -> storage.mysqlStore().createAppointment(userId, goodsId, intendedTime, intendedLocation, remark),
                () -> storage.memoryStore().createAppointment(userId, goodsId, intendedTime, intendedLocation, remark)
        );
    }

    @Override
    public List<AppointmentVo> myList(Long userId) {
        return storage.route(
                () -> storage.mysqlStore().listMyAppointments(userId),
                () -> storage.memoryStore().listMyAppointments(userId)
        );
    }

    @Override
    public AppointmentVo cancel(Long userId, Long appointmentId) {
        return storage.route(
                () -> storage.mysqlStore().cancelAppointment(userId, appointmentId),
                () -> storage.memoryStore().cancelAppointment(userId, appointmentId)
        );
    }
}
