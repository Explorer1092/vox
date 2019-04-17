package com.voxlearning.utopia.service.parent.homework.impl.template;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkCorrectStatus;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.*;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.annotation.SubType;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 做作业接口
 *
 * @author Wenlong Meng
 * @version 20181111
 * @date 2018-11-13
 */
@Named
@Slf4j
public class HomeworkDoTemplateImpl extends SpringContainerSupport implements HomeworkDoTemplate {

    //local variables
    @Inject private RaikouSystem raikouSystem;

    @Inject private HomeworkLoader homeworkLoader;
    @Inject private HomeworkResultLoader homeworkResultLoader;
    @Inject private StudentLoaderClient studentLoaderClient;

    /**
     * 作业首页
     *
     * @param param 作业参数
     * @return
     */
    public MapMessage index(HomeworkParam param) {
        String homeworkId = param.getHomeworkId();
        Long currentUserId = param.getCurrentUserId();
        Long studentId = param.getStudentId();
        //check
        if (ObjectUtils.anyBlank(homeworkId, currentUserId, studentId)) {
            return MapMessage.errorMessage("作业id和学生id不能为空")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        }
        //当前用户
        User user = raikouSystem.loadUser(currentUserId);
        if (user == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        //作业
        Homework homework = homeworkLoader.loadHomework(homeworkId);
        HomeworkPractice homeworkPractice = homeworkLoader.loadHomeworkPractice(homeworkId);
        if (null == homework || homeworkPractice == null) {
            LoggerUtils.info("index", studentId, homeworkId, ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
            return MapMessage.successMessage().setInfo("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("subject", homework.getSubject());
        result.put("remark", homework.getRemark());
        result.put("unitName", ObjectUtils.get(() -> homework.getAdditions().get("unitName")));
        result.put("homeworkType", homework.getType());
        result.put("homeworkTag", homework.getHomeworkTag());
        result.put("userId", studentId);
//        result.put("newProcess", true);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        result.put("isInPaymentBlackListRegion", ObjectUtils.get(() -> studentDetail.isInPaymentBlackListRegion(), false));

        HomeworkResult homeworkResult = homeworkResultLoader.loadHomeworkResult(homeworkId, studentId);
        long undoPracticesCount = homeworkPractice.getPractices().size();
        int doQuestionCount = 0;
        final Map<String, List<HomeworkProcessResult>> doPractices = new HashMap<>();
        if (homeworkResult != null) {
            List<HomeworkProcessResult> homeworkProcessResults = homeworkResultLoader.loadHomeworkProcessResults(homeworkResult.getId());
            doPractices.putAll(homeworkProcessResults.stream().collect(Collectors.groupingBy(HomeworkProcessResult::getObjectiveConfigType)));
            undoPracticesCount = homeworkPractice.getPractices().stream().filter(e -> e.getQuestions().size() > ObjectUtils.get(() -> doPractices.get(e.getType()).size(), 0)).count();
            doQuestionCount = homeworkProcessResults.size();
        }
        List<Map<String, Object>> practices = new ArrayList<>();
        Map<String, Object> practice = new HashMap<>();
        homeworkPractice.getPractices().forEach(e -> {
            practice.put("objectiveConfigType", e.getType());
            practice.put("objectiveConfigTypeName", ObjectiveConfigType.of(e.getType()).getValue());
            practice.put("questionCount", e.getQuestions().size());
            practice.put("doCount", ObjectUtils.get(() -> doPractices.get(e.getType()).size(), 0));
            practice.put("finished", e.getQuestions().size() == ObjectUtils.get(() -> doPractices.get(e.getType()).size(), 0));
            practice.put("doHomeworkUrl", url(homework.getSource(), e.getType(), "do", homeworkId, studentId));
            practice.put("middleResultUrl", url(homework.getSource(), e.getType(), "submit", homeworkId, studentId));
            practice.put("timeLimit", e.getTimeLimit());
            practice.put("mentalAward", false);
        });
        practices.add(practice);

        result.put("practices", practices);
        result.put("hCorrectStatus", HomeworkCorrectStatus.WITHOUT_CORRECT.name());
        result.put("finishingRate", new BigDecimal(doQuestionCount * 100).divide(new BigDecimal(homework.getQuestionCount()), 0, BigDecimal.ROUND_HALF_UP).intValue());
        result.put("undoPracticesCount", undoPracticesCount);
        result.put("terminated", ObjectUtils.get(() -> homeworkResult.getFinished(), Boolean.FALSE) || System.currentTimeMillis() > homework.getEndTime().getTime());
        result.put("finished", ObjectUtils.get(() -> homeworkResult.getFinished(), Boolean.FALSE));
        result.put("isCurrentDayFinished", false);
        result.put("days", DateUtils.dayDiff(homework.getEndTime(), new Date()));
        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(studentId);
        result.put("fairylandClosed", studentExtAttribute != null && studentExtAttribute.fairylandClosed());
        result.put("newNaturalSpelling", true);
        result.put("practiceCount", homeworkPractice.getPractices().size());

        result.put("homeworkId", homeworkId);
        result.put("homeworkName", DateUtils.dateToString(homework.getStartTime(), "M月dd日") + Subject.of(homework.getSubject()).getValue() + "练习");
        result.put("isBindParents", Boolean.TRUE); // 是否绑定家长通
        result.put("useVenus", Boolean.TRUE);
        return MapMessage.successMessage().add("homeworkList", result);
    }

    /**
     * do
     *
     * @param param 作业参数
     * @return
     */
    public MapMessage od(HomeworkParam param) {
        String homeworkId = param.getHomeworkId();
        Long studentId = param.getStudentId();
        //作业
        Homework homework = homeworkLoader.loadHomework(homeworkId);
        HomeworkPractice homeworkPractice = homeworkLoader.loadHomeworkPractice(homeworkId);
        String objectiveConfigType = homeworkPractice.getPractices().get(0).getType();
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> flashVars = new HashMap<>();
        flashVars.put("homeworkId", homework.getId());
        flashVars.put("objectiveConfigType", objectiveConfigType);
        flashVars.put("objectiveConfigTypeName", ObjectiveConfigType.of(objectiveConfigType).getValue());
        flashVars.put("subject", homework.getSubject());
        flashVars.put("userId", studentId);
        flashVars.put("learningType", StudyType.homework);
        flashVars.put("questionUrl", url(homework.getSource(), objectiveConfigType, "questions", homeworkId, studentId));
        flashVars.put("processResultUrl", url(homework.getSource(), objectiveConfigType, "submit", homeworkId, studentId));
        flashVars.put("completedUrl", url(homework.getSource(), objectiveConfigType, "answers", homeworkId, studentId));
        data.put("flashVars", JsonUtils.toJson(flashVars));
        return MapMessage.successMessage().add("data", data);
    }

    /**
     * 生成url
     *
     * @param source
     * @param objectiveConfigType
     * @param command
     * @param homeworkId
     * @param studentId
     * @return
     */
    private String url(String source, String objectiveConfigType, String command, String homeworkId, Long studentId) {
        return UrlUtils.buildUrlQuery("/parent/homework/do/index.api", MapUtils.m("command", command, "objectiveConfigType", objectiveConfigType, "homeworkId", homeworkId, "studentId", studentId));
    }

    /**
     * 题目
     *
     * @param param 作业参数
     * @return
     */
    public MapMessage questions(HomeworkParam param) {
        String homeworkId = param.getHomeworkId();
        try {
            //作业
            Homework homework = homeworkLoader.loadHomework(homeworkId);
            HomeworkPractice homeworkPractice = homeworkLoader.loadHomeworkPractice(homeworkId);
            Map<String, Object> result = new HashMap<>();
            result.put("normalTime", homework.getDuration());
            result.put("examUnitMap", homeworkPractice.getPractices().stream().map(e -> e.getQuestions()).flatMap(x -> x.stream()).collect(Collectors.toMap(Questions::getQuestionId, q -> MapUtils.m("bookId", homework.getAdditions().get("bookId"), "unitId", homework.getAdditions().get("unitId")))));
            result.put("eids", homeworkPractice.getPractices().stream().map(e -> e.getQuestions()).flatMap(x -> x.stream()).map(Questions::getQuestionId).collect(Collectors.toList()));
            return MapMessage.successMessage().add("result", result);
        } catch (Exception e) {
            log.error("{}", JsonUtils.toJson(param), e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }

    /**
     * 获取答案
     *
     * @param param 作业参数
     * @return 答案信息
     */
    public MapMessage answers(HomeworkParam param) {
        String homeworkId = param.getHomeworkId();
        Long studentId = param.getStudentId();
        //作业
        HomeworkResult homeworkResult = homeworkResultLoader.loadHomeworkResult(homeworkId, studentId);
        final Map<String, Object> result = new LinkedHashMap<>();
        if (homeworkResult != null) {
            List<HomeworkProcessResult> hprs = homeworkResultLoader.loadHomeworkProcessResults(homeworkResult.getId());
            if (ObjectUtils.get(() -> homeworkResult.getBizType(), "EXAM").equals("MENTAL_ARITHMETIC")) {
                Map<String, HomeworkProcessResult> hprMap = hprs.stream().collect(Collectors.toMap(HomeworkProcessResult::getQuestionId, t -> t));
                HomeworkPractice homeworkPractice = homeworkLoader.loadHomeworkPractice(homeworkId);
                //批量提交会导致题目顺序无法按照时间排序，通过作业里的顺序重新排序
                homeworkPractice.getPractices().get(0).getQuestions().forEach(q -> {
                    try {
                        HomeworkProcessResult e = hprMap.get(q.getQuestionId());
                        if (e != null) {
                            result.put(e.getQuestionId(), MapUtils.m(
                                    "subMaster", e.getUserSubGrasp(),
                                    "master", e.getRight(),
                                    "userAnswers", e.getUserAnswers(),
                                    "fullScore", e.getScore(),
                                    "score", e.getUserScore(),
                                    "intervention", false
                            ));
                        } else {
                            logger.info("hprMap:{}; q:{}", JsonUtils.toJson(hprMap), JsonUtils.toJson(q));
                        }
                    } catch (Exception e) {
                        log.info("answers:{}", e.getMessage());
                    }

                });
            } else {
                Map<String, Object> result1 = hprs.stream().collect(Collectors.toMap(HomeworkProcessResult::getQuestionId, e -> MapUtils.m(
                        "subMaster", e.getUserSubGrasp(),
                        "master", e.getRight(),
                        "userAnswers", e.getUserAnswers(),
                        "fullScore", e.getScore(),
                        "score", e.getUserScore(),
                        "intervention", false
                )));
                return MapMessage.successMessage().add("result", result1);
            }
        }

        return MapMessage.successMessage().add("result", result);
    }

    /**
     * 上报结果
     *
     * @param param 作业参数
     * @return 结果信息
     */
    public MapMessage submit(HomeworkParam param) {
        return homeworkSubmitTemplaeCache.get(param.getObjectiveConfigType()).submit(param);
    }

    /**
     * Cache, allowing for fast iteration.
     */
    private static final Map<String, HomeworkSubmitTemplate> homeworkSubmitTemplaeCache = new HashMap<>();

    /**
     * 初始化模板
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        applicationContext.getBeansOfType(HomeworkSubmitTemplate.class).values()
                .forEach(p -> {
                    SubType annotation = p.getClass().getAnnotation(SubType.class);
                    if (annotation == null) {
                        return;
                    }
                    Arrays.stream(annotation.value()).forEach(a ->
                            homeworkSubmitTemplaeCache.put(a.name(), p)
                    );

                });
    }
}
