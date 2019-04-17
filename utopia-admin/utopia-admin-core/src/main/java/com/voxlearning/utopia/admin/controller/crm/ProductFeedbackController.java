package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.service.crm.CrmProductFeedbackService;
import com.voxlearning.utopia.service.crm.api.bean.ProductFeedbackListCondition;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackCategory;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentProductFeedbackLoadClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentProductFeedbackServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmProductFeedbackRecordServiceClient;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * 产品反馈参数获取
 * Created by yaguang.wang on 2017/3/2.
 */
public class ProductFeedbackController extends CrmAbstractController {
    @Inject
    protected CrmProductFeedbackService crmProductFeedbackService;
    @Inject
    protected AgentProductFeedbackLoadClient agentProductFeedbackLoadClient;
    @Inject
    protected AgentProductFeedbackServiceClient agentProductFeedbackServiceClient;
    @Inject
    protected MessageCommandServiceClient messageCommandServiceClient;
    @Inject
    protected CrmProductFeedbackRecordServiceClient crmProductFeedbackServiceClient;

    @Inject
    protected EmailServiceClient emailServiceClient;

    protected static final Integer PAGE_SIZE = 20;

    protected ProductFeedbackListCondition getFeedbackOfRequest() {
        ProductFeedbackListCondition condition = new ProductFeedbackListCondition();
        condition.setStartDate(DateUtils.stringToDate(getRequestString("startDate"), DateUtils.FORMAT_SQL_DATE));
        Date endDate = DateUtils.stringToDate(getRequestString("endDate"), DateUtils.FORMAT_SQL_DATE);
        condition.setEndDate(endDate != null ? DateUtils.nextDay(endDate, 1) : null);
        condition.setSubject(AgentProductFeedbackSubject.of(getRequestInt("subject")));
        condition.setType(AgentProductFeedbackType.of(getRequestInt("type")));
        condition.setStatus(AgentProductFeedbackStatus.of(getRequestInt("status")));
        condition.setFirstCategory(AgentProductFeedbackCategory.nameOf(getRequestString("firstCategory")));
        condition.setSecondCategory(AgentProductFeedbackCategory.nameOf(getRequestString("secondCategory")));
        condition.setThirdCategory(AgentProductFeedbackCategory.nameOf(getRequestString("thirdCategory")));
        condition.setPmData(Objects.equals(getRequestString("pmData"), "0") || Objects.equals(getRequestString("pmData"), "") ? null : getRequestString("pmData"));
        condition.setOnlineFlag(getRequestInt("online") == 0 ? null : getRequestInt("online") == 1);
        condition.setContent(getRequestString("content"));
        condition.setFeedbackPeople(getRequestString("feedbackPeople"));
        condition.setTeacher(getRequestString("teacher"));
        condition.setId(getRequestLong("id"));
        condition.setOnlineEstimateDate(getRequestString("onlineEstimateDate"));
        condition.setCallback(getRequestBool("callback"));
        condition.setPic1Url(getRequestString("file"));
        return condition;
    }

    protected List<Map<String, Object>> createAgentFeedbackType() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AgentProductFeedbackType type : AgentProductFeedbackType.values()) {
            Map<String, Object> data = new HashMap<>();
            data.put("type", type.getType());
            data.put("desc", type.getDesc());
            data.put("this", type);
            result.add(data);
        }
        return result;
    }

    protected List<Map<String, Object>> createAgentFeedbackSubject() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (AgentProductFeedbackSubject type : AgentProductFeedbackSubject.values()) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", type.getId());
            data.put("desc", type.getDesc());
            data.put("this", type);
            result.add(data);
        }
        return result;

    }

    protected void sendOnlineEmail(String content, String to) {
        emailServiceClient.createPlainEmail()
                .body(content)
                .subject("产品反馈上线通知")
                .to(to)
                .send();
    }
}
