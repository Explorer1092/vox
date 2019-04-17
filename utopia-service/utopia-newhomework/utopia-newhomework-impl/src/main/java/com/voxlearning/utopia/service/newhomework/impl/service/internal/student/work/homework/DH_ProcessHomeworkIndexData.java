package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.homework;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkCorrectStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.selfstudy.HomeworkSelfStudyRef;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.selfstudy.HomeworkSelfStudyRefDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

import static com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants.NeedSelfStudyHomeworkSubjects;
import static com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType.*;

/**
 * @author guohong.tan
 * @since 2017/6/29
 */
@Named
public class DH_ProcessHomeworkIndexData extends AbstractHomeworkIndexDataProcessor {

    // 需要链接到自学提升的
    private static List<ObjectiveConfigType> SelfStudyImproveTypeList = Arrays.asList(EXAM, INTELLIGENCE_EXAM, UNIT_QUIZ, KEY_POINTS, BASIC_KNOWLEDGE, CHINESE_READING, INTERESTING_PICTURE, FALLIBILITY_QUESTION, MENTAL);

    @Inject
    private HomeworkSelfStudyRefDao homeworkSelfStudyRefDao;

    @Override
    protected void doProcess(HomeworkIndexDataContext context) {
        NewHomework newHomework = context.getNewHomework();
        Long studentId = context.getStudentId();
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);

        Map<String, Object> result = new HashMap<>();
        result.put("homeworkId", newHomework.getId());
        result.put("homeworkType", newHomework.getNewHomeworkType());
        result.put("homeworkTag", newHomework.getHomeworkTag());
        result.put("practiceCount", newHomework.findPracticeContents().size());
        if (NewHomeworkType.OCR == newHomework.getType()) {
            result.put("homeworkName", DateUtils.dateToString(newHomework.getStartTime(), "MM月dd日") + newHomework.getSubject().getValue() + "纸质作业");
        } else {
            result.put("homeworkName", DateUtils.dateToString(newHomework.getStartTime(), "MM月dd日") + newHomework.getSubject().getValue() + "作业");
        }
        result.put("unitName", context.getUnitName());
        result.put("terminated", newHomework.isHomeworkTerminated());
        if (!newHomework.isHomeworkTerminated()) {
            result.put("days", DateUtils.dayDiff(newHomework.getEndTime(), new Date()));
        }
        result.put("remark", newHomework.getRemark());
        result.put("subject", newHomework.getSubject());

        NewHomeworkResult newHomeworkResult = context.getNewHomeworkResult();

        boolean isCurrentDayFinished = false;
        if (newHomeworkResult != null && newHomeworkResult.getPractices() != null) {
            if (newHomeworkResult.isFinished()) {
                // 为了判断一个很蠢的弹窗加的属性，前端没有弹过窗（前端缓存）&&后端是当天完成的作业，则前端弹窗否则不弹
                isCurrentDayFinished = DateUtils.isSameDay(new Date(), newHomeworkResult.getFinishAt());
                Integer integral = newHomeworkResult.getIntegral();
                Integer correctIntegral = 1;
                result.put("integral", integral);
                result.put("comment", newHomeworkResult.getComment());
                result.put("audioComment", newHomeworkResult.getAudioComment());
                result.put("rewardIntegral", newHomeworkResult.getRewardIntegral());
                result.put("correctIntegral", correctIntegral);
                User teacher = context.getTeacher();
                result.put("teacherName", teacher != null ? teacher.fetchRealname() + "老师" : "老师");
                Integer score = newHomeworkResult.processScore();
                int commentScore;
                if (score == null) {
                    commentScore = 100;//score为null的时候说明只有主观作业
                } else {
                    commentScore = score;
                }
                if (commentScore >= 90 && commentScore <= 100) {
                    result.put("praise", "干得漂亮！");
                    result.put("star", 3);
                } else if (commentScore >= 60 && commentScore <= 89) {
                    result.put("praise", "还不错哦！");
                    result.put("star", 2);
                } else {
                    result.put("praise", "不太理想哦！");
                    result.put("star", 1);
                }
                // 这部分是自学任务专属的
                Set<ObjectiveConfigType> homeworkTypeSet = context.getPracticeMap().keySet();
                if (NeedSelfStudyHomeworkSubjects.contains(newHomework.getSubject())
                        && CollectionUtils.containsAny(homeworkTypeSet, SelfStudyImproveTypeList)) {
                    result.put("selfStudyName", "前往扫除错题，奖励多多");
                    result.put("openAppUrl", "/view/mobile/student/wonderland/task?active=1&refer=300006");
                } else {
                    result.put("selfStudyName", "前往提升学习，奖励多多");
                    result.put("openAppUrl", "/view/mobile/student/wonderland/task?refer=300006");
                }
            }
        }

        result.put("finished", context.getFinished());
        result.put("isCurrentDayFinished", isCurrentDayFinished);
        result.put("practices", context.getPracticeInfos());
        result.put("undoPracticesCount", context.getUndoPracticesCount());
        result.put("finishingRate", context.getTotalQuestionCount() != 0 ? new BigDecimal(context.getDoTotalQuestionCount() * 100).divide(new BigDecimal(context.getTotalQuestionCount()), 0, BigDecimal.ROUND_HALF_UP).intValue() : 0);
        result.put("rewards", Collections.singletonList(MapUtils.m("iconUrl", "https://oss-image.17zuoye.com/wonderland/reward/img/2017/09/20/20170920153458516937.png",
                "rewardUrl", UrlUtils.buildUrlQuery("/view/mobile/student/wonderland/openapp", MapUtils.m("url", "/resources/apps/hwh5/growingworld/v100/index.html?HasWorkAward=true")))));
        result.put("isInPaymentBlackListRegion", studentDetail.isInPaymentBlackListRegion());
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        result.put("fairylandClosed", studentExtAttribute != null && studentExtAttribute.fairylandClosed());
        result.put("useVenus", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "UseVenus"));
        result.put("newProcess", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "NewIndexUrl"));
        result.put("newNaturalSpelling", grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "StudentHomework", "NewNaturalSpelling"));
        HomeworkCorrectStatus homeworkCorrectStatus = newHomeworkResultService.getHomeworkCorrectStatus(newHomework, newHomeworkResult);
        result.put("hCorrectStatus", homeworkCorrectStatus);
        if (!homeworkCorrectStatus.equals(HomeworkCorrectStatus.WITHOUT_CORRECT)) {
            HomeworkSelfStudyRef.ID refId = new HomeworkSelfStudyRef.ID(newHomework.getId(), studentId);
            HomeworkSelfStudyRef homeworkSelfStudyRef = homeworkSelfStudyRefDao.load(refId.toString());
            if (homeworkSelfStudyRef != null) {
                result.put("selfStudyUrl", UrlUtils.buildUrlQuery("/student/selfstudy/homework/index" + Constants.AntiHijackExt, MapUtils.m("homeworkId", homeworkSelfStudyRef.getSelfStudyId(), "sid", studentId)));
            }
        }

        // 这步dao操作在实现里做了处理
        if (newHomework.getCreateAt().after(NewHomeworkConstants.ALLOW_UPDATE_HOMEWORK_START_TIME)) {
            newHomeworkResultService.initNewHomeworkResult(newHomework.toLocation(), studentId);
        }
        context.setResult(result);

    }
}
