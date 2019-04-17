package com.voxlearning.utopia.mizar.audit;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.service.mizar.api.entity.change.MizarEntityChangeRecord;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarChangeRecordServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarNotifyServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarServiceClient;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;

/**
 * Created by Yuechen.Wang on 2016/10/12.
 */
public abstract class MizarAuditProcessor extends SpringContainerSupport {

    @Inject protected MizarLoaderClient mizarLoaderClient;
    @Inject protected MizarChangeRecordServiceClient mizarChangeRecordServiceClient;

    @Inject protected MizarServiceClient mizarServiceClient;
    @Inject protected MizarNotifyServiceClient mizarNotifyServiceClient;

    @Getter
    @Setter
    private MizarAuditContext context;

    abstract public MapMessage approve();

    abstract public MapMessage reject();


    boolean checkBeforeAudit() {
        return context != null
                && context.getRecord() != null
                && context.getCurrentUser() != null;
    }

    public MapMessage defaultReject() {
        if (!checkBeforeAudit()) {
            return MapMessage.errorMessage("参数异常");
        }
        MizarEntityChangeRecord record = getContext().getRecord();

        MizarAuthUser currentUser = getContext().getCurrentUser();
        record.setAuditorId(currentUser.getUserId());
        record.setAuditor(currentUser.getRealName());
        record.setAuditStatus(getContext().getProcessNotes());
        // 拒绝只需要修改记录状态就好了
        return mizarChangeRecordServiceClient.reject(record);
    }
}
