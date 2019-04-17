/**
 * Author:   xianlong.zhang
 * Date:     2018/10/17 18:39
 * Description:
 * History:
 */
package com.voxlearning.utopia.agent.service.worksheet;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.WorkSheetEvent;
import com.voxlearning.utopia.agent.dao.mongo.worksheet.WorkSheetDao;
import com.voxlearning.utopia.agent.dao.mongo.worksheet.WorkSheetLogDao;
import com.voxlearning.utopia.agent.persist.entity.worksheel.WorkSheet;
import com.voxlearning.utopia.agent.persist.entity.worksheel.WorkSheetLog;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class WorkSheetService  extends AbstractAgentService {

    @Inject private WorkSheetDao workSheetDao;
    @Inject private WorkSheetLogDao workSheetLogDao;

    @Inject private AgentUserLoaderClient agentUserLoaderClient;
    public List<WorkSheet> getUserWorkSheetList(Integer taskStatus){
        AuthCurrentUser currentUser = getCurrentUser();
        List<WorkSheet> list = workSheetDao.findWorkSheetListByUserPhone(currentUser.getUserPhone());
        if(CollectionUtils.isNotEmpty(list) && taskStatus != null && taskStatus > 0){
            list = list.stream().filter(p -> Objects.equals(p.getStatus(),taskStatus)).collect(Collectors.toList());
        }
        return list;
    }

    public Map<String,Object> getWorkSheetInfo(Long sheetId){
        WorkSheet workSheet = workSheetDao.findWorkSheetBySheetId(sheetId);
        if(workSheet == null){
            return Collections.emptyMap();
        }
        List<WorkSheetLog> logList = workSheetLogDao.findLogBySheetId(sheetId);
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("id",workSheet.getId());
        resultMap.put("sheetId",workSheet.getSheetId());
        resultMap.put("title",workSheet.getTitle());
        resultMap.put("content",workSheet.getContent());
        resultMap.put("status",workSheet.getStatus());
        resultMap.put("time",workSheet.getTime());
        resultMap.put("creator",workSheet.getCreator() != null ? workSheet.getCreator().get("name") : "");
        resultMap.put("attachList",workSheet.getAttachList());
        resultMap.put("priority",workSheet.getPriority());
        resultMap.put("templateName",workSheet.getTemplate() != null ? workSheet.getTemplate().get("name") : "");
        resultMap.put("templateId",workSheet.getTemplate() != null ? workSheet.getTemplate().get("id") : "");
        resultMap.put("customFiledList",workSheet.getCustomFiledList());
        List<Map<String,Object>> list = new ArrayList<>();
        logList.forEach(p->{
            Map<String,Object> map = new HashMap<>();
            map.put("operatorName",p.getOperator() != null ? p.getOperator().get("name") : "");
            map.put("time",p.getTime());
            map.put("sheetId",p.getSheetId());
            map.put("attachList",p.getAttachList());
            map.put("remark",p.getRemark());
            list.add(map);
        });
        resultMap.put("serviceItem",list);
        return resultMap;
    }

    public void saveInsertEvent(WorkSheet workSheet){
        Long sheetId = workSheet.getSheetId();
        //已经存在了就不再保存
        WorkSheet sheet = workSheetDao.findWorkSheetBySheetId(sheetId);
        if(sheet != null){
            return;
        }
        Map<String,Object> userMap = workSheet.getUser();
        Object phone = userMap.get("phone");
        String phoneStr = SafeConverter.toString(phone);
        if(StringUtils.isNotBlank(phoneStr)){
            AgentUser agentUser = agentUserLoaderClient.findByMobile(phoneStr);
            if(agentUser != null){
                workSheet.setMarketingPerson(true);
            }
        }else {
            workSheet.setMarketingPerson(false);
        }

        workSheet.setStatus(1);
        workSheetDao.upsert(workSheet);
    }
    public void saveOtherEvent(WorkSheetLog workSheetLog){
        //主记录不存在 修改信息就不保存了
        WorkSheet workSheet = workSheetDao.findWorkSheetBySheetId(workSheetLog.getSheetId());
        if(workSheet == null){
            return;
        }
        workSheetLogDao.upsert(workSheetLog);

        //status 1 未受理  2 受理中  3 已完结 4 已驳回
        int status = 0;
        if(Objects.equals(workSheetLog.getEvent() ,WorkSheetEvent.FINISH.getTypeId())){
            status = 3;
        }else if(Objects.equals(workSheetLog.getEvent() ,WorkSheetEvent.REJECT.getTypeId())){
            status = 4;
        }else if(WorkSheetEvent.eventMap.keySet().contains(workSheetLog.getEvent())) {
            status = 2;
        }

        workSheet.setStatus(status);
        workSheetDao.upsert(workSheet);
    }

    public void  deleteByIds(Collection<String> ids){
        workSheetDao.removes(ids);
        workSheetLogDao.removes(ids);
    }
}
