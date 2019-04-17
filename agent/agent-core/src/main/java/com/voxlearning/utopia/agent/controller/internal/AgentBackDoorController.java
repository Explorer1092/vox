package com.voxlearning.utopia.agent.controller.internal;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.galaxy.service.studycourse.api.entity.user.StudyCourseFinishStructLessonHistory;
import com.voxlearning.galaxy.service.studycourse.api.user.DPStudyCourseUserDataLoader;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.dao.mongo.AgentTaskDetailDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.palace.PalaceActivityRecordDao;
import com.voxlearning.utopia.agent.dao.mongo.activity.palace.PalaceActivityUserStatisticsDao;
import com.voxlearning.utopia.agent.mockexam.dao.entity.ExamPaperEntity;
import com.voxlearning.utopia.agent.mockexam.dao.support.ExamPaperDaoJdbcImpl;
import com.voxlearning.utopia.agent.mockexam.service.support.ExamPaperServiceImpl;
import com.voxlearning.utopia.agent.persist.entity.activity.palace.PalaceActivityRecord;
import com.voxlearning.utopia.agent.service.activity.ActivityStatisticsService;
import com.voxlearning.utopia.agent.service.activity.palace.PalaceActivityService;
import com.voxlearning.utopia.agent.service.mobile.AgentTaskService;
import com.voxlearning.utopia.agent.service.partner.AgentPartnerService;
import com.voxlearning.utopia.agent.service.workspace.UserOrderService;
import com.voxlearning.utopia.api.constant.AgentTaskCategory;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserServiceClient;
import com.voxlearning.utopia.service.workflow.api.bean.WorkFlowProcessUser;
import com.voxlearning.utopia.service.workflow.consumer.WorkFlowDataServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AgentBackDoorController
 *
 * @author song.wang
 * @date 2018/9/26
 */
@Controller
@RequestMapping("/backdoor")
public class AgentBackDoorController extends AbstractAgentController {

    @Resource
    ExamPaperDaoJdbcImpl examPaperDao;
    @Inject
    private AgentTaskService agentTaskService;
    @Resource
    UserOrderService userOrderService;
    @Inject
    private AgentTaskDetailDao agentTaskDetailDao;
    @Inject
    private AgentUserServiceClient agentUserServiceClient;
    @Inject
    private AgentUserLoaderClient agentUserLoaderClient;
    @Inject
    private ExamPaperServiceImpl examPaperService;

    @Inject
    private PalaceActivityUserStatisticsDao statisticsDao;
    @Inject
    private PalaceActivityRecordDao palaceActivityRecordDao;
    @Inject
    private PalaceActivityService palaceActivityService;
    @ImportService(interfaceClass = DPStudyCourseUserDataLoader.class)
    private DPStudyCourseUserDataLoader dpStudyCourseUserDataLoader;
    @Inject
    private AgentPartnerService agentPartnerService;
    @Inject
    private WorkFlowDataServiceClient workFlowDataServiceClient;
    @Inject
    private ActivityStatisticsService activityStatisticsService;
    /**
     * 根据试卷ids删除测评功能中试卷数据  AGENT_MEXAM_PAPER
     * 示例： paperIds=P_10200193286120,P_10200257406357
     * @return
     */
    @RequestMapping(value = "delete_exam_paper_by_paperids.vpage")
    @ResponseBody
    public MapMessage deleteExamPaperByPaperIds() {
        MapMessage message = MapMessage.successMessage();
        String paperIdsStr = getRequestString("paperIds");
        if(StringUtils.isBlank(paperIdsStr)){
            message.add("更新数据条数", 0);
            return message;
        }
        Set<String> paperIdSet = new HashSet<>();
        String[] paperIds = StringUtils.split(paperIdsStr, ",");
        for(String paperId : paperIds){
            if(StringUtils.isNotBlank(paperId) && StringUtils.isNotBlank(StringUtils.trim(paperId))){
                paperIdSet.add(StringUtils.trim(paperId));
            }
        }

        Map<String, ExamPaperEntity> paperEntityMap = examPaperDao.findByPaperId(paperIdSet);
        if(MapUtils.isEmpty(paperEntityMap)){
            message.add("更新数据条数", 0);
            return message;
        }

        List<Long> ids = paperEntityMap.values().stream().map(ExamPaperEntity::getId).collect(Collectors.toList());
        long count = examPaperDao.removes(ids);
        message.add("更新数据条数", count);
        return message;
    }

    /**
     * 更新试卷目前已上线的测评次数
     * 示例： data={"P_10200193286120":1,"P_10200257406357": 2}
     * @return
     */
    @RequestMapping(value = "update_paper_plan_times_by_paperids.vpage")
    @ResponseBody
    public MapMessage updatePaperPlanTimesByPaperIds() {
        MapMessage message = MapMessage.successMessage();
        String data = getRequestString("data");
        if(StringUtils.isBlank(data)){
            message.add("更新数据条数", 0);
            return message;
        }
        Map<String, Object> dataMap = JsonUtils.fromJson(data);
        if(MapUtils.isEmpty(dataMap)){
            message.add("更新数据条数", 0);
            return message;
        }
        Map<String, ExamPaperEntity> paperEntityMap = examPaperDao.findByPaperId(dataMap.keySet());
        if(MapUtils.isEmpty(paperEntityMap)){
            message.add("更新数据条数", 0);
            return message;
        }

        Integer count = 0;
        for(String k : paperEntityMap.keySet()){
            ExamPaperEntity v = paperEntityMap.get(k);
            int planTimes = SafeConverter.toInt(dataMap.get(k));
            planTimes = planTimes < 0 ? 0 : planTimes;
            v.setPlanTimes(planTimes);
            examPaperDao.replace(v);
            count ++;
        }
        message.add("更新数据条数", count);
        return message;
    }

    @RequestMapping(value = "add_workflow_record_by_orderids.vpage")
    @ResponseBody
    public MapMessage addWorkflowRecordByOrderId() {
        MapMessage message = MapMessage.successMessage();
        String orderIdsStr = getRequestString("orderIds");
        if(StringUtils.isBlank(orderIdsStr)){
            message.add("更新数据条数", 0);
            return message;
        }
        int successCount = 0;
        int errorCount = 0;
        String[] orderIds = StringUtils.split(orderIdsStr, ",");
        for(String orderId : orderIds){
            if(StringUtils.isNotBlank(orderId) && SafeConverter.toLong(StringUtils.trim(orderId)) > 0){
                MapMessage tmpMessage = userOrderService.addWorkflowRecord(SafeConverter.toLong(StringUtils.trim(orderId)));
                if(tmpMessage.isSuccess()){
                    successCount++;
                }else {
                    errorCount++;
                }
            }
        }
        message.add("成功：", successCount);
        message.add("失败：", errorCount);
        return message;
    }

    /**
     * 将任务流转到Crm
     * @return mapMessage
     */
    @ResponseBody
    @RequestMapping(value = "transfer_task_to_crm.vpage")
    public MapMessage transferTaskDetailsToCrm(){
        String ids = getRequestString("taskDetailIds");
        if(StringUtils.isBlank(ids)){
            return MapMessage.successMessage();
        }
        String[] idArr = StringUtils.split(ids, ",");
        Set<String> idSet = new HashSet<>();
        for(String id : idArr){
            if(StringUtils.isNotBlank(id)){
                idSet.add(id);
            }
        }
        return agentTaskService.transferToCrmByIds(idSet);
    }

    @ResponseBody
    @RequestMapping(value = "transfer_task_to_crm_by_time.vpage")
    public MapMessage transferTaskDetailsToCrmByTime(){
        Date startDate = getRequestDate("startDate", DateUtils.addDays(new Date(), -1));
        Date endDate = getRequestDate("endDate", new Date());

        List<AgentTaskDetail> taskDetails = agentTaskDetailDao.findByCreateTime(startDate, endDate);
        if(CollectionUtils.isEmpty(taskDetails)){
            return MapMessage.successMessage().add("执行数据条数", 0);
        }
        List<AgentTaskCategory> targetCategories = new ArrayList<>();
        targetCategories.add(AgentTaskCategory.TEACHER_CHANGE_SCHOOL);
        targetCategories.add(AgentTaskCategory.TEACHER_CREATE_CLAZZ);
        targetCategories.add(AgentTaskCategory.TEACHER_BIND_MOBILE);

        List<String> ids = taskDetails.stream()
                .filter(p -> p.getCategory() != null && targetCategories.contains(p.getCategory()))
                .map(AgentTaskDetail::getId)
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(ids)){
            return MapMessage.successMessage().add("执行数据条数", 0);
        }
        return agentTaskService.transferToCrmByIds(ids);
    }


    @ResponseBody
    @RequestMapping(value = "active_users.vpage")
    public MapMessage activeUsers(){
        Set<Long> userIds = requestLongSet("userIds");
        if(CollectionUtils.isEmpty(userIds)){
            return MapMessage.successMessage();
        }
        Integer totalCount = userIds.size();
        Integer count = 0;
        for(Long p : userIds){
            AgentUser user = agentUserLoaderClient.loadIncludeDel(p);
            if(user == null){
                continue;
            }
            if(user.isValidUser()){
                continue;
            }
            user.setStatus(1);
            agentUserServiceClient.update(p, user);
            count ++;
        }
        return MapMessage.successMessage().add("总数", totalCount).add("更新数", count);
    }

    @ResponseBody
    @RequestMapping(value = "refresh_palace_data.vpage")
    public MapMessage refreshPalaceActivityData2(){
        String activityId = getRequestString("activityId");
        if(StringUtils.isBlank(activityId)){
            return MapMessage.errorMessage();
        }


        Date startDate = getRequestDate("startDate", DateUtils.stringToDate("20190124", "yyyyMMdd"));
        Date endDate = getRequestDate("endDate", new Date());

        Set<Long> studentIds = requestLongSet("studentIds");



        Long courseId = 25001L;

        List<PalaceActivityRecord> recordList = new ArrayList<>();
        Set<Long> targetUserIds = new HashSet<>();

        if(CollectionUtils.isNotEmpty(studentIds)){
            studentIds.forEach(s -> {
                List<PalaceActivityRecord> list = palaceActivityRecordDao.loadByStudentId(s);
                if(CollectionUtils.isNotEmpty(list)){
                    recordList.addAll(list);

                    list.forEach(t -> targetUserIds.add(t.getUserId()));
                }
            });
        }else {
            AgentGroup group = baseOrgService.getGroupByName("市场部");
            if (group == null) {
                return MapMessage.successMessage();
            }

            List<AgentGroupUser> groupUserList = baseOrgService.getAllGroupUsersByGroupId(group.getId());
            Set<Long> userIds = groupUserList.stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
            if(CollectionUtils.isNotEmpty(userIds)){
                targetUserIds.addAll(userIds);
            }

            recordList.addAll(palaceActivityRecordDao.loadByActivityAndUserAndTime(activityId, userIds, startDate, endDate));
        }
        if(CollectionUtils.isNotEmpty(recordList)){
            List<PalaceActivityRecord> targetRecordList = recordList.stream().filter(p -> p.getStudentId() != null).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(targetRecordList)){
                targetRecordList.forEach(p -> {

                    List<StudyCourseFinishStructLessonHistory> attendClassList = dpStudyCourseUserDataLoader.loadFinishLessonHistoryBySidAndSkuId(p.getStudentId(), courseId);
                    if(CollectionUtils.isEmpty(attendClassList)){
                        p.setAttendClassDayCount(0);
                        p.setAttendClassCourseCount(0);
                    }else {
                        p.setAttendClassCourseCount(attendClassList.size());

                        Set<Integer> days = attendClassList.stream().map(t -> SafeConverter.toInt(DateUtils.dateToString(t.getCreateDate(), "yyyyMMdd"))).collect(Collectors.toSet());
                        p.setAttendClassDayCount(days.size());
                        p.setAttendClassLatestDay(days.stream().max(Comparator.comparing(Function.identity())).get());
                    }
                    palaceActivityRecordDao.replace(p);
                });
            }
        }


        palaceActivityService.updateUserStatisticData(activityId, targetUserIds, startDate, endDate);

        return MapMessage.successMessage();
    }
    /**
     * 初始化试卷的测评计划，（试卷的测评计划以第一次上线测评为准）
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "mexam_init_planform_job.vpage")
    public MapMessage mexamInitPlanForm(){
        examPaperService.initPlanForm();
        return MapMessage.successMessage();
    }

    /**
     * 迁移蜂巢粉丝关联异业机构数据
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "mv_partner_linkman_ref.vpage")
    public MapMessage mvPartnerLinkmanRef(){
        return agentPartnerService.mvPartnerLinkmanRef();
    }


    @ResponseBody
    @RequestMapping(value = "cal_activity_statistics_data.vpage")
    public MapMessage calActivityStatisticsData(){
        activityStatisticsService.calOrderStatisticsData();
        return MapMessage.successMessage();
    }
}
