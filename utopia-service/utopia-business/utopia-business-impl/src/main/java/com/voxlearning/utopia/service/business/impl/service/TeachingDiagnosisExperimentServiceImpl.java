package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.business.api.TeachingDiagnosisExperimentService;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.DiagnosisExperimentContent;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.DiagnosisExperimentGroup;
import com.voxlearning.utopia.business.api.constant.teachingdiagnosis.ExperimentType;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.DiagnosisExperimentLog;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentConfig;
import com.voxlearning.utopia.entity.teachingdiagnosis.experiment.TeachingDiagnosisExperimentGroupConfig;
import com.voxlearning.utopia.service.business.impl.support.AbstractTeachingDiagnosisSupport;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.intelligent.diagnosis.IntelDiagnosisCourse;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = TeachingDiagnosisExperimentService.class)
public class TeachingDiagnosisExperimentServiceImpl extends AbstractTeachingDiagnosisSupport implements TeachingDiagnosisExperimentService {

    @Override
    public List<DiagnosisExperimentGroup> fetchAllExperimentGroup(ExperimentType type) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<TeachingDiagnosisExperimentGroupConfig> groupConfigList = teachingDiagnosisExperimentGroupConfigDao.findAll(type);
        if (CollectionUtils.isEmpty(groupConfigList)) {
            return Collections.emptyList();
        }
        List<DiagnosisExperimentGroup> groupList = new ArrayList<>();
        groupConfigList.forEach(e -> {
            DiagnosisExperimentGroup group = new DiagnosisExperimentGroup();
            group.setId(e.getId());
            group.setName(e.getName());
            group.setExperimentList(DiagnosisExperimentContent.toContentList(teachingDiagnosisExperimentConfigDao.findByGroupId(e.getId())));
            groupList.add(group);
        });
        return groupList;
    }

    @Override
    public MapMessage createExperimentGroup(String name, ExperimentType type, String updater) {
        if (StringUtils.isBlank(name) || type == null) {
            return MapMessage.errorMessage("参数异常");
        }
        TeachingDiagnosisExperimentGroupConfig config = new TeachingDiagnosisExperimentGroupConfig();
        config.setId(RandomUtils.nextObjectId());
        config.setName(name);
        config.setCreateTime(new Date());
        config.setDisabled(false);
        config.setUpdater(updater);
        config.setGroupType(type);
        config.setUpdateTime(new Date());
        teachingDiagnosisExperimentGroupConfigDao.insert(config);
        return MapMessage.successMessage().add("id", config.getId());
    }

    @Override
    public MapMessage deleteExperimentGroup(String id) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }
        teachingDiagnosisExperimentGroupConfigDao.deleteById(id);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage fetchExperimentInfoById(String id) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }
        TeachingDiagnosisExperimentConfig experiment = teachingDiagnosisExperimentConfigDao.load(id);
        return MapMessage.successMessage().add("experiment", experiment != null ? DiagnosisExperimentContent.toContent(experiment) : Collections.emptyMap())
                .add("logList", experiment != null ? parseLogList(diagnosisExperimentLogDao.findByExperimentId(id)) : Collections.emptyList());
    }

    private List<Map<String, Object>> parseLogList(List<DiagnosisExperimentLog> logList) {
        if (CollectionUtils.isEmpty(logList)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        logList.forEach(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("createTime", DateUtils.dateToString(e.getCreateTime()));
            map.put("operation", e.getOperation().getDescription());
            result.add(map);
        });
        return result;
    }

    @Override
    public MapMessage createExperiment(String name, String groupId, String updater) {
        if (StringUtils.isAnyEmpty(name, groupId)) {
            return MapMessage.errorMessage("参数异常");
        }
        TeachingDiagnosisExperimentGroupConfig group = teachingDiagnosisExperimentGroupConfigDao.load(groupId);
        if (group == null) {
            return MapMessage.errorMessage("实验组不存在");
        }
        TeachingDiagnosisExperimentConfig experimentConfig = new TeachingDiagnosisExperimentConfig();
        experimentConfig.setCreateTime(new Date());
        experimentConfig.setUpdateTime(new Date());
        experimentConfig.setDisabled(false);
        experimentConfig.setType(group.getGroupType());
        experimentConfig.setName(name);
        experimentConfig.setGroupId(groupId);
        experimentConfig.setGroupName(group.getName());
        experimentConfig.setStatus(TeachingDiagnosisExperimentConfig.Status.WAITING);
        teachingDiagnosisExperimentConfigDao.insert(experimentConfig);

        DiagnosisExperimentLog log = new DiagnosisExperimentLog();
        log.setCreateTime(new Date());
        log.setDisabled(false);
        log.setExperimentId(experimentConfig.getId());
        log.setUpdater(updater);
        log.setUpdateTime(new Date());
        log.setOperation(DiagnosisExperimentLog.Operation.Create);
        diagnosisExperimentLogDao.insert(log);

        return MapMessage.successMessage().add("id", experimentConfig.getId());
    }

    @Override
    public MapMessage updateExperimentStatus(String id, TeachingDiagnosisExperimentConfig.Status to, String updater) {
        if (StringUtils.isBlank(id) || to == null) {
            return MapMessage.errorMessage("参数异常");
        }
        TeachingDiagnosisExperimentConfig experimentConfig = teachingDiagnosisExperimentConfigDao.load(id);
        if (experimentConfig == null) {
            return MapMessage.errorMessage("数据不存在");
        }

        if (to == TeachingDiagnosisExperimentConfig.Status.ONLINE && (CollectionUtils.isEmpty(experimentConfig.getDiagnoses()) ||
                experimentConfig.getDiagnoses().stream().filter(e -> CollectionUtils.isNotEmpty(e.getCourse_ids())).findFirst().orElse(null) == null
                || StringUtils.isAnyBlank(experimentConfig.getPreQuestion(), experimentConfig.getPostQuestion()))) {
            return MapMessage.errorMessage("请检查配置再上线");
        }

        teachingDiagnosisExperimentConfigDao.updateStatus(id, to);

        DiagnosisExperimentLog log = new DiagnosisExperimentLog();
        log.setCreateTime(new Date());
        log.setDisabled(false);
        log.setExperimentId(experimentConfig.getId());
        log.setUpdater(updater);
        log.setUpdateTime(new Date());
        DiagnosisExperimentLog.Operation operation = null;
        switch (to) {
            case WAITING:
                operation = DiagnosisExperimentLog.Operation.Modify;
                break;
            case ONLINE:
                operation = DiagnosisExperimentLog.Operation.GoOnline;
                break;
            case OFFLINE:
                operation = DiagnosisExperimentLog.Operation.GoOffline;
                break;

        }

        log.setOperation(operation);
        diagnosisExperimentLogDao.insert(log);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateExperiment(DiagnosisExperimentContent content, String updater) {
        if (content == null || StringUtils.isBlank(content.getId())) {
            return MapMessage.errorMessage("参数异常");
        }
        TeachingDiagnosisExperimentConfig experimentConfig = teachingDiagnosisExperimentConfigDao.load(content.getId());
        if (experimentConfig == null) {
            return MapMessage.errorMessage("数据不存在");
        }
        TeachingDiagnosisExperimentConfig newExperiment = TeachingDiagnosisExperimentConfig.toExperiment(content);

        if (StringUtils.isNotBlank(newExperiment.getPreQuestion())) {
            NewQuestion newQuestion = questionLoaderClient.loadQuestionByDocId(newExperiment.getPreQuestion());
            if (newQuestion == null) {
                return MapMessage.errorMessage("前测题配置异常");
            }
        }

        if (StringUtils.isNotBlank(newExperiment.getPostQuestion())) {
            NewQuestion newQuestion = questionLoaderClient.loadQuestionByDocId(newExperiment.getPostQuestion());
            if (newQuestion == null) {
                return MapMessage.errorMessage("后测题配置异常");
            }
        }

        List<String> courseIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(newExperiment.getDiagnoses())) {
            for (TeachingDiagnosisExperimentConfig.ExperimentCourseConfig courseConfig :
                    newExperiment.getDiagnoses().stream().filter(e -> CollectionUtils.isNotEmpty(e.getCourse_ids())).collect(Collectors.toList())) {
                courseIds.addAll(courseConfig.getCourse_ids());
            }
        }

        if (CollectionUtils.isNotEmpty(courseIds)) {
            Set<String> couseIdSet = courseIds.stream().collect(Collectors.toSet());
            List<IntelDiagnosisCourse> courseList = intelDiagnosisClient.loadDiagnosisCoursesByIds(couseIdSet);
            if (CollectionUtils.isEmpty(courseList) || courseList.size() != couseIdSet.size()) {
                return MapMessage.errorMessage("输入的课程有问题，请检查");
            }
        }
        teachingDiagnosisExperimentConfigDao.updateContent(newExperiment);

        DiagnosisExperimentLog log = new DiagnosisExperimentLog();
        log.setCreateTime(new Date());
        log.setDisabled(false);
        log.setExperimentId(content.getId());
        log.setUpdater(updater);
        log.setUpdateTime(new Date());
        log.setOperation(DiagnosisExperimentLog.Operation.Modify);
        diagnosisExperimentLogDao.insert(log);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteExperiment(String id, String updater) {
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }

        TeachingDiagnosisExperimentConfig experimentConfig = teachingDiagnosisExperimentConfigDao.load(id);
        if (experimentConfig == null) {
            return MapMessage.errorMessage("数据不存在");
        }

        teachingDiagnosisExperimentConfigDao.deleteById(id);
        DiagnosisExperimentLog log = new DiagnosisExperimentLog();
        log.setCreateTime(new Date());
        log.setDisabled(false);
        log.setExperimentId(id);
        log.setUpdater(updater);
        log.setUpdateTime(new Date());
        log.setOperation(DiagnosisExperimentLog.Operation.Modify);
        diagnosisExperimentLogDao.insert(log);

        return MapMessage.successMessage();
    }

    @Override
    public TeachingDiagnosisExperimentGroupConfig loadGroupById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return teachingDiagnosisExperimentGroupConfigDao.load(id);
    }

    @Override
    public TeachingDiagnosisExperimentConfig loadExperimentById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        return teachingDiagnosisExperimentConfigDao.load(id);
    }

    @Override
    public Map<String, TeachingDiagnosisExperimentConfig> loadExperimentByIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }

        Map<String, TeachingDiagnosisExperimentConfig> map = new HashMap<>();
        for (String id : ids) {
            map.put(id, teachingDiagnosisExperimentConfigDao.load(id));
        }
        return map;
    }
}

