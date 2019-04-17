package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.constants.AgentPushType;
import com.voxlearning.utopia.agent.constants.AgentTag;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.persist.entity.AgentAppContentPacket;
import com.voxlearning.utopia.agent.service.messagecenter.AgentMessageService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.workspace.AgentAppContentPacketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

/**
 * 首页消息页面
 * Created by Yuechen.Wang on 2016/8/1.
 */
@Controller
@RequestMapping("/mobile/notice")
public class AgentNoticeController extends AbstractAgentController {

    @Inject private AgentNotifyService agentNotifyService;
    @Inject private AgentAppContentPacketService agentAppContentPacketService;
    @Inject private AgentCacheSystem agentCacheSystem;
    @Inject private AgentMessageService agentMessageService;

    @RequestMapping(value = "readNotice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readNotice(@RequestParam Long noticeId) {
        try {
            return agentNotifyService.readNotify(getCurrentUserId(), noticeId);
        } catch (Exception ex) {
            logger.error("Failed read notice, user={}, notice={}", getCurrentUserId(), noticeId);
            return MapMessage.errorMessage("消息已读失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "noticeReader.vpage", method = RequestMethod.GET)
    public String noticeReader(Model model) {
        String contentId = getRequestString("contentId");
        AgentAppContentPacket content = agentAppContentPacketService.loadById(contentId);
        if (content == null) {
            return errorInfoPage(AgentErrorCode.NOTIFY_CONTENT_ERROR, "消息内容无法找到", model);
        }
        String appContent = content.getContent();
        model.addAttribute("title", content.getContentTitle());
        model.addAttribute("content", StringUtils.isBlank(appContent) ? "内容未找到" : appContent);
        return "rebuildViewDir/mobile/notice/noticeReader";
    }

    @RequestMapping(value = "matterReader.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage matterReader() {
        MapMessage mapMessage = MapMessage.successMessage();
        String contentId = getRequestString("contentId");
        AgentAppContentPacket content = agentAppContentPacketService.loadById(contentId);
        if (content == null) {
            return MapMessage.errorMessage( "消息内容无法找到");
        }
        String appContent = content.getContent();
        Map<String,Object> map = new HashMap<>();
        map.put("title", content.getContentTitle());
        map.put("content", StringUtils.isBlank(appContent) ? "内容未找到" : appContent);
        mapMessage.put("data",map);
        return mapMessage;
    }
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage index(){
        MapMessage mapMessage = MapMessage.successMessage();
        Long userId = getCurrentUserId();
        int totalUnreadCount = agentNotifyService.getTotalUnreadNotifyCount(mapMessage, userId);
        agentCacheSystem.updateUserUnreadNotifyCount(userId, totalUnreadCount);
        agentMessageService.clearSendFlag(userId, AgentPushType.NEW_MESSAGE);
        return mapMessage;
    }


    // 周报、月报、重要通知、陪访提醒、陪访记录 以及 平台更新日志 plus 换班请求
    @RequestMapping(value = "noticeList.vpage", method = RequestMethod.GET)
    public String noticeList(Model model) {
        String category = getRequestString("category");
        List<Map<String, Object>> notifyList = agentNotifyService.getNotifyListByType(getCurrentUserId(), AgentNotifyType.fetchByCategory(category));

        if(StringUtils.equals("new_teacher", category)){
            notifyList.forEach(p -> {
                Date createDatetime = SafeConverter.toDate(p.get("createDatetime"));
                p.put("createDatetime",createDatetime != null ? DateUtils.dateToString(createDatetime,"MM-dd HH:mm") : "");
            });
            model.addAttribute("notifyList", notifyList);
            return "rebuildViewDir/mobile/notice/teacherList";
        }else if(StringUtils.equals("alteration_clazz_new", category)){
            model.addAttribute("notifyList", notifyList);
            return "rebuildViewDir/mobile/notice/alterationList";
        } else if (StringUtils.equals("warning", category)) {
            model.addAttribute("notifyList", notifyList);
            return "rebuildViewDir/mobile/notice/warningList";
        }else if(StringUtils.equals("system", category)){
            notifyList.forEach(p -> {
                Object notifyUrl = p.get("notifyUrl");
                if(notifyUrl != null && notifyUrl.toString().contains("ecordId")){//前端接口变workRecordId的notifyUrl中有的叫workRecordId 有的叫 recordId
                    String[] arr = notifyUrl.toString().split("\\?");
                    if (arr.length==2 && arr[1].contains("=")){
                        p.put("workRecordId",arr[1].split("=")[1]);
                    }
                }

            });
        }
        dealNotifyList(notifyList);
        model.addAttribute("notifyList", notifyList);
        return "rebuildViewDir/mobile/notice/messageList";
    }

    @RequestMapping(value = "readNoticeList.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readNoticeList() {
        Long userId = getCurrentUserId();
        Set<Long> noticeIds = requestLongSet("notifyIds");
        MapMessage message = MapMessage.successMessage();
        if(CollectionUtils.isNotEmpty(noticeIds)){
            noticeIds.forEach(p -> {
                MapMessage mapMessage = agentNotifyService.readNotify(userId, p);
                if (!mapMessage.isSuccess()){
                    mapMessage.setSuccess(false);
                }
            });
        }
        return message;
    }

    /**
     * 预处理信息内容，分离出拒绝原因
     * @param notifyList
     */
    private void dealNotifyList(List<Map<String, Object>> notifyList){
        if (CollectionUtils.isNotEmpty(notifyList)){
            notifyList.forEach(item -> {
                String notifyContent = (String) item.get("notifyContent");
                List<AgentTag> tagList = (List<AgentTag>) item.get("tagList");
                if (CollectionUtils.isNotEmpty(tagList) && tagList.contains(AgentTag.REJECT) && StringUtils.isNotEmpty(notifyContent) ){
                    List<String> contentList = Arrays.asList(notifyContent.split("\\r?\\n"));
                    if (contentList.size() > 1){
                        List<String> tempList = contentList.subList(0, contentList.size() - 1);
                        String rejectReason = contentList.get(contentList.size() - 1);
                        item.put("notifyContent",StringUtils.join(tempList,"\r\n"));
                        item.put("rejectReason",rejectReason);
                    }
                }
            });
        }
    }

}
