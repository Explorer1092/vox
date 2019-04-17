package com.voxlearning.utopia.service.crm.api.bean;

import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackCategory;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 产品反馈列表的条件
 * Created by yaguang.wang on 2017/2/28.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductFeedbackListCondition implements Serializable {

    private static final long serialVersionUID = 8561119929127859829L;

    private Date startDate;
    private Date endDate;
    private AgentProductFeedbackSubject subject;
    private AgentProductFeedbackType type;
    private AgentProductFeedbackStatus status;
    private AgentProductFeedbackCategory firstCategory;
    private AgentProductFeedbackCategory secondCategory;
    private AgentProductFeedbackCategory thirdCategory;
    private String pmData;
    private Boolean onlineFlag;
    private String content;
    private String feedbackPeople;
    private String feedbackPeopleId;
    private String teacher;
    private Long id;
    private List<Long> teacherIds;
    private String onlineEstimateDate;
    private String teacherName;
    private Boolean callback;
    private String pic1Url;
    private String pic2Url;
    private String pic3Url;
    private String pic4Url;
    private String pic5Url;

    private Boolean checkIdExist() {
        return this.id != null;
    }
}
