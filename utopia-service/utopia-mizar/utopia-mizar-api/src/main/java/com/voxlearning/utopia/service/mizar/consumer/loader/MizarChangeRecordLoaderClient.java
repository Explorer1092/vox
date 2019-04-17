package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.api.loader.MizarChangeRecordLoader;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * Created by yuechen.wang on 2016/10/10.
 */
public class MizarChangeRecordLoaderClient {

    @Getter
    @ImportService(interfaceClass = MizarChangeRecordLoader.class)
    private MizarChangeRecordLoader remoteReference;

    public MizarEntityChangeRecord load(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return remoteReference.loadRecordById(id);
    }

    public List<MizarEntityChangeRecord> loadByApplicant(String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        return remoteReference.loadRecordsByApplicant(userId);
    }

    public List<MizarEntityChangeRecord> loadByStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return Collections.emptyList();
        }
        return remoteReference.loadRecordsByStatus(status);
    }

    public MizarEntityChangeRecord loadByTarget(String targetId, String entityType) {
        if (StringUtils.isBlank(targetId) || StringUtils.isBlank(entityType)) {
            return null;
        }
        List<MizarEntityChangeRecord> records = remoteReference.loadRecordByTarget(targetId, entityType);
        if (records == null) {
            return null;
        }
        return records.stream().findFirst().orElse(null);
    }
    public List<MizarEntityChangeRecord> loadByTargetAndType(String targetId, String entityType) {
        if (StringUtils.isBlank(targetId) || StringUtils.isBlank(entityType)) {
            return Collections.emptyList();
        }
        return remoteReference.loadRecordByTarget(targetId, entityType);
    }

    public List<MizarEntityChangeRecord> loadByEntityType(String entityType) {
        if (StringUtils.isBlank(entityType)) {
            return Collections.emptyList();
        }
        return remoteReference.loadRecordsByEntityType(entityType);
    }

}
