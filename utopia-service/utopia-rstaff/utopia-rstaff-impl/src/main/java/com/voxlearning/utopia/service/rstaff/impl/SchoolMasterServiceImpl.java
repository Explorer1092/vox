package com.voxlearning.utopia.service.rstaff.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.LoadDubheDataService;
import com.voxlearning.athena.api.LoadPrdDataService;
import com.voxlearning.athena.bean.PrdSchoolMasterHomeworkData;
import com.voxlearning.athena.bean.PrdSchoolMasterSummaryData;
import com.voxlearning.athena.bean.PrdSchoolMasterUnitData;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.rstaff.api.SchoolMasterService;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-06-19 11:36
 */

@Named
@Service(interfaceClass = SchoolMasterService.class)
@ExposeService(interfaceClass = SchoolMasterService.class)
public class SchoolMasterServiceImpl extends SpringContainerSupport implements SchoolMasterService {

    @Inject private RaikouSystem raikouSystem;

    @Getter
    @ImportService(interfaceClass = LoadPrdDataService.class)
    private LoadPrdDataService loadPrdDataService;

    @Getter
    @ImportService(interfaceClass = LoadDubheDataService.class)
    private LoadDubheDataService loadDubheDataService;

    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    @Override
    public Map<String, Object> loadSchoolUsageData(Long schoolId, String month) {
        MapMessage mapMessage = loadPrdDataService.loadSchoolUsageData(schoolId, month);
        boolean isSuccess = (boolean) mapMessage.get("success");
        if (isSuccess && !"{}".equals(mapMessage.get("dataMap"))) {
            PrdSchoolMasterSummaryData summaryData = (PrdSchoolMasterSummaryData) mapMessage.get("dataMap");
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("usageTeachers", summaryData.getUsageTeachers());
            data.put("usageStudents", summaryData.getUsageStudents());
            data.put("increaseTeachers", summaryData.getIncreaseTeachers());
            data.put("increaseStudents", summaryData.getIncreaseStudents());

            Map<String, Object> usageSubjectTeacherMap = JsonUtils.fromJson(summaryData.getUsageSubjectTeachers());
            data.put("usageSubjectTeachers", usageSubjectTeacherMap);
            List<Map> usageStudentMapList = JsonUtils.fromJsonToList(summaryData.getUsageSubjectStudents(), Map.class);
            Map<String, Integer> usageStudentMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(usageStudentMapList)) {
                for (Map<String, Integer> temp : usageStudentMapList) {
                    usageStudentMap.putAll(temp);
                }
            }
            data.put("usageStudentsMap", usageStudentMap);
            Map<String, Object> increaseSubjectTeacherMap = JsonUtils.fromJson(summaryData.getIncreaseSubjectTeachers());
            data.put("increaseSubjectTeachers", increaseSubjectTeacherMap);
            Map<String, Integer> increaseStudentMap = new LinkedHashMap<>();
            List<Map> increaseStudentMapList = JsonUtils.fromJsonToList(summaryData.getIncreaseSubjectStudents(), Map.class);
            if (CollectionUtils.isNotEmpty(increaseStudentMapList)) {
                for (Map<String, Integer> temp : increaseStudentMapList) {
                    increaseStudentMap.putAll(temp);
                }
            }
            data.put("increaseStudentsMap", increaseStudentMap);
            return data;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> loadHomework(Long schoolId, String grade, String subject, String month) {
        MapMessage mapMessage = loadPrdDataService.loadHomework(schoolId, grade, subject, month);
        boolean isSuccess = (boolean) mapMessage.get("success");
        if (isSuccess && !"{}".equals(mapMessage.get("dataMap"))) {
            PrdSchoolMasterHomeworkData summaryData = (PrdSchoolMasterHomeworkData) mapMessage.get("dataMap");
            Map<String, Object> data = new LinkedHashMap<>();

            List<Map> tempHomeworkScenes = JsonUtils.fromJsonToList(summaryData.getHomeworkScenes(), Map.class);
            Map<String, Integer> homeworkScenes = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(tempHomeworkScenes)) {
                for (Map<String, Integer> temp : tempHomeworkScenes) {
                    homeworkScenes.putAll(temp);
                }
            }

            List<Map> tempClazzDoHomeworkCounts = JsonUtils.fromJsonToList(summaryData.getClazzDoHomeworkCounts(), Map.class);
            Map<String, Integer> clazzDoHomeworkCounts = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(tempClazzDoHomeworkCounts)) {
                for (Map<String, Integer> temp : tempClazzDoHomeworkCounts) {
                    clazzDoHomeworkCounts.putAll(temp);
                }
            }

            List<Map> tempAssignmentCounts = JsonUtils.fromJsonToList(summaryData.getAssignmentCounts(), Map.class);
            Map<String, Integer> assignmentCounts = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(tempAssignmentCounts)) {
                for (Map<String, Integer> temp : tempAssignmentCounts) {
                    assignmentCounts.putAll(temp);
                }
            }

            List<Map> tempWeekHomeworkScenes = JsonUtils.fromJsonToList(summaryData.getWeekHomeworkScenes(), Map.class);
            Map<String, Map<String, Integer>> weekHomeworkScenes = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(tempWeekHomeworkScenes)) {
                for (Map listMap : tempWeekHomeworkScenes) {
                    String week = (String) listMap.keySet().iterator().next();
                    List<Map<String, Integer>> scenesL = (List<Map<String, Integer>>) listMap.get(week);
                    Map<String, Integer> scenesMap = new LinkedHashMap<>();
                    for (Map<String, Integer> scenes : scenesL) {
                        scenesMap.putAll(scenes);
                    }
                    weekHomeworkScenes.put(week, scenesMap);
                }
            }

            data.put("homeworkScenes", homeworkScenes);
            data.put("clazzDoHomeworkCounts", clazzDoHomeworkCounts);
            data.put("assignmentCounts", assignmentCounts);
            data.put("weekHomeworkScenes", weekHomeworkScenes);
            return data;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> loadUnitAvgQuestions(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear, String term) {
        MapMessage mapMessage = loadPrdDataService.loadUnitAvgQuestions(areaId, schoolId, subject, grade, clazz, schoolYear, term);
        boolean isSuccess = (boolean) mapMessage.get("success");
        if (isSuccess && !"{}".equals(mapMessage.get("dataMap"))) {
            PrdSchoolMasterUnitData summaryData = (PrdSchoolMasterUnitData) mapMessage.get("dataMap");
            Map<String, Object> data = new LinkedHashMap<>();

            List<Map> tempUnitAvgQuestions = JsonUtils.fromJsonToList(summaryData.getUnitAvgQuestions(), Map.class);
            Map<String, Integer> unitAvgQuestions = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(tempUnitAvgQuestions)) {
                for (Map<String, Integer> temp : tempUnitAvgQuestions) {
                    unitAvgQuestions.putAll(temp);
                }
            }

            List<Map> tempAreaUnitAvgQuestions = JsonUtils.fromJsonToList(summaryData.getAreaUnitAvgQuestions(), Map.class);
            Map<String, Integer> areaUnitAvgQuestions = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(tempAreaUnitAvgQuestions)) {
                for (Map<String, Integer> temp : tempAreaUnitAvgQuestions) {
                    areaUnitAvgQuestions.putAll(temp);
                }
            }

            List<Map> tempUnitQuestionRightRatio = JsonUtils.fromJsonToList(summaryData.getUnitQuestionRightRatio(), Map.class);
            Map<String, Double> unitQuestionRightRatio = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(tempUnitQuestionRightRatio)) {
                for (Map<String, Double> temp : tempUnitQuestionRightRatio) {
                    unitQuestionRightRatio.putAll(temp);
                }
            }

            List<Map> tempAreaUnitQuestionRightRatio = JsonUtils.fromJsonToList(summaryData.getAreaUnitQuestionRightRatio(), Map.class);
            Map<String, Double> areaUnitQuestionRightRatio = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(tempAreaUnitQuestionRightRatio)) {
                for (Map<String, Double> temp : tempAreaUnitQuestionRightRatio) {
                    areaUnitQuestionRightRatio.putAll(temp);
                }
            }

            data.put("bookId", summaryData.getBookId());
            data.put("unitAvgQuestions", unitAvgQuestions);
            data.put("areaUnitAvgQuestions", areaUnitAvgQuestions);
            data.put("unitQuestionRightRatio", unitQuestionRightRatio);
            data.put("areaUnitQuestionRightRatio", areaUnitQuestionRightRatio);
            return data;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> loadBaseData(Integer month, Long schoolId) {
        if (RuntimeMode.current().le(Mode.TEST)) {
            month = 201903;
            schoolId = 108236L;
        }

        MapMessage resMap = loadDubheDataService.loadBaseData(month, schoolId);
        if (resMap.isSuccess() && Objects.nonNull(resMap.get("dataMap"))) {
            Map<String, Object> dataMap = (Map<String, Object>) resMap.get("dataMap");
            Integer activeStudentNum = SafeConverter.toInt(dataMap.get("activeStudentNum"));
            Integer activeTeacherNum = SafeConverter.toInt(dataMap.get("activeTeacherNum"));
            Integer activeClazzNum = SafeConverter.toInt(dataMap.get("activeClazzNum"));
            Integer totalTeacherNum = SafeConverter.toInt(dataMap.get("totalTeacherNum"));
            //学生活跃率
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                    .loadSchoolExtInfo(RuntimeMode.current().le(Mode.TEST) ? 273997L : schoolId)
                    .getUninterruptibly();
            Integer clazzs = 0;
            if (Objects.nonNull(schoolExtInfo) && Objects.nonNull(schoolExtInfo.getGradeClazzCount())) {
                Map<String, Integer> clazzMap = schoolExtInfo.getGradeClazzCount();
                clazzs = clazzMap.values().stream().mapToInt(Integer::intValue).sum();
            }

            Integer students = 0;
            if (Objects.nonNull(schoolExtInfo) && Objects.nonNull(schoolExtInfo.getGradeStudentCount())) {
                Map<String, Integer> studentMap = schoolExtInfo.getGradeStudentCount();
                students = studentMap.values().stream().mapToInt(Integer::intValue).sum();
            }
            if (RuntimeMode.current().le(Mode.TEST)) {
                students = 600;
            }

            Map<String, Object> data = new LinkedHashMap<>();
            //班级活跃率
            BigDecimal clazzActiveRate = BigDecimal.ZERO;
            if (BigDecimal.ZERO.compareTo(new BigDecimal(clazzs)) != 0) {
                clazzActiveRate = new BigDecimal(activeClazzNum).divide(new BigDecimal(clazzs), 2, RoundingMode.HALF_UP);
            }
            //学生活跃率
            BigDecimal studentActiveRate = BigDecimal.ZERO;
            if (BigDecimal.ZERO.compareTo(new BigDecimal(students)) != 0) {
                studentActiveRate = new BigDecimal(activeStudentNum).divide(new BigDecimal(students), 2, RoundingMode.HALF_UP);
            }
            //老师活跃率
            BigDecimal teacherActiveRate = BigDecimal.ZERO;
            if (BigDecimal.ZERO.compareTo(new BigDecimal(totalTeacherNum)) != 0) {
                teacherActiveRate = new BigDecimal(activeTeacherNum).divide(new BigDecimal(totalTeacherNum), 2, RoundingMode.HALF_UP);
            }
            data.put("activeClazzNum", activeClazzNum);
            data.put("clazzs", clazzs);
            data.put("clazzActiveRate", clazzActiveRate);
            data.put("activeStudentNum", activeStudentNum);
            data.put("students", students);
            data.put("studentActiveRate", studentActiveRate);
            data.put("activeTeacherNum", activeTeacherNum);
            data.put("totalTeacherNum", totalTeacherNum);
            data.put("teacherActiveRate", teacherActiveRate);
            data.put("homeWorkAssignedNum", Objects.isNull(dataMap.get("homeWorkAssignedNum")) ? 0 : dataMap.get("homeWorkAssignedNum"));
            data.put("examParticipateNum", Objects.isNull(dataMap.get("examParticipateNum")) ? 0 : dataMap.get("examParticipateNum"));
            data.put("activityParticpateNum", Objects.isNull(dataMap.get("activityParticipateNum")) ? 0 : dataMap.get("activityParticipateNum"));
            data.put("teachResourceDownloadNum", Objects.isNull(dataMap.get("teachResourceDownNum")) ? 0 : dataMap.get("teachResourceDownNum"));
            return data;
        } else {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> loadHomeWorkData(Integer month, Long schoolId) {
        if (RuntimeMode.current().le(Mode.TEST)) {
            month = 201903;
            schoolId = 172941L;
        }
        List<String> subjects = new LinkedList<>();
        subjects.add("ENGLISH");
        subjects.add("MATH");
        subjects.add("CHINESE");
        MapMessage resMap = loadDubheDataService.loadHomeWorkData(month, schoolId);
        if (resMap.isSuccess() && Objects.nonNull(resMap.get("dataMap"))) {
            List<Map<String, Object>> dataList = new LinkedList<>();
            List<Map<String, Object>> dataMapList = (List<Map<String, Object>>) resMap.get("dataMap");
            for (int i = 0; i < subjects.size(); i++) {
                String subject = subjects.get(i);
                boolean isHaveData = false;
                for (Map<String, Object> temp : dataMapList) {
                    String subjectTemp = (String) temp.get("subject");
                    if (Objects.equals(subject, subjectTemp)) {
                        Map<String, Object> subjectData = new LinkedHashMap<>();
                        Map<String, Object> pieData = JsonUtils.fromJson(SafeConverter.toString(temp.get("homeWorkTypeChartData")));
                        pieData.put("type", "pie");
                        pieData.put("legendData", pieData.get("legenddata"));
                        pieData.put("seriesData", pieData.get("seriesdata"));
                        pieData.remove("legenddata");
                        pieData.remove("seriesdata");

                        Map<String, Object> barData = JsonUtils.fromJson(SafeConverter.toString(temp.get("homeWorkAssignedChartData")));
                        barData.put("type", "bar");
                        List<Integer> seriesData = (List<Integer>) barData.get("seriesdata");
                        Integer max = seriesData.stream().max(Integer::compare).get();
                        barData.put("max", max);
                        barData.put("xAxisData", barData.get("xaxisdata"));
                        barData.put("seriesData", barData.get("seriesdata"));
                        barData.remove("xaxisdata");
                        barData.remove("seriesdata");

                        List<Map<String, Object>> listData = new LinkedList<>();
                        listData.add(pieData);
                        listData.add(barData);
                        subjectData.put("subject", subject);
                        subjectData.put("listData", listData);
                        dataList.add(subjectData);
                        isHaveData = true;
                        break;
                    }
                }

                if (!isHaveData) {
                    Map<String, Object> subjectData = new LinkedHashMap<>();
                    subjectData.put("subject", subject);
                    dataList.add(subjectData);
                }
            }
            return dataList;
        } else {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> loadExamData(Integer month, Long schoolId) {
        if (RuntimeMode.current().le(Mode.TEST)) {
            month = 201903;
            schoolId = 115413L;
        }
        List<String> subjects = new LinkedList<>();
        subjects.add("ENGLISH");
        subjects.add("MATH");
        subjects.add("CHINESE");
        MapMessage resMap = loadDubheDataService.loadExamData(month, schoolId);
        if (resMap.isSuccess() && Objects.nonNull(resMap.get("dataMap"))) {
            List<Map<String, Object>> dataList = new LinkedList<>();
            List<Map<String, Object>> dataMapList = (List<Map<String, Object>>) resMap.get("dataMap");
            for (int i = 0; i < subjects.size(); i++) {
                String subject = subjects.get(i);
                boolean isHaveData = false;
                for (Map<String, Object> temp : dataMapList) {
                    String subjectTemp = (String) temp.get("subject");
                    if (Objects.equals(subject, subjectTemp)) {
                        Map<String, Object> subjectData = new LinkedHashMap<>();
                        Map<String, Object> pieData = JsonUtils.fromJson(SafeConverter.toString(temp.get("examRegionLevelData")));
                        pieData.put("type", "pie");
                        pieData.put("legendData", pieData.get("legenddata"));
                        pieData.put("seriesData", pieData.get("seriesdata"));
                        pieData.remove("legenddata");
                        pieData.remove("seriesdata");

                        Map<String, Object> barData = JsonUtils.fromJson(SafeConverter.toString(temp.get("examParticipateData")));
                        barData.put("type", "flow");
                        barData.put("xAxisData", barData.get("xaxisdata"));
                        barData.put("yAxisData", barData.get("yaxisdata"));
                        barData.put("seriesData", barData.get("seriesdata"));
                        List<Map<String, Object>> seriesData = (List<Map<String, Object>>) barData.get("seriesdata");
                        List<Map<String, Object>> yAxisData = (List<Map<String, Object>>) barData.get("yAxisData");
                        for (int j = 0; j < seriesData.size(); j++) {
                            Map<String, Object> tempSer = seriesData.get(j);
                            Map<String, Object> tempYax = yAxisData.get(j);
                            List<Integer> data = (List<Integer>) tempSer.get("data");
                            Integer max = data.stream().max(Integer::compare).get();
                            tempYax.put("max", max);
                        }

                        barData.remove("xaxisdata");
                        barData.remove("yaxisdata");
                        barData.remove("seriesdata");

                        List<Map<String, Object>> listData = new LinkedList<>();
                        listData.add(pieData);
                        listData.add(barData);
                        subjectData.put("subject", subject);
                        subjectData.put("listData", listData);
                        String examNoNameListStr = SafeConverter.toString(temp.get("examNoNameList"));
                        List examNoNameListMap = JsonUtils.fromJsonToList(examNoNameListStr, Map.class);
                        subjectData.put("examNoNameList", examNoNameListMap);
                        dataList.add(subjectData);
                        isHaveData = true;
                        break;
                    }
                }

                if (!isHaveData) {
                    Map<String, Object> subjectData = new LinkedHashMap<>();
                    subjectData.put("subject", subject);
                    dataList.add(subjectData);
                }
            }
            return dataList;
        } else {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> loadActivityData(Integer month, Long schoolId) {
        if (RuntimeMode.current().le(Mode.TEST)) {
            month = 201903;
            schoolId = 113088L;
        }
        List<String> subjects = new LinkedList<>();
        subjects.add("ENGLISH");
        subjects.add("MATH");
        subjects.add("CHINESE");
        MapMessage resMap = loadDubheDataService.loadActivityData(month, schoolId);
        if (resMap.isSuccess() && Objects.nonNull(resMap.get("dataMap"))) {
            List<Map<String, Object>> dataList = new LinkedList<>();
            List<Map<String, Object>> dataMapList = (List<Map<String, Object>>) resMap.get("dataMap");
            for (int i = 0; i < subjects.size(); i++) {
                String subject = subjects.get(i);
                boolean isHaveData = false;
                for (Map<String, Object> temp : dataMapList) {
                    String subjectTemp = (String) temp.get("subject");
                    if (Objects.equals(subject, subjectTemp)) {
                        Map<String, Object> subjectData = new LinkedHashMap<>();
                        Map<String, Object> pieData = JsonUtils.fromJson(SafeConverter.toString(temp.get("activityTypeData")));
                        pieData.put("type", "pie");
                        pieData.put("legendData", pieData.get("legenddata"));
                        pieData.put("seriesData", pieData.get("seriesdata"));
                        pieData.remove("legenddata");
                        pieData.remove("seriesdata");

                        Map<String, Object> barData = JsonUtils.fromJson(SafeConverter.toString(temp.get("activityParticipateData")));
                        barData.put("type", "flow");
                        barData.put("xAxisData", barData.get("xaxisdata"));
                        barData.put("yAxisData", barData.get("yaxisdata"));
                        barData.put("seriesData", barData.get("seriesdata"));
                        List<Map<String, Object>> seriesData = (List<Map<String, Object>>) barData.get("seriesdata");
                        List<Map<String, Object>> yAxisData = (List<Map<String, Object>>) barData.get("yAxisData");
                        for (int j = 0; j < seriesData.size(); j++) {
                            Map<String, Object> tempSer = seriesData.get(j);
                            Map<String, Object> tempYax = yAxisData.get(j);
                            List<Integer> data = (List<Integer>) tempSer.get("data");
                            Integer max = data.stream().max(Integer::compare).get();
                            tempYax.put("max", max);
                        }

                        barData.remove("xaxisdata");
                        barData.remove("yaxisdata");
                        barData.remove("seriesdata");

                        List<Map<String, Object>> listData = new LinkedList<>();
                        listData.add(pieData);
                        listData.add(barData);
                        subjectData.put("subject", subject);
                        subjectData.put("listData", listData);
                        dataList.add(subjectData);
                        isHaveData = true;
                        break;
                    }
                }

                if (!isHaveData) {
                    Map<String, Object> subjectData = new LinkedHashMap<>();
                    subjectData.put("subject", subject);
                    dataList.add(subjectData);
                }
            }
            return dataList;
        } else {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> loadTeachResourceData(Integer month, Long schoolId) {
        if (RuntimeMode.current().le(Mode.TEST)) {
            month = 201903;
            schoolId = 100210L;
        }
        List<String> subjects = new LinkedList<>();
        subjects.add("ENGLISH");
        subjects.add("MATH");
        subjects.add("CHINESE");
        MapMessage resMap = loadDubheDataService.loadTeachResourceData(month, schoolId);
        if (resMap.isSuccess() && Objects.nonNull(resMap.get("dataMap"))) {
            List<Map<String, Object>> dataList = new LinkedList<>();
            List<Map<String, Object>> dataMapList = (List<Map<String, Object>>) resMap.get("dataMap");
            for (int i = 0; i < subjects.size(); i++) {
                String subject = subjects.get(i);
                boolean isHaveData = false;
                for (Map<String, Object> temp : dataMapList) {
                    String subjectTemp = (String) temp.get("subject");
                    if (Objects.equals(subject, subjectTemp)) {
                        Map<String, Object> subjectData = new LinkedHashMap<>();
                        Map<String, Object> pieData = JsonUtils.fromJson(SafeConverter.toString(temp.get("teachResourceTypeData")));
                        pieData.put("type", "pie");
                        pieData.put("legendData", pieData.get("legenddata"));
                        pieData.put("seriesData", pieData.get("seriesdata"));
                        pieData.remove("legenddata");
                        pieData.remove("seriesdata");

                        Map<String, Object> barData = JsonUtils.fromJson(SafeConverter.toString(temp.get("teachResourceDownData")));
                        barData.put("type", "colorBar");
                        barData.put("legendData", barData.get("legenddata"));
                        barData.put("yAxisData", barData.get("yaxisdata"));
                        barData.put("seriesData", barData.get("seriesdata"));
                        List<Map<String, Object>> seriesData = (List<Map<String, Object>>) barData.get("seriesdata");
                        List<Integer> maxList = new LinkedList<>();
                        for (Map<String, Object> dataTemp : seriesData) {
                            List<Integer> data = (List<Integer>) dataTemp.get("data");
                            Integer sum = data.stream().mapToInt((x) -> x).sum();
                            maxList.add(sum);
                        }

                        barData.remove("legenddata");
                        barData.remove("yaxisdata");
                        barData.remove("seriesdata");

                        List<Map<String, Object>> listData = new LinkedList<>();
                        listData.add(pieData);
                        listData.add(barData);
                        subjectData.put("subject", subject);
                        subjectData.put("listData", listData);
                        Integer max = maxList.stream().max(Integer::compare).get();
                        subjectData.put("max", max);
                        dataList.add(subjectData);
                        isHaveData = true;
                        break;
                    }
                }

                if (!isHaveData) {
                    Map<String, Object> subjectData = new LinkedHashMap<>();
                    subjectData.put("subject", subject);
                    dataList.add(subjectData);
                }
            }
            return dataList;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> loadLearningSkillsData(Long schoolId, Integer regionCode, String subject, String grade, String schoolYear, String term) {
        MapMessage resMap = null;
        if (RuntimeMode.current().le(Mode.TEST)) {
            resMap = loadPrdDataService.loadLearningSkill(127192L, 110101, "ENGLISH", "2", "36768465", "2018", "LAST_TERM");
        } else {
            resMap = loadPrdDataService.loadLearningSkill(schoolId, regionCode, subject, grade, "0", schoolYear, term);
        }
        List<Map<String, Object>> skillDataList = (List<Map<String, Object>>) resMap.get("dataMap");
        if (resMap.isSuccess() && Objects.nonNull(skillDataList) && CollectionUtils.isNotEmpty(skillDataList)) {
            Map<String, Object> skillDateMap = skillDataList.get(0);

            List<String> legendData = new ArrayList<>();
            List<String> skillNames = new ArrayList<>();
            List<Map<String, Object>> indicatorDatas = new LinkedList<>();
            List<Map<String, Object>> seriesData = new LinkedList<>();

            String nationSkillRightRate = (String) skillDateMap.get("nationSkillRightRate");
            List<Map> nationSkillList = JsonUtils.fromJsonToList(nationSkillRightRate, Map.class);
            Map<String, Object> nationDatasMap = new LinkedHashMap<>();
            List<Double> nationDatas = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(nationSkillList)) {
                for (Map map : nationSkillList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);

                        skillNames.add(key.split("-*-")[2]);
                        Map<String, Object> indicatorData = new LinkedHashMap<>();
                        indicatorData.put("name", key.split("-*-")[2]);
                        indicatorData.put("max", 100);
                        indicatorDatas.add(indicatorData);
                        nationDatas.add(value);
                    }
                }
            }
            nationDatasMap.put("name", "全国");
            nationDatasMap.put("data", nationDatas);
            seriesData.add(nationDatasMap);
            legendData.add("全国");

            String areaSkillRightRate = (String) skillDateMap.get("areaSkillRightRate");
            List<Map> areaSkillList = JsonUtils.fromJsonToList(areaSkillRightRate, Map.class);
            Map<String, Double> areaSkillMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(areaSkillList)) {
                for (Map map : areaSkillList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        areaSkillMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String schoolSkillRightRate = (String) skillDateMap.get("schoolSkillRightRate");
            List<Map> schoolSkillList = JsonUtils.fromJsonToList(schoolSkillRightRate, Map.class);
            Map<String, Double> schoolSkillMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(schoolSkillList)) {
                for (Map map : schoolSkillList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        schoolSkillMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            List<Double> areaDatas = new LinkedList<>();
            List<Double> schoolDatas = new LinkedList<>();
            for (String skill : skillNames) {
                Double ad = areaSkillMap.get(skill);
                if (Objects.isNull(ad)) {
                    areaDatas.add(0d);
                } else {
                    areaDatas.add(ad);
                }

                Double sd = schoolSkillMap.get(skill);
                if (Objects.isNull(sd)) {
                    schoolDatas.add(0d);
                } else {
                    schoolDatas.add(sd);
                }
            }

            Map<String, Object> areaDatasMap = new LinkedHashMap<>();
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            areaDatasMap.put("name", exRegion.getCountyName());
            areaDatasMap.put("data", areaDatas);
            seriesData.add(areaDatasMap);
            legendData.add(exRegion.getCountyName());

            School school = raikouSystem.loadSchool(schoolId);
            Map<String, Object> schoolDatasMap = new LinkedHashMap<>();
            schoolDatasMap.put("name", school.getShortName());
            schoolDatasMap.put("data", schoolDatas);
            seriesData.add(schoolDatasMap);
            legendData.add(school.getShortName());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("legendData", legendData);
            data.put("indicatorData", indicatorDatas);
            data.put("legendData", seriesData);
            return data;
        } else {
            return null;
        }
    }

    @Override
    public Map<String, Object> loadKnowledgeModuleData(Long schoolId, Integer regionCode, String subject, String grade, String schoolYear, String term) {
        MapMessage resMap = null;
        if (RuntimeMode.current().le(Mode.TEST)) {
            resMap = loadPrdDataService.loadKnowledgeModule(127192L, 110101, "ENGLISH", "2", "36768465", "2018", "LAST_TERM");
        } else {
            resMap = loadPrdDataService.loadKnowledgeModule(schoolId, regionCode, subject, grade, "0", schoolYear, term);
        }
        List<Map<String, Object>> knowledgeDataList = (List<Map<String, Object>>) resMap.get("dataMap");
        if (resMap.isSuccess() && CollectionUtils.isNotEmpty(knowledgeDataList)) {
            Map<String, Object> knowledgeMap = knowledgeDataList.get(0);

            List<String> legendData = new ArrayList<>();
            List<String> knowledgeNames = new ArrayList<>();
            List<Map<String, Object>> indicatorDatas = new LinkedList<>();
            List<Map<String, Object>> seriesData = new LinkedList<>();

            String nationKnowledgeModuleRightRate = (String) knowledgeMap.get("nationKnowledgeModuleRightRate");
            List<Map> nationKnowledgeList = JsonUtils.fromJsonToList(nationKnowledgeModuleRightRate, Map.class);
            List<Double> nationDatas = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(nationKnowledgeList)) {
                for (Map map : nationKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);

                        knowledgeNames.add(key.split("-*-")[2]);
                        Map<String, Object> indicatorData = new LinkedHashMap<>();
                        indicatorData.put("name", key.split("-*-")[2]);
                        indicatorData.put("max", 100);
                        indicatorDatas.add(indicatorData);
                        nationDatas.add(value);
                    }
                }
            }

            String areaKnowledgeModuleRightRate = (String) knowledgeMap.get("areaKnowledgeModuleRightRate");
            List<Map> areaKnowledgeList = JsonUtils.fromJsonToList(areaKnowledgeModuleRightRate, Map.class);
            Map<String, Double> areaKnowledgeMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(areaKnowledgeList)) {
                for (Map map : areaKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        areaKnowledgeMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String schoolKnowledgeModuleRightRate = (String) knowledgeMap.get("schoolKnowledgeModuleRightRate");
            List<Map> schoolKnowledgeList = JsonUtils.fromJsonToList(schoolKnowledgeModuleRightRate, Map.class);
            Map<String, Double> schoolKnowledgeMap = new LinkedHashMap<>();
            if (CollectionUtils.isNotEmpty(schoolKnowledgeList)) {
                for (Map map : schoolKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        schoolKnowledgeMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            List<Double> areaDatas = new LinkedList<>();
            List<Double> schoolDatas = new LinkedList<>();
            for (String knowledge : knowledgeNames) {
                Double ad = areaKnowledgeMap.get(knowledge);
                if (Objects.isNull(ad)) {
                    areaDatas.add(0d);
                } else {
                    areaDatas.add(ad);
                }

                Double sd = schoolKnowledgeMap.get(knowledge);
                if (Objects.isNull(sd)) {
                    schoolDatas.add(0d);
                } else {
                    schoolDatas.add(sd);
                }
            }

            Map<String, Object> areaDatasMap = new LinkedHashMap<>();
            ExRegion exRegion = raikouSystem.loadRegion(regionCode);
            areaDatasMap.put("name", exRegion.getCountyName());
            areaDatasMap.put("data", areaDatas);
            seriesData.add(areaDatasMap);
            legendData.add(exRegion.getCountyName());

            School school = raikouSystem.loadSchool(schoolId);
            Map<String, Object> schoolDatasMap = new LinkedHashMap<>();
            schoolDatasMap.put("name", school.getShortName());
            schoolDatasMap.put("data", schoolDatas);
            seriesData.add(schoolDatasMap);
            legendData.add(school.getShortName());

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("legendData", legendData);
            data.put("indicatorData", indicatorDatas);
            data.put("legendData", seriesData);
            return data;
        } else {
            return null;
        }
    }
}
