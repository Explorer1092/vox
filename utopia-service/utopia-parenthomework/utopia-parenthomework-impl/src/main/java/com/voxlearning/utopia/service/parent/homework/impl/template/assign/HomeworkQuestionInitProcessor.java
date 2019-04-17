package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.user.api.entities.Group;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 初始化
 *
 * @author chongfeng.qi
 * @data 20190111
 */
@Named
public class HomeworkQuestionInitProcessor implements HomeworkProcessor {

    @Inject private RaikouSDK raikouSDK;

    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private HomeworkAssignLoader assignHomeworkService;
    @Inject private HomeworkUserPreferencesLoader homeworkUserPreferencesLoader;
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
        // 用户进度
        hc.setProgress(homeworkUserProgressLoader.loadUserProgress(param.getStudentId(), param.getBizType()));
        hc.setStudentInfo(studentInfo);
        if (StringUtils.isBlank(param.getBizType())) {
            param.setBizType("EXAM");
        }
        // 初始化groupId
        List<Long> groupIds = raikouSDK.getClazzClient()
                .getGroupStudentTupleServiceClient()
                .findByStudentId(studentInfo.getStudentId())
                .stream()
                .map(GroupStudentTuple::getGroupId)
                .distinct()
                .collect(Collectors.toList());
        Group group = ObjectUtils.get(() ->
                groupLoaderClient.getGroupLoader()
                        .loadGroups(groupIds)
                        .getUninterruptibly()
                        .values()
                        .stream()
                        .filter(e -> !e.isDisabledTrue())
                        .filter(e -> e.getSubject() != null)
                        .filter(g -> Objects.equals(g.getSubject().name(), subject))
                        .findFirst()
                        .orElse(null));
        if (group != null) {
            hc.setGroupId(group.getId());
        }
        // 教材兼容
        HomeworkUserPreferences userPreferences = homeworkUserPreferencesLoader.loadHomeworkUserPreference(studentId, subject);
        hc.setUserPreferences(userPreferences);
        if (StringUtils.isBlank(param.getBookId())) {
            if (userPreferences == null) {
                hc.setMapMessage(MapMessage.errorMessage("学生未进行初始化"));
                return;
            }
            param.setBookId(userPreferences.getBookId());
        }
        hc.setData(new LinkedHashMap<>());
    }
}
