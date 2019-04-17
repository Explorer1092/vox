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

/**
 * CrmReviewSchoolClueHandler
 *
 * @author song.wang
 * @date 2017/5/24
 */
@Named
public class CrmReviewSchoolClueHandler extends SpringContainerSupport {

    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentTagService agentTagService;

    public void handle(Long schoolId, String schoolName, int reviewStatus, String reviewerName, String reviewNote, Long receiverId){
        if(schoolId == null || StringUtils.isBlank(schoolName) || receiverId == null){
            return;
        }
        if(reviewStatus != -1 && reviewStatus != 2){ // CrmSchoolClueStatus 枚举： -1: 已驳回、0: 暂存、1: 待审核、2: 已通过
            return;
        }
        AgentUser user = baseOrgService.getUser(receiverId);
        if(user == null){
            return;
        }
        if(reviewStatus == 2){ // 已通过
            String content = StringUtils.formatMessage("您提交的“{}（{}）”学校鉴定申请已通过。",schoolName,schoolId);
            agentNotifyService.sendNotify(AgentNotifyType.REVIEW_SCHOOL_CLUE.getType(), "鉴定学校", content,
                    Collections.singleton(receiverId), null);
        }else { // 已驳回
            String content =StringUtils.formatMessage("您提交的“{}（{}）”学校鉴定申请被驳回。\r\n" +
                    "驳回原因：{}【驳回人：{}】。",schoolName,schoolId,reviewNote == null? "" : reviewNote,reviewerName == null? "": reviewerName);
            List<Long> tagIds = agentTagService.getNotifyTagIdsByName("驳回");
            agentNotifyService.sendNotifyWithTags(AgentNotifyType.REVIEW_SCHOOL_CLUE.getType(), "鉴定学校", content ,
                    Collections.singleton(receiverId), null, null, null, tagIds);
        }
    }
}
