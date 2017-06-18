package com.github.gavlyukovskiy.boot.jdbc.decorator.dsproxy;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.listener.QueryUtils;

import java.util.List;

public class DataSourceQueryTotalCountListener implements QueryExecutionListener {

    private final QueryCountStorage queryCountStorage;

    public DataSourceQueryTotalCountListener(QueryCountStorage queryCountStorage) {
        this.queryCountStorage = queryCountStorage;
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        final String dataSourceName = execInfo.getDataSourceName();

        QueryCount count = queryCountStorage.getOrCreate(dataSourceName);

        // increment db call
        count.incrementTotal();
        if (execInfo.isSuccess()) {
            count.incrementSuccess();
        } else {
            count.incrementFailure();
        }

        // increment elapsed time
        final long elapsedTime = execInfo.getElapsedTime();
        count.incrementTime(elapsedTime);

        // increment statement type
        count.increment(execInfo.getStatementType());

        // increment query count
        for (QueryInfo queryInfo : queryInfoList) {
            final String query = queryInfo.getQuery();
            final QueryType type = QueryUtils.getQueryType(query);
            count.increment(type);
        }

    }
}
