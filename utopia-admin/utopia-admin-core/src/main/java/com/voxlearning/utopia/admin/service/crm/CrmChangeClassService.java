package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.user.api.entities.GroupKlxStudentRef;
import com.voxlearning.utopia.service.user.consumer.client.kuailexue.NewKuailexueServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @Author:XiaochaoWei
 * @Description:
 * @CreateTime: 2017/5/10
 */
@Named
public class CrmChangeClassService extends AbstractAdminService {

    @Inject private RaikouSDK raikouSDK;

    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject private NewKuailexueServiceClient newKuailexueServiceClient;

    public MapMessage removeStudentGroupRef(Long studentId, List<Long> groupIds) {
        raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .getGroupStudentTupleService()
                .dbFindByStudentIdIncludeDisabled(studentId)
                .getUninterruptibly()
                .stream()
                .filter(e -> !e.isDisabledTrue())
                .filter(e -> groupIds.contains(e.getGroupId()))
                .map(GroupStudentTuple::getId)
                .forEach(e -> raikouSDK.getClazzClient()
                        .getGroupStudentTupleServiceClient()
                        .getGroupStudentTupleService()
                        .disable(e)
                        .awaitUninterruptibly());
        return MapMessage.successMessage();
    }

    public MapMessage importStudent(Long teacherId, Long studentId, Long classId) {
        MapMessage flag = groupServiceClient.linkStudentTeacher(studentId, teacherId, classId, false, 100, OperationSourceType.crm);
        if (!flag.isSuccess()) {
            return flag;
        }
        return MapMessage.successMessage();
    }

    // FIXME ALex20171113， 需要重构
    public MapMessage linkTeacher(Long teacherId, Long studentId, Long classId) {
        MapMessage flag = groupServiceClient.linkStudentTeacher(studentId, teacherId, classId, false, 100, OperationSourceType.crm);
        if (!flag.isSuccess()) {
            return flag;
        }
        return MapMessage.successMessage();
    }

    public MapMessage removeKlxVirtualStudentGroupRef(String studentUserName) {
        try {
            List<GroupKlxStudentRef> groupKlxStudentRefs = asyncGroupServiceClient.getAsyncGroupService()
                    .findGroupKlxStudentRefsByStudent(studentUserName).take();
            newKuailexueServiceClient.disableGroupKlxStudentRefs(groupKlxStudentRefs);
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }

    public MapMessage addKlxVirtualStudentGroupRef(Long targetGroupId, String studentUserName, Long a17id) {
        try {
            newKuailexueServiceClient.persistGroupKlxStudentRef(GroupKlxStudentRef.newInstance(targetGroupId, studentUserName, a17id));
        } catch (Exception e) {
            return MapMessage.errorMessage(e.getMessage());
        }
        return MapMessage.successMessage();
    }
}
