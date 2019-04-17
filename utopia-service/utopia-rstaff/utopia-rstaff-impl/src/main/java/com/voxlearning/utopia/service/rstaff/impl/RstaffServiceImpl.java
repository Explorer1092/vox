package com.voxlearning.utopia.service.rstaff.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.util.CollectionUtils;
import com.voxlearning.athena.api.LoadPrdDataService;
import com.voxlearning.athena.bean.PrdResearchStaffHomeworkData;
import com.voxlearning.athena.bean.PrdResearchStaffSummaryData;
import com.voxlearning.athena.bean.PrdSchoolMasterUnitData;
import com.voxlearning.utopia.service.rstaff.api.RstaffService;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Getter;

import javax.inject.Named;
import java.util.*;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-06-19 11:31
 */
@Named
@Service(interfaceClass = RstaffService.class)
@ExposeService(interfaceClass = RstaffService.class)
public class RstaffServiceImpl extends SpringContainerSupport implements RstaffService {

    @Getter
    @ImportService(interfaceClass = LoadPrdDataService.class)
    private LoadPrdDataService loadPrdDataService;



    @Override
    public Map<String, Object> loadResearchUsageData(List<Long> schoolIds, List<Integer> regionCodes, List<Integer> cityCodes, String subject, String month) {
        MapMessage mapMessage = loadPrdDataService.loadResearchUsageData(schoolIds,regionCodes,cityCodes,subject,month);
        boolean isSuccess = (boolean) mapMessage.get("success");
        List<PrdResearchStaffSummaryData> summaryDataL = (List<PrdResearchStaffSummaryData>) mapMessage.get("dataMap");
        Long usageTeachersSum = 0L;
        Long usageStudentsSum = 0L;
        Long increaseTeachersSum = 0L;
        Long increaseStudentsSum = 0L;
        for(PrdResearchStaffSummaryData staffSummaryData : summaryDataL){
            Long usageTeachers = staffSummaryData.getUsageTeachers();
            if(usageTeachers != null){
                usageTeachersSum += usageTeachers;
            }
            Long usageStudents = staffSummaryData.getUsageStudents();
            if(usageStudents != null){
                usageStudentsSum += usageStudents;
            }
            Long increaseTeachers = staffSummaryData.getIncreaseTeachers();
            if(increaseTeachers != null){
                increaseTeachersSum += increaseTeachers;
            }
            Long increaseStudents = staffSummaryData.getIncreaseStudents();
            if(increaseStudents != null){
                increaseStudentsSum += increaseStudents;
            }
        }
        Map<String,Object> data = new LinkedHashMap<>();
        data.put("usageTeachers",usageTeachersSum);
        data.put("usageStudents",usageStudentsSum);
        data.put("increaseTeachers",increaseTeachersSum);
        data.put("increaseStudents",increaseStudentsSum);
        if(isSuccess){
            return data;
        }
        return null;
    }

    @Override
    public Map<String, Object> loadResearchHomework(Map<Long,School> schoolIds, List<Integer> regionCodes, List<Integer> cityCodes, String grade, String subject, String month) {
        MapMessage mapMessage = loadPrdDataService.loadResearchHomework(schoolIds.keySet(),regionCodes,cityCodes,subject,month,grade);
        boolean isSuccess = (boolean) mapMessage.get("success");
        List<PrdResearchStaffHomeworkData> homeworkDataL = (List<PrdResearchStaffHomeworkData>) mapMessage.get("dataMap");
        Map<String,Integer> homeworkScenes = new LinkedHashMap<>();
        Map<String,Integer> clazzDoHomeworkCounts = new LinkedHashMap<>();
        Map<String,Integer> assignmentCounts = new LinkedHashMap<>();
        Map<String,Map<String,Integer>> weekHomeworkScenes = new LinkedHashMap<>();
        //根据不同的code返回的数据格式不一样
        if(CollectionUtils.isNotEmpty(schoolIds.keySet())){
            //场景和其对应的数据
            for(PrdResearchStaffHomeworkData homeworkData : homeworkDataL) {
                if(homeworkData.getHomeworkScenes() != null){
                    List<Map> tempHomeworkScenes = JsonUtils.fromJsonToList(homeworkData.getHomeworkScenes(), Map.class);
                    buildMap(homeworkScenes, tempHomeworkScenes);
                }

                if(homeworkData.getClazzDoHomeworkCounts() != null){
                    Integer doHomeworkCounts = SafeConverter.toInt(homeworkData.getClazzDoHomeworkCounts());
                    School school = schoolIds.get(homeworkData.getSchoolId());
                    clazzDoHomeworkCounts.put(school.getCname(),doHomeworkCounts);
                }

                if(homeworkData.getAssignmentCounts() != null){
                    List<Map> tempAssignmentCounts = JsonUtils.fromJsonToList(homeworkData.getAssignmentCounts(),Map.class);
                    Map<String,Integer> assignmentCountMap = new LinkedHashMap<>();
                    buildMap(assignmentCountMap, tempAssignmentCounts);
                    School school = schoolIds.get(homeworkData.getSchoolId());
                    for (Map.Entry<String, Integer> entry : assignmentCountMap.entrySet()) {
                        String key = entry.getKey();
                        assignmentCounts.put(key+"-*-"+school.getCname(),entry.getValue());
                    }
                }

                if(homeworkData.getWeekHomeworkScenes() != null){
                    List<Map> tempWeekHomeworkScenes = JsonUtils.fromJsonToList(homeworkData.getWeekHomeworkScenes(),Map.class);
                    buildWeekHomeWorkScenes(weekHomeworkScenes, tempWeekHomeworkScenes);
                }
            }
        }else if(CollectionUtils.isNotEmpty(regionCodes)){
            //每个区的数据就是所有学校的汇总数据
            for(PrdResearchStaffHomeworkData homeworkData : homeworkDataL) {
                if(homeworkData.getSchoolId() == -1){
                    if(homeworkData.getHomeworkScenes() != null){
                        List<Map> tempHomeworkScenes = JsonUtils.fromJsonToList(homeworkData.getHomeworkScenes(), Map.class);
                        buildMap(homeworkScenes, tempHomeworkScenes);
                    }

                    if(homeworkData.getClazzDoHomeworkCounts() != null){
                        List<Map> tempClazzDoHomeworkCounts = JsonUtils.fromJsonToList(homeworkData.getClazzDoHomeworkCounts(), Map.class);
                        if(CollectionUtils.isNotEmpty(tempClazzDoHomeworkCounts)){
                            for(Map<String,Integer> temp : tempClazzDoHomeworkCounts){
                                clazzDoHomeworkCounts.putAll(temp);
                            }
                        }
                    }

                    if(homeworkData.getAssignmentCounts() != null){
                        List<Map> tempAssignmentCounts = JsonUtils.fromJsonToList(homeworkData.getAssignmentCounts(),Map.class);
                        if(CollectionUtils.isNotEmpty(tempAssignmentCounts)){
                            for(Map<String,Integer> temp : tempAssignmentCounts){
                                assignmentCounts.putAll(temp);
                            }
                        }
                    }

                    if(homeworkData.getWeekHomeworkScenes() != null){
                        List<Map> tempWeekHomeworkScenes = JsonUtils.fromJsonToList(homeworkData.getWeekHomeworkScenes(),Map.class);
                        buildWeekHomeWorkScenes(weekHomeworkScenes, tempWeekHomeworkScenes);
                    }
                }
            }
        }else if(CollectionUtils.isNotEmpty(cityCodes)){
            //每个市显示的是市下面每个区的学校排名
            for(PrdResearchStaffHomeworkData homeworkData : homeworkDataL) {
                if(homeworkData.getSchoolId() == -1){
                    if (homeworkData.getHomeworkScenes() != null) {
                        List<Map> tempHomeworkScenes = JsonUtils.fromJsonToList(homeworkData.getHomeworkScenes(), Map.class);
                        buildMap(homeworkScenes, tempHomeworkScenes);
                    }

                    if(homeworkData.getClazzDoHomeworkCounts() != null){
                        List<Map> tempClazzDoHomeworkCounts = JsonUtils.fromJsonToList(homeworkData.getClazzDoHomeworkCounts(), Map.class);
                        if(CollectionUtils.isNotEmpty(tempClazzDoHomeworkCounts)){
                            for(Map<String,Integer> temp : tempClazzDoHomeworkCounts){
                                clazzDoHomeworkCounts.putAll(temp);
                            }
                        }
                    }

                    if(homeworkData.getAssignmentCounts() != null){
                        List<Map> tempAssignmentCounts = JsonUtils.fromJsonToList(homeworkData.getAssignmentCounts(),Map.class);
                        if(CollectionUtils.isNotEmpty(tempAssignmentCounts)){
                            for(Map<String,Integer> temp : tempAssignmentCounts){
                                assignmentCounts.putAll(temp);
                            }
                        }
                    }

                    if(homeworkData.getWeekHomeworkScenes() != null){
                        List<Map> tempWeekHomeworkScenes = JsonUtils.fromJsonToList(homeworkData.getWeekHomeworkScenes(),Map.class);
                        buildWeekHomeWorkScenes(weekHomeworkScenes, tempWeekHomeworkScenes);
                    }
                }
            }
        }

        Map<String,Object> data = new LinkedHashMap<>();
        data.put("homeworkScenes",homeworkScenes);
        data.put("weekHomeworkScenes",weekHomeworkScenes);
        data.put("clazzDoHomeworkCounts",clazzDoHomeworkCounts);
        data.put("assignmentCounts",assignmentCounts);

        if(isSuccess){
            return data;
        }
        return null;
    }

    private void buildWeekHomeWorkScenes(Map<String, Map<String, Integer>> weekHomeworkScenes, List<Map> tempWeekHomeworkScenes) {
        if(CollectionUtils.isNotEmpty(tempWeekHomeworkScenes)){
            for(Map<String,List<Map<String,Integer>>> temp : tempWeekHomeworkScenes){
                Iterator<String> tempIt = temp.keySet().iterator();
                while(tempIt.hasNext()){
                    String week = tempIt.next();
                    Map<String,Integer> scenesCountMap = weekHomeworkScenes.get(week);
                    if(scenesCountMap == null){
                        scenesCountMap = new LinkedHashMap<>();
                        weekHomeworkScenes.put(week,scenesCountMap);
                    }
                    List<Map<String,Integer>> val = temp.get(week);
                    for(Map<String,Integer> singleScenesMap : val){
                        String tempScenenName = singleScenesMap.keySet().iterator().next();
                        Integer tempCounts = singleScenesMap.get(tempScenenName);
                        Integer counts = scenesCountMap.get(tempScenenName);
                        if(counts == null){
                            counts = tempCounts;
                        }else{
                            counts += tempCounts;
                        }
                        scenesCountMap.put(tempScenenName,counts);
                    }
                }
            }
        }
    }

    private void buildMap(Map<String, Integer> assignmentCounts, List<Map> tempAssignmentCounts) {
        if(CollectionUtils.isNotEmpty(tempAssignmentCounts)){
            for (Map<String,Integer> tempMap : tempAssignmentCounts) {
                Iterator<String> tempAsigmentMapIt = tempMap.keySet().iterator();
                while(tempAsigmentMapIt.hasNext()){
                    String teacherName = tempAsigmentMapIt.next();
                    Integer tempAssignmentCount = tempMap.get(teacherName);
                    Integer scenesCount = assignmentCounts.get(teacherName);
                    if(scenesCount == null){
                        scenesCount = tempAssignmentCount;
                    }else{
                        scenesCount += tempAssignmentCount;
                    }
                    assignmentCounts.put(teacherName,scenesCount);
                }
            }
        }
    }

    @Override
    public Map<String, Object> loadResearchUnitAvgQuestions(Long schoolId, Integer regionCode, String subject, String grade, String clazz, String schoolYear, String term) {
        MapMessage mapMessage = loadPrdDataService.loadResearchUnitAvgQuestions(schoolId,regionCode,subject,grade,clazz,schoolYear,term);
        boolean isSuccess = (boolean) mapMessage.get("success");
        List<PrdSchoolMasterUnitData> unitDataList = (List<PrdSchoolMasterUnitData>) mapMessage.get("dataMap");
        Map<String,Object> data = new LinkedHashMap<>();
        if(isSuccess && !"{}".equals(mapMessage.get("dataMap"))){
            if(CollectionUtils.isNotEmpty(unitDataList)){
                PrdSchoolMasterUnitData summaryData = unitDataList.get(0);
                List<Map> tempUnitAvgQuestions = JsonUtils.fromJsonToList(summaryData.getUnitAvgQuestions(),Map.class);
                Map<String,Double> unitAvgQuestions = new LinkedHashMap<>();
                if(CollectionUtils.isNotEmpty(tempUnitAvgQuestions)){
                    for(Map<String,Double> temp : tempUnitAvgQuestions){
                        unitAvgQuestions.putAll(temp);
                    }
                }

                List<Map> tempAreaUnitAvgQuestions = JsonUtils.fromJsonToList(summaryData.getAreaUnitAvgQuestions(),Map.class);
                Map<String,Double> areaUnitAvgQuestions = new LinkedHashMap<>();
                if(CollectionUtils.isNotEmpty(tempAreaUnitAvgQuestions)) {
                    for (Map<String, Double> temp : tempAreaUnitAvgQuestions) {
                        areaUnitAvgQuestions.putAll(temp);
                    }
                }

                List<Map> tempUnitQuestionRightRatio = JsonUtils.fromJsonToList(summaryData.getUnitQuestionRightRatio(),Map.class);
                Map<String,Double> unitQuestionRightRatio = new LinkedHashMap<>();
                if(CollectionUtils.isNotEmpty(tempUnitQuestionRightRatio)) {
                    for (Map<String, Double> temp : tempUnitQuestionRightRatio) {
                        unitQuestionRightRatio.putAll(temp);
                    }
                }

                List<Map> tempAreaUnitQuestionRightRatio = JsonUtils.fromJsonToList(summaryData.getAreaUnitQuestionRightRatio(),Map.class);
                Map<String,Double> areaUnitQuestionRightRatio = new LinkedHashMap<>();
                if(CollectionUtils.isNotEmpty(tempAreaUnitQuestionRightRatio)) {
                    for (Map<String, Double> temp : tempAreaUnitQuestionRightRatio) {
                        areaUnitQuestionRightRatio.putAll(temp);
                    }
                }
                data.put("bookId",summaryData.getBookId());
                data.put("unitAvgQuestions",unitAvgQuestions);
                data.put("areaUnitAvgQuestions",areaUnitAvgQuestions);
                data.put("unitQuestionRightRatio",unitQuestionRightRatio);
                data.put("areaUnitQuestionRightRatio",areaUnitQuestionRightRatio);
                return data;
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    @Override
    public Map<String, Object> loadLearningSkills(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear, String term) {
        MapMessage mapMessage = loadPrdDataService.loadLearningSkill(schoolId, areaId, subject, grade, clazz, schoolYear, term);
        boolean isSuccess = (boolean) mapMessage.get("success");
        List<Map<String,Object>> skillDataList = (List<Map<String,Object>>) mapMessage.get("dataMap");
        Map<String,Object> data = new LinkedHashMap<>();
        if(isSuccess && CollectionUtils.isNotEmpty(skillDataList)){
            Map<String,Object> skillDateMap = skillDataList.get(0);
            String schoolSkillRightRate = (String) skillDateMap.get("schoolSkillRightRate");
            List<Map> schoolSkillList = JsonUtils.fromJsonToList(schoolSkillRightRate,Map.class);
            Map<String,Double> schoolSkillMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(schoolSkillList)) {
                for (Map map : schoolSkillList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        schoolSkillMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String areaSkillRightRate = (String) skillDateMap.get("areaSkillRightRate");
            List<Map> areaSkillList = JsonUtils.fromJsonToList(areaSkillRightRate,Map.class);
            Map<String,Double> areaSkillMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(areaSkillList)) {
                for (Map map : areaSkillList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        areaSkillMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String nationSkillRightRate = (String) skillDateMap.get("nationSkillRightRate");
            List<Map> nationSkillList = JsonUtils.fromJsonToList(nationSkillRightRate,Map.class);
            Map<String,Double> nationSkillMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(nationSkillList)) {
                for (Map map : nationSkillList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        nationSkillMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            data.put("schoolSkillRightRates",schoolSkillMap);
            data.put("areaSkillRightRates",areaSkillMap);
            data.put("nationSkillRightRates",nationSkillMap);
            return data;
        }else{
            return null;
        }
    }

    @Override
    public Map<String, Object> loadKnowledgeModule(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear, String term,String knowledgeModuleLevel) {
        MapMessage mapMessage = loadPrdDataService.loadKnowledgeModule(schoolId, areaId, subject, grade, clazz, schoolYear, term);
        boolean isSuccess = (boolean) mapMessage.get("success");
        List<Map<String,Object>> knowledgeDataList = (List<Map<String,Object>>) mapMessage.get("dataMap");
        Map<String,Object> data = new LinkedHashMap<>();
        if(isSuccess && CollectionUtils.isNotEmpty(knowledgeDataList)){
            Map<String,Object> knowledgeMap = knowledgeDataList.get(0);
            String schoolKnowledgeModuleRightRate = (String) knowledgeMap.get("schoolKnowledgeModuleRightRate");
            List<Map> schoolKnowledgeList = JsonUtils.fromJsonToList(schoolKnowledgeModuleRightRate,Map.class);
            Map<String,Double> schoolKnowledgeMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(schoolKnowledgeList)) {
                for (Map map : schoolKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        schoolKnowledgeMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String schoolSecondKnowledgeModuleRightRate = (String) knowledgeMap.get("schoolSecondKnowledgeModuleRightRate");
            List<Map> schoolSecondKnowledgeList = JsonUtils.fromJsonToList(schoolSecondKnowledgeModuleRightRate,Map.class);
            Map<String,Double> schoolSecondKnowledgeMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(schoolSecondKnowledgeList)) {
                for (Map map : schoolSecondKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        schoolSecondKnowledgeMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String areaKnowledgeModuleRightRate = (String) knowledgeMap.get("areaKnowledgeModuleRightRate");
            List<Map> areaKnowledgeList = JsonUtils.fromJsonToList(areaKnowledgeModuleRightRate,Map.class);
            Map<String,Double> areaKnowledgeMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(areaKnowledgeList)) {
                for (Map map : areaKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        areaKnowledgeMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String areaSecondKnowledgeModuleRightRate = (String) knowledgeMap.get("areaSecondKnowledgeModuleRightRate");
            List<Map> areaSecondKnowledgeList = JsonUtils.fromJsonToList(areaSecondKnowledgeModuleRightRate,Map.class);
            Map<String,Double> areaSecondKnowledgeMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(areaSecondKnowledgeList)) {
                for (Map map : areaSecondKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        areaSecondKnowledgeMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String nationKnowledgeModuleRightRate = (String) knowledgeMap.get("nationKnowledgeModuleRightRate");
            List<Map> nationKnowledgeList = JsonUtils.fromJsonToList(nationKnowledgeModuleRightRate,Map.class);
            Map<String,Double> nationKnowledgeMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(nationKnowledgeList)) {
                for (Map map : nationKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        nationKnowledgeMap.put(key.split("-*-")[2], value);
                    }
                }
            }

            String nationSecondKnowledgeModuleRightRate = (String) knowledgeMap.get("nationSecondKnowledgeModuleRightRate");
            List<Map> nationSecondKnowledgeList = JsonUtils.fromJsonToList(nationSecondKnowledgeModuleRightRate,Map.class);
            Map<String,Double> nationSecondKnowledgeMap = new LinkedHashMap<>();
            if(CollectionUtils.isNotEmpty(nationSecondKnowledgeList)) {
                for (Map map : nationSecondKnowledgeList) {
                    Iterator it = map.keySet().iterator();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        Double value = (Double) map.get(key);
                        nationSecondKnowledgeMap.put(key.split("-*-")[2], value);
                    }
                }
            }
            if(Objects.equals("1",knowledgeModuleLevel)){
                data.put("schoolKnowledgeRightRates",schoolKnowledgeMap);
                data.put("areaKnowledgeRightRates",areaKnowledgeMap);
                data.put("nationKnowledgeRightRates",nationKnowledgeMap);
            }else{
                data.put("schoolKnowledgeRightRates",schoolSecondKnowledgeMap);
                data.put("areaKnowledgeRightRates",areaSecondKnowledgeMap);
                data.put("nationKnowledgeRightRates",nationSecondKnowledgeMap);
            }
            return data;
        }
        return null;
    }
}
