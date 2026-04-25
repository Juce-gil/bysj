package com.kecheng.market.common.store;

import java.util.function.Supplier;

public final class StorageAccessSupport {

    private final boolean mysql;
    private final MarketStore marketStore;
    private final MarketPersistenceService persistenceService;

    public StorageAccessSupport(String storageMode,
                                MarketStore marketStore,
                                MarketPersistenceService persistenceService) {
        this.mysql = "mysql".equalsIgnoreCase(storageMode);
        this.marketStore = marketStore;
        this.persistenceService = persistenceService;
    }

    public boolean useMysql() {
        return mysql;
    }

    public MarketStore memoryStore() {
        if (marketStore == null) {
            throw new IllegalStateException("Memory store is not available for current storage mode");
        }
        return marketStore;
    }

    public MarketPersistenceService mysqlStore() {
        if (persistenceService == null) {
            throw new IllegalStateException("MySQL persistence service is not available for current storage mode");
        }
        return persistenceService;
    }

    public <T> T route(Supplier<T> mysqlAction, Supplier<T> memoryAction) {
        return mysql ? mysqlAction.get() : memoryAction.get();
    }

    public void route(Runnable mysqlAction, Runnable memoryAction) {
        if (mysql) {
            mysqlAction.run();
            return;
        }
        memoryAction.run();
    }
}
