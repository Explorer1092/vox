package com.voxlearning.utopia.service.newhomework.impl.template.report.template;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.MentalArithmeticTimeLimit;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.ObjectiveConfigTypeParameter;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.MentalTypePart;
import com.voxlearning.utopia.service.newhomework.api.mapper.report.newhomework.app.TypePartContext;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewHomeworkStudyMasterDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
public class AppMentalArithmeticObjectiveConfigTypeProcessorTemplate extends AppMentalObjectiveConfigTypeProcessorTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.MENTAL_ARITHMETIC;
    }

    @Inject
    private NewHomeworkStudyMasterDao newHomeworkStudyMasterDao;

    @Override
    public void fetchTypePart(TypePartContext typePartContext) {
        //*********** begin 初始化数据准备 *********** //
        /**
         * 1:type =》 类型
         * 2:result =》 返回数据
         * 3:newHomeworkResults =》 完成该类型的中间表数据
         * 4:newHomeworkProcessResultMap
         */
        ObjectiveConfigType type = typePartContext.getType();
        Map<ObjectiveConfigType, Object> result = typePartContext.getResult();
        Map<Long, NewHomeworkResult> newHomeworkResultMap = typePartContext.getNewHomeworkResultMap();
        NewHomework newHomework = typePartContext.getNewHomework();

        //******** end newHomeworkResults 数据处理 *******//
        super.fetchTypePart(typePartContext);
        MentalTypePart mentalTypePart = (MentalTypePart) result.get(type);
        if (mentalTypePart == null) {
            return;
        }
        ObjectiveConfigTypeParameter param = new ObjectiveConfigTypeParameter();
        String url = UrlUtils.buildUrlQuery("/view/reportv5/clazz/mentaldetail",
                MapUtils.m(
                        "homeworkId", newHomework.getId(),
                        "type", type,
                        "subject", newHomework.getSubject(),
                        "param", JsonUtils.toJson(param)));
        mentalTypePart.setUrl(url);

        NewHomeworkPracticeContent target = newHomework.findTargetNewHomeworkPracticeContentByObjectiveConfigType(ObjectiveConfigType.MENTAL_ARITHMETIC);
        if (target == null || target.getTimeLimit() == MentalArithmeticTimeLimit.ZERO) {
            return;
        }

        NewHomeworkStudyMaster studyMaster = newHomeworkStudyMasterDao.load(newHomework.getId());
        if (studyMaster != null) {
            String cdnBaseUrl = typePartContext.getCdnBaseUrl();
            if (CollectionUtils.isNotEmpty(studyMaster.getCalculationList())) {
                Map<Long, Student> userMap = studentLoaderClient.loadStudents(studyMaster.getCalculationList());
                for (Long sid : studyMaster.getCalculationList()) {
                    if (!newHomeworkResultMap.containsKey(sid))
                        continue;
                    NewHomeworkResult newHomeworkResult = newHomeworkResultMap.get(sid);
                    NewHomeworkResultAnswer newHomeworkResultAnswer = newHomeworkResult.getPractices().get(type);
                    Student user = userMap.get(sid);
                    MentalTypePart.CalculationStudent student = new MentalTypePart.CalculationStudent();
                    int score = SafeConverter.toInt(newHomeworkResultAnswer.processScore(type));
                    int duration = SafeConverter.toInt(newHomeworkResultAnswer.processDuration());
                    String durationStr = NewHomeworkUtils.handlerEnTime(duration);
                    student.setDurationStr(durationStr);
                    student.setScore(score);
                    student.setDuration(duration);
                    student.setUserId(sid);
                    student.setUserName(user.fetchRealnameIfBlankId());
                    student.setScoreStr(score + "分");
                    student.setImageUrl(NewHomeworkUtils.getUserAvatarImgUrl(cdnBaseUrl, user.fetchImageUrl()));
                    mentalTypePart.getCalculationStudents().add(student);
                }
            }
        }
    }
}
