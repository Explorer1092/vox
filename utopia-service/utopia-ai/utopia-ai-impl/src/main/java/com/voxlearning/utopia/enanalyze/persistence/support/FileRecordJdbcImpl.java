package com.voxlearning.utopia.enanalyze.persistence.support;

import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.enanalyze.entity.FileRecordEntity;
import com.voxlearning.utopia.enanalyze.persistence.FileRecordDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * 文件记录muysql实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Slf4j
@Repository
public class FileRecordJdbcImpl extends AlpsStaticJdbcDao<FileRecordEntity, Long> implements FileRecordDao {

    @Override
    public void insert(FileRecordEntity record) {
        super.insert(record);
    }


    @Override
    protected void calculateCacheDimensions(FileRecordEntity document, Collection<String> dimensions) {

    }
}
