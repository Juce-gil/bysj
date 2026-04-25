package com.kecheng.market.appointment.service.impl;

import com.kecheng.market.appointment.service.AppointmentService;
import com.kecheng.market.appointment.vo.AppointmentVo;
import com.kecheng.market.common.store.MarketPersistenceService;
import com.kecheng.market.common.store.MarketStore;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final String storageMode;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public AppointmentServiceImpl(
            @Value("${market.storage-mode:mysql}") String storageMode,
            ObjectProvider<MarketStore> marketStoreProvider,
            ObjectProvider<MarketPersistenceService> persistenceServiceProvider) {
        this.storageMode = storageMode;
        this.marketStore = marketStoreProvider.getIfAvailable();
        this.persistenceService = persistenceServiceProvider.getIfAvailable();
    }

    @Override
    public AppointmentVo create(Long userId, Long goodsId, LocalDateTime intendedTime, String intendedLocation, String remark) {
        return useMysql()
                ? requirePersistence().createAppointment(userId, goodsId, intendedTime, intendedLocation, remark)
                : requireStore().createAppointment(userId, goodsId, intendedTime, intendedLocation, remark);
    }

    @Override
    public List<AppointmentVo> myList(Long userId) {
        return useMysql() ? requirePersistence().listMyAppointments(userId) : requireStore().listMyAppointments(userId);
    }

    @Override
    public AppointmentVo cancel(Long userId, Long appointmentId) {
        return useMysql() ? requirePersistence().cancelAppointment(userId, appointmentId) : requireStore().cancelAppointment(userId, appointmentId);
    }

    private boolean useMysql() {
        return "mysql".equalsIgnoreCase(storageMode);
    }

    private MarketPersistenceService requirePersistence() {
        if (persistenceService == null) {
            throw new IllegalStateException("MySQL persistence service is not available");
        }
        return persistenceService;
    }

    private MarketStore requireStore() {
        if (marketStore == null) {
            throw new IllegalStateException("Memory store is not available");
        }
        return marketStore;
    }
}
