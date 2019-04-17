package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkAssignLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
public class HomeworkAssignInitProcessor implements HomeworkProcessor {
    @Inject
    private HomeworkUserPreferencesLoader homeworkUserPreferencesLoader;
    @Inject
    private HomeworkAssignLoader assignHomeworkService;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        Long studentId = param.getStudentId();
        // 学生基本信息
        StudentInfo studentInfo = assignHomeworkService.loadStudentInfo(studentId);
        if (studentInfo == null) {
            hc.setMapMessage(MapMessage.errorMessage("学生信息有误"));
            return;
        }
        hc.setStudentInfo(studentInfo);
        String subject = param.getSubject();
        // 教材兼容
        HomeworkUserPreferences userPreferences = homeworkUserPreferencesLoader.loadHomeworkUserPreference(studentId, subject);
        if (StringUtils.isBlank(param.getBookId())) {
            if (userPreferences == null) {
                hc.setMapMessage(MapMessage.errorMessage("学生未进行初始化"));
                return;
            }
            param.setBookId(userPreferences.getBookId());
        }
        List<String> boxIds = ObjectUtils.get(() -> (List<String>)(param.getData().get("boxIds")));
        if (CollectionUtils.isEmpty(boxIds)) {
            hc.setMapMessage(MapMessage.errorMessage("题包参数校验失败"));
            return;
        }
        String bizType = StringUtils.isBlank(param.getBizType()) ? "EXAM" : param.getBizType();
        param.setBizType(bizType);
        // 合并题包
        List<String> sections = new ArrayList<>();
        QuestionPackage mergePackage = null;
        for (String boxId : boxIds) {
            QuestionPackage questionPackage = HomeWorkCache.load(CacheKey.BOX, bizType , boxId);
            if (questionPackage == null) {
                hc.setMapMessage(MapMessage.errorMessage("题包异常, 请重新进入页面"));
                return;
            }
            sections.add(questionPackage.getSection());
            if (mergePackage == null) {
                mergePackage = questionPackage;
            } else {
                mergePackage.getDocIds().addAll(questionPackage.getDocIds());
            }
        }
        if (mergePackage == null) {
            hc.setMapMessage(MapMessage.errorMessage("题包异常, 请重新进入页面"));
            return;
        }
        param.setSectionIds(sections);
        String unitId = mergePackage.getUnitId();
        hc.setUnitId(unitId);
        hc.setQuestionPackages(Collections.singletonList(mergePackage));
    }
}
