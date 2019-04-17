package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * CrmReviewTeacherFakeHandler
 *
 * @author song.wang
 * @date 2017/5/24
 */
@Named
public class CrmReviewTeacherFakeHandler extends SpringContainerSupport {
    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentTagService agentTagService;

    public void handle(Long teacherId, String teacherName, String reviewStatus, String reviewerName, String reviewNote, Long receiverId){
        if(teacherId == null || StringUtils.isBlank(teacherName) || receiverId == null){
            return;
        }
        if(!Objects.equals(reviewStatus, "PASS") && !Objects.equals(reviewStatus, "REJECT")){ // ReviewStatus 枚举
            return;
        }
        AgentUser user = baseOrgService.getUser(receiverId);
        if(user == null){
            return;
        }
        if(Objects.equals(reviewStatus, "PASS")){ // 已通过
            sendApproveMessage(teacherName,teacherId,receiverId);
        }else { // 已驳回
            sendRejectMessage(teacherName,teacherId,receiverId,reviewNote,reviewerName);
        }
    }


    private void sendApproveMessage(String teacherName,Long teacherId,Long receiverId){
        String content = StringUtils.formatMessage("您提交的老师“{}（{}）”判假申请已审批通过。",teacherName,teacherId);
        agentNotifyService.sendNotify(AgentNotifyType.REVIEW_TEACHER_FAKE.getType(), "老师判假", content,
                Collections.singleton(receiverId), null);
    }

    private void sendRejectMessage(String teacherName,Long teacherId,Long receiverId,String rejectNote,String rejectName){
        String content = StringUtils.formatMessage("您提交的老师“{}（{}）”判假申请被驳回。\r\n" +
                "驳回原因：{}【驳回人：{}】。", teacherName,teacherId, rejectNote, rejectName);
        List<Long> tagIds = agentTagService.getNotifyTagIdsByName("驳回");
        agentNotifyService.sendNotifyWithTags(AgentNotifyType.REVIEW_TEACHER_FAKE.getType(), "老师判假", content,
                Collections.singleton(receiverId), null, null, null, tagIds);
    }
}
