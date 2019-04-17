/**
 * Author:   xianlong.zhang
 * Date:     2018/12/6 15:06
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.controller.mobile.workrecord;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.mobile.workrecord.WorkRecordTeacherService;
import com.voxlearning.utopia.service.crm.api.constants.agent.FollowUpType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

@Controller
@RequestMapping("/mobile/work_record/follow")
public class WorkRecordTeacherController extends AbstractAgentController {

    @Inject
    private WorkRecordTeacherService workRecordTeacherService;


    @RequestMapping(value = "add_follow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addFollow() {
        String content = requestString("content");
        Long teacherId = requestLong("teacherId");
        String followUpType = redirect("followUpType");
        if(StringUtils.isEmpty(content)  || teacherId == null || StringUtils.isEmpty(followUpType)){
            return MapMessage.errorMessage("请求参数不正确");
        }
        if(FollowUpType.valueOf(followUpType) != FollowUpType.ONLINE && FollowUpType.valueOf(followUpType) != FollowUpType.PHONE_CALL ){
            return MapMessage.errorMessage("拜访类型只能是 '微信/QQ/短信' 或 '打电话'");
        }
        workRecordTeacherService.addFollow(teacherId,content,FollowUpType.valueOf(followUpType));
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "get_follow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getFollow() {
        String id = requestString("id");

        if(StringUtils.isEmpty(id)){
            return MapMessage.errorMessage("请求id不能为空");
        }
       return workRecordTeacherService.getFollow(id);
    }

    @RequestMapping(value = "delete_follow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteFollow() {
        String id = requestString("id");

        if(StringUtils.isEmpty(id)){
            return MapMessage.errorMessage("请求id不能为空");
        }
        return workRecordTeacherService.delFollow(id);
    }

    @RequestMapping(value = "update_follow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateFollow() {
        String id = requestString("id");
        String content = requestString("content");
        String followUpType = redirect("followUpType");
        if(StringUtils.isEmpty(id) || StringUtils.isEmpty(content)){
            return MapMessage.errorMessage("请求参数 id 或 content 为空");
        }
        if(FollowUpType.valueOf(followUpType) != FollowUpType.ONLINE && FollowUpType.valueOf(followUpType) != FollowUpType.PHONE_CALL ){
            return MapMessage.errorMessage("拜访类型只能是 '微信/QQ/短信' 或 '打电话'");
        }

        return workRecordTeacherService.updateFollow(id,content,FollowUpType.valueOf(followUpType));
    }

    @RequestMapping(value = "follow_list.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage followList() {
        Long teacherId = requestLong("teacherId");
        return workRecordTeacherService.followList(teacherId);
    }
}
