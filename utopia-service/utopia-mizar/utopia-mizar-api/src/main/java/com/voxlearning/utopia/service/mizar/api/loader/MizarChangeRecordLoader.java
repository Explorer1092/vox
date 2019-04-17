package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 信息变更申请相关
 *
 * @author yuechen.wang
 * @date 2016/10/09
 */
@ServiceVersion(version = "20161009")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarChangeRecordLoader extends IPingable {

    /**
     * 根据用户ID取出其相关的变更记录
     */
    MizarEntityChangeRecord loadRecordById(String id);

    /**
     * 根据用户ID取出其相关的变更记录
     */
    List<MizarEntityChangeRecord> loadRecordsByApplicant(String userId);

    /**
     * 根据状态取出其相关的变更记录
     */
    List<MizarEntityChangeRecord> loadRecordsByStatus(String status);

    /**
     * 根据类型取出其相关的变更记录
     */
    List<MizarEntityChangeRecord> loadRecordsByEntityType(String entityType);

    /**
     * 根据指定的变更记录
     */
    List<MizarEntityChangeRecord> loadRecordByTarget(String targetId, String entityType);

}
