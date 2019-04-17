package com.voxlearning.utopia.agent.controller.messagecenter;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.bean.group.GroupData;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.constants.AgentPushType;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentNotify;
import com.voxlearning.utopia.agent.persist.entity.AgentNotifyUser;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentGroupRoleAuthority;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentMessage;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentMessageUser;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.messagecenter.AgentMessageService;
import com.voxlearning.utopia.agent.service.messagecenter.AgentMessageUserService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupUserLoaderClient;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/message/manage/")
@Slf4j
public class AgentMessageController extends AbstractAgentController {

    @Inject private AgentMessageService agentMessageService;

    @Inject private BaseOrgService baseOrgService;

    @Inject private AgentNotifyService agentNotifyService;

    @Inject private AgentGroupUserLoaderClient agentGroupUserLoaderClient;

    @Inject private AgentMessageUserService agentMessageUserService;
    // list页
    @RequestMapping("message_list.vpage")
    public String list(Model model){
        Integer messageType = getRequestInt("messageType",1);
        model.addAttribute("messageType",messageType);
        return "/message/manage/message_list";
    }
    // 新建页
    @RequestMapping("createPage.vpage")
    public String createPage(Model model){
        agentMessageService.groupRoleType(model);
        return "/message/manage/createPage";
    }

    @RequestMapping(value = "edit_page.vpage")
    public String  edit(Model model){
        String id = getRequestString("id");
        Integer messageType = requestInteger("messageTypes");//消息类型 1 push消息 2 系统消息

        if (StringUtils.isNotBlank(id)){
            if(1 == messageType){
                AgentMessage pushMessage = agentMessageService.findPushById(id);
                AgentGroupRoleAuthority authority = agentMessageService.getGroupRoleAuthorityBySourceId(id);
                model.addAttribute("messageInfo",pushMessage);
                model.addAttribute("groupIds",StringUtils.join(authority.getGroupIdList()));
                model.addAttribute("roleTypes",authority.getRoleTypeList());
                model.addAttribute("userIds",authority.getUserIds() != null ? StringUtils.join(authority.getUserIds(),"\n") : "");
                model.addAttribute("groupListWithName",agentMessageService.getSelectGroupByIds(authority.getGroupIdList()));
            }else if(2 ==messageType){
                AgentNotify agentNotify = agentNotifyService.getNotifyById(SafeConverter.toLong(id));
                AgentGroupRoleAuthority authority = agentMessageService.getGroupRoleAuthorityBySourceId(id);
                model.addAttribute("messageInfo",agentNotify);
                model.addAttribute("groupIds",StringUtils.join(authority.getGroupIdList()));
                model.addAttribute("roleTypes",authority.getRoleTypeList());
                model.addAttribute("userIds",authority.getUserIds() != null ? StringUtils.join(authority.getUserIds(),"\n") : "");
                model.addAttribute("groupListWithName",agentMessageService.getSelectGroupByIds(authority.getGroupIdList()));
            }
        }
//        model.addAttribute("allRoleTypeList",AgentRoleType.values());
        agentMessageService.groupRoleType(model);
        model.addAttribute("messageTypes",requestString("messageTypes"));
        return "/message/manage/edit_page";
    }

    // 保存功能
    @RequestMapping(value = "saveData.vpage",method = RequestMethod.GET)
    @ResponseBody
    public MapMessage saveData() {
        String id = getRequestString("id");
        Set<Integer> messageTypes = requestIntegerSet("messageTypes");//消息类型 1 push消息 2 系统消息
        String pushContent = getRequestString("pushContent");//push内容
        Integer expireTime = getRequestInt("expireTime"); //过期时间  （push消息在极光的有效期 超过多久未发送失效）
        String notifyType = getRequestString("notifyType"); //系统消息类型  目前只有  SYSTEM
        String notifyTitle = getRequestString("notifyTitle");            // 通知题目
        String notifyContent = getRequestString("notifyContent");           // 通知内容

        String photoUrl = getRequestString("photoUrl");  //系统配图地址  目前只做 不展示
        String linkUrl = getRequestString("linkUrl"); //跳转链接地址
        Integer sendRange =getRequestInt("sendRange");//发送范围 1 指定部门 2 指定用户

        Set<Long> groupIds = requestLongSet("groupIds"); //选择的部门列表
        Set<Integer> roleIds = requestIntegerSet("roleIds");
//        Set<Long> userIds = requestLongSet("userIds","\n");
        //前端过来的数据有可能是
        //21,12,13
//        14
//        15  有逗号有回车分隔的数据
        String userIdStr = requestString("userIds");
        List<Long> realIdList = null;
        if(messageTypes.contains(1)){
            if(StringUtils.isBlank(pushContent)){
                MapMessage.errorMessage("请填写消息内容");
            }
            if(0 == expireTime){
                expireTime = 14;
            }
        }else if(messageTypes.contains(2) ){
            if(StringUtils.isBlank(notifyType)){
                MapMessage.errorMessage("请填写系统消息类型");
            }
            if(StringUtils.isBlank(notifyTitle)){
                MapMessage.errorMessage("请填写系统消息标题");
            }
            if(StringUtils.isBlank(notifyContent)){
                MapMessage.errorMessage("请填写系统消息内容");
            }
        }else{
            MapMessage.errorMessage("消息类型不正确");
        }
        Set<Long> userIds = new HashSet<>();
        if(1 == sendRange){
            if(groupIds.size()<1 && roleIds.size()<1){
                MapMessage.errorMessage("部门或角色至少要选择一个");
            }

        }else if(2 == sendRange){
            if(StringUtils.isNotBlank(userIdStr)){
                userIdStr = userIdStr.replaceAll(",","\n");
                String[] idArr = userIdStr.split("\n");
                for (int i = 0; i < idArr.length; i++) {
                    userIds.add(SafeConverter.toLong(idArr[i]));
                }
            }
            if(userIds.size() < 1){
                MapMessage.errorMessage("至少要有一个联系人");
            }
            realIdList = baseOrgService.getUsers(userIds).stream().map(AgentUser::getId).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(realIdList)){
                return MapMessage.errorMessage("填写的用户ID无效");
            }
        }else {
            MapMessage.errorMessage("消息类型不正确");
        }
        List<AgentRoleType> roleTypeList = roleIds.stream().map(AgentRoleType::of).filter(Objects::nonNull).collect(Collectors.toList());
        AuthCurrentUser user = getCurrentUser();
        agentMessageService.saveData(id,messageTypes,notifyType,pushContent,notifyTitle,notifyContent,photoUrl,linkUrl,sendRange,user.getUserId(),user.getUserName(),realIdList,roleTypeList,new ArrayList<>(groupIds),expireTime);
        return MapMessage.successMessage();
    }

    /**
     * 获取发布部门（所有部门树）
     * @return
     */
    @RequestMapping(value = "get_user_department_tree.vpage" , method = RequestMethod.GET)
    @ResponseBody
    public String getUserDepartmentTree(){
        Set<Long> groupIds = requestLongSet("groupIds"); //选择的部门列表

       /* AuthCurrentUser user = getCurrentUser();
        List<Map<String, Object>> list = baseOrgService.loadUserGroupTree(user);
        if(CollectionUtils.isNotEmpty(list) && CollectionUtils.isNotEmpty(groupIds)){
            baseOrgService.markSelectedGroup(list,new ArrayList<>(groupIds));
        }*/
        Map<String, Map<String, Object>> allGroupTree = baseOrgService.buildAllGroupTree();
        List<Map<String, Object>> allGroupList = new ArrayList<>();

        //获取admin管理员权限范围的部门
        List<Long> groupIdList = new ArrayList<>();
        List<AgentGroup> groupList = baseOrgService.getRootAgentGroups();
        if (CollectionUtils.isNotEmpty(groupList)) {
            groupIdList.addAll(groupList.stream().map(AbstractDatabaseEntity::getId).collect(Collectors.toSet()));
        }
        for (Long groupId : groupIdList) {
            CollectionUtils.addNonNullElement(allGroupList, allGroupTree.get(String.valueOf(groupId)));
        }

        if(CollectionUtils.isNotEmpty(groupIds) && CollectionUtils.isNotEmpty(allGroupList)){
            baseOrgService.markSelectedGroup(allGroupList, groupIds);
        }
        return JsonUtils.toJson(allGroupList);
    }

    @RequestMapping(value = "agent_message_list.vpage")
    @ResponseBody
    public MapMessage getMessageList(){
        Integer messageType = getRequestInt("messageType");
        Long createUserId = requestLong("createUserId");
        Date beginDate = requestDate("beginDate");
        Date endDate = requestDate("endDate");
        Integer msgStatus = requestInteger("msgStatus");
        String notifyType = getRequestString("notifyType"); //系统消息类型  目前只有  SYSTEM

        if (endDate == null) {
            endDate = DateUtils.stringToDate(DateUtils.dateToString(new Date(),DateUtils.FORMAT_SQL_DATETIME));
        }
        if(beginDate == null || DateUtils.dayDiff(endDate, beginDate) > 31){
            beginDate = DateUtils.addMonths(endDate,-1);
        }
        if(beginDate != null){
            beginDate = DateUtils.stringToDate(DateUtils.dateToString(beginDate,DateUtils.FORMAT_SQL_DATETIME));
        }
        return agentMessageService.findMessageList(messageType,createUserId, beginDate,endDate,msgStatus,notifyType);
    }

    @RequestMapping(value = "send_message.vpage")
    @ResponseBody
    public MapMessage sendMessage(){
        MapMessage mapMessage = MapMessage.successMessage();
        String id = getRequestString("id");
        Integer messageType = getRequestInt("messageType");
        AgentGroupRoleAuthority authority = agentMessageService.getGroupRoleAuthorityBySourceId(id);
        if(1 == messageType){//push
            AgentMessage agentMessage = agentMessageService.findPushById(id);
            if(0 != agentMessage.getMsgStatus()){
                mapMessage.put("info","消息状态不正确不可以发送");
                return mapMessage;
            }
            agentMessageService.sendPushMessage(agentMessage,authority);
        }else {//notify
            AgentNotify agentNotify = agentNotifyService.getNotifyById(SafeConverter.toLong(id));
            if(0 != agentNotify.getMsgStatus()){
                return MapMessage.errorMessage("消息状态不正确不可以发送");
            }
            sendNotifyMessage(agentNotify,authority);
        }
        return mapMessage;
    }

    @RequestMapping(value = "send_silent_message.vpage")
    @ResponseBody
    public MapMessage sendSilentMessage(){
        Set<Long> userIds = requestLongSet("userIds");
        if(CollectionUtils.isEmpty(userIds)){
            userIds.add(getCurrentUserId());
        }
        agentMessageService.sendSilentMessageByIds(userIds, AgentPushType.NEW_MESSAGE);
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "delete_message.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteMessage(){
        MapMessage mapMessage = MapMessage.successMessage();
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)){
            return MapMessage.errorMessage("ID不正确");
        }
        Integer messageType = getRequestInt("messageType");
        if (messageType < 1){
            return MapMessage.errorMessage("请选择类型");
        }
        agentMessageService.deletePushMessage(id,messageType);
        return mapMessage;
    }

    //发送测试
    @RequestMapping(value = "test_send_message.vpage",method = RequestMethod.POST)
    @ResponseBody
    public MapMessage testSendMessage(){
        String id = getRequestString("messageId");
        Integer messageType = getRequestInt("messageType");
        Set<Long> userIds = requestLongSet("userIds");
        if (messageType < 1){
            return MapMessage.errorMessage("请选择类型");
        }

        List<Long> realIdList = baseOrgService.getUsers(userIds).stream().map(AgentUser::getId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(realIdList)){
            return MapMessage.errorMessage("填写的用户ID无效");
        }
        if (realIdList.size() > 1){
            return MapMessage.errorMessage("测试一次只能发送一个");
        }
        AgentGroupRoleAuthority authority = agentMessageService.getGroupRoleAuthorityBySourceId(id);
        if(1 == messageType){//push
            AgentMessage agentMessage = agentMessageService.findPushById(id);
            if(0 != agentMessage.getMsgStatus()){
                return MapMessage.errorMessage("info","消息状态不正确不可以发送");
            }
            agentMessageService.sendUserIdsPushMessage(agentMessage,realIdList,true);
        }else {//notify
            AgentNotify agentNotify = agentNotifyService.getNotifyById(SafeConverter.toLong(id));
            if(0 != agentNotify.getMsgStatus()){
                return MapMessage.errorMessage("消息状态不正确不可以发送");
            }
            List<AgentNotifyUser> notifyUsers = agentNotifyService.fingNotifyUserListByNotifyId(agentNotify.getId());
            if(CollectionUtils.isNotEmpty(notifyUsers)){
                List<Long> recivitedUser =  notifyUsers.stream().map(AgentNotifyUser::getUserId).collect(Collectors.toList());
                if(recivitedUser.contains(realIdList.get(0))){
                    return MapMessage.successMessage();
                }
            }
            agentNotifyService.saveNotifyUser(AgentNotifyType.GROUP_MESSAGE.getType(),agentNotify.getId(),CollectionUtils.toLinkedHashSet(realIdList));
        }
        return MapMessage.successMessage();
    }

    public void sendNotifyMessage(AgentNotify agentNotify, AgentGroupRoleAuthority authority){
        List<Long> userIds;
        if(agentNotify.getSendRange() == 1){//指定部门
            List<Long> groupIds = authority.getGroupIdList();
            List<AgentRoleType> roleTypes = authority.getRoleTypeList();
            userIds = agentMessageService.getGroupUsers(groupIds,roleTypes);
        }else{
            userIds = authority.getUserIds();
        }
        //测试发送过的不在保存
        List<AgentNotifyUser> notifyUsers = agentNotifyService.fingNotifyUserListByNotifyId(agentNotify.getId());
        if(CollectionUtils.isNotEmpty(notifyUsers)){
            notifyUsers.forEach(p->{
                if(userIds.contains(p.getUserId())){
                    userIds.remove(p.getUserId());
                }
            });
        }
        agentNotifyService.saveNotifyUser(AgentNotifyType.GROUP_MESSAGE.getType(),agentNotify.getId(),CollectionUtils.toLinkedHashSet(userIds));

        agentNotify.setSendNum(userIds.size());
        agentNotify.setSendDatetime(new Date());
        agentNotify.setMsgStatus(1);
        agentNotifyService.updateNotify(agentNotify);
    }

    /**
     * 发送消息详情导出
     */  @RequestMapping(value = "message_info_export.vpage", method = RequestMethod.GET)
    public void messageInfoExport() {
        try{
            String id = getRequestString("id");
            Integer messageType = getRequestInt("messageType");
            List<Map<String,Object>> reciverList = new ArrayList<>();
            List<Long> userIds ;
            if(1 != messageType && 2 != messageType){
                return;
            }else if(1 == messageType){
                AgentMessage agentMessage = agentMessageService.findPushById(id);
                if(agentMessage.getMsgStatus() !=1){
                    return;
                }
                List<AgentMessageUser> messageUserList = agentMessageUserService.findUserListByMessageId(id);
                Map<Long,AgentMessageUser> idMessagentUserMap = messageUserList.stream().collect(Collectors.toMap(AgentMessageUser::getUserId,Function.identity()));
                userIds = messageUserList.stream().map(AgentMessageUser::getUserId).collect(Collectors.toList());
                Map<Long, List<AgentGroupUser>>  userIdGroupUserMap = agentGroupUserLoaderClient.findByUserIds(userIds);
                Map<Long, AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
                //组装各部门之间的等级对应关系
                List<GroupData> groupDataList = new ArrayList<>();
                List<AgentGroup> allGroupList = baseOrgService.findAllGroups();
                Map<Long,List<AgentGroup>> parentGroupMap = allGroupList.stream().collect(Collectors.groupingBy(AgentGroup::getParentId));
                allGroupList.forEach(p -> {
                    if(parentGroupMap.keySet().contains(p.getId())){
//                        List<AgentGroup> subSubGroupList = baseOrgService.getGroupListByParentId(p.getId());
                        List<AgentGroup> subSubGroupList =  parentGroupMap.get(p.getId());
                        subSubGroupList.forEach(item -> {
                            baseOrgService.getSubGroupListWithGroupData(item.getId(),groupDataList);
                        });
                    }
                });
                Map<Long, GroupData> groupDataMap = groupDataList.stream().collect(Collectors.toMap(GroupData::getGroupId, Function.identity(), (o1, o2) -> o1));
                userIds.forEach(userId -> {
                    AgentMessageUser agentMessageUser = idMessagentUserMap.get(userId);
                    Map<String,Object> receicerMap = new HashMap<>();
                    receicerMap.put("userName", userMap.get(userId) == null ? "" : userMap.get(userId).getRealName());
                    //获取角色信息
                    AgentRoleType agentRoleType = null;
                    Integer roleId = null;
                    List<AgentGroupUser> groupUsers = userIdGroupUserMap.get(userId);
                    if(CollectionUtils.isNotEmpty(groupUsers)){
                        roleId = groupUsers.get(0).getUserRoleId();
                    }
                    if (null != roleId){
                        agentRoleType = AgentRoleType.of(roleId);
                        //设置“角色”
                        receicerMap.put("userRole", agentRoleType != null ? agentRoleType.getRoleName() : "");
                    }else {
                        receicerMap.put("userRole", "");
                    }
                    GroupData groupData = groupDataMap.get(groupUsers.get(0).getGroupId());
                    if (null != groupData){
                        receicerMap.put("marketingName",groupData.getMarketingName());
                        receicerMap.put("regionName",groupData.getRegionName());
                        receicerMap.put("areaName",groupData.getAreaName());
                        receicerMap.put("cityName",groupData.getCityName());
                    }
                    receicerMap.put("openDatetime",agentMessageUser.getReadFlag() == true ? DateUtils.dateToString(agentMessageUser.getUpdateTime(),DateUtils.FORMAT_SQL_DATETIME) :"");
//                    receicerMap.put("content",agentMessage.getPushContent());
//                    receicerMap.put("sendDatetime",agentMessage.getSendDatetime() != null ? DateUtils.dateToString(agentMessage.getSendDatetime(),DateUtils.FORMAT_SQL_DATETIME) : "");
                    reciverList.add(receicerMap);
                });
                //设置导出文件名
                String fileName = "消息发送列表-" + DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME) + ".xlsx";
                //导出Excel文件
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                agentMessageService.exportReceiverList(workbook,reciverList,agentMessage.getPushContent(),DateUtils.dateToString(agentMessage.getSendDatetime(),DateUtils.FORMAT_SQL_DATE));
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                workbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
                outStream.close();
                workbook.dispose();
            }else if(2 == messageType){
                AgentNotify agentNotify = agentNotifyService.getNotifyById(SafeConverter.toLong(id));
                if(agentNotify.getMsgStatus() !=1){
                    return;
                }
                List<AgentNotifyUser> notifyUserList = agentNotifyService.fingNotifyUserListByNotifyId(SafeConverter.toLong(id));
                Map<Long,AgentNotifyUser> idNotifyUserMap = notifyUserList.stream().collect(Collectors.toMap(AgentNotifyUser::getUserId,Function.identity()));
                userIds = notifyUserList.stream().map(AgentNotifyUser::getUserId).collect(Collectors.toList());
                Map<Long, List<AgentGroupUser>>  userIdGroupUserMap = agentGroupUserLoaderClient.findByUserIds(userIds);
                Map<Long, AgentUser> userMap = baseOrgService.getUsers(userIds).stream().collect(Collectors.toMap(AgentUser::getId, Function.identity(), (o1, o2) -> o1));
                //组装各部门之间的等级对应关系
                List<GroupData> groupDataList = new ArrayList<>();
                List<AgentGroup> allGroupList = baseOrgService.findAllGroups();
                allGroupList.forEach(p -> {
                    List<AgentGroup> subSubGroupList = baseOrgService.getGroupListByParentId(p.getId());
                    subSubGroupList.forEach(item -> {
                        baseOrgService.getSubGroupListWithGroupData(item.getId(),groupDataList);
                    });

                });
                Map<Long, GroupData> groupDataMap = groupDataList.stream().collect(Collectors.toMap(GroupData::getGroupId, Function.identity(), (o1, o2) -> o1));
                userIds.forEach(userId -> {
                    AgentNotifyUser agentMessageUser = idNotifyUserMap.get(userId);
                    Map<String,Object> receicerMap = new HashMap<>();
                    receicerMap.put("userName", userMap.get(userId) == null ? "" : userMap.get(userId).getRealName());
                    //获取角色信息
                    AgentRoleType agentRoleType = null;
                    Integer roleId = null;
                    List<AgentGroupUser> groupUsers = userIdGroupUserMap.get(userId);
                    if(CollectionUtils.isNotEmpty(groupUsers)){
                        roleId = groupUsers.get(0).getUserRoleId();
                    }
                    if (null != roleId){
                        agentRoleType = AgentRoleType.of(roleId);
                        //设置“角色”
                        receicerMap.put("userRole", agentRoleType != null ? agentRoleType.getRoleName() : "");
                    }else {
                        receicerMap.put("userRole", "");
                    }
                    GroupData groupData = groupDataMap.get(groupUsers.get(0).getGroupId());
                    if (null != groupData){
                        receicerMap.put("marketingName",groupData.getMarketingName());
                        receicerMap.put("regionName",groupData.getRegionName());
                        receicerMap.put("areaName",groupData.getAreaName());
                        receicerMap.put("cityName",groupData.getCityName());
                    }
                    receicerMap.put("openDatetime",agentMessageUser.getUpdateDatetime() != null ?DateUtils.dateToString(agentMessageUser.getUpdateDatetime(),DateUtils.FORMAT_SQL_DATETIME) : "");
//                    receicerMap.put("content",agentNotify.getNotifyContent());
//                    receicerMap.put("sendDatetime",DateUtils.dateToString(agentNotify.getSendDatetime(),DateUtils.FORMAT_SQL_DATETIME));
                    reciverList.add(receicerMap);
                });
                //设置导出文件名   把标题中的逗号替换成空格  文件名有逗号时chrome 会报错 网上说是chrome的bug
                String fileName = agentNotify.getNotifyTitle().replace(","," ")+ DateUtils.dateToString(agentNotify.getSendDatetime(),DateUtils.FORMAT_SQL_DATE) +".xlsx";
                //导出Excel文件
                SXSSFWorkbook workbook = new SXSSFWorkbook();
                agentMessageService.exportReceiverList(workbook,reciverList,agentNotify.getNotifyContent(),DateUtils.dateToString(agentNotify.getSendDatetime(),DateUtils.FORMAT_SQL_DATE) );
                @Cleanup ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                workbook.write(outStream);
                outStream.flush();
                HttpRequestContextUtils.currentRequestContext().downloadFile(
                        fileName,
                        "application/vnd.ms-excel",
                        outStream.toByteArray());
                outStream.close();
                workbook.dispose();
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("error info: ",e);
        }
    }

}
