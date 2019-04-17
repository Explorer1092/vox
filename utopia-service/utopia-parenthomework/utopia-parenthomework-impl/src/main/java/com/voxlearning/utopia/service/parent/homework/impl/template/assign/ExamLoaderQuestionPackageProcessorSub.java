package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.athena.api.recom.entity.EliteSchoolPackage;
import com.voxlearning.athena.api.recom.loader.ParentRecommendLoader;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.api.mapper.QuestionPackage;
import com.voxlearning.utopia.service.parent.homework.api.mapper.StudentInfo;
import com.voxlearning.utopia.service.parent.homework.util.SubjectUtils;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 同步习题获取题包
 * @author chongfeng.qi
 * @data 20190115
 */
@Named
@SubType({
        ObjectiveConfigType.EXAM
})
public class ExamLoaderQuestionPackageProcessorSub implements HomeworkProcessor {
    @ImportService(interfaceClass = ParentRecommendLoader.class)
    private ParentRecommendLoader parentRecommendLoader;
    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam param = hc.getHomeworkParam();
        String subject = param.getSubject();
        HomeworkUserPreferences userPreferences = hc.getUserPreferences();
        if (userPreferences == null) {
            hc.setMapMessage(MapMessage.errorMessage("学生没有进行初始化:"+subject));
            return;
        }
        Long studentId = param.getStudentId();
        String unitId = hc.getUnitId();
        StudentInfo studentInfo = hc.getStudentInfo();
        // 如果当前单元已经布置，就不返回题包
        boolean isAssign = SafeConverter.toBoolean(HomeWorkCache.load(CacheKey.TODAYASSIGN, param.getBizType(), param.getStudentId(), unitId, userPreferences.getLevels().get(0)));
        Map<String, Object> data = hc.getData();
        data.put("subjects", SubjectUtils.BASIC_SUBJECTS.stream().map(s -> MapUtils.m("name", s.name(), "value", s.getValue())).collect(Collectors.toList()));
        data.put("boxLevel", HomeworkUtil.levelCName(userPreferences.getLevels().get(0)));
        if (isAssign) {
            data.put("questionBoxes", new ArrayList<>());
            data.put("isAssign", true);
            return;
        }
        List<QuestionPackage> questionPackages = HomeWorkCache.load(CacheKey.BOX, param.getBizType(), studentId, unitId);
        if (questionPackages  == null && !ObjectUtils.anyBlank(studentInfo.getCityCode(), studentInfo.getRegionCode())) {
            List<EliteSchoolPackage> eliteSchoolPackages = parentRecommendLoader.eliteSchoolQuestion(
                    param.getSubject(),
                    userPreferences.getBookId(),
                    unitId,
                    studentId,
                    studentInfo.getSchoolId(),
                    hc.getGroupId(),
                    (long) studentInfo.getCityCode(),
                    (long) studentInfo.getRegionCode(),
                    userPreferences.getLevels());
            questionPackages = new ArrayList<>();
            for (EliteSchoolPackage eliteSchoolPackage : eliteSchoolPackages) {
                QuestionPackage questionPackage = new QuestionPackage();
                questionPackage.setId(eliteSchoolPackage.getId());
                questionPackage.setDuration(eliteSchoolPackage.getDuration());
                questionPackage.setDocIds(eliteSchoolPackage.getDocIds());
                questionPackage.setName(eliteSchoolPackage.getName());
                questionPackage.setBizType(ObjectiveConfigType.EXAM.name());
                questionPackage.setUnitId(unitId);
                questionPackages.add(questionPackage);
            }
            // 日志
            LogCollector.info("parent", MapUtils.map(
                    "op", "parentRecommendLoader",
                    "mod1", "eliteSchoolQuestion",
                    "packageSize", ObjectUtils.get(eliteSchoolPackages::size, 0),
                    "docIdsSize", ObjectUtils.get(() -> eliteSchoolPackages.get(0).getDocIds().size(), 0),
                    "params", JsonUtils.toJson(
                            MapUtils.m("subject", param.getSubject(),
                                    "bookId", userPreferences.getBookId(),
                                    "unitId", param.getUnitIds(),
                                    "studentId", studentId,
                                    "schoolId", studentInfo.getSchoolId(),
                                    "groupId", hc.getGroupId(),
                                    "cityCode", studentInfo.getCityCode(),
                                    "regionCode", studentInfo.getRegionCode(),
                                    "levels", userPreferences.getLevels()))));
        }
        List<Map<String, Object>> questionBoxes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(questionPackages)) {
            HomeWorkCache.set(DateUtils.getCurrentToDayEndSecond(), questionPackages, CacheKey.BOX, ObjectiveConfigType.EXAM.name(), studentId, unitId);
            // 构建返回值
            questionBoxes.addAll(questionPackages.stream().map(question -> {
                // 以题包id缓存维度缓存题包, 布置的时候根据id查询具体题包布置
                HomeWorkCache.set(60 * 60, question, CacheKey.BOX, question.getBizType(), question.getId());
                return MapUtils.m(
                        "id", question.getId(),
                        "duration", (question.getDuration() + 59) / 60,
                        "level", question.getName(),
                        "questionCount", ObjectUtils.get(() -> question.getDocIds().size(), 0));
            }).collect(Collectors.toList()));
        }
        // 缓存默认单元
        HomeWorkCache.set(unitId, CacheKey.UNIT, studentId, subject);
        data.put("questionBoxes", questionBoxes);
        hc.setData(data);
    }
}
