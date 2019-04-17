package com.voxlearning.utopia.agent.listener.handler;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentModifyDictSchoolApply;
import com.voxlearning.utopia.service.crm.consumer.loader.ApplyManagementLoaderClient;
import com.voxlearning.utopia.service.email.api.entities.PlainEmail;
import com.voxlearning.utopia.service.email.client.EmailServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 邮件发送未评分的字典学校，默认发当天通过申请的学校
 * @author: kaibo.he
 * @create: 2019-01-24 17:37
 **/
@Named
public class AgentEmailUnScoreDictSchoolHandler extends SpringContainerSupport {

    private static  String emailTo = "yanpei.fang@17zuoye.com";
    private static  String testEmailTo = "kaibo.he@17zuoye.com;dongshuang.zhao@17zuoye.com;";

    @Inject
    private ApplyManagementLoaderClient applyManagementLoaderClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    public void handle(Long beginTime, Long endTime) {
        if (Objects.isNull(beginTime) || beginTime==0L) {
            beginTime = DayRange.current().previous().getStartTime();
        }
        if (Objects.isNull(endTime) || endTime==0L) {
            endTime = DayRange.current().previous().getEndTime();
        }
        List<AgentModifyDictSchoolApply> applies = applyManagementLoaderClient.fetchDictSchoolApplyListByUpdateDate(new Date(beginTime), new Date(endTime));
        applies = applies
                .stream()
                .filter(apply -> apply.getResolved())//已处理
                .filter(apply -> apply.getModifyType() == AgentModifyDictSchoolApply.ModifyType.ADD_SCHOOL.getType()
                        && !Objects.equals(apply.getSchoolLevel(), SchoolLevel.MIDDLE.getLevel()) && !Objects.equals(apply.getSchoolLevel(),SchoolLevel.HIGH.getLevel()))//满足加入字典时，非初中、高中等级
                .filter(apply -> Objects.nonNull(apply.getSchoolId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(applies)) {
            String day = DateUtils.dateToString(DayRange.current().previous().getEndDate(),"MM月dd日");
            String title = String.format("%s, 新加入字典表学校%s所未经风控审核", day, applies.size());
            StringBuilder sb = new StringBuilder(title).append("\r\n");
            applies.forEach(apply -> {
                sb.append(apply.getSchoolId()).append("   ").append(apply.getSchoolName()).append("   ").append(SchoolLevel.safeParse(apply.getSchoolLevel()).getDescription()).append("\r\n");
            });
            PlainEmail plainEmail = new PlainEmail();
            plainEmail.setBody(sb.toString());

            if (!RuntimeMode.isProduction()) {
                plainEmail.setSubject(title);
                plainEmail.setTo(testEmailTo);
            } else {
                plainEmail.setSubject(title);
                plainEmail.setTo(emailTo);
            }
            emailServiceClient.getEmailService().sendEmail(plainEmail);
        }
    }
}
