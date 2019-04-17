package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.google.common.collect.Lists;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.api.constant.Constants;
import com.voxlearning.utopia.business.api.TeachingDiagnosisService;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.TeachingDiagnosisCourseStatus;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.TeachingDiagnosisQuestionResult;
import com.voxlearning.utopia.business.api.context.teachingdiagnosis.PreQuestionResultContext;
import com.voxlearning.utopia.entity.teachingdiagnosis.*;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.service.business.impl.processor.teachingdiagnosis.PreTeachingDiagnosisQuestionResultDataProcessor;
import com.voxlearning.utopia.service.business.impl.support.AbstractTeachingDiagnosisSupport;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.support.IntegralHistoryBuilderFactory;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Service(interfaceClass = TeachingDiagnosisService.class)
@ExposeServices({
        @ExposeService(interfaceClass = TeachingDiagnosisService.class, version = @ServiceVersion(version = "20180719")),
        @ExposeService(interfaceClass = TeachingDiagnosisService.class, version = @ServiceVersion(version = "20180508"))
})
public class TeachingDiagnosisServiceImpl extends AbstractTeachingDiagnosisSupport implements TeachingDiagnosisService {

    @Inject private PreTeachingDiagnosisQuestionResultDataProcessor preTeachingDiagnosisQuestionResultDataProcessor;
    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public MapMessage fetchPreQuestionsByStudent(StudentDetail student) {
        if (student == null || student.getClazz() == null) {
            return MapMessage.errorMessage("参数异常");
        }
        List<TeachingDiagnosisTask> teachingDiagnosisTasks = fetchTeachingDiagnosisTaskListByUserId(student.getId());

        List<TeachingDiagnosisExperimentConfig> experimentConfigList = fetchOnlineDiagnosisExperiment(student);

        //过滤掉订正任务中已经学过的课程
        NewHomeworkDiagnosisCourseResult doCourse = newHomeworkDiagnosisCourseResultDao.load(student.getId());
        if (doCourse != null && CollectionUtils.isNotEmpty(doCourse.getCourseIds())) {
            List<TeachingDiagnosisExperimentConfig> experimentConfigs = Lists.newLinkedList();
            for (TeachingDiagnosisExperimentConfig experimentConfig : experimentConfigList) {
                if (CollectionUtils.isEmpty(experimentConfig.getDiagnoses())) {
                    continue;
                }
                List<TeachingDiagnosisExperimentConfig.ExperimentCourseConfig> courseConfigs = Lists.newLinkedList();
                for (TeachingDiagnosisExperimentConfig.ExperimentCourseConfig courseConfig : experimentConfig.getDiagnoses()) {
                    List<String> courseIds = courseConfig.getCourse_ids().stream().filter(courseId -> !doCourse.getCourseIds().contains(courseId)).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(courseIds)) {
                        continue;
                    }
                    courseConfig.setCourse_ids(courseIds);
                    courseConfigs.add(courseConfig);
                }
                if (CollectionUtils.isNotEmpty(courseConfigs)) {
                    experimentConfig.setDiagnoses(courseConfigs);
                    experimentConfigs.add(experimentConfig);
                }
            }
            experimentConfigList = experimentConfigs;
        }

        if (CollectionUtils.isEmpty(experimentConfigList)) {
            return MapMessage.successMessage()
                    .set("questions", Collections.emptyList())
                    .set("bonus", 0)
                    .set("bonusDesc", "")
                    .set("experimentId", "")
                    .set("experimentGroupId", "");
        }

        Set<String> studentExpGroupIds = teachingDiagnosisTasks.stream().map(TeachingDiagnosisTask::getExperimentGroupId).collect(Collectors.toSet());
        TeachingDiagnosisExperimentConfig config = experimentConfigList.stream().filter(e -> CollectionUtils.isEmpty(studentExpGroupIds) || !studentExpGroupIds.contains(e.getGroupId())).findFirst().orElse(null);

        if (config == null) {
            return MapMessage.successMessage()
                    .set("questions", Collections.emptyList())
                    .set("bonus", 0)
                    .set("bonusDesc", "")
                    .set("experimentId", "")
                    .set("experimentGroupId", "");
        }
        return MapMessage.successMessage()
                .set("experimentId", config.getId())
                .set("experimentGroupId", config.getGroupId())
                .set("questions", Arrays.asList(config.getPreQuestion()))
                .set("bonus", config.getBonus() != null && config.getBonus() > 0 ? config.getBonus() : 0)
                .set("bonusDesc", config.getPreBonusDescription());
    }

    @Override
    public MapMessage processPreQuestionResult(PreQuestionResultContext context) {
        PreQuestionResultContext resultContext = preTeachingDiagnosisQuestionResultDataProcessor.process(context);
        MapMessage res;
        if (resultContext.isSuccessful()) {
            res = MapMessage.successMessage();
            res.putAll(resultContext.getResult());
        } else {
            res = MapMessage.errorMessage();
            res.setErrorCode(resultContext.getErrorCode()).setInfo(resultContext.getMessage());
        }
        return res;
    }

    @Override
    public MapMessage fetchIndexMessage(String taskId) {
        TeachingDiagnosisTask task = teachingDiagnosisTaskDao.load(taskId);
        if (task == null) {
            return MapMessage.errorMessage("错误数据");
        }

        //学生ID奇数走原始课, 偶数走第三方
        Map<String, Map> courseUrlConfigMap = null;
        if (task.getUserId() != null && task.getUserId() % 2 == 0) {
            courseUrlConfigMap = loadCourseConfig();
        }
        TeachingDiagnosisCourseStatus taskStatus = TeachingDiagnosisCourseStatus.todo;
        int doneNum = 0;
        List<Map<String, Object>> courseList = new ArrayList<>();
        Map<String, List<TeachingDiagnosisCourseQuestionResult>> questionMap = teachingDiagnosisCourseQuestionResultDao.loadByTaskId(taskId).stream().collect(Collectors.groupingBy(TeachingDiagnosisCourseQuestionResult::getCourseId));
        for (TeachingDiagnosisTaskCourse course : task.getCourses()) {
            Map<String, Object> coursesMap = new HashMap<>();
            String courseUrl = "exam/flash/light/interaction/v2/course" + Constants.AntiHijackExt;
            String courseType = "PPT";
            String courseId = course.getId();
            if (courseUrlConfigMap != null && courseUrlConfigMap.get(courseId) != null) {
                courseUrl = SafeConverter.toString(courseUrlConfigMap.get(courseId).get("url"));
                courseType = "TOUCH_STUDY";
                courseId = SafeConverter.toString(courseUrlConfigMap.get(courseId).get("urlIndex"));
            }

            coursesMap.put("id", courseId);
            coursesMap.put("name", course.getName());
            coursesMap.put("description", course.getDescription());
            TeachingDiagnosisCourseStatus status = TeachingDiagnosisCourseStatus.todo;
            BigDecimal correctRate = new BigDecimal(0);
            TeachingDiagnosisCourseResult result = teachingDiagnosisCourseResultDao.load(TeachingDiagnosisCourseResult.generateID(taskId, courseId));
            if (result != null && MapUtils.isNotEmpty(questionMap) && CollectionUtils.isNotEmpty(questionMap.get(courseId))) {
                status = TeachingDiagnosisCourseStatus.done;
                List<TeachingDiagnosisCourseQuestionResult> results = questionMap.get(courseId);
                List<TeachingDiagnosisCourseQuestionResult> rightResult = results.stream().filter(e -> e.getMaster() != null && e.getMaster()).collect(Collectors.toList());
                correctRate = new BigDecimal(rightResult.size()).divide(new BigDecimal(results.size()), 4, BigDecimal.ROUND_HALF_UP);
                taskStatus = TeachingDiagnosisCourseStatus.doing;
                doneNum++;
            } else if (MapUtils.isNotEmpty(questionMap) && CollectionUtils.isNotEmpty(questionMap.get(courseId))) {
                status = TeachingDiagnosisCourseStatus.doing;
                taskStatus = TeachingDiagnosisCourseStatus.doing;
            }

            coursesMap.put("courseUrl", courseUrl);
            coursesMap.put("courseType", courseType);
            coursesMap.put("status", status);
            coursesMap.put("correctRate", correctRate);
            courseList.add(coursesMap);
        }

        if (doneNum == courseList.size()) {
            taskStatus = TeachingDiagnosisCourseStatus.done;
        }
        int bonus = Optional.ofNullable(teachingDiagnosisExperimentConfigDao.load(task.getExperimentId()))
                .map(TeachingDiagnosisExperimentConfig::getBonus)
                .orElse(0);
        MapMessage res = MapMessage.successMessage();
        res.put("totalNum", task.getTotalNumber());
        res.put("experimentId", task.getExperimentId());
        res.put("experimentGroupId", task.getExperimentGroupId());
        res.put("bonus", bonus);
        res.put("wrongNum", task.getWrongNumber());
        res.put("courses", courseList);
        res.put("previewBonus", bonus);
        res.put("taskStatus", taskStatus);
        return res;
    }

    @Override
    public MapMessage saveCourseQuestionResult(TeachingDiagnosisQuestionResult questionResult, Boolean last) {
        String courseResultID = TeachingDiagnosisCourseResult.generateID(questionResult.getTaskId(), questionResult.getCourseId());
        if (teachingDiagnosisCourseResultDao.load(courseResultID) != null) {
            return MapMessage.errorMessage("题目已经做完");
        }

        TeachingDiagnosisTask task = teachingDiagnosisTaskDao.load(questionResult.getTaskId());
        if (task == null || CollectionUtils.isEmpty(task.getCourses())) {
            return MapMessage.errorMessage("课程数据异常");
        }

        TeachingDiagnosisCourseQuestionResult result = new TeachingDiagnosisCourseQuestionResult();
        Date now = new Date();
        String id = TeachingDiagnosisCourseQuestionResult.generateId(questionResult.getTaskId(), questionResult.getQuestionId());
        result.setId(id);
        result.setUserId(questionResult.getStudentId());
        result.setTaskId(questionResult.getTaskId());
        result.setQuestionId(questionResult.getQuestionId());
        result.setMaster(questionResult.isMaster());
        result.setCreateTime(now);
        result.setUpdateTime(now);
        result.setCourseId(questionResult.getCourseId());
        result.setDuration(questionResult.getDuration());

        if (CollectionUtils.isNotEmpty(questionResult.getUserAnswer())) {
            result.setUserAnswer(questionResult.getUserAnswer());
        }

        teachingDiagnosisCourseQuestionResultDao.upsert(result);
        if (!last) {
            return MapMessage.successMessage();
        }


        TeachingDiagnosisCourseResult courseResult = new TeachingDiagnosisCourseResult();
        courseResult.setCourseId(questionResult.getCourseId());
        courseResult.setTaskId(questionResult.getTaskId());
        courseResult.setFinishTime(now);
        courseResult.setId(courseResultID);
        teachingDiagnosisCourseResultDao.upsert(courseResult);

        if (questionResult.getTaskId().equals(task.getId())) {
            int bonus = Optional.ofNullable(teachingDiagnosisTaskDao.load(questionResult.getTaskId()))
                    .filter(e -> StringUtils.isNotBlank(e.getExperimentId()))
                    .map(e -> teachingDiagnosisExperimentConfigDao.load(e.getExperimentId()))
                    .map(TeachingDiagnosisExperimentConfig::getBonus)
                    .orElse(0);
            if (bonus > 0) {
                IntegralHistory history = IntegralHistoryBuilderFactory.newBuilder(questionResult.getStudentId(), IntegralType.PRIMARY_STUDENT_TEACHING_DIAGNOSIS_REWARD)
                        .withIntegral(bonus)
                        .withComment("参与诊断测评获得奖励")
                        .build();
                try {
                    userIntegralService.changeIntegral(studentLoaderClient.loadStudent(questionResult.getStudentId()), history);
                } catch (Exception e) {
                    logger.error("add Integral error. history:{}", history, e);
                }
            }
        }


        return MapMessage.successMessage();
    }

    @Override
    public List<TeachingDiagnosisTask> fetchTeachingDiagnosisTaskListByUserId(Long studentId) {
        if (studentId == null) {
            return Collections.emptyList();
        }
        return teachingDiagnosisTaskDao.findByUserId(studentId);
    }

    @Override
    public TeachingDiagnosisTask fetchDiagnosisTaskCheckedExperimented(Long studentId) {
        List<TeachingDiagnosisTask> teachingDiagnosisTasks = fetchTeachingDiagnosisTaskListByUserId(studentId);

        //过滤掉订正任务中已经学过的课程
        NewHomeworkDiagnosisCourseResult doCourse = newHomeworkDiagnosisCourseResultDao.load(studentId);
        if (doCourse != null && CollectionUtils.isNotEmpty(doCourse.getCourseIds())) {
            List<TeachingDiagnosisTask> teachingDiagnosisTasksNew = Lists.newLinkedList();
            teachingDiagnosisTasks.forEach(diagnosisTask -> {
                List<TeachingDiagnosisTaskCourse> taskCourses = diagnosisTask.getCourses().stream().filter(c -> !doCourse.getCourseIds().contains(c.getId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(taskCourses)) {
                    diagnosisTask.setCourses(taskCourses);
                    teachingDiagnosisTasksNew.add(diagnosisTask);
                }
            });
            teachingDiagnosisTasks = teachingDiagnosisTasksNew;
        }
        if (CollectionUtils.isEmpty(teachingDiagnosisTasks)) {
            return null;
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        List<TeachingDiagnosisExperimentConfig> experimentConfigList = fetchOnlineDiagnosisExperiment(studentDetail);
        if (CollectionUtils.isEmpty(experimentConfigList)) {
            return null;
        }
        Set<String> studentExpGroupIds = teachingDiagnosisTasks.stream().map(TeachingDiagnosisTask::getExperimentGroupId).collect(Collectors.toSet());
        TeachingDiagnosisExperimentConfig config = experimentConfigList.stream().filter(e -> !studentExpGroupIds.contains(e.getGroupId())).findFirst().orElse(null);

        //过滤掉订正任务中已经学过的课程
        if (config != null && doCourse != null && CollectionUtils.isNotEmpty(doCourse.getCourseIds())) {
            List<TeachingDiagnosisExperimentConfig.ExperimentCourseConfig> diagnoses = Lists.newLinkedList();
            config.getDiagnoses().forEach(o -> {
                List<String> courseIds = o.getCourse_ids().stream().filter(courseId -> !doCourse.getCourseIds().contains(courseId)).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(courseIds)) {
                    o.setCourse_ids(courseIds);
                    diagnoses.add(o);
                }
            });
            if (CollectionUtils.isEmpty(diagnoses)) {
                config = null;
            }
        }
        return config == null ? teachingDiagnosisTasks.stream().sorted((e1, e2) -> e2.getUpdateTime().compareTo(e1.getUpdateTime())).findFirst().orElse(null) : null;
    }

    @Override
    public TeachingDiagnosisTask fetchDiagnosisTaskById(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return null;
        }
        return teachingDiagnosisTaskDao.load(taskId);
    }


    private Map<String, Map> loadCourseConfig() {
        List<PageBlockContent> configs = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("ExperimentCourse");
        if (CollectionUtils.isNotEmpty(configs)) {
            PageBlockContent configPageBlockContent = configs.stream()
                    .filter(p -> "CourseUrlConfig".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
                return JsonUtils.fromJsonToMapStringMap(configContent);
            }
        }
        return null;
    }
}
