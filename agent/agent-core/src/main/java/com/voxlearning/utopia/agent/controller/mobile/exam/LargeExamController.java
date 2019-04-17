package com.voxlearning.utopia.agent.controller.mobile.exam;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.exam.AgentExamSchoolVO;
import com.voxlearning.utopia.agent.bean.exam.AgentExamSubjectVO;
import com.voxlearning.utopia.agent.bean.exam.AgentKlxScanPaperVO;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.exam.AgentLargeExamService;
import com.voxlearning.utopia.service.user.api.entities.School;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 大考管理相关接口
 *
 * @author chunlin.yu
 * @create 2018-03-14 20:13
 **/

@Controller
@RequestMapping("/mobile/exam")
public class LargeExamController extends AbstractAgentController {

    @Inject
    private AgentLargeExamService agentLargeExamService;

    /**
     * 查询有合同的学校
     *
     * @return
     */
    @RequestMapping(value = "school/search.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolManage(@RequestParam(required = false) String schoolKey) {
        Collection<School> schools = agentLargeExamService.searchSchoolWithinContract(getCurrentUserId(), schoolKey);
        return MapMessage.successMessage().add("dataList",schools);
    }

    /**
     * 学校大考按月份查询
     *
     * @return
     */
    @RequestMapping(value = "school/month/manage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolManage() {
        Integer month = requestInteger("month");
        if (null == month) {
            return MapMessage.errorMessage("月份不正确");
        }
        List<AgentExamSchoolVO> dataList = agentLargeExamService.getAgentExamSchoolByMonth(month,getCurrentUserId());
        return MapMessage.successMessage().add("dataList", dataList).add("canEdit",canEdit());
    }

    /**
     * 查询学校所有的大考信息
     *
     * @return
     */
    @RequestMapping(value = "school/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolDetail() {
        Long schoolId =requestLong("schoolId");
        List<AgentExamSchoolVO> dataList = agentLargeExamService.getAgentExamSchoolBySchoolId(schoolId);
        MapMessage mapMessage = MapMessage.successMessage().add("dataList", dataList).add("canEdit",canEdit());
        return mapMessage;
    }

    /**
     * 学校某个月份的大考详情
     *
     * @return
     */
    @RequestMapping(value = "school/month/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage schoolMonthDetail() {
        Long schoolId = requestLong("schoolId");
        Integer month = requestInteger("month");
        if (null == month) {
            return MapMessage.errorMessage("月份不正确");
        }
        AgentExamSchoolVO data = agentLargeExamService.getAgentExamSchoolByMonthAndSchoolId(month,schoolId);
        MapMessage mapMessage = MapMessage.successMessage().add("data", data);
        mapMessage.add("gradeMap",agentLargeExamService.getSchoolGradeMap(schoolId)).add("canEdit",canEdit());
        return mapMessage;
    }


    /**
     * 添加学校大考
     * @param agentExamSchoolVO
     * @return
     */
    @RequestMapping(value = "school/add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addSchoolExam(@RequestBody(required = true) AgentExamSchoolVO agentExamSchoolVO){
        return agentLargeExamService.upsertAgentExamSchool(agentExamSchoolVO);
    }


    /**
     * 编辑学校大考
     * @param agentExamSchoolVO
     * @return
     */
    @RequestMapping(value = "school/edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editSchoolExam(@RequestBody AgentExamSchoolVO agentExamSchoolVO){
        return agentLargeExamService.upsertAgentExamSchool(agentExamSchoolVO);
    }




    /**
     * 科目大考详情
     *
     * @return
     */
    @RequestMapping(value = "subject/detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage detail() {
        Long examSubjectId = requestLong("examSubjectId");
        if (null == examSubjectId) {
            return MapMessage.errorMessage("大考ID不正确");
        }
        AgentExamSubjectVO agentExamSubjectVO = agentLargeExamService.getAgentExamSubject(examSubjectId);
        return MapMessage.successMessage().add("data", agentExamSubjectVO).add("canEdit",canEdit());
    }

    /**
     * 删除大考
     * @return
     */
    @RequestMapping(value = "subject/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteExamSubject(){
        Long examSubjectId = requestLong("examSubjectId");
        return agentLargeExamService.deleteAgentExamSubject(examSubjectId);
    }


    /**
     * 大考试卷检索
     * @return
     */
    @RequestMapping(value = "paper/school/grade/search.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage searchPaper(@RequestParam(required = false) String nameKey,@RequestParam(required = false) List<String> exludePaperIds) {
        Long schoolId = requestLong("schoolId");
        Integer grade = requestInteger("grade");

        List<AgentKlxScanPaperVO> voList = agentLargeExamService.searchPaper(schoolId,grade, nameKey);
        if (CollectionUtils.isNotEmpty(exludePaperIds)){
            voList = voList.stream().filter(item -> !exludePaperIds.contains(item.getPaperId())).collect(Collectors.toList());
        }
        return MapMessage.successMessage().add("dataList",voList);
    }



    private boolean canEdit(){
        AuthCurrentUser currentUser = getCurrentUser();
        if (null != currentUser && (currentUser.isBusinessDeveloper() || currentUser.isCityManager())){
            return true;
        }
        return false;
    }






}
