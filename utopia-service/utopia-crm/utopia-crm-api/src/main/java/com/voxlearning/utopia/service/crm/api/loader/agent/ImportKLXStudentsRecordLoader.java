package com.voxlearning.utopia.service.crm.api.loader.agent;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.crm.api.entities.agent.ImportKLXStudentsRecord;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by tao.zang
 * on 2017/4/11.
 */
@ServiceVersion(version = "2017.04.11")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
public interface ImportKLXStudentsRecordLoader extends IPingable {
    //此处暂不做扩展
    List<ImportKLXStudentsRecord> findByOperatorIdAndSourceType(Long OperatorId,String SourceType);

    /**
     * 保存上传日志
     * @param importKLXStudentsRecords
     */
    void saveImportKLXStudentsRecords(Collection<ImportKLXStudentsRecord> importKLXStudentsRecords);

    /**
     * 根据id标识获取 上传记录
     * @param recordId
     * @return
     */
     ImportKLXStudentsRecord findByRecordId(String recordId);

}
