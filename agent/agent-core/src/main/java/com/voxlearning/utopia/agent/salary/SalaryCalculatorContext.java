/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.salary;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.FormerEmployeeData;
import com.voxlearning.utopia.agent.constants.AgentCityLevelType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex on 15-2-27.
 */
@Data
@AllArgsConstructor
public class SalaryCalculatorContext {
    // 计算时间
    @Getter
    @Setter
    private Date runTime;
    @Getter @Setter private Map<AgentRoleType, List<AgentUser>> roleUsers; // 在职员工
    @Getter @Setter private Map<Integer, AgentCityLevelType> levelRegions;

    @Getter @Setter private Map<String, Map<String, Object>> regionTree;
    @Getter @Setter private List<AgentDictSchool> dictSchoolList;

    @Getter @Setter private Map<AgentRoleType, List<FormerEmployeeData>> formerEmployeeDatas; // 已离职人员数据
    @Getter @Setter private Boolean includeDictSchool;  // 是否重刷字典表业绩数据

    public AgentCityLevelType regionLevel(Integer region) {
        return levelRegions == null ? null : levelRegions.get(region);
    }

    public Integer getRunDay(){
        return SafeConverter.toInt(DateUtils.dateToString(runTime, "yyyyMMdd"));
    }

    public Integer getSalaryMonth(){
        return SafeConverter.toInt(DateUtils.dateToString(runTime, "yyyyMM"));
    }

    public String regionName(Integer regionCode) {
        if (MapUtils.isEmpty(regionTree) || regionCode == null) {
            return "";
        }
        Map<String, Object> region = regionTree.get(String.valueOf(regionCode));
        Object title = region == null ? null : region.get("title");
        return title == null ? "" : String.valueOf(title);
    }

    public String regionNames(List<Integer> regionCodes) {
        if (MapUtils.isEmpty(regionTree) || CollectionUtils.isEmpty(regionCodes)) {
            return "";
        }
        List<String> regionNames = new ArrayList();
        for (Integer regionCode : regionCodes) {
            Map<String, Object> region = regionTree.get(String.valueOf(regionCode));
            Object title = region == null ? null : region.get("title");
            regionNames.add(title == null ? "" : String.valueOf(title));
        }
        return StringUtils.arrayToCommaDelimitedString(regionNames.toArray());
    }
//
//    public List<FormerEmployeeData> getFormerEmployeeDataList(AgentRoleType roleType){
//        if(MapUtils.isEmpty(formerEmployeeDatas)){
//            return Collections.emptyList();
//        }
//        List<FormerEmployeeData> dataList = formerEmployeeDatas.get(roleType);
//        if(CollectionUtils.isEmpty(dataList)){
//            return Collections.emptyList();
//        }
//        return  dataList;
//    }
}
