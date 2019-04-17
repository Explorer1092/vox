package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.dao.CrmTaskRecordDao;
import com.voxlearning.utopia.agent.dao.mongo.AgentTaskDetailDao;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.mobile.resource.TeacherResourceService;
import com.voxlearning.utopia.agent.view.teacher.TeacherBasicInfo;
import com.voxlearning.utopia.api.constant.AgentTaskStatus;
import com.voxlearning.utopia.entity.agent.AgentTaskDetail;
import com.voxlearning.utopia.entity.crm.CrmTaskRecord;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.CrmWorkRecordLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yaguang.wang on 2016/5/12.
 */
@Named
public class AgentTaskDetailService extends AbstractAgentService {

    @Inject private CrmWorkRecordLoaderClient crmWorkRecordLoaderClient;

    @Inject AgentTaskDetailDao agentTaskDetailDao;

    @Inject private CrmTaskRecordDao crmTaskRecordDao;

    @Inject private TeacherResourceService teacherResourceService;

    @Inject private  AgentTaskService agentTaskService;
    public MapMessage finishTaskDetailStatus(String taskDetailId, Long userId) {
        AgentTaskDetail agentTaskDetail = findAgentTaskDetailById(taskDetailId);

        if (agentTaskDetail == null) {
            return MapMessage.errorMessage("无法找到任务详情 任务ID={}", taskDetailId);
        }
        if (hasWorkRecord(taskDetailId, userId)) {
            agentTaskDetail.setStatus(AgentTaskStatus.FINISHED);
        } else {
            return MapMessage.errorMessage("请先填写任务记录");
        }
        try {
            agentTaskDetailDao.upsert(agentTaskDetail);
        } catch (Exception ex) {
            return MapMessage.errorMessage("跟新任务详情失败");
        }
        return MapMessage.successMessage("任务已完成");
    }

    public AgentTaskDetail findAgentTaskDetailById(String taskDetailId) {
        if (StringUtils.isEmpty(taskDetailId)) {
            return null;
        }
        return agentTaskDetailDao.load(taskDetailId);
    }


//    public MapMessage deleteTaskDetailStatus(String taskId, boolean disabled) {
//        try {
//            return agentTaskDetailDao.updateTaskDetailByTaskId(taskId, disabled);
//        } catch (Exception ex) {
//            return MapMessage.errorMessage("任务详情无法删除");
//        }
//    }

//    private AgentTaskDetail loadTaskDetail(String id) {
//        return id == null ? null : agentTaskDetailDao.load(id);
//    }

    //是否有工作记录
    private boolean hasWorkRecord(String agentTaskDetailId, Long userId) {
        long workRecordCount = crmWorkRecordLoaderClient.countByTaskDetailIdAndWorkerId(agentTaskDetailId, userId);
        return workRecordCount > 0;//有工作记录
    }

    public void deleteTaskDetail(String docId) {
        if (StringUtils.isBlank(docId)) {
            return;
        }
        AgentTaskDetail agentTaskDetail = agentTaskDetailDao.load(docId);
        if (agentTaskDetail != null) {
            agentTaskDetail.setDisabled(true);
            agentTaskDetailDao.upsert(agentTaskDetail);
        }
    }

    private boolean finishTaskDetail(String docId) {
        if (StringUtils.isBlank(docId)) {
            return false;
        }
        AgentTaskDetail agentTaskDetail = agentTaskDetailDao.load(docId);
        if (agentTaskDetail != null) {
            agentTaskDetail.setStatus(AgentTaskStatus.FINISHED);
            agentTaskDetailDao.upsert(agentTaskDetail);
            return true;
        }
        return false;
    }

    public List<AgentTaskDetail> findByNeedCustomerService(String taskId) {
        List<AgentTaskDetail> taskDetailList = agentTaskDetailDao.findByTaskId(taskId);
        if (CollectionUtils.isEmpty(taskDetailList)) {
            return Collections.emptyList();
        }
        return taskDetailList.stream().filter(AgentTaskDetail::getNeedCustomerService).collect(Collectors.toList());
    }

    public List<AgentTaskDetail> findByUserId(Long userId) {
        return agentTaskDetailDao.findByUserId(userId);
    }

    public List<AgentTaskDetail> findByTaskId(String taskId) {
        return agentTaskDetailDao.findByTaskId(taskId);
    }


    // 修改流转回市场的任务状态
    public boolean updateTransferBackStatus(String taskDetailId, boolean needAgentFollow) {
        AgentTaskDetail agentTaskDetail = agentTaskDetailDao.load(taskDetailId);

        if (agentTaskDetail == null) {
            return false;
        }
        //不需要市场人员跟踪任务认为任务可以完成做完成任务的校验
        if (!needAgentFollow) {
            agentTaskDetail.setStatus(AgentTaskStatus.FINISHED);
        } else {
            agentTaskDetail.setStatus(AgentTaskStatus.FINISHED);
        }
        agentTaskDetailDao.upsert(agentTaskDetail);
        return true;
    }

    public boolean updateStatus(String taskDetailId, AgentTaskStatus status) {
        if (StringUtils.isBlank(taskDetailId)) {
            return false;
        }
        AgentTaskDetail agentTaskDetail = agentTaskDetailDao.load(taskDetailId);
        if (agentTaskDetail == null) {
            return false;
        }
        agentTaskDetail.setStatus(status);
        agentTaskDetailDao.upsert(agentTaskDetail);
        return true;
    }

    public MapMessage findTaskDetailInfo(String taskDetailId){
        MapMessage mapMessage = MapMessage.successMessage();
        if(StringUtils.isBlank(taskDetailId)){
            return MapMessage.errorMessage("taskDetailId 不能为空");
        }
        AgentTaskDetail agentTaskDetail = agentTaskDetailDao.load(taskDetailId);
        if(agentTaskDetail == null){
            return MapMessage.errorMessage("未找到对应的任务");
        }
        TeacherBasicInfo teacherBasicInfo = teacherResourceService.generateTeacherBasicInfo(agentTaskDetail.getTeacherId(), false);


        List<CrmTaskRecord> crmTaskRecordList = crmTaskRecordDao.findByAgentTaskDetailId(taskDetailId);
        List<String> CSRemark = new ArrayList<>();
        for (CrmTaskRecord crmTaskRecord :  crmTaskRecordList){
            if(StringUtils.isNotBlank(crmTaskRecord.getContent())){
                CSRemark.add(crmTaskRecord.getContent());
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("taskDetailId",agentTaskDetail.getId());
        map.put("category",agentTaskDetail.getCategory() == null ? "" : agentTaskDetail.getCategory().getValue());
        map.put("content",agentTaskDetail.getContent());
        map.put("taskStatus",agentTaskDetail.getStatus() == null ? "" : agentTaskDetail.getStatus().getValue());
        map.put("CSRemark",CSRemark);
        map.put("teacherId",teacherBasicInfo.getTeacherId());
        map.put("expired",agentTaskDetail.isExpired());
        map.put("teacherName",teacherBasicInfo.getTeacherName());
        map.put("schoolId",teacherBasicInfo.getSchoolId());
        map.put("schoolName",teacherBasicInfo.getSchoolName());
        map.put("schoolLevel",teacherBasicInfo.getSchoolLevel());
        map.put("subjects",teacherBasicInfo.getSubjects());
        mapMessage.put("dataMap",map);
        return mapMessage;
    }

    public List<Map<String,Object>> findTeacherTaskDetailList(Long teacherId){
        List<AgentTaskDetail> list = agentTaskDetailDao.findByTeacherId(teacherId);
        list =  list.stream().filter( p -> p.getCreateTime() != null && p.getCreateTime().after(DateUtils.addDays(new Date(),-60)) && p.getStatus() ==AgentTaskStatus.FOLLOWING ).collect(Collectors.toList());
        Collections.sort(list, (o1, o2) -> {
            if(o1.getCreateTime().before(o2.getCreateTime())){
                return 1;
            }else {
                return -1;
            }
        });

        return agentTaskService.getDetailList(list);
    }

}
