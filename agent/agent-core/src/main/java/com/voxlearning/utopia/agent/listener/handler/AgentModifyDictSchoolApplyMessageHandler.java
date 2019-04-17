package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.AgentTagService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.service.workflow.api.constants.WorkFlowProcessResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 字典表审核消息
 *
 * @author chunlin.yu
 * @create 2017-08-01 12:50
 **/
@Named
public class AgentModifyDictSchoolApplyMessageHandler extends SpringContainerSupport {
    @Inject
    AgentNotifyService agentNotifyService;
    @Inject
    private AgentTagService agentTagService;
    public void handle(Map<String,Object> command){
        int modifyType = SafeConverter.toInt(command.get("modifyType"));
        String schoolName =SafeConverter.toString(command.get("schoolName"));
        long schoolId =SafeConverter.toLong(command.get("schoolId"));
        long receiverId =SafeConverter.toLong(command.get("receiverId"));
        WorkFlowProcessResult processResult = WorkFlowProcessResult.nameOf((String) command.get("processResult"));
        if(WorkFlowProcessResult.agree == processResult){
            //本处审核同意不再发送通知，天权字典表处理中学校更新字典表的时候，再发送通知
            //sendApproveMessage(modifyType,schoolName,schoolId,receiverId);
        }else if(WorkFlowProcessResult.reject == processResult) {
            String rejectName = SafeConverter.toString(command.get("rejectName"));
            String rejectNote = SafeConverter.toString(command.get("rejectNote"));
            sendRejectMessage(modifyType,schoolName,schoolId,receiverId,rejectNote,rejectName);
        }

    }



    public void sendApproveMessage(Integer modifyType,String schoolName,Long schoolId,Long receiverId){
        String content = StringUtils.formatMessage("您提交的“{}-{}（{}）”申请已审批通过。",getNotifyPrefix(modifyType),schoolName,schoolId);
        agentNotifyService.sendNotify(AgentNotifyType.MODIFY_DICT_SCHOOL_APPLY.getType(), "字典表调整", content,
                Collections.singleton(receiverId), null);
    }

    private void sendRejectMessage(Integer modifyType,String schoolName,Long schoolId,Long receiverId,String rejectNote,String rejectName){
        String content = StringUtils.formatMessage("您提交的“{}-{}（{}）”申请被驳回。\r\n" +
                "驳回原因：{}【驳回人：{}】。", getNotifyPrefix(modifyType), schoolName, schoolId, rejectNote, rejectName);
        List<Long> tagIds = agentTagService.getNotifyTagIdsByName("驳回");
        agentNotifyService.sendNotifyWithTags(AgentNotifyType.MODIFY_DICT_SCHOOL_APPLY.getType(), "字典表调整", content,
                Collections.singleton(receiverId), null, null, null, tagIds);
    }

    private String getNotifyPrefix(Integer modifyType){
        String notifyPrefix = "";
        if(modifyType == 1){
            notifyPrefix = "添加学校";
        }else if(modifyType == 2){
            notifyPrefix = "删除学校";
        }else if(modifyType == 3){
            notifyPrefix = "业务变更";
        }
        return notifyPrefix;
    }

}
