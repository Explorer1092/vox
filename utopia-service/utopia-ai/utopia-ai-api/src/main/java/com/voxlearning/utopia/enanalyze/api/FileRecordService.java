package com.voxlearning.utopia.enanalyze.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.utopia.enanalyze.model.FileRecord;

import java.util.concurrent.TimeUnit;

/**
 * 文件记录服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@ServiceTimeout(timeout = 1000, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "20180701")
public interface FileRecordService {

    /**
     * 存储文件记录
     *
     * @param record
     */
    void insert(FileRecord record);
}
