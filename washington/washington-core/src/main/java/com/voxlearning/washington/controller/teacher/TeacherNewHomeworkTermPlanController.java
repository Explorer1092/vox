package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.newexam.api.mapper.evaluation.report.EvaluationAverScoreInfo;
import com.voxlearning.utopia.service.newexam.consumer.client.EvaluationReportLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.termplan.*;
import com.voxlearning.utopia.service.newhomework.consumer.TermPlanLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.GroupTeacherMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhangbin
 * @since 2018/3/5
 */

@Controller
@RequestMapping("/teacher/newhomework/term/plan")
public class TeacherNewHomeworkTermPlanController extends AbstractTeacherController {

    @Inject private RaikouSDK raikouSDK;

    @Inject
    protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    private TermPlanLoaderClient termPlanLoaderClient;
    @Inject
    private EvaluationReportLoaderClient evaluationReportLoaderClient;

    /**
     * 班级列表
     */
    @RequestMapping(value = "clazzlist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage clazzList() {
        Teacher teacher = currentTeacher();
        if (teacher == null) {
            return MapMessage.errorMessage("请用老师帐号登录");
        }
        List<Subject> subjects = teacher.getSubjects();
        if (!subjects.contains(Subject.ENGLISH) && !subjects.contains(Subject.MATH)) {
            return MapMessage.errorMessage("只支持英语和数学学科");
        }
        Set<Long> teacherIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
        List<TermPlanClazzBO> termPlanClazzBOList = transformClassList(loadSubjectClazzList(teacherIds));
        return MapMessage.successMessage().add("clazzList", termPlanClazzBOList);
    }

    /**
     * 切换班级
     */
    @RequestMapping(value = "changeclazz.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage changeClazz() {
        Long clazzId = getRequestLong("clazzId");
        Long groupId = getRequestLong("groupId");
        if (clazzId == 0) {
            return MapMessage.errorMessage("clazzId is null");
        }
        if (groupId == 0) {
            return MapMessage.errorMessage("groupId is null");
        }
        try {
            String key = "TERM_PLAN_TEACHER_CHANGE_CLAZZ";
            washingtonCacheSystem.CBS.flushable.set(key, 0, Arrays.asList(clazzId, groupId));
            return MapMessage.successMessage();
        } catch (Exception ex) {
            return MapMessage.errorMessage("切换班级异常");
        }
    }

    /**
     * 学期规划-知识-单元详情、单元检测
     */
    @RequestMapping(value = "knowledge.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getKnowledge() {
        Long groupId = getRequestLong("groupId");
        String unitId = getRequestString("unitId");
        if (groupId == 0) {
            return MapMessage.errorMessage("groupId is null");
        }
        if (StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("unitId is null");
        }
        try {
            TermPlanKnowledgeBO termPlanKnowledgeBO = termPlanLoaderClient.loadKnowledgePointGraspInfos(groupId, unitId);
            List<TermPlanKnowledgeBO.UnitTest> unitTests = new ArrayList<>();
            List<EvaluationAverScoreInfo> evaluationAverScoreInfos = evaluationReportLoaderClient.fetchAverScoreByExamIds(groupId, unitId);
            if (CollectionUtils.isNotEmpty(evaluationAverScoreInfos)) {
                for (EvaluationAverScoreInfo info : evaluationAverScoreInfos) {
                    TermPlanKnowledgeBO.UnitTest unitTest = new TermPlanKnowledgeBO.UnitTest();
                    unitTest.setPaperId(info.getPaperId());
                    unitTest.setPaperName(info.getPaperName());
                    unitTest.setNewExamId(info.getNewExamId());
                    int averScore = SafeConverter.toInt(info.getAverScore());
                    if (!SafeConverter.toBoolean(info.isAssigned())) {
                        unitTest.setScore(-1);
                    }
                    unitTest.setScore(averScore);
                    unitTests.add(unitTest);
                }
            }
            termPlanKnowledgeBO.setUnitTests(unitTests);
            return MapMessage.successMessage().add("data", termPlanKnowledgeBO);
        } catch (Exception ex) {
            logger.error("Failed to get knowledge, groupId:{},unitId:{}", ex.getMessage(), groupId, unitId);
            return MapMessage.errorMessage("获取学期规划知识异常");
        }
    }

    /**
     * 学期规划-知识-查看详情
     */
    @RequestMapping(value = "knowledge/detail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getKnowledgeDetail() {
        Long groupId = getRequestLong("groupId");
        String unitId = getRequestString("unitId");
        if (groupId == 0) {
            return MapMessage.errorMessage("groupId is null");
        }
        if (StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("unitId is null");
        }
        try {
            Map<Long, GroupMapper> groupMap = groupLoaderClient.loadGroups(Collections.singletonList(groupId), false);
            Subject subject = null;
            Long clazzId = 0L;
            if (MapUtils.isNotEmpty(groupMap)) {
                GroupMapper groupMapper = groupMap.get(groupId);
                if (groupMapper != null) {
                    subject = groupMapper.getSubject();
                    clazzId = groupMapper.getClazzId();
                }
            }
            Clazz clazz = raikouSDK.getClazzClient()
                    .getClazzLoaderClient()
                    .loadClazz(clazzId);
            String clazzName = clazz.formalizeClazzName();
            List<TermPlanKnowledgeDetailBO> termPlanKnowledgeDetailBOs = termPlanLoaderClient.loadKnowledgeDetail(groupId, unitId);
            return MapMessage.successMessage()
                    .add("subject", subject)
                    .add("clazz", MapUtils.m(
                            "clazzId", clazzId,
                            "groupId", groupId,
                            "clazzName", clazzName))
                    .add("data", termPlanKnowledgeDetailBOs);
        } catch (Exception ex) {
            logger.error("Failed to get knowledge detail, groupId:{},unitId:{}", ex.getMessage(), groupId, unitId);
            return MapMessage.errorMessage("获取学期规划知识详情异常");
        }
    }

    /**
     * 学期规划-学习习惯
     */
    @RequestMapping(value = "studyhabit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage getStudyHabit() {
        Long groupId = getRequestLong("groupId");
        String unitId = getRequestString("unitId");
        if (groupId == 0) {
            return MapMessage.errorMessage("groupId is null");
        }
        if (StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("unitId is null");
        }
        try {
            TermPlanStudyHabitBO termPlanStudyHabitBO = termPlanLoaderClient.loadHomeworkFinishInfo(groupId, unitId);
            return MapMessage.successMessage().add("data", termPlanStudyHabitBO);
        } catch (Exception ex) {
            logger.error("Failed to get study habit, groupId:{},unitId:{}", ex.getMessage(), groupId, unitId);
            return MapMessage.errorMessage("获取学期规划学习习惯异常");
        }
    }

    /**
     * 学期规划-单元切换
     */
    @RequestMapping(value = "changeunit.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage changeUnit() {
        String clazzs = getRequestString("clazzs");
        if (StringUtils.isBlank(clazzs)) {
            return MapMessage.errorMessage("clazzs is null");
        }
        Teacher teacher = getSubjectSpecifiedTeacher();
        List<String> clazzIdGroupIdList = Arrays.asList(clazzs.trim().split(","));
        Map<Long, Long> clazzIdGroupIdMap = new HashMap<>();
        clazzIdGroupIdList.forEach(clazzIdGroupId -> {
            String[] str = clazzIdGroupId.split("_");
            if (str.length == 2) {
                clazzIdGroupIdMap.put(SafeConverter.toLong(str[0]), SafeConverter.toLong(str[1]));
            }
        });
        if (MapUtils.isEmpty(clazzIdGroupIdMap)) {
            return MapMessage.errorMessage("信息不全");
        }
        try {
            String defaultUnitId = "";
            Map<String, String> unitIdNameMap = new LinkedHashMap<>();
            MapMessage mapMessage = newHomeworkContentServiceClient.loadClazzBook(teacher, clazzIdGroupIdMap, false);
            if (mapMessage.isSuccess()) {
                Map<String, Object> bookMap = (Map<String, Object>) mapMessage.get("clazzBook");
                if (MapUtils.isNotEmpty(bookMap)) {
                    List<Map<String, Object>> unitList = (List<Map<String, Object>>) bookMap.get("unitList");
                    if (CollectionUtils.isNotEmpty(unitList)) {
                        for (Map<String, Object> map : unitList) {
                            String unitId = SafeConverter.toString(map.get("unitId"));
                            String unitName = SafeConverter.toString(map.get("cname"));
                            unitIdNameMap.put(unitId, unitName);
                            if (SafeConverter.toBoolean(map.get("defaultUnit"))) {
                                defaultUnitId = unitId;
                            }
                        }
                    }
                }
            }

            List<TermPlanUnitDetailBO> termPlanUnitDetailBOs = termPlanLoaderClient.changeUnit(clazzIdGroupIdMap, unitIdNameMap, defaultUnitId);
            return MapMessage.successMessage().add("data", termPlanUnitDetailBOs);
        } catch (Exception ex) {
            logger.error("Failed to change unit", ex.getMessage());
            return MapMessage.errorMessage("学期规划切换单元异常");
        }
    }

    private List<Map<String, Object>> loadSubjectClazzList(Set<Long> teacherIds) {
        List<Map<String, Object>> subjectClazzList = new ArrayList<>();
        Map<Long, List<GroupTeacherMapper>> teacherGroups = deprecatedGroupLoaderClient.loadTeacherGroups(teacherIds, true);
        Map<Long, List<Long>> clazzIdGroupIdsMap = new LinkedHashMap<>();
        Map<Long, Subject> groupIdSubjectMap = new LinkedHashMap<>();
        teacherGroups.forEach((tId, groups) -> groups.forEach(group -> {
            if (group.isTeacherGroupRefStatusValid(tId) && CollectionUtils.isNotEmpty(group.getStudents())) {
                clazzIdGroupIdsMap.computeIfAbsent(group.getClazzId(), k -> new ArrayList<>()).add(group.getId());
                groupIdSubjectMap.put(group.getId(), group.getSubject());
            }
        }));
        List<Clazz> clazzList = raikouSDK.getClazzClient()
                .getClazzLoaderClient()
                .loadClazzs(clazzIdGroupIdsMap.keySet())
                .stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(new Clazz.ClazzLevelAndNameComparator())
                .collect(Collectors.toList());
        Map<Integer, List<Map<String, Object>>> englishClazzMap = new LinkedHashMap<>();
        Map<Integer, List<Map<String, Object>>> mathClazzMap = new LinkedHashMap<>();
        clazzList.forEach(clazz -> {
            List<Long> groupIds = clazzIdGroupIdsMap.get(clazz.getId());
            if (CollectionUtils.isNotEmpty(groupIds)) {
                for (Long groupId : groupIds) {
                    Subject subject = groupIdSubjectMap.get(groupId);
                    Map<String, Object> clazzMapper = MapUtils.m(
                            "clazzId", clazz.getId(),
                            "groupId", groupId,
                            "clazzName", clazz.getClassName()

                    );
                    int clazzLevel = clazz.getClazzLevel().getLevel();
                    if (Subject.ENGLISH == subject) {
                        englishClazzMap.computeIfAbsent(clazzLevel, k -> new ArrayList<>()).add(clazzMapper);
                    } else if (Subject.MATH == subject) {
                        mathClazzMap.computeIfAbsent(clazzLevel, k -> new ArrayList<>()).add(clazzMapper);
                    }
                }
            }
        });
        if (MapUtils.isEmpty(englishClazzMap) && MapUtils.isEmpty(mathClazzMap)) {
            return Collections.emptyList();
        }
        if (MapUtils.isNotEmpty(englishClazzMap)) {
            List<Map<String, Object>> englishClazzList = new ArrayList<>();
            englishClazzMap.forEach((clazzLevel, clazzMappers) -> englishClazzList.add(MapUtils.m(
                    "clazzLevel", clazzLevel,
                    "clazzLevelName", ClazzLevel.getDescription(clazzLevel),
                    "clazzList", clazzMappers)));
            subjectClazzList.add(MapUtils.m(
                    "subject", Subject.ENGLISH,
                    "subjectName", Subject.ENGLISH.getValue(),
                    "clazzLevelList", englishClazzList));
        }
        if (MapUtils.isNotEmpty(mathClazzMap)) {
            List<Map<String, Object>> mathClazzList = new ArrayList<>();
            mathClazzMap.forEach((clazzLevel, clazzMappers) -> mathClazzList.add(MapUtils.m(
                    "clazzLevel", clazzLevel,
                    "clazzLevelName", ClazzLevel.getDescription(clazzLevel),
                    "clazzList", clazzMappers)));
            subjectClazzList.add(MapUtils.m(
                    "subject", Subject.MATH,
                    "subjectName", Subject.MATH.getValue(),
                    "clazzLevelList", mathClazzList));
        }
        return subjectClazzList;
    }

    private List<TermPlanClazzBO> transformClassList(List<Map<String, Object>> subjectClazzList) {
        List<TermPlanClazzBO> termPlanClazzBOList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(subjectClazzList)) {
            for (Map<String, Object> entry : subjectClazzList) {
                String subject = SafeConverter.toString(entry.get("subject"));
                List<Map<String, Object>> clazzLevelList = (List<Map<String, Object>>) entry.get("clazzLevelList");
                if (CollectionUtils.isNotEmpty(clazzLevelList)) {
                    for (Map<String, Object> clazzLevel : clazzLevelList) {
                        List<Map<String, Object>> clazzList = (List<Map<String, Object>>) clazzLevel.get("clazzList");
                        if (CollectionUtils.isNotEmpty(clazzList)) {
                            for (Map<String, Object> clazz : clazzList) {
                                TermPlanClazzBO termPlanClazzBO = new TermPlanClazzBO();
                                termPlanClazzBO.setSubjectId(Subject.of(subject).getId());
                                termPlanClazzBO.setSubject(Subject.of(subject));
                                termPlanClazzBO.setSubjectName(Subject.of(subject).getValue());
                                termPlanClazzBO.setClazzLevel(SafeConverter.toInt(clazzLevel.get("clazzLevel")));
                                termPlanClazzBO.setGroupLevel(SafeConverter.toInt(SafeConverter.toString(clazz.get("clazzName")).replace("班", "")));
                                Long clazzId = SafeConverter.toLong(clazz.get("clazzId"));
                                termPlanClazzBO.setClazzId(clazzId);
                                Long groupId = SafeConverter.toLong(clazz.get("groupId"));
                                termPlanClazzBO.setGroupId(groupId);
                                termPlanClazzBO.setClazzName(SafeConverter.toString(clazzLevel.get("clazzLevelName")) + SafeConverter.toString(clazz.get("clazzName")));
                                Boolean selected = false;
                                String key = "TERM_PLAN_TEACHER_CHANGE_CLAZZ";
                                CacheObject<List<Long>> cacheObject = washingtonCacheSystem.CBS.flushable.get(key);
                                if (cacheObject != null && cacheObject.getValue() != null) {
                                    List<Long> value = cacheObject.getValue();
                                    if (value.size() == 2) {
                                        if (Objects.equals(value.get(0), clazzId) && Objects.equals(value.get(1), groupId)) {
                                            selected = true;
                                        }
                                    }
                                }
                                termPlanClazzBO.setSelected(selected);
                                termPlanClazzBOList.add(termPlanClazzBO);
                            }
                        }
                    }
                }

            }
        }
        Comparator<TermPlanClazzBO> comparator = Comparator.comparingInt(e -> SafeConverter.toInt(e.getClazzLevel()));
        comparator = comparator.thenComparing(Comparator.comparingInt(e -> SafeConverter.toInt(e.getGroupLevel())));
        comparator = comparator.thenComparing((e1, e2) -> Integer.compare(SafeConverter.toInt(e2.getSubjectId()), SafeConverter.toInt(e1.getSubjectId())));
        termPlanClazzBOList = termPlanClazzBOList.stream()
                .filter(Objects::nonNull)
                .sorted(comparator)
                .collect(Collectors.toList());
        return termPlanClazzBOList;
    }
}
