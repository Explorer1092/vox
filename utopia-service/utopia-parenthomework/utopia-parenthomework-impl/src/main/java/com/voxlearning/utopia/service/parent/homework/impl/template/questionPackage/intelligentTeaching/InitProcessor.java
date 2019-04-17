package com.voxlearning.utopia.service.parent.homework.impl.template.questionPackage.intelligentTeaching;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressLoader;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.user.api.entities.Group;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 初始化
 *
 * @author Wenlong Meng
 * @since 20190111
 */
@Named("IntelligentTeaching.InitProcessor")
public class InitProcessor implements HomeworkProcessor {

    @Inject private RaikouSDK raikouSDK;

    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private HomeworkAssignLoader assignHomeworkService;
    @Inject private HomeworkUserProgressLoader homeworkUserProgressLoader;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        Long studentId = param.getStudentId();
        String subject = param.getSubject();
        // 学生基本信息
        StudentInfo studentInfo = assignHomeworkService.loadStudentInfo(studentId);
        if (studentInfo == null) {
            hc.setMapMessage(MapMessage.errorMessage("学生信息有误"));
            return;
        }
        hc.setStudentInfo(studentInfo);
        // 用户进度
        hc.setProgress(homeworkUserProgressLoader.loadUserProgress(param.getStudentId(), param.getBizType()));
        // 初始化groupId
        Group group = ObjectUtils.get(() ->
                groupLoaderClient.getGroupLoader().loadGroups(
                        raikouSDK.getClazzClient()
                                .getGroupStudentTupleServiceClient()
                                .findByStudentId(studentInfo.getStudentId())
                                .stream()
                                .map(GroupStudentTuple::getGroupId)
                                .collect(Collectors.toList())
                ).getUninterruptibly().values().stream().filter(g -> !g.isDisabledTrue() && g.getSubject() != null && Objects.equals(g.getSubject().name(), subject)).findFirst().orElse(null));
        if (group != null) {
            hc.setGroupId(group.getId());
        }
        hc.setData(new LinkedHashMap<>());
    }
}
