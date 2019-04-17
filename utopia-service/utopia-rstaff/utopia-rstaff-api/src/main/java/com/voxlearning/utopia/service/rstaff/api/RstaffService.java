package com.voxlearning.utopia.service.rstaff.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.user.api.entities.School;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-06-19 11:03
 */

@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface RstaffService extends IPingable {

    /**
     * 根据教研员所管辖的区域或者学校统计当月已有和新增布置作业的老师人数和做作业的学生人数。
     * @param schoolIds     学校IDs，如果schoolId不为空则为学校教研员
     * @param regionCodes   区域Codes，如果regionCodes不为空则为区教研员
     * @param cidyCodes     城市Codes, 如果cityCodes不为空则为市教研员
     * @param subject        学科，取值为：ENGLISH 或者 MATH 或者 CHINESE
     * @param month          日期，例如：201805
     * @return
     */
    public Map<String,Object> loadResearchUsageData(List<Long> schoolIds, List<Integer> regionCodes, List<Integer> cidyCodes, String subject, String month);


    /**
     * 根据教研员所管辖的区域或者学校按时间，年级，学科三个维度统计作业相关的数据
     * @param schoolIds      学校IDs，如果schoolId不为空则为学校教研员
     * @param regionCodes   区域Codes，如果regionCodes不为空则为区教研员
     * @param cityCodes     城市Codes, 如果cityCodes不为空则为市教研员
     * @param grade          年级，全部年级为0，其他1,2,3,4,5,6
     * @param subject       学科，取值为：ENGLISH 或者 MATH 或者 CHINESE
     * @param month          日期，例如： 201801
     * @return
     */
    public Map<String,Object> loadResearchHomework(Map<Long,School> schoolIds, List<Integer> regionCodes, List<Integer> cityCodes, String grade, String subject, String month);


    /**
     * 教研员单元训练情况
     * 根据学科，年级，班级，时间查询条件获得每个单元的人均题数
     * @param schoolId     学校ID
     * @param regionCode   区域ID
     * @param subject      学科，取值为：ENGLISH，MATH，CHINESE
     * @param grade        年级，取值为：1,2,3,4,5,6
     * @param clazz        班级，取值为：选择全部班级值为0，班级ID，注：当班级选择为全部班级的时候，正确率的数据为：单校单年级单元正确率：
     * @param schoolYear   学年 , 取值为： 2017 ，2018 等
     * @param term          学期，取值为：LAST_TERM 上学期 ，NEXT_TERM 下学期
     * @return
     */
    public Map<String,Object> loadResearchUnitAvgQuestions(Long schoolId, Integer regionCode, String subject, String grade, String clazz, String schoolYear, String term);


    /**
     * 学科能力养成
     * @param areaId
     * @param schoolId
     * @param subject
     * @param grade
     * @param clazz
     * @param schoolYear
     * @param term
     * @return
     */
    Map<String,Object> loadLearningSkills(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear, String term);

    /**
     * 知识板块
     * @param areaId
     * @param schoolId
     * @param subject
     * @param grade
     * @param clazz
     * @param schoolYear
     * @param term
     * @return
     */
    Map<String,Object> loadKnowledgeModule(Integer areaId, Long schoolId, String subject, String grade, String clazz, String schoolYear, String term,String knowledgeModuleLevel);
}
