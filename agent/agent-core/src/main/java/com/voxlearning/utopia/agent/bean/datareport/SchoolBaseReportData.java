package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.agent.bean.group.GroupWithParent;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.BeanUtils;


/**
 * online和offline模式学校数据基类
 *
 * @author deliang.che
 * @date 2018-03-19
 **/
@Getter
@Setter
public class SchoolBaseReportData {
    protected Integer day;
    protected String groupName;
    protected String chargePerson;
    protected String cityName;
    protected String countyName;
    protected Long schoolId;
    protected String schoolName;
    protected SchoolLevel schoolLevel;
    protected EduSystemType eduSystemType;
    protected Integer englishStartGrade;
    protected AgentSchoolPopularityType schoolPopularity;
    protected AgentSchoolPermeabilityType permeabilityType;


    public  <T extends SchoolBaseReportData> T convert(Class<T> clazz){
        try {
            T data = clazz.newInstance();
            BeanUtils.copyProperties(this, data);
            return data;
        }catch (Exception e){
            return null;
        }
    }


}
