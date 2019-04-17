package com.voxlearning.utopia.service.newhomework.impl.template.internal.report;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticTimeLimit;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypePartContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ReportRateContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.MentalArithmeticStudent;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.pc.CalculationStudent;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkStudyMasterDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
public class ProcessNewHomeworkAnswerDetailMentalArithmeticTemplate extends ProcessNewHomeworkAnswerDetailMentalTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.MENTAL_ARITHMETIC;
    }

    @Inject
    private NewHomeworkStudyMasterDao newHomeworkStudyMasterDao;


    @Override
    public void fetchNewHomeworkCommonObjectiveConfigTypePart(ObjectiveConfigTypePartContext context) {

        //************ begin 数据初始化准备 *******************//
        Map<Long, NewHomeworkResult> newHomeworkResultMap = context.getNewHomeworkResultMap();
        ObjectiveConfigType type = context.getType();
        Map<Long, User> userMap = context.getUserMap();
        super.fetchNewHomeworkCommonObjectiveConfigTypePart(context);
        //********* end 数据后处理，失分率和答案的显示 ********//

        List<MentalArithmeticStudent> calculationStudents = getCalculationStudents(type, newHomeworkResultMap, userMap);
        MapMessage mapMessage = context.getMapMessage();
        context.setMapMessage(mapMessage);
        mapMessage.add("mentalArithmeticStudents", calculationStudents);
    }


    //新口算排名
    private List<MentalArithmeticStudent> getCalculationStudents(ObjectiveConfigType type, Map<Long, NewHomeworkResult> newHomeworkResultMap, Map<Long, User> userMap) {
        List<MentalArithmeticStudent> calculationStudents = new LinkedList<>();
        for (User user : userMap.values()) {
            MentalArithmeticStudent student = new MentalArithmeticStudent();
            calculationStudents.add(student);
            student.setUserId(user.getId());
            student.setUserName(user.fetchRealnameIfBlankId());
            if (newHomeworkResultMap.containsKey(user.getId())) {
                NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(user.getId());
                NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
                int duration = SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
                String durationStr = NewHomeworkUtils.handlerEnTime(duration);
                student.setFinished(true);
                student.setRepair(SafeConverter.toBoolean(newHomeworkResult.getRepair()));
                student.setScore(SafeConverter.toInt(newHomeworkResultAnswer.processScore(type)));
                student.setDuration(SafeConverter.toInt(newHomeworkResultAnswer.processDuration()));
                student.setDurationStr(durationStr);
            }
        }
        calculationStudents.sort(MentalArithmeticStudent.comparator);
        return calculationStudents;
    }


    @Override
    public void processNewHomeworkAnswerDetail(ReportRateContext reportRateContext) {
        super.processNewHomeworkAnswerDetail(reportRateContext);
        ObjectiveConfigType type = reportRateContext.getType();
        Map<String, Object> result = reportRateContext.getResult();
        if (!result.containsKey(type.name())) {
            return;
        }
        Map<Long, NewHomeworkResult> newHomeworkResultMap = reportRateContext.getNewHomeworkResultMap()
                .values()
                .stream()
                .filter(o -> o.isFinishedOfObjectiveConfigType(type))
                .collect(Collectors
                        .toMap(NewHomeworkResult::getUserId, Function.identity()));
        Map<Long, User> userMap = reportRateContext.getUserMap();
        List<MentalArithmeticStudent> calculationStudents = getCalculationStudents(type, newHomeworkResultMap, userMap);
        Map<String, Object> typeResult = MapUtils.m("calculationStudents", calculationStudents,
                "questionDetails", result.get(type.name()));
        reportRateContext.getResult().put(reportRateContext.getType().name(), typeResult);
    }


    @Override
    @SuppressWarnings("unchecked")
    public void processQuestionPartTypeInfo(Map<Long, NewHomeworkResult> newHomeworkResultMap, NewHomework newHomework, ObjectiveConfigType type, Map<ObjectiveConfigType, Object> result, String cdnBaseUrl) {
        super.processQuestionPartTypeInfo(newHomeworkResultMap, newHomework, type, result, cdnBaseUrl);
        Map<String, Object> typeResult = (Map<String, Object>) result.get(type);
        if (typeResult == null || !typeResult.containsKey("knowledgePointData"))
            return;
        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.MENTAL_ARITHMETIC);
        //非限时的口算不出现榜单
        if (target == null || target.getTimeLimit() == MentalArithmeticTimeLimit.ZERO) {
            return;
        }
        NewHomeworkStudyMaster studyMaster = newHomeworkStudyMasterDao.load(newHomework.getId());
        List<CalculationStudent> calculationStudents = new LinkedList<>();
        if (studyMaster != null) {
            if (CollectionUtils.isNotEmpty(studyMaster.getCalculationList())) {
                Map<Long, Student> userMap = studentLoaderClient.loadStudents(studyMaster.getCalculationList());
                for (Long sid : studyMaster.getCalculationList()) {
                    if (!newHomeworkResultMap.containsKey(sid))
                        continue;
                    NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(sid);
                    NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
                    //转班的情况下榜单，学生还是存在该榜单里面（产品江震新定的结论）
                    Student user = userMap.get(sid);
                    CalculationStudent student = new CalculationStudent();
                    int score = SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
                    int duration = SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
                    String durationStr = NewHomeworkUtils.handlerEnTime(duration);
                    student.setUserId(sid);
                    student.setUserName(user.fetchRealnameIfBlankId());
                    student.setScore(score);
                    student.setDuration(duration);
                    student.setDurationStr(durationStr);
                    student.setImageUrl(NewHomeworkUtils.getUserAvatarImgUrl(cdnBaseUrl, user.fetchImageUrl()));
                    calculationStudents.add(student);
                }
            }
        }
        typeResult.put("calculationStudents", calculationStudents);
    }

}
