package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.zone.api.ClassCricleParentService;
import com.voxlearning.utopia.service.zone.api.entity.ClazzParentOrderRecord;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzParentOrderPersistence;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author chensn
 * @date 2018-12-24 11:16
 */
@Named("com.voxlearning.utopia.service.zone.impl.service.ClassCricleParentServiceImpl")
@ExposeService(interfaceClass = ClassCricleParentService.class, version = @ServiceVersion(version = "20181023"))
@Slf4j
public class ClassCricleParentServiceImpl implements ClassCricleParentService {
    @Inject
    private ClazzParentOrderPersistence clazzParentOrderPersistence;

    @Override
    public MapMessage orderCommit(Long userId, Integer type, String subject) {
        ClazzParentOrderRecord clazzParentOrderRecord = new ClazzParentOrderRecord();
        clazzParentOrderRecord.setId(userId + "_" + type);
        clazzParentOrderRecord.setSubject(subject);
        clazzParentOrderRecord.setType(type);
        if (clazzParentOrderPersistence.load(clazzParentOrderRecord.getId()) == null) {
            clazzParentOrderPersistence.upsert(clazzParentOrderRecord);
        }
        return MapMessage.successMessage("提交成功");
    }

    @Override
    public MapMessage loadOrderRecord(Long userId, Integer type) {
        ClazzParentOrderRecord clazzParentOrderRecord = clazzParentOrderPersistence.load(userId + "_" + type);
        return MapMessage.successMessage().add("isPay", clazzParentOrderRecord != null).add("subject", clazzParentOrderRecord != null ? clazzParentOrderRecord.getSubject() : "");
    }
}
