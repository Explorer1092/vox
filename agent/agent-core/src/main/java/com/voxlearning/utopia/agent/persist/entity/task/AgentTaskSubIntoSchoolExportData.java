package com.voxlearning.utopia.agent.persist.entity.task;

import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.constants.AgentTaskFeedbackType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *进校维护老师导出数据
 *
 * @author deliang.che
 * @create 2018-05-29
 **/
@Getter
@Setter
public class AgentTaskSubIntoSchoolExportData extends AgentTaskSubBaseExportData implements Serializable,ExportAble {

    private Boolean isIntoSchool;     //任务期间是否进校
    private Boolean isVisitTeacher; //任务期间是否进校拜访老师


    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(getOperatorName());
        result.add(getMarketingName());
        result.add(getRegionName());
        result.add(getAreaName());
        result.add(getCityName());
        result.add(getSchoolId());
        result.add(getSchoolName());
        result.add(getTeacherId());
        result.add(getTeacherName());
        result.add(getSubject());
        result.add(getProvince());
        result.add(getCity());
        result.add(getCounty());
        result.add((null != isIntoSchool && isIntoSchool) ? "是" : "否");
        result.add((null != isVisitTeacher && isVisitTeacher) ? "是" : "否");
        result.add(getIsHomework() ? "是" : "否");

        return result;
    }
}
