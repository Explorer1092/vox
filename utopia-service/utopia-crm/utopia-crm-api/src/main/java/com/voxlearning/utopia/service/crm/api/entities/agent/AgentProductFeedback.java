package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.crm.api.bean.OperationObject;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackCategory;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackStatus;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackSubject;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductFeedbackType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 产品反馈
 *
 * @author song.wang
 * @date 2017/2/21
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@DocumentTable(table = "AGENT_PRODUCT_FEEDBACK")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20170320")
@DocumentConnection(configName = "agent")
public class AgentProductFeedback extends AbstractBaseApply implements OperationObject {

    private static final long serialVersionUID = 6459548780277547420L;

    @UtopiaSqlColumn AgentProductFeedbackType feedbackType; // 反馈类型
    @UtopiaSqlColumn AgentProductFeedbackCategory firstCategory; // 一级分类
    @UtopiaSqlColumn AgentProductFeedbackCategory secondCategory; // 二级分类
    @UtopiaSqlColumn AgentProductFeedbackCategory thirdCategory; // 三级分类

    @UtopiaSqlColumn Long teacherId; // 老师ID
    @UtopiaSqlColumn String teacherName; // 老师姓名
    @UtopiaSqlColumn AgentProductFeedbackSubject teacherSubject; // 老师学科 （小学英语、小学数学、小学语文、初中英语、初中数学、高中数学）

    @UtopiaSqlColumn Boolean noticeFlag; // 是否运行发送消息感谢老师
    @UtopiaSqlColumn String noticeContent; // 感谢老师的通知内容

    @UtopiaSqlColumn String bookName; // 教材名称
    @UtopiaSqlColumn String bookGrade; // 教材对应的年级
    @UtopiaSqlColumn String bookUnit; // 教材单元
    @UtopiaSqlColumn String bookCoveredArea; // 教材覆盖的地区
    @UtopiaSqlColumn Integer bookCoveredStudentCount; // 教材覆盖的学生数

    @UtopiaSqlColumn String content; // 反馈内容
    @UtopiaSqlColumn String pic1Url; // 附图1 URL
    @UtopiaSqlColumn String pic2Url; // 附图2 URL
    @UtopiaSqlColumn String pic3Url; // 附图3 URL
    @UtopiaSqlColumn String pic4Url; // 附图4 URL
    @UtopiaSqlColumn String pic5Url; // 附图5 URL

    @UtopiaSqlColumn String pmAccount; // pm的账号
    @UtopiaSqlColumn String pmAccountName; // pm的姓名

    @UtopiaSqlColumn String onlineEstimateDate; // 预计上线时间（yyyy-MM）
    @UtopiaSqlColumn Boolean onlineFlag; // 是否上线
    @UtopiaSqlColumn Date onlineDate; // 是否上线
    @UtopiaSqlColumn String onlineNotice; // 上线通知
    @UtopiaSqlColumn Boolean callback; // 是否需要回电

    @UtopiaSqlColumn AgentProductFeedbackStatus feedbackStatus; // 产品反馈的状态

    @UtopiaSqlColumn private Long relationCode;

    @Getter
    @Setter
    @DocumentFieldIgnore
    private Boolean mySelf; // 是否是他自己的反馈

    public static String ck_wid(Long workflowId) {
        return CacheKeyGenerator.generateCacheKey(AgentProductFeedback.class, "wid", workflowId);
    }

    public static String ck_platform_uid(SystemPlatformType userPlatform, String userAccount) {
        return CacheKeyGenerator.generateCacheKey(AgentProductFeedback.class,
                new String[]{"platform", "uid"},
                new Object[]{userPlatform, userAccount});
    }

    @Override
    public String generateSummary() {
        StringBuilder builder = new StringBuilder("");
        if (feedbackType != null) {
            builder.append("反馈类型：").append(feedbackType.getDesc()).append("   ");
        }
        if (teacherSubject != null) {
            builder.append("学科：").append(teacherSubject.getDesc()).append("   ");
        }
        if (StringUtils.isNotBlank(content)) {
            builder.append("建议/需求：").append(content).append("   ");
        }
        if (callback != null) {
            builder.append("是否需要回电：").append(callback).append("   ");
        }
        if (StringUtils.isNotBlank(pic1Url)) {
            builder.append("附图1 URL：").append(pic1Url).append("   ");
        }
        if (StringUtils.isNotBlank(pic2Url)) {
            builder.append("附图2 URL：").append(pic2Url).append("   ");
        }
        if (StringUtils.isNotBlank(pic3Url)) {
            builder.append("附图3 URL：").append(pic3Url).append("   ");
        }
        if (StringUtils.isNotBlank(pic4Url)) {
            builder.append("附图4 URL：").append(pic4Url).append("   ");
        }
        if (StringUtils.isNotBlank(pic5Url)) {
            builder.append("附图5 URL：").append(pic5Url).append("   ");
        }
        return builder.toString();
    }

    @Override
    public Map<String, String> loadFieldDescription() {
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put("id", String.valueOf(this.getId()));
        fieldMap.put("subject", String.valueOf(this.getTeacherSubject()));
        fieldMap.put("type", String.valueOf(this.getFeedbackType()));
        fieldMap.put("firstCategory", String.valueOf(this.getFirstCategory()));
        fieldMap.put("secondCategory", String.valueOf(this.getSecondCategory()));
        fieldMap.put("thirdCategory", String.valueOf(this.getThirdCategory()));
        fieldMap.put("pm", pmAccount);
        fieldMap.put("onlineEstimateDate", onlineEstimateDate);
        fieldMap.put("onlineFlag", String.valueOf(onlineFlag));
        fieldMap.put("callback", String.valueOf(callback));
        fieldMap.put("onlineNotice", onlineNotice);
        fieldMap.put("pic1Url", pic1Url);
        fieldMap.put("pic2Url", pic2Url);
        fieldMap.put("pic3Url", pic3Url);
        fieldMap.put("pic4Url", pic4Url);
        fieldMap.put("pic5Url", pic5Url);
        return fieldMap;
    }
}
