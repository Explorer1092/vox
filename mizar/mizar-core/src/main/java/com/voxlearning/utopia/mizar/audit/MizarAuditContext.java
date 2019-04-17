package com.voxlearning.utopia.mizar.audit;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.service.mizar.api.constants.MizarEntityType;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Yuechen.Wang on 2016/10/12.
 */
public class MizarAuditContext {

    @Getter
    private MizarAuthUser currentUser;

    @Getter
    private MizarEntityChangeRecord record;

    @Getter
    @Setter
    private String processNotes;

    public MizarAuditContext(MizarAuthUser currentUser, MizarEntityChangeRecord record) {
        this.currentUser = currentUser;
        this.record = record;
    }

    MizarEntityType fetchEntityType() {
        if (record == null || StringUtils.isBlank(record.getEntityType())) {
            return null;
        }
        return MizarEntityType.of(record.getEntityType());
    }
}
