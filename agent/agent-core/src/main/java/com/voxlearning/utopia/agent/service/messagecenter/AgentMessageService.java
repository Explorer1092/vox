package com.voxlearning.utopia.agent.service.messagecenter;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.HssfUtils;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentAuthorityType;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.constants.AgentPushType;
import com.voxlearning.utopia.agent.constants.JPushCrmType;
import com.voxlearning.utopia.agent.dao.mongo.messagecenter.AgentGroupRoleAuthorityDao;
import com.voxlearning.utopia.agent.dao.mongo.messagecenter.AgentMessageDao;
import com.voxlearning.utopia.agent.dao.mongo.messagecenter.AgentMessageUserDao;
import com.voxlearning.utopia.agent.persist.AgentNotifyPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentNotify;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentGroupRoleAuthority;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentMessage;
import com.voxlearning.utopia.agent.persist.entity.messagecenter.AgentMessageUser;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentGroupLoaderClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.ui.Model;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class AgentMessageService extends AbstractAgentService {
    @Inject private AgentMessageDao pushMessageDao;
    @Inject private AgentGroupRoleAuthorityDao agentGroupRoleAuthorityDao;
    @Inject private AgentNotifyPersistence agentNotifyPersistence;
    @Inject private AgentGroupLoaderClient agentGroupLoaderClient;
    @Inject private AgentMessageUserDao agentMessageUserDao;

    @Inject private BaseOrgService baseOrgService;
    @Inject private AppMessageServiceClient appMessageServiceClient;
    @Inject private AgentCacheSystem agentCacheSystem;

    //保存推送消息
    public MapMessage saveData(String id, Set<Integer> messageTypes, String notifyType, String pushContent, String notifyTitle, String notifyContent, String photoUrl, String linkUrl, Integer sendRange,
                               Long createUserId, String createUserName, List<Long> userIds, List<AgentRoleType> roleTypeList, List<Long> groupIdList, Integer expireTime){
        MapMessage mapMessage = MapMessage.successMessage();
        if(messageTypes.contains(1)){//push消息
            mapMessage = savePushMessage(id,pushContent,linkUrl,sendRange,createUserId,createUserName,expireTime,userIds,roleTypeList,groupIdList,mapMessage);
        }
        if (messageTypes.contains(2)){//系统消息
            mapMessage = saveNotifyMessage(id,notifyTitle,notifyContent,photoUrl,linkUrl,sendRange,createUserId,createUserName,userIds,roleTypeList,groupIdList,mapMessage);
        }
        if(messageTypes.contains(2) && messageTypes.contains(1) && StringUtils.isBlank(linkUrl)){//两种类型消息全选且跳转链接为空时
            String pushId = SafeConverter.toString(mapMessage.get("pushId"));
            AgentMessage agentMessage = pushMessageDao.load(pushId);
            Long notifyId = SafeConverter.toLong(mapMessage.get("notifyId"));
            AgentNotify agentNotify = agentNotifyPersistence.load(notifyId);
            if(agentMessage != null){
                agentMessage.setLinkUrl("/mobile/notice/noticeList.vpage?category=system");
                pushMessageDao.upsert(agentMessage);
            }
            if(agentNotify != null){
                agentNotify.setNotifyUrl("/mobile/notice/noticeList.vpage?category=system");
                agentNotifyPersistence.update(notifyId,agentNotify);
            }
        }
        return mapMessage;
    }

    public MapMessage savePushMessage(String id, String pushContent, String linkUrl, Integer sendRange, Long createUserId, String createUserName, Integer expireTime, List<Long> userIds, List<AgentRoleType> roleTypeList, List<Long> groupIdList, MapMessage mapMessage){

        AgentMessage agentMessage = pushMessageDao.load(id);
        if(agentMessage != null && agentMessage.getDisabled()){
            return MapMessage.errorMessage("要保存的记录为删除状态，无法保存！");
        }
        if(agentMessage == null){
            agentMessage = new AgentMessage();
            agentMessage.setDisabled(false);
            agentMessage.setOpenNum(0);

        }
        agentMessage.setPushContent(pushContent);

        agentMessage.setLinkUrl(linkUrl);
        agentMessage.setSendRange(sendRange);
        agentMessage.setCreateUserId(createUserId);
        agentMessage.setCreateUserName(createUserName);
        agentMessage.setMsgStatus(0);
        agentMessage.setOpenNum(0);
        agentMessage.setExpireTime(expireTime);
        agentMessage =  pushMessageDao.upsert(agentMessage);
        saveAgentGroupRoleAuthority(agentMessage.getId() ,userIds,roleTypeList, groupIdList);
        mapMessage.put("pushId",agentMessage.getId());
        return mapMessage;
    }

    public MapMessage saveNotifyMessage(String id, String notifyTitle, String notifyContent, String photoUrl, String linkUrl, Integer sendRange, Long createUserId, String createUserName, List<Long> userIds, List<AgentRoleType> roleTypeList, List<Long> groupIdList, MapMessage mapMessage){
        AgentNotify agentNotify =  agentNotifyPersistence.load(SafeConverter.toLong(id));
        if(agentNotify != null && agentNotify.getDisabled()!= null && agentNotify.getDisabled()){
            return MapMessage.errorMessage("要保存的记录为删除状态，无法保存！");
        }
        boolean createFlag = false;
        if(agentNotify == null){
            agentNotify = new AgentNotify();
            agentNotify.setDisabled(false);
            agentNotify.setOpenNum(0);
            createFlag = true;
        }
        agentNotify.setNotifyTitle(notifyTitle);
        agentNotify.setNotifyContent(notifyContent);
        agentNotify.setPhotoUrl(photoUrl);
        agentNotify.setNotifyUrl(linkUrl);
        agentNotify.setSendRange(sendRange);
        agentNotify.setCreateUserId(createUserId);
        agentNotify.setCreateUserName(createUserName);
        agentNotify.setNotifyType(AgentNotifyType.GROUP_MESSAGE.getDesc());
        agentNotify.setMsgStatus(0);
        agentNotify.setOpenNum(0);
        Long notifyId;
        if(createFlag){
            notifyId = agentNotifyPersistence.persist(agentNotify);
        }else {
            notifyId = SafeConverter.toLong(id);
            agentNotifyPersistence.update(notifyId,agentNotify);
        }
        saveAgentGroupRoleAuthority(notifyId.toString() ,userIds,roleTypeList, groupIdList);
        mapMessage.put("notifyId",notifyId);
        return mapMessage;
    }
    //根据消息id查出关联的角色权限数据
    public AgentGroupRoleAuthority getGroupRoleAuthorityBySourceId(String sourceId){
       List<AgentGroupRoleAuthority> list = agentGroupRoleAuthorityDao.findBySourceId(sourceId).stream().filter(p -> p.getAgentAuthorityType().equals(AgentAuthorityType.MESSAGE)).collect(Collectors.toList());
       if(CollectionUtils.isNotEmpty(list) && list.size() > 0){
           return list.get(0);
       }
       return null;
    }

    //查询push消息的详情
    public AgentMessage findPushById(String id){
        return pushMessageDao.load(id);
    }

    //保存关联角色权限表数据
    public void saveAgentGroupRoleAuthority(String sourceId , List<Long> userIds, List<AgentRoleType> roleTypeList, List<Long> groupIdList) {
        AgentGroupRoleAuthority agentGroupRoleAuthority = getGroupRoleAuthorityBySourceId(sourceId);
        if(agentGroupRoleAuthority == null){
            agentGroupRoleAuthority = new AgentGroupRoleAuthority();
            agentGroupRoleAuthority.setDisabled(false);
            agentGroupRoleAuthority.setAgentAuthorityType(AgentAuthorityType.MESSAGE);
        }
        agentGroupRoleAuthority.setGroupIdList(groupIdList);
        agentGroupRoleAuthority.setRoleTypeList(roleTypeList);
        agentGroupRoleAuthority.setSourceId(sourceId);
        agentGroupRoleAuthority.setUserIds(userIds);
        agentGroupRoleAuthorityDao.upsert(agentGroupRoleAuthority);
    }

    /**
     * 根据条件查询消息列表
     * @param messageType 消息类型 1 push 2 通知
     * @param createUserId 创建人 姓名
     * @param beginDate 开始时间
     * @param endDate 结束时间
     * @param msgStatus 消息状态 -1 删除 0 待发送 1 已发送
     * @param notifyType  这个字段暂时没有用  因为目前类型就一个   后期如果加类型  AgentNotifyType 这个枚举得加个根据desc获取枚举对象的 然后再根据枚举遍历
     * @return
     */
    public MapMessage findMessageList( Integer messageType,Long createUserId, Date beginDate, Date endDate, Integer msgStatus, String notifyType){
        MapMessage mapMessage = MapMessage.successMessage();
        if(1 == messageType){//push
            List<AgentMessage> list = pushMessageDao.findAgentNotifyList(createUserId, msgStatus, beginDate, endDate);
            mapMessage.put("messageList",list);
        }else if(2 == messageType){
            List<AgentNotify> list = agentNotifyPersistence.findAgentNotifyList(AgentNotifyType.GROUP_MESSAGE.getDesc(),beginDate,endDate);
            if(createUserId != null && createUserId > 0){
                list = list.stream().filter( p -> p.getCreateUserId() == createUserId).collect(Collectors.toList());
            }
            if(msgStatus != null){
                list = list.stream().filter( p -> p.getMsgStatus() == msgStatus).collect(Collectors.toList());
            }
            mapMessage.put("messageList",list);
        }
        return mapMessage;
    }

    //选中的部门查询出对应的名称 用于前端回显
    public List<Map<String,Object>> getSelectGroupByIds(List<Long> groupIds){
        List<Map<String,Object>> result = new ArrayList<>();
        Map<Long, AgentGroup> groupMap = agentGroupLoaderClient.loads(groupIds);
        groupIds.forEach(p->{
            Map<String,Object> map = new HashMap<>();
            map.put("id",groupMap.get(p).getId());
            map.put("groupName",groupMap.get(p).getGroupName());
            result.add(map);
        });
        return result;
    }

    public void  deletePushMessage(String id,Integer messageType){
        if(1 == messageType){
            AgentMessage agentMessage = pushMessageDao.load(id);
            if(agentMessage == null || agentMessage.getDisabled() ){
                return;
            }
            agentMessage.setDisabled(true);
            agentMessage.setMsgStatus(-1);
            pushMessageDao.upsert(agentMessage);
            AgentGroupRoleAuthority agentGroupRoleAuthority =getGroupRoleAuthorityBySourceId(id);
            agentGroupRoleAuthority.setDisabled(true);
            agentGroupRoleAuthorityDao.upsert(agentGroupRoleAuthority);
        }else if(2 == messageType){
            AgentNotify agentNotify =agentNotifyPersistence.load(SafeConverter.toLong(id));
            if(agentNotify != null || agentNotify.getDisabled() ){
                return ;
            }
            agentNotify.setMsgStatus(-1);
            agentNotify.setDisabled(true);
            agentNotifyPersistence.update(agentNotify.getId(),agentNotify);
            AgentGroupRoleAuthority agentGroupRoleAuthority =getGroupRoleAuthorityBySourceId(id);
            agentGroupRoleAuthority.setDisabled(true);
            agentGroupRoleAuthorityDao.upsert(agentGroupRoleAuthority);
        }
    }

    //发送推送消息
    public void sendPushMessage(AgentMessage agentMessage,AgentGroupRoleAuthority authority){
        List<Long> userIds;
        if(agentMessage.getSendRange() == 1){//指定部门
            List<Long> groupIds = authority.getGroupIdList();
            List<AgentRoleType> roleTypes = authority.getRoleTypeList();
            userIds = getGroupUsers(groupIds,roleTypes);
            List<String> tags = new ArrayList<>();
            List<String> tagsAnd = new ArrayList<>();
            Map<String,Object> extInfo = new HashMap<>();
            extInfo.put("messageId",agentMessage.getId());
            extInfo.put("url",agentMessage.getLinkUrl());
            if(CollectionUtils.isNotEmpty(groupIds) && CollectionUtils.isNotEmpty(roleTypes)){//jpush不能都取交集
                groupIds.forEach(p-> tags.add(JPushCrmType.AGENT_GROUP.generateTag(p)));
                roleTypes.forEach(role ->{
                    tagsAnd.add(JPushCrmType.AGENT_ROLE.generateTag(role.getId()));
                    appMessageServiceClient.sendAppJpushMessageByTags(agentMessage.getPushContent(),AppMessageSource.AGENT,tags,tagsAnd,extInfo);
                    tagsAnd.clear();
                });
            }else if(CollectionUtils.isNotEmpty(groupIds) && CollectionUtils.isEmpty(roleTypes)){//部门空  角色不为空时  取部门的并集
                groupIds.forEach(p-> tags.add(JPushCrmType.AGENT_GROUP.generateTag(p)));
                appMessageServiceClient.sendAppJpushMessageByTags(agentMessage.getPushContent(),AppMessageSource.AGENT,tags,null,extInfo);
            }else if(CollectionUtils.isEmpty(groupIds) && CollectionUtils.isNotEmpty(roleTypes)){//部门列表为空  角色列表不为空 取角色列表并集发
                roleTypes.forEach(p-> tagsAnd.add(JPushCrmType.AGENT_ROLE.generateTag(p)));
                appMessageServiceClient.sendAppJpushMessageByTags(agentMessage.getPushContent(),AppMessageSource.AGENT,tags,null,extInfo);
            }
        }else{//指定用户时
            userIds = authority.getUserIds();
        }
        sendUserIdsPushMessage(agentMessage,userIds,false);
        agentMessage.setSendNum(userIds.size());
        agentMessage.setSendDatetime(new Date());
        agentMessage.setMsgStatus(1);
        pushMessageDao.upsert(agentMessage);
    }

    //根据用户ID发送推送消息
    public void sendUserIdsPushMessage(AgentMessage agentMessage,List<Long> userIds,boolean isTest){
        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("url",agentMessage.getLinkUrl());
        extInfo.put("messageId",agentMessage.getId());
        appMessageServiceClient.sendAppJpushMessageByIds(agentMessage.getPushContent(),AppMessageSource.AGENT,userIds,extInfo);
        //发送测试消息不保存接收人
        if(!isTest){
            userIds.forEach(u->{
                AgentMessageUser messageUser = new AgentMessageUser();
                messageUser.setDisabled(false);
                messageUser.setMessageId(agentMessage.getId());
                messageUser.setReadFlag(false);
                messageUser.setUserId(u);
                agentMessageUserDao.$insert(messageUser);
            });
        }
    }


    public void clearSendFlag(Long userId, AgentPushType pushType){
        String key = pushType.name() + "_" + userId;
        agentCacheSystem.CBS.flushable.delete(key);
    }


    public void sendSilentMessageByIds(Collection<Long> userIds, AgentPushType pushType){
        if(CollectionUtils.isEmpty(userIds) || pushType == null){
            return;
        }
        List<Long> targetUserIds = new ArrayList<>();

        userIds.forEach(p -> {
            String key = pushType.name() + "_" + p;
            Boolean hasSend = agentCacheSystem.CBS.flushable.load(key);
            if(!SafeConverter.toBoolean(hasSend)){
                targetUserIds.add(p);
            }
        });


        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("tag",pushType.name());
        extInfo.put("content-available",true);
        appMessageServiceClient.sendAppJpushMessageByIds(pushType.getDesc(),AppMessageSource.AGENT, targetUserIds,extInfo);
        targetUserIds.forEach(p -> {
            String key = pushType.name() + "_" + p;
            agentCacheSystem.CBS.flushable.add(key, SafeConverter.toInt(DateUtils.addDays(new Date(), 1).getTime() / 1000), true);
        });
    }
    //根据前端选的部门和角色过滤出符合条件的人数
    public List<Long> getGroupUsers(List<Long> groupIds,List<AgentRoleType> roleTypes ){
        List<Long> allUserIds = new ArrayList<>();
        Set<Integer> roleIds = roleTypes.stream().map(AgentRoleType::getId).collect(Collectors.toSet());
        if(CollectionUtils.isNotEmpty(groupIds) && CollectionUtils.isNotEmpty(roleTypes)){
            groupIds.forEach(p->{
                List<AgentGroupUser>  groupUsers = baseOrgService.getAllGroupUsersByGroupId(p);
                Set<Long> userIds = groupUsers.stream().filter(user -> roleIds.contains(user.getUserRoleId())).map(AgentGroupUser::getUserId).collect(Collectors.toSet());
                allUserIds.addAll(userIds);
            });
        }else if(CollectionUtils.isNotEmpty(groupIds) && CollectionUtils.isEmpty(roleTypes)){
            groupIds.forEach(p->{
                Set<Long> userIds = baseOrgService.getAllGroupUsersByGroupId(p).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
                allUserIds.addAll(userIds);
            });
        }else if(CollectionUtils.isEmpty(groupIds) && CollectionUtils.isNotEmpty(roleTypes)){
            roleTypes.forEach(role -> {
                Set<Long> userIds = baseOrgService.getGroupUserByRole(role.getId()).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
                allUserIds.addAll(userIds);
            });

        }
        return allUserIds;
    }

    /**
     * 设置消息已读
     *
     * @param userId   用户id
     * @param notifyId 通知id
     */
    public void readMessage(Long userId, String notifyId) {
        if (StringUtils.isBlank(notifyId) || userId == null) {
            return ;
        }
        AgentMessage agentMessage = pushMessageDao.load(notifyId);
        if (agentMessage != null) {
            AgentMessageUser agentMessageUser = agentMessageUserDao.findByUserIdAndMessageId(userId, notifyId);
            if (agentMessageUser != null) {
                agentMessageUser.setReadFlag(true);
                agentMessageUserDao.upsert(agentMessageUser);
            }
            agentMessage.setOpenNum(agentMessage.getOpenNum() + 1);
            pushMessageDao.upsert(agentMessage);

        }
    }

    /**
     * 导出详情
     * @param receiverList
     */
    public void exportReceiverList(SXSSFWorkbook workbook, List<Map<String,Object>> receiverList,String content,String sendDatetime){
        try {
            Sheet sheet = workbook.createSheet("接收人列表");
            sheet.createFreezePane(0, 1, 0, 1);
            Font font = workbook.createFont();
            font.setFontName("宋体");
            font.setFontHeightInPoints((short) 10);
            CellStyle firstRowStyle = workbook.createCellStyle();
            firstRowStyle.setFont(font);
            firstRowStyle.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
            firstRowStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            firstRowStyle.setAlignment(CellStyle.ALIGN_CENTER);
            Row headRow = sheet.createRow(0);
            CellRangeAddress  cellRangeAddress = new CellRangeAddress(0, 0, 1, 4);
            sheet.addMergedRegion(cellRangeAddress);

            HssfUtils.setCellValue(headRow, 0, firstRowStyle, "消息内容：");
            HssfUtils.setCellValue(headRow, 1, firstRowStyle, content);
            HssfUtils.setCellValue(headRow, 5, firstRowStyle, "发送时间");
            HssfUtils.setCellValue(headRow, 6, firstRowStyle, sendDatetime);

            Row firstRow = sheet.createRow(1);
            HssfUtils.setCellValue(firstRow, 0, firstRowStyle, "姓名");
            HssfUtils.setCellValue(firstRow, 1, firstRowStyle, "角色");
            HssfUtils.setCellValue(firstRow, 2, firstRowStyle, "市场");
            HssfUtils.setCellValue(firstRow, 3, firstRowStyle, "大区");
            HssfUtils.setCellValue(firstRow, 4, firstRowStyle, "区域");
            HssfUtils.setCellValue(firstRow, 5, firstRowStyle, "分区");
            HssfUtils.setCellValue(firstRow, 6, firstRowStyle, "打开时间");
//            HssfUtils.setCellValue(firstRow, 7, firstRowStyle, "消息内容");
//            HssfUtils.setCellValue(firstRow, 8, firstRowStyle, "发送日期");

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            cellStyle.setAlignment(CellStyle.ALIGN_CENTER);

            if(CollectionUtils.isNotEmpty(receiverList)){
                Integer index = 2;
                for(Map<String,Object> reciver : receiverList){
                    Row row = sheet.createRow(index++);
                    HssfUtils.setCellValue(row,0,cellStyle,ConversionUtils.toString(reciver.get("userName")));
                    HssfUtils.setCellValue(row,1,cellStyle,ConversionUtils.toString(reciver.get("userRole")));
                    HssfUtils.setCellValue(row,2,cellStyle,ConversionUtils.toString(reciver.get("marketingName")));
                    HssfUtils.setCellValue(row,3,cellStyle,ConversionUtils.toString(reciver.get("regionName")));
                    HssfUtils.setCellValue(row,4,cellStyle,ConversionUtils.toString(reciver.get("areaName")));
                    HssfUtils.setCellValue(row,5,cellStyle,ConversionUtils.toString(reciver.get("cityName")));
                    HssfUtils.setCellValue(row,6,cellStyle,ConversionUtils.toString(reciver.get("openDatetime")));
//                    HssfUtils.setCellValue(row,7,cellStyle,ConversionUtils.toString(reciver.get("content")));
//                    HssfUtils.setCellValue(row,8,cellStyle,ConversionUtils.toString(reciver.get("sendDatetime")));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("error info: ",ex);
        }
    }

    public void groupRoleType(Model model){
        List<AgentRoleType> allList = Arrays.asList(AgentRoleType.values());
        List<AgentRoleType> marketList = new ArrayList<>();
        marketList.add(AgentRoleType.Country);
        marketList.add(AgentRoleType.Region);
        marketList.add(AgentRoleType.AreaManager);
        marketList.add(AgentRoleType.CityManager);
        marketList.add(AgentRoleType.BusinessDeveloper);
        model.addAttribute("marketList",marketList);
        List<AgentRoleType> channelList = new ArrayList<>();
        channelList.add(AgentRoleType.ChannelDirector);
        channelList.add(AgentRoleType.ChannelManager);
        channelList.add(AgentRoleType.CityAgent);
        model.addAttribute("channelList",channelList);
        List<AgentRoleType> bigCustomerList = new ArrayList<>();
        bigCustomerList.add(AgentRoleType.BigCustomerDirector);
        bigCustomerList.add(AgentRoleType.BigCustomerPreSales);
        bigCustomerList.add(AgentRoleType.BigCustomerSales);
        model.addAttribute("bigCustomerList",bigCustomerList);
        List<AgentRoleType> otherList = allList.stream().filter(p -> !marketList.contains(p) && ! channelList.contains(p) && !channelList.contains(p)).collect(Collectors.toList());
        model.addAttribute("others",otherList);
    }
}
