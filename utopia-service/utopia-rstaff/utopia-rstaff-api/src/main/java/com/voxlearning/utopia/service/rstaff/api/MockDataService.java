package com.voxlearning.utopia.service.rstaff.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-08-07 14:30
 */

@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MockDataService {

    /**
     * 获得模考统测概况
     * @param cityCode          市code
     * @param regionCode        区域code
     * @param schoolId          学校ID
     * @param examId            考试ID
     * @return
     */
    public Map<String,Object> loadExamSurvey(Integer cityCode, Integer regionCode, Long schoolId, String examId);


    /**
     * 获得模考统测得分状况
     * @param cityCode
     * @param regionCode
     * @param schoolId
     * @param examId
     * @return
     */
    public Map<String,Object> loadExamScoreState(Integer cityCode, Integer regionCode, Long schoolId, String examId);


    /**
     * 获得散点图数据
     * @param cityCode
     * @param regionCode
     * @param schoolId
     * @param examId
     * @return
     */
    public Map<String,Object> loadExamScatterPoint(Integer cityCode, Integer regionCode, Long schoolId, String examId);


    /**
     * 获得学业水平信息
     * @param cityCode
     * @param regionCode
     * @param schoolId
     * @param examId
     * @return
     */
    public Map<String,Object> loadStudyLevelInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId);


    /**
     * 获得学科能力信息
     * @param cityCode
     * @param regionCode
     * @param schoolId
     * @param examId
     * @return
     */
    public Map<String,Object> loadSubjectAbilityInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId);


    /**
     * 获得知识板块掌握程度
     * @param cityCode
     * @param regionCode
     * @param schoolId
     * @param examId
     * @return
     */
    public Map<String,Object> loadKnowledgePlateInfo(Integer cityCode, Integer regionCode, Long schoolId, String examId);

}
