/**
 * Author:   xianlong.zhang
 * Date:     2018/12/6 18:22
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.service.mobile.workrecord;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;
import com.voxlearning.utopia.service.crm.api.constants.agent.FollowUpType;
import com.voxlearning.utopia.service.crm.api.entities.agent.work.WorkRecordTeacher;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.work.WorkRecordTeacherLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.work.WorkRecordTeacherServiceClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class WorkRecordTeacherService {
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject
    private WorkRecordTeacherLoaderClient workRecordTeacherLoaderClient;

    @Inject
    private WorkRecordTeacherServiceClient workRecordTeacherServiceClient;
    @Inject
    private AgentMemorandumService agentMemorandumService;


    public void addFollow(Long teacherId, String content, FollowUpType followUpType){
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if(teacher == null){
            return;
        }
        School school = asyncTeacherServiceClient.getAsyncTeacherService().loadTeacherSchool(teacherId).getUninterruptibly();

        WorkRecordTeacher workRecordTeacher = new WorkRecordTeacher();
        workRecordTeacher.setContent(content);
        workRecordTeacher.setTeacherId(teacherId);
        workRecordTeacher.setTeacherName(teacher.getProfile().getRealname());
        workRecordTeacher.setSubjects(new ArrayList<>(Collections.singleton(teacher.getSubject())));
        if(school != null){
            workRecordTeacher.setSchoolId(school.getId());
            workRecordTeacher.setSchoolName(school.getCname());
        }
        workRecordTeacher.setFollowUpType(followUpType);
        workRecordTeacherServiceClient.insert(workRecordTeacher);
    }

    public MapMessage getFollow(String id){
        WorkRecordTeacher workRecordTeacher = workRecordTeacherLoaderClient.load(id);
        if(workRecordTeacher == null){
            return MapMessage.errorMessage("拜访已删除或者不存在");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("id",workRecordTeacher.getId());
        map.put("content",workRecordTeacher.getContent());
        map.put("followUpType",workRecordTeacher.getFollowUpType().name());
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("data",map);
        return mapMessage;
    }

    public MapMessage delFollow(String id){
        WorkRecordTeacher workRecordTeacher = workRecordTeacherLoaderClient.load(id);
        if(workRecordTeacher == null){
            return MapMessage.successMessage();
        }
        if(DateUtils.isSameDay(workRecordTeacher.getCreateTime(),new Date())){
            workRecordTeacherServiceClient.update(workRecordTeacher);
        }
        return MapMessage.successMessage();
    }

    public MapMessage updateFollow(String id, String content,FollowUpType followUpType){
        WorkRecordTeacher workRecordTeacher = workRecordTeacherLoaderClient.load(id);
        if(!DateUtils.isSameDay(workRecordTeacher.getCreateTime(),new Date())){
            return MapMessage.errorMessage("只能更新当天数据");
        }

        workRecordTeacher.setContent(content);
        workRecordTeacher.setFollowUpType(followUpType);
        workRecordTeacherServiceClient.update(workRecordTeacher);
        return MapMessage.successMessage();
    }

    public MapMessage followList(Long teacherId){

        List<WorkRecordTeacher> newTeacherList = workRecordTeacherLoaderClient.loadByTeacherId(teacherId);
        Date endTime = DateUtils.calculateDateDay(new Date(),-180);
        List<AgentMemorandum> list = agentMemorandumService.loadMemorandumByTeacherId(teacherId,null,null,endTime);
        List<Map<String,Object>> resultList = new ArrayList<>();
        newTeacherList.forEach(nt -> {
            Map<String,Object> map = new HashMap<>();
            map.put("id",nt.getId());
            map.put("content",nt.getContent());
            map.put("createTime",nt.getCreateTime());
            map.put("followUpType",nt.getFollowUpType().name());
            resultList.add(map);
        });
        list.forEach(ot ->{
            Map<String,Object> map = new HashMap<>();
            map.put("id",ot.getId());
            map.put("content",ot.getContent());
            map.put("createTime",ot.getCreateTime());
            if(StringUtils.isNotBlank(ot.getIntoSchoolRecordId())){
                map.put("followUpType",FollowUpType.SCHOOL.name());
            }else{
                map.put("followUpType","");
            }
            resultList.add(map);
        });
        return MapMessage.successMessage("dataList",resultList);
    }
}
