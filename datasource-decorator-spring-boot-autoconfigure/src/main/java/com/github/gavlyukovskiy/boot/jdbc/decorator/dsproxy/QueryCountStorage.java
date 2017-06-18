package com.github.gavlyukovskiy.boot.jdbc.decorator.dsproxy;

import net.ttddyy.dsproxy.QueryCount;

public interface QueryCountStorage {

    QueryCount getOrCreate(String dataSourceName);
}
