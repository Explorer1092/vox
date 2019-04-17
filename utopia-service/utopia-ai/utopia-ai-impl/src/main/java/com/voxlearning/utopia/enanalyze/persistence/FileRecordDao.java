package com.voxlearning.utopia.enanalyze.persistence;

import com.voxlearning.utopia.enanalyze.entity.FileRecordEntity;

/**
 * 文集记录持久层
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface FileRecordDao {

    /**
     * 新增
     *
     * @param record
     */
    void insert(FileRecordEntity record);

}
