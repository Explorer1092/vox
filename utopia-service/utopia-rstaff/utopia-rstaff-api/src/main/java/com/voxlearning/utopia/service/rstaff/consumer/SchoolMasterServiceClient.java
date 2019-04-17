package com.voxlearning.utopia.service.rstaff.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.rstaff.api.MockDataService;
import com.voxlearning.utopia.service.rstaff.api.RstaffService;
import com.voxlearning.utopia.service.rstaff.api.SchoolMasterService;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-06-19 12:08
 */

public class SchoolMasterServiceClient {

    @Getter
    @ImportService(interfaceClass = SchoolMasterService.class)
    private SchoolMasterService schoolMasterService;

    @Getter
    @ImportService(interfaceClass = RstaffService.class)
    private RstaffService rstaffService;

    @Getter
    @ImportService(interfaceClass = MockDataService.class)
    private MockDataService mockDataService;

    public Map<String, Object> loadSchoolUsageData(Long schoolId, String month){
        Map<String,Object> data = schoolMasterService.loadSchoolUsageData(schoolId,month);
        return data;
    }


    public Map<String,Object> loadHomework(Long schoolId, String grade, String subject, String dateStr) {
        Map<String,Object> data = schoolMasterService.loadHomework(schoolId,grade,subject,dateStr);
        return data;
    }


    public Map<String,Object> loadUnitAvgQuestions(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear ,String term) {
        Map<String,Object> data = schoolMasterService.loadUnitAvgQuestions(areaId, schoolId, subject, grade, clazz, schoolYear, term);
        return data;
    }

    public Map<String,Object>  loadResearchUsageData(List<Long> schoolIds, List<Integer> regionCodes, List<Integer> cityCodes, String subject, String month) {
        Map<String,Object> data = rstaffService.loadResearchUsageData(schoolIds,regionCodes,cityCodes,subject,month);
        return data;
    }

    public Map<String, Object> loadResearchHomework(Map<Long,School> schoolIds, List<Integer> regionCodes, List<Integer> cityCodes, String grade, String subject, String month){
        Map<String,Object> data = rstaffService.loadResearchHomework(schoolIds,regionCodes,cityCodes,grade,subject,month);
        return data;
    }

    public Map<String, Object> loadResearchUnitAvgQuestions(Long schoolId, Integer regionCode, String subject, String grade, String clazz, String schoolYear, String term) {
        Map<String,Object> data = rstaffService.loadResearchUnitAvgQuestions(schoolId,regionCode,subject,grade,clazz,schoolYear,term);
        return data;
    }

    public Map<String,Object> loadLearningSkills(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear, String term) {
        Map<String,Object> data = rstaffService.loadLearningSkills(areaId,schoolId,subject,grade,clazz,schoolYear,term);
        return data;
    }

    public Map<String, Object> loadExamSurvey(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        Map<String,Object> data = mockDataService.loadExamSurvey(cityCode,regionCode,schoolId,examId);
        return data;
    }

    public Map<String, Object> loadExamScoreState(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        Map<String,Object> data = mockDataService.loadExamScoreState(cityCode,regionCode,schoolId,examId);
        return data;
    }

    public Map<String, Object> loadExamScatterPoint(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        Map<String,Object> data = mockDataService.loadExamScatterPoint(cityCode,regionCode,schoolId,examId);
        return data;
    }

    public Map<String, Object> loadStudyLevelInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        Map<String,Object> data = mockDataService.loadStudyLevelInfo(cityCode,regionCode,schoolId,examId);
        return data;
    }

    public Map<String, Object> loadSubjectAbilityInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        Map<String,Object> data = mockDataService.loadSubjectAbilityInfo(cityCode,regionCode,schoolId,examId);
        return data;
    }

    public Map<String, Object> loadKnowledgePlateInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId) {
        Map<String,Object> data = mockDataService.loadKnowledgePlateInfo(cityCode,regionCode,schoolId,examId);
        return data;
    }

    public Map<String,Object> loadKnowledgeModule(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear, String term,String knowledgeModuleLevel) {
        Map<String,Object> data = rstaffService.loadKnowledgeModule(areaId,schoolId,subject,grade,clazz,schoolYear,term,knowledgeModuleLevel);
        return data;
    }

    public Map<String,Object> loadBaseData(Integer month, Long schoolId){
        return schoolMasterService.loadBaseData(month, schoolId);
    }

    public List<Map<String,Object>> loadHomeWorkData(Integer month, Long schoolId){
        return schoolMasterService.loadHomeWorkData(month, schoolId);
    }

    public List<Map<String, Object>> loadExamData(Integer month, Long schoolId){
        return schoolMasterService.loadExamData(month, schoolId);
    }

    public List<Map<String,Object>> loadActivityData(Integer month, Long schoolId){
        return schoolMasterService.loadActivityData(month, schoolId);
    }

    public List<Map<String,Object>> loadTeachResourceData(Integer month, Long schoolId){
        return schoolMasterService.loadTeachResourceData(month, schoolId);
    }

    public Map<String,Object> loadLearningSkillsData(Long schoolId, Integer regionCode, String subject, String grade, String schoolYear, String term){
        return schoolMasterService.loadLearningSkillsData(schoolId, regionCode, subject, grade, schoolYear, term);
    }

    public Map<String,Object> loadKnowledgeModuleData(Long schoolId, Integer regionCode, String subject, String grade, String schoolYear, String term){
        return schoolMasterService.loadKnowledgeModuleData(schoolId, regionCode, subject, grade, schoolYear, term);
    }
}
