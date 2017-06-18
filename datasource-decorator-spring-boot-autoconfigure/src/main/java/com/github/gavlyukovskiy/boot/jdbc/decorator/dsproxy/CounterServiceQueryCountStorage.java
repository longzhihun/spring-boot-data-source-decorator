package com.github.gavlyukovskiy.boot.jdbc.decorator.dsproxy;

import net.ttddyy.dsproxy.QueryCount;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class CounterServiceQueryCountStorage implements QueryCountStorage {

    private static final String DATASOURCE_SUFFIX = "dataSource";

    private final Map<String, QueryCount> queryCountMapHolder = new ConcurrentHashMap<>();
    private final CounterService counterService;

    @Autowired
    private ApplicationContext applicationContext;
    private String primaryDataSourceName;

    public CounterServiceQueryCountStorage(CounterService counterService) {
        this.counterService = counterService;
    }

    @PostConstruct
    public void initialize() {
        try {
            DataSource primaryDataSource = applicationContext.getBean(DataSource.class);
            Map<String, DataSource> allDataSources = applicationContext.getBeansOfType(DataSource.class);
            for (Entry<String, DataSource> entry : allDataSources.entrySet()) {
                if (entry.getValue() == primaryDataSource) {
                    primaryDataSourceName = entry.getKey();
                    break;
                }
            }
        }
        catch (NoSuchBeanDefinitionException ignored) {
        }
    }

    @Override
    public QueryCount getOrCreate(String dataSourceName) {
        return queryCountMapHolder.computeIfAbsent(dataSourceName, name -> new CounterServiceQueryCount(createPrefix(name), counterService));
    }

    private String createPrefix(String name) {
        if (name.equals(primaryDataSourceName)) {
            return "datasource.primary";
        }
        if (name.length() > DATASOURCE_SUFFIX.length()
                && name.toLowerCase().endsWith(DATASOURCE_SUFFIX.toLowerCase())) {
            name = name.substring(0, name.length() - DATASOURCE_SUFFIX.length());
        }
        return "datasource." + name;
    }

    protected static class CounterServiceQueryCount extends QueryCount {
        private final String prefix;
        private final CounterService counterService;

        CounterServiceQueryCount(String prefix, CounterService counterService) {
            this.prefix = prefix;
            this.counterService = counterService;
        }

        @Override
        public void incrementSelect() {
            super.incrementSelect();
            counterService.increment(prefix + ".select");
        }

        @Override
        public void incrementInsert() {
            super.incrementInsert();
            counterService.increment(prefix + ".insert");
        }

        @Override
        public void incrementUpdate() {
            super.incrementUpdate();
            counterService.increment(prefix + ".update");
        }

        @Override
        public void incrementDelete() {
            super.incrementDelete();
            counterService.increment(prefix + ".delete");
        }

        @Override
        public void incrementOther() {
            super.incrementOther();
            counterService.increment(prefix + ".other");
        }

        @Override
        public void incrementStatement() {
            super.incrementStatement();
            counterService.increment(prefix + ".statement");
        }

        @Override
        public void incrementPrepared() {
            super.incrementPrepared();
            counterService.increment(prefix + ".prepared");
        }

        @Override
        public void incrementCallable() {
            super.incrementCallable();
            counterService.increment(prefix + ".callable");
        }

        @Override
        public void incrementTotal() {
            super.incrementTotal();
            counterService.increment(prefix + ".total");
        }

        @Override
        public void incrementSuccess() {
            super.incrementSuccess();
            counterService.increment(prefix + ".success");
        }

        @Override
        public void incrementFailure() {
            super.incrementFailure();
            counterService.increment(prefix + ".failure");
        }

        @Override
        public void incrementTime(long delta) {
            super.incrementTime(delta);
            // CounterService does not have batch increment
            for (int i = 0; i < delta; i++) {
                counterService.increment(prefix + ".time");
            }
        }
    }
}
