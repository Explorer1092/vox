package com.voxlearning.utopia.agent.persist.entity.task;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.agent.constants.AgentTaskFeedbackType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *线上维护老师导出数据
 *
 * @author deliang.che
 * @create 2018-05-29
 **/
@Getter
@Setter
public class AgentTaskSubOnlineExportData extends AgentTaskSubBaseExportData implements Serializable,ExportAble {
    private Date feedbackTime;                  //维护时间
    private AgentTaskFeedbackType feedbackType; //跟进方式
    private String feedbackResult;              //跟进结果


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
        result.add(null != feedbackTime ? DateUtils.dateToString(feedbackTime,"yyyy-MM-dd HH:mm") : "");
        result.add(null != feedbackType ? feedbackType.getValue() : "");
        result.add(feedbackResult);
        result.add((null != getIsHomework() && getIsHomework()) ? "是" : "否");

        return result;
    }
}
