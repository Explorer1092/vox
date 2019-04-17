/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.salary.autumn2016;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.DayUtils;
import com.voxlearning.utopia.agent.bean.FormerEmployeeData;
import com.voxlearning.utopia.agent.dao.mongo.SchoolDayIncreaseDataDao;
import com.voxlearning.utopia.agent.persist.entity.SchoolDayIncreaseData;
import com.voxlearning.utopia.agent.salary.SalaryCalculator;
import com.voxlearning.utopia.agent.salary.type.SalaryKpiType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DictSchoolCalculator
 *
 * @author song.wang
 * @date 2016/9/26
 */
@Named
@Slf4j
public class DictSchoolCalculator extends SalaryCalculator {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject
    private SchoolDayIncreaseDataDao schoolDayIncreaseDataDao;

    @Override
    public void calculate(List<AgentUser> users) {
        // 删除数据
        agentUserKpiResultSpring2016Persistence.deleteDictSchoolData(context.getSalaryMonth());
        // 生成数据
        List<AgentDictSchool> dictSchoolList = context.getDictSchoolList();
        List<Long> schoolIdList = dictSchoolList.stream().map(AgentDictSchool::getSchoolId).collect(Collectors.toList());

        Map<Long, School> schoolMap = new HashMap<>();
        Map<Integer, List<Long>> groupSchoolIdMap = schoolIdList.stream().collect(Collectors.groupingBy(p -> schoolIdList.indexOf(p) / 300, Collectors.toList()));
        groupSchoolIdMap.values().forEach(p -> {
            if (CollectionUtils.isNotEmpty(p)) {
                Map<Long, School> tempSchoolMap = schoolLoaderClient.getSchoolLoader()
                        .loadSchools(schoolIdList)
                        .getUninterruptibly();
                if (MapUtils.isNotEmpty(tempSchoolMap)) {
                    schoolMap.putAll(tempSchoolMap);
                }
            }
        });


        List<SchoolDayIncreaseData> schoolDayIncreaseDataList = schoolDayIncreaseDataDao.findSchoolData(schoolIdList, context.getRunDay());
        Map<Long, SchoolDayIncreaseData> schoolDayIncreaseDataMap = schoolDayIncreaseDataList.stream().collect(Collectors.toMap(SchoolDayIncreaseData::getSchoolId, Function.identity()));

        for (AgentDictSchool dictSchool : dictSchoolList) {
            calculatePerformanceData(schoolMap, dictSchool, schoolDayIncreaseDataMap.get(dictSchool.getSchoolId()));
        }
    }

    @Override
    public void calculateFormer(List<FormerEmployeeData> formerEmployeeDataList) {

    }

    private void calculatePerformanceData(Map<Long, School> schoolMap, AgentDictSchool dictSchool, SchoolDayIncreaseData schoolDayIncreaseData) {
        Date salaryStartDate = DayUtils.getFirstDayOfMonth(context.getRunTime());
        Date salaryEndDate = DayUtils.getLastDayOfMonth(context.getRunTime());
        List<AgentUser> userList = baseOrgService.getSchoolManager(dictSchool.getSchoolId());
        if (CollectionUtils.isNotEmpty(userList)) {
            School school = schoolMap.get(dictSchool.getSchoolId());
            for (AgentUser user : userList) {
                saveUserCpaMonthlyResult(user, context.getSalaryMonth(), school, salaryStartDate, salaryEndDate, SalaryKpiType.DICT_SCHOOL_SASC, dictSchool.getEngBudget(context.getRunDay()), schoolDayIncreaseData == null ? 0 : SafeConverter.toLong(schoolDayIncreaseData.getEngMauc()), 0d, "");
                // 如果是小学的话， 保存双活数据
                if (dictSchool.getSchoolLevel() != null && SchoolLevel.JUNIOR.getLevel() == dictSchool.getSchoolLevel()) {
                    saveUserCpaMonthlyResult(user, context.getSalaryMonth(), school, salaryStartDate, salaryEndDate, SalaryKpiType.DICT_SCHOOL_DASC, dictSchool.getMathBudget(context.getRunDay()), schoolDayIncreaseData == null ? 0 : SafeConverter.toLong(schoolDayIncreaseData.getMathMauc()), 0d, "");
                }
            }
        }
    }

}
