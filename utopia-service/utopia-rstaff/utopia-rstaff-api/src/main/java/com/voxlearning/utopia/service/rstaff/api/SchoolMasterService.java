package com.voxlearning.utopia.service.rstaff.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-06-19 11:04
 */
@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface SchoolMasterService extends IPingable {

    /**
     * 按学科和年级统计学校当月已有和新增布置作业的老师人数和做作业的学生人数。
     * @param schoolId 学校ID
     * @param month  日期，例如：201805
     * @return
     */
    public Map<String,Object> loadSchoolUsageData(Long schoolId, String month);


    /**
     * 根据时间，年级，学科三个维度统计作业相关的数据
     * @param schoolId 学校ID
     * @param grade 全部年级为0，其他1,2,3,4,5,6
     * @param subject 学科，取值为：ENGLISH，MATH，CHINESE
     * @param month 日期，例如： 201801
     * @return
     */
    public Map<String,Object> loadHomework(Long schoolId, String grade, String subject, String month);


    /**
     * 根据学科，年级，班级，时间查询条件获得每个单元的人均题数
     * @param areaId    区域ID
     * @param schoolId  学校ID
     * @param subject   学科，取值为：ENGLISH，MATH，CHINESE
     * @param grade     年级，取值为：1,2,3,4,5,6
     * @param clazz     班级，取值为：选择全部班级值为0，班级ID，注：当班级选择为全部班级的时候，正确率的数据为：单校单年级单元正确率：
     * @param schoolYear  学年 , 取值为： 2017 ，2018 等
     * @param term         学期，取值为：LAST_TERM 上学期 ，NEXT_TERM 下学期
     * @return
     */
    public Map<String,Object> loadUnitAvgQuestions(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear ,String term);


    /**
     * 校长首页 获得基础数据接口
     * @param month
     * @param schoolId
     * @return
     */
    public Map<String,Object> loadBaseData(Integer month, Long schoolId);

    /**
     * 校长首页 获得作业统计数据接口
     * @param month
     * @param schoolId
     * @return
     */
    public List<Map<String, Object>> loadHomeWorkData(Integer month, Long schoolId);

    /**
     * 校长首页 获得参与测评统计数据接口
     * @param month
     * @param schoolId
     * @return
     */
    public List<Map<String, Object>> loadExamData(Integer month, Long schoolId);

    /**
     * 校长首页 获得趣味活动统计数据接口
     * @param month
     * @param schoolId
     * @return
     */
    public List<Map<String,Object>>  loadActivityData(Integer month, Long schoolId);

    /**
     * 校长首页 获得教学资源统计数据接口
     * @param month
     * @param schoolId
     * @return
     */
    public List<Map<String,Object>> loadTeachResourceData(Integer month, Long schoolId);


    /**
     * 校长首页 获得学科能力养成统计数据接口
     * @param schoolId
     * @param regionCode
     * @param subject
     * @param grade
     * @param schoolYear
     * @param term
     * @return
     */
    public Map<String,Object> loadLearningSkillsData(Long schoolId, Integer regionCode, String subject, String grade, String schoolYear, String term);


    /**
     * 校长首页 获得知识版块掌握度统计数据接口
     * @param schoolId
     * @param subject
     * @return
     */
    public Map<String,Object> loadKnowledgeModuleData(Long schoolId, Integer regionCode, String subject, String grade, String schoolYear, String term);



}
