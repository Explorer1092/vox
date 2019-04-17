/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.api.entity.summerreport.StudentCityInterviewCount;
import com.voxlearning.utopia.service.business.api.entity.summerreport.StudentCollectSchool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiangpeng on 16/6/12.
 */

@ServiceVersion(version = "1.2")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface SummerReporterService {
    public String test();


    /**
     * 获取该城市的采访排行榜
     * 显示该学生所在城市的排行
     * 按带有位置信息的学校提交数计算排名
     * 数量>0才能上榜
     * 数量>0的人数超过100时，显示前100名
     * 如果出现相同采访数量，最后一次提交时间较早的学生排在前
     * 4小时缓存
     *
     * @param cityId 城市id
     * @return
     */
    public List<StudentCityInterviewCount> loadCityTopRange(Integer cityId);


    /**
     * 获取学生所在的城市排名
     * 4小时缓存
     *
     * @param userId
     * @return
     */
    public Integer loadStudentCityRank(Long userId);

    /**
     * 获取该区域随机推荐20个学校信息
     * 大数据 提供的特定学校数据；
     * >=3个学生提交相同学校名称的、且系统内无重名的学校；
     * 通过学生所在区域，随机显示当前区域的20所学校；
     * 每4小时刷新一次；
     * 不显示采访人数>=10人的学校，通过学校名称判断；
     * 显示该学校目前的采访人数；
     *
     * @param countyId 区id
     * @return
     */
    public List<Map<String, Object>> loadRecommendSchoolByCounty(Integer countyId);


    /**
     * 获取学生的采访记录(最多30条)
     *
     * @param userId
     * @return
     */
    public List<StudentCollectSchool> loadInterviewRecord(Long userId);


    /**
     * 获取该学生采访条数
     *
     * @param userId
     * @return
     */
    default Integer loadInterviewCount(Long userId) {
        List<StudentCollectSchool> list = loadInterviewRecord(userId);
        return list == null ? 0 : list.size();
    }


    public MapMessage uploadInterviewSchool(StudentCollectSchool studentCollectSchool);


}
