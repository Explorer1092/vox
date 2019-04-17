/**
 * Author:   xianlong.zhang
 * Date:     2018/9/11 16:04
 * Description: 天玑 新大考管理（考试管理员创建的考试）
 * History:
 */
package com.voxlearning.utopia.agent.controller.mobile.exam;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.exam.AgentExamNewService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/mobile/exams")
public class NewExamController extends AbstractAgentController {

    @Inject
    private AgentExamNewService agentExamNewService;

    @RequestMapping(value = "examsList.vpage")
    @ResponseBody
    public MapMessage examsList(){
        Integer type = getRequestInt("type",1);//类型  1全部  2 待分配 3 待评价
        Integer grade = requestInteger("grade");//年级
        String name = requestString("name");// 学校名称
        Integer pageSize = getRequestInt("pageSize");
        Integer pageNo = getRequestInt("pageNo");
        return agentExamNewService.getExamList(type,grade,name,pageSize,pageNo);
    }

    /**
     * 考试详情
     * @return
     */
    @RequestMapping(value = "examsInfo.vpage")
    @ResponseBody
    public MapMessage examsInfo(){
        String examId = requestString("examId");// 考试名称
        Map<String,Object> examInfo = agentExamNewService.getExamInfo(examId);
        if(MapUtils.isEmpty(examInfo)){
            return MapMessage.errorMessage("考试信息异常 请在微信群联系技术同事");
        }
        return MapMessage.successMessage().add("dataMap",examInfo);
    }

    /**
     * 获取中学专员和市经理
     */
    @RequestMapping(value = "middle_school_marketers_list.vpage")
    @ResponseBody
    public MapMessage businessDeveloperList(){
        AuthCurrentUser currentUser = getCurrentUser();
        if (currentUser.isBusinessDeveloper()){
            return MapMessage.errorMessage("您无权限查询");
        }
        return MapMessage.successMessage().add("dataList",agentExamNewService.businessDeveloperList());
    }

    /**
     * 保存分配专员
     */
    @RequestMapping(value = "save_distribution_user.vpage")
    @ResponseBody
    public MapMessage saveDistributionUser(){
        String examId = requestString("examId");// 考试名称
        Set<Long> userIds = requestLongSet("userIds",",");
        return agentExamNewService.saveDistributionUser(examId,userIds);
    }

    /**
     * 保存评价
     */
    @RequestMapping(value = "save_evaluate_state_user.vpage")
    @ResponseBody
    public MapMessage saveEvaluateStateUser(){
        String examId = requestString("examId");// 考试名称
        Long userId =  requestLong("userId");
        String level = requestString("level");
        String desc = requestString("desc");
        return agentExamNewService.saveEvaluateStateUser(examId,userId,level,desc);
    }

    /**
     * 代理商列表
     */
    @RequestMapping(value = "get_agent_user_list.vpage")
    @ResponseBody
    public MapMessage getAgentUserList(){
        return MapMessage.successMessage().add("dataList",agentExamNewService.getAgentUserList());
    }

    /**
     * 更新代理商信息
     */
    @RequestMapping(value = "update_exam_agent_user.vpage")
    @ResponseBody
    public MapMessage updateExamAgentUser(){
        Long agentId = requestLong("agentId");
        String examId = requestString("examId");
        Integer signType = requestInteger("signType");//1 标记代理商 2 取消标记
        return MapMessage.successMessage().add("dataList",agentExamNewService.updateExamAgentUser(examId,agentId,signType));
    }

    /**
     * 更新扫描方式
     */
    @RequestMapping(value = "update_exam_agent_scan_type.vpage")
    @ResponseBody
    public MapMessage updateExamScanTypeUser(){
        Integer scantType = getRequestInt("scanType");
        String examId = requestString("examId");
        return agentExamNewService.updateExamSacnType(examId,scantType);
    }
}
