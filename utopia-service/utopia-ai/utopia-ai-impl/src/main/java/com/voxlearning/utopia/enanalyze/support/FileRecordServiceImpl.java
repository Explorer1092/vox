package com.voxlearning.utopia.enanalyze.support;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.enanalyze.api.FileRecordService;
import com.voxlearning.utopia.enanalyze.entity.FileRecordEntity;
import com.voxlearning.utopia.enanalyze.model.FileRecord;
import com.voxlearning.utopia.enanalyze.persistence.FileRecordDao;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.Date;

/**
 * 文件服务实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */

@Named
@Slf4j
@ExposeService(interfaceClass = FileRecordService.class)
public class FileRecordServiceImpl implements FileRecordService {

    @Resource
    private FileRecordDao fileRecordDao;

    @Override
    public void insert(FileRecord record) {
        FileRecordEntity entity = new FileRecordEntity();
        entity.setFileId(record.getFileId());
        entity.setOpenId(record.getOpenId());
        entity.setUrl(record.getUrl());
        Date now = new Date();
        if (null == record.getCreateDate())
            entity.setCreateDate(now);
        if (null == record.getUpdateDate())
            entity.setUpdateDate(now);
        fileRecordDao.insert(entity);
    }
}
