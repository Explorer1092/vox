package com.voxlearning.utopia.service.parent.homework.impl.template.correct;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.athena.api.cuotizhenduan.loader.CuotizhenduanLoader;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultService;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.api.model.Command;
import com.voxlearning.utopia.service.parent.homework.api.model.CorrectParam;
import com.voxlearning.utopia.service.parent.homework.api.model.DoType;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.SupportCommand;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectBaseTemplate;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.CorrectContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.correct.ErrorDiagnostic;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.IntelligentTeachingService;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;
import com.voxlearning.utopia.service.question.consumer.IntelDiagnosisClient;
import com.voxlearning.utopia.service.user.api.UserLoader;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 订正作业首页
 *
 * @author Wenlong Meng
 * @since Mar 18, 2019
 */
@Named
@Slf4j
@SupportCommand(Command.INDEX)
public class IndexCorrectTemplate extends CorrectBaseTemplate {

    //local variables
    @Inject private HomeworkLoader homeworkLoader;
    @Inject private HomeworkResultLoader homeworkResultLoader;
    @Inject private UserLoader userLoader;
    @ImportService(interfaceClass = CuotizhenduanLoader.class)
    private CuotizhenduanLoader cuotizhenduanLoader;
    @Inject private IntelDiagnosisClient intelDiagnosisClient;
    @Inject private HomeworkResultService homeworkResultService;
    @Inject private IntelligentTeachingService intelligentTeachingService;

    /**
     * 作业首页
     *
     * @param c 作业参数
     * @return
     */
    @Override
    public MapMessage process(CorrectContext c){
        CorrectParam param = c.getParam();
        String homeworkId = param.getHomeworkId();
        Long currentUserId = param.getCurrentUserId();
        Long studentId = param.getStudentId();
        //check
        if(ObjectUtils.anyBlank(homeworkId, currentUserId, studentId)){
            LoggerUtils.info("params error", homeworkId, currentUserId, studentId);
            return MapMessage.errorMessage("作业id和用户id不能为空")
                    .setErrorCode(ErrorCodeConstants.ERROR_CODE_PARAMETER);
        }
        //作业
        Homework homework = homeworkLoader.loadHomework(homeworkId);
        if (null == homework) {
            LoggerUtils.info("homework not exist", studentId, homeworkId);
            return MapMessage.successMessage().setInfo("作业不存在").setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_NOT_EXIST);
        }

        //初始化订正作业结果
        HomeworkResult correctHR = initAndGetCHR(param);

        Map<String, Object> homeworkList = new HashMap<>();
        homeworkList.put("homeworkId",homeworkId);
        homeworkList.put("homeworkName", correctHR.getName());
        homeworkList.put("homeworkType", StudyType.selfstudy);
        homeworkList.put("homeworkTag", "Correct");
        homeworkList.put("subject", correctHR.getSubject());

        List<HomeworkProcessResult> correctHPRS = homeworkResultLoader.loadHomeworkProcessResults(correctHR.getId());
        int doQuestionCount = correctHPRS.size();

        List<Map<String, Object>> practices = new ArrayList<>();
        //错题诊断
        if(!ObjectUtils.anyBlank(correctHR.getErrorDiagnostics())){
            Map<String, Object> errorDiagnosticsPractice = new HashMap<>();
            practices.add(errorDiagnosticsPractice);
            errorDiagnosticsPractice.put("objectiveConfigType", ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.name());
            errorDiagnosticsPractice.put("objectiveConfigTypeName", ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.getValue());
            errorDiagnosticsPractice.put("doHomeworkUrl", url(ObjectiveConfigType.DIAGNOSTIC_INTERVENTIONS.name(), Command.DO, homeworkId,studentId,correctHR.getId()));
            List<String> couseIds = correctHR.getErrorDiagnostics().stream().map(m->(String)m.get("courseId")).distinct().collect(Collectors.toList());
            Map<String, IntelDiagnosisCourse> intelDiagnosisCourseMap = intelDiagnosisClient.loadDiagnosisCoursesByIdsIncludeDisabled(couseIds);
            AtomicBoolean finished= new AtomicBoolean(true);
            List<Map> edTaskList = correctHR.getErrorDiagnostics().stream().collect(Collectors.groupingBy(m->m.get("courseId"))).values().stream().map(ds->{
                Map<String, Object> task = Maps.newHashMap();
                String courseId = (String)ds.get(0).get("courseId");
                IntelDiagnosisCourse course = intelDiagnosisCourseMap.get(courseId);
                task.put("qTitle", "错题" + ds.size() + "道");
                task.put("qIds", ds.stream().map(m->m.get("qId")).collect(Collectors.toList()));
                task.put("diagnosisSource", "SyncDiagnosis");
                task.put("courseId", course.getId());
               task.put("courseName", ObjectUtils.get(()->course.getName(), "辅导课程"));
                List<String> similarQids = ds.stream().map(m->(String)m.get("similarQid")).collect(Collectors.toList());
                task.put("similarQids", similarQids);
                int wrongCount = (int)correctHPRS.stream().filter(p->similarQids.contains(p.getQuestionId())&& !p.getRight()).count();
                int correctCount = (int)correctHPRS.stream().filter(p->similarQids.contains(p.getQuestionId())&& p.getRight()).count();
                task.put("correctCount", correctCount);
                if(correctCount+wrongCount < ds.size()){
                    finished.set(false);
                }
                task.put("status", correctCount+wrongCount == 0 ? "TODO" : (correctCount+wrongCount == ds.size() ? "FINISH":"DOING"));
                task.put("category", course.getCategory());
                task.put("wrongCount", wrongCount);
                return task;
            }).collect(Collectors.toList());
            errorDiagnosticsPractice.put("taskList", edTaskList);
            errorDiagnosticsPractice.put("finished",  finished.get());
            homeworkList.put("courseCount", edTaskList.size());
        }
        if(!ObjectUtils.anyBlank(correctHR.getErrorQIds())){
            Map<String, Object> correctionsPractice = new HashMap<>();
            practices.add(correctionsPractice);
            correctionsPractice.put("objectiveConfigType", ObjectiveConfigType.DIAGNOSTIC_CORRECTIONS.name());
            correctionsPractice.put("objectiveConfigTypeName", ObjectiveConfigType.DIAGNOSTIC_CORRECTIONS.getValue());
            correctionsPractice.put("finished", correctHR != null && correctHR.getFinished());
            correctionsPractice.put("doHomeworkUrl", url(ObjectiveConfigType.DIAGNOSTIC_CORRECTIONS.name(), Command.DO, homeworkId,studentId,correctHR.getId()));
            AtomicInteger i = new AtomicInteger(0);
            List<Map> taskList = correctHR.getErrorQIds().stream().map(id->{
                Map<String, Object> task = Maps.newHashMap();
                task.put("qTitle", "第" + i.incrementAndGet() + "题");
                task.put("qIds", Lists.newArrayList(id));
                HomeworkProcessResult hpr = correctHPRS.stream().filter(p->p.getQuestionId().equals(id)).findFirst().orElse(null);
                task.put("status", hpr == null ? "TODO" : (hpr.getRight() ? "CORRECT":"WRONG"));
                task.put("similarQid", id);
                return task;
            }).collect(Collectors.toList());
            correctionsPractice.put("taskList", taskList);
        }
        homeworkList.put("practices", practices);
        homeworkList.put("errorsCount", correctHR.getQuestionCount());
        homeworkList.put("finishingRate",new BigDecimal(doQuestionCount * 100).divide(new BigDecimal(correctHR.getQuestionCount()+correctHR.getErrorDiagnostics().size()), 0, BigDecimal.ROUND_HALF_UP).intValue());
        homeworkList.put("finished", ObjectUtils.get(()->correctHR.getFinished(), Boolean.FALSE));
        return MapMessage.successMessage().add("homeworkList", homeworkList);
    }

    /**
     * 初始化并返回订正结果
     *
     * @param param
     * @return
     */
    private HomeworkResult initAndGetCHR(CorrectParam param) {
        //订正结果id
        String correctHRId = HomeworkUtil.generatorID(param.getHomeworkId(), param.getStudentId(), DoType.CORRECT);
        //订正结果
        HomeworkResult correctHR = homeworkResultLoader.loadHomeworkResult(correctHRId);
        if(correctHR != null){
            return correctHR;
        }

        //答题结果
        HomeworkResult hr = homeworkResultLoader.loadHomeworkResult(param.getHomeworkId(), param.getStudentId());
        List<HomeworkProcessResult> hprs = homeworkResultLoader.loadHomeworkProcessResults(hr.getId())
                .stream()
                .filter(hpr->!hpr.getRight())//过滤错题
                .collect(Collectors.toList());
        correctHR = new HomeworkResult();
        correctHR.setDoCount(0);
        correctHR.setId(correctHRId);
        correctHR.setSubject(hr.getSubject());
        correctHR.setAdditions(hr.getAdditions());
        correctHR.setActionId(hr.getActionId());
        correctHR.setGrade(hr.getGrade());
        correctHR.setStartTime(new Date());
        correctHR.setUserId(hr.getUserId());
        correctHR.setSource(hr.getSource());
        correctHR.setBizType(hr.getBizType());
        correctHR.setDoType(DoType.CORRECT);
        correctHR.setFinished(false);
        //算分
        Double score = hprs.stream().mapToDouble(HomeworkProcessResult::getScore).sum();
        correctHR.setScore(score);
        //计时
        Long duration = hprs.stream().mapToLong(HomeworkProcessResult::getDuration).sum();
        correctHR.setDuration(duration);
        correctHR.setHomeworkId(hr.getHomeworkId());
        correctHR.setClientType(hr.getClientType());
        correctHR.setClientName(hr.getClientName());
        correctHR.setIpImei(hr.getIpImei());
        correctHR.setName(DateUtils.dateToString(hr.getStartTime(), "M月dd日") + Subject.of(hr.getSubject()).getValue() + "作业订正");

        //错题诊断
        List<Map<String, Object>> errorDiagnostics = errorDiagnostics(hprs);
        correctHR.setErrorDiagnostics(errorDiagnostics);
        Set<String> errorDiagnosticsIds = errorDiagnostics.stream().map(r->(String)r.get("qId")).collect(Collectors.toSet());
        List<String> errorQIds = hprs.stream().map(HomeworkProcessResult::getQuestionId).filter(id->!errorDiagnosticsIds.contains(id)).collect(Collectors.toList());
        correctHR.setErrorQIds(errorQIds);
        correctHR.setQuestionCount(errorQIds.size());

        homeworkResultService.saveHomeworkResult(correctHR);
        LoggerUtils.debug("saveHomeworkResult", correctHR);

        return correctHR;
    }

    /**
     * 错题诊断
     *
     * @param hprs
     */
    private List<Map<String, Object>> errorDiagnostics(List<HomeworkProcessResult> hprs){
        if(!hprs.get(0).getSubject().equals(Subject.MATH.name())){
            return Collections.EMPTY_LIST;
        }
        Long studentId = hprs.get(0).getUserId();
        Integer grade = hprs.get(0).getGrade();
        //由于qid无法推荐
        Map<String, String> zId2qId = new HashMap<>();
        Map<String, Object> params = Maps.newHashMap();
        params.put("studentId", studentId);
        params.put("grade", grade);
        List srcDocIds = hprs.stream().map(hpr->{
            String zid = intelligentTeachingService.loadZIdByQId(hpr.getQuestionId());
            zId2qId.put(zid, hpr.getQuestionId());
            zId2qId.put(hpr.getQuestionId(), zid);
            return MapUtils.m("userAnswer", hpr.getUserAnswers(),
                    "questionId", zid
            );}).collect(Collectors.toList());
        params.put("srcDocIds", srcDocIds);

        String response = cuotizhenduanLoader.loadZhenduanRecommendation(JsonUtils.toJson(params));
        LoggerUtils.debug("loadZhenduanRecommendation", params, response);
        if(ObjectUtils.anyBlank(response)){
            LoggerUtils.info("responseError", response);
            return Collections.EMPTY_LIST;
        }
        Map<String, Object> respMap = JsonUtils.fromJson(response);
        if(!ObjectUtils.get(()->(Boolean) respMap.get("success"), Boolean.FALSE)){
            LoggerUtils.info("responseError", response);
            return Collections.EMPTY_LIST;
        }
        List<Map<String, Object>> result = (List<Map<String, Object>>)respMap.get("interventionList");

        return result.stream().filter(r->!ObjectUtils.anyBlank(r.get("courseId")))
                .map(r->{r.put("qId", zId2qId.get(r.get("qId"))); return r;})
                .collect(Collectors.toList());
    }

}
