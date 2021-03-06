package com.yongle.dshelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author weinh
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 写数据源
     */
    private DataSource writeDataSource;
    /**
     * 多个读数据源
     */
    private List<DataSource> readDataSources;
    /**
     * 读数据源个数
     */
    private int readDataSourceSize;
    /**
     * 获取读数据源方式，0：随机，1：轮询
     */
    private int readDataSourcePollPattern;
    private AtomicInteger counter = new AtomicInteger(0);
    private final Lock lock = new ReentrantLock();

    @Override
    public void afterPropertiesSet() {
        if (this.writeDataSource == null) {
            throw new IllegalArgumentException("Property 'writeDataSource' is required");
        }
        setDefaultTargetDataSource(writeDataSource);
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DynamicDataSourceGlobal.WRITE.name(), writeDataSource);
        if (CollectionUtils.isEmpty(this.readDataSources)) {
            readDataSourceSize = 0;
        } else {
            for (int i = 0; i < readDataSources.size(); i++) {
                targetDataSources.put(DynamicDataSourceGlobal.READ.name() + i, readDataSources.get(i));
            }
            readDataSourceSize = readDataSources.size();
        }
        setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        DynamicDataSourceGlobal dynamicDataSourceGlobal = DynamicDataSourceHolder.getDataSource();
        if (dynamicDataSourceGlobal == null
                || dynamicDataSourceGlobal == DynamicDataSourceGlobal.WRITE
                || readDataSourceSize <= 0) {
            return DynamicDataSourceGlobal.WRITE.name();
        }
        int index = 0;
        if (readDataSourceSize != 1) {
            if (readDataSourcePollPattern == 1) {
                index = counter.get() % readDataSourceSize;
                //轮询方式
                long currValue = counter.incrementAndGet();
                if (currValue >= readDataSourceSize) {
                    try {
                        lock.lock();
                        if (currValue >= readDataSourceSize) {
                            counter.set(0);
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            } else {
                //随机方式
                index = ThreadLocalRandom.current().nextInt(0, readDataSourceSize);
            }
        }
        logger.debug("读数据源为下标：{}", index);
        return dynamicDataSourceGlobal.name() + index;
    }

    public DataSource getWriteDataSource() {
        return writeDataSource;
    }

    public void setWriteDataSource(DataSource writeDataSource) {
        this.writeDataSource = writeDataSource;
    }

    public List<DataSource> getReadDataSources() {
        return readDataSources;
    }

    public void setReadDataSources(List<DataSource> readDataSources) {
        this.readDataSources = readDataSources;
    }

    public int getReadDataSourcePollPattern() {
        return readDataSourcePollPattern;
    }

    public void setReadDataSourcePollPattern(int readDataSourcePollPattern) {
        this.readDataSourcePollPattern = readDataSourcePollPattern;
    }
}