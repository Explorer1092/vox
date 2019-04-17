package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.util.StringUtils;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.crm.api.entities.crm.AppPushWfMessage;
import com.voxlearning.utopia.service.crm.consumer.loader.crm.CrmAppPushLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.crm.CrmAppPushServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;
import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * @Author: liuyong
 * @Description:
 * @Date:Created in 2018-06-06 16:54
 */
@Controller
@Slf4j
@RequestMapping(value = "/v1/apppush")
public class AppJpushMessageApiController extends AbstractApiController  {

    @Inject private AppMessageServiceClient appMessageClient;
    @Inject private CrmAppPushLoaderClient crmAppPushLoaderClient;
    @Inject private CrmAppPushServiceClient crmAppPushServiceClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    public static Map<Integer,String> pushTypeMap = new LinkedHashMap<>();

    static {
        pushTypeMap.put(1000,"wakeup");
        pushTypeMap.put(1001,"msg_list");
        pushTypeMap.put(1002,"wakeup");
        pushTypeMap.put(1003,"msg_list");
        pushTypeMap.put(1004,"msg_list");
        pushTypeMap.put(1005,"h5");
        pushTypeMap.put(1006,"msg_list");
        pushTypeMap.put(1007,"msg_list");
        pushTypeMap.put(1008,"msg_list");
        pushTypeMap.put(1009,"native");
        pushTypeMap.put(1080,"h5");
    }


    /**
     * @title 给用户推送消息
     * request请求参数列表为：
     * userIds 用户ID
     * content 消息内容
     * source  消息来源
     * sendTimeEpochMilli 消息推送时间
     * messageType 消息推送类型
     * messageLink 消息推送地址
     * schoolLevel 学校级别
     * @respBody {"result":"success","message":"发送成功"}
     */
    @RequestMapping(value = "users.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendUserJpushMessage() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_APP_MSG_UIDS, "用户ID");
            validateRequired(REQ_APP_MESSAGE_CONTENT,"消息内容");
            validateRequired(REQ_APP_MESSAGE_SOURCE,"消息来源");
            validateRequired(REQ_SENDTIME_EPOCH_MILLI,"消息推送时间");
            validateRequired(REQ_APP_MSG_TYPE,"推送消息类型");
            validateRequired(REQ_APP_MSG_LINK,"推送消息跳转地址");
            validateRequired(REQ_APP_MSG_SCHOOL_LEVEL,"学校级别");

            validateRequest(
                    REQ_APP_MSG_UIDS,
                    REQ_APP_MESSAGE_CONTENT,
                    REQ_APP_MESSAGE_SOURCE,
                    REQ_SENDTIME_EPOCH_MILLI,
                    REQ_APP_MSG_TYPE,
                    REQ_APP_MSG_LINK,
                    REQ_APP_MSG_SCHOOL_LEVEL,
                    REQ_APP_MESSAGE_EXTINFO
            );
        }catch (IllegalArgumentException e){
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        try{
            String userIdStrs = getRequestString(REQ_APP_MSG_UIDS);
            String content = getRequestString(REQ_APP_MESSAGE_CONTENT);
            String source = getRequestString(REQ_APP_MESSAGE_SOURCE);
            Long sendTimeEpochMilli = getRequestLong(REQ_SENDTIME_EPOCH_MILLI);
            //封装推送前端所需参数
            String jpushMsgType = getRequestString(REQ_APP_MSG_TYPE);
            String jpushMsgLink = getRequestString(REQ_APP_MSG_LINK);
            String schoolLevel = getRequestString(REQ_APP_MSG_SCHOOL_LEVEL);
            if(!Arrays.asList("j","m","s").contains(schoolLevel)){
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "学校级别有误");
                return resultMap;
            }

            Map<String, Object> extInfo = getStringObjectMap(jpushMsgType,jpushMsgLink,schoolLevel);
            String extInfoParam = getRequestString(REQ_APP_MESSAGE_EXTINFO);
            if(StringUtils.isNotBlank(extInfoParam)){
                Map<String,Object> extInfoMap = JsonUtils.fromJson(extInfoParam);
                extInfo.putAll(extInfoMap);
            }
            String[] userIds = userIdStrs.split(",");
            List<Long> userIdList = new LinkedList<>();
            for(String userId : userIds){
                if(StringUtils.isNotEmpty(userId)){
                    userIdList.add(Long.valueOf(userId));
                }
            }
            AppMessageSource appMessageSource = AppMessageSource.of(source);
            if(appMessageSource == AppMessageSource.UNKNOWN){
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "未知的消息来源");
                return resultMap;
            }

            appMessageClient.sendAppJpushMessageByIds(content,appMessageSource,userIdList,extInfo,sendTimeEpochMilli);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE,"发送成功" );
            return resultMap;
        }catch(Exception e){
            logger.error("推送消息异常",e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }

    /**
     * @title 根据用户所属的组推送消息
     * request请求参数列表为：
     * groupIds 用户组ID
     * content 消息内容
     * source  消息来源
     * messageType 消息推送类型
     * messageLink 消息推送地址
     * schoolLevel 学校级别
     * @respBody {"result":"success","message":"发送成功"}
     */
    @RequestMapping(value = "groups.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendTagsJpushMessage() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_APP_MESSAGE_CONTENT, "消息内容");
            validateRequired(REQ_APP_MESSAGE_SOURCE, "消息来源");
            validateRequired(REQ_APP_MSG_GROUP_IDS, "用户所属组");
            validateRequired(REQ_APP_MSG_TYPE, "推送消息类型");
            validateRequired(REQ_APP_MSG_LINK,"推送消息跳转地址");
            validateRequired(REQ_APP_MSG_SCHOOL_LEVEL,"学校级别");

            validateRequest(
                    REQ_APP_MESSAGE_CONTENT,
                    REQ_APP_MESSAGE_SOURCE,
                    REQ_APP_MSG_GROUP_IDS,
                    REQ_APP_MSG_TYPE,
                    REQ_APP_MSG_LINK,
                    REQ_APP_MSG_SCHOOL_LEVEL
            );
        }catch(IllegalArgumentException e){
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        try{
            String content = getRequestString(REQ_APP_MESSAGE_CONTENT);
            String source = getRequestString(REQ_APP_MESSAGE_SOURCE);
            //用户所属的组id，可以传多个ID，以"," 隔开。
            String group_ids = getRequestString(REQ_APP_MSG_GROUP_IDS);
            String[] groupIds = group_ids.split(",");
            List<String> tags = new LinkedList<>();
            for(String groupId : groupIds){
                if(StringUtils.isNotEmpty(groupId)){
                    tags.add("group_"+groupId);
                }
            }
            //封装推送前端所需参数
            String jpushMsgType = getRequestString(REQ_APP_MSG_TYPE);
            String jpushMsgLink = getRequestString(REQ_APP_MSG_LINK);
            String schoolLevel = getRequestString(REQ_APP_MSG_SCHOOL_LEVEL);
            if(!Arrays.asList("j","m","s").contains(schoolLevel)){
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "学校级别有误");
                return resultMap;
            }
            Map<String, Object> extInfo = getStringObjectMap(jpushMsgType,jpushMsgLink,schoolLevel);
            AppMessageSource appMessageSource = AppMessageSource.of(source);
            if(appMessageSource == AppMessageSource.UNKNOWN){
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, "未知的消息来源");
                return resultMap;
            }

            appMessageClient.sendAppJpushMessageByTags(content,appMessageSource,tags,null,extInfo,0);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE,"发送成功" );
            return resultMap;
        } catch (Exception e) {
            logger.error("推送消息异常",e);
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
    }

    /**
     * @title 封装封装极送推送消息客户端所需参数
     * 极送客户端所需的参数，s 消息类型，t 消息触发场景， link 推送消息跳转地址，为空跳转到首页，不为空为，app内其他H5页面的地址，
     *  key 通过当前登录的用户获取，学生的级别，小学j，中学m，s高中，i学前,timestamp：默认值当前时间，ios推送需要的固定参数
     * @return Map<String,Object></>
     */
    private Map<String, Object> getStringObjectMap(String jpushMsgType,String jpushMsgLink,String schoolLevel) {
        //消息推送客户端需要的参数如下：
        // 1 , 消息类型
        // 2， 消息触发场景 t ,根据消息的类型区分出发场景的类型,h5,wakeup,msg_list,这个还需要跟需求确定一下需求
        // 3，link 推送消息跳转地址，为空跳转到首页，不为空为，app内其他H5页面的地址
        // 4，key 通过当前登录的用户获取，学生的级别，小学j，中学m，s高中，i学前等
        // 5，timestamp：默认值，ios推送需要的固定参数
        Map<String,Object> extInfo = new LinkedHashMap<>();
        extInfo.put("s",jpushMsgType);
        extInfo.put("t",pushTypeMap.get(SafeConverter.toInt(jpushMsgType)));
        extInfo.put("link",jpushMsgLink);
        extInfo.put("key",schoolLevel);
        extInfo.put("timestamp",System.currentTimeMillis());
        return extInfo;
    }

    @RequestMapping(value = "send_by_userids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendByUserIds() {
        MapMessage resultMap = new MapMessage();

        validateRequired("messageId", "消息ID");
        validateRequired("userIds", "用户ID");
        try {
            validateRequired("messageId", "消息ID");
            validateRequired(REQ_APP_MSG_UIDS, "用户ID");

            validateRequestNoSessionKey("messageId");
        }catch(IllegalArgumentException e){
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        LogCollector.info("backend-general",
                MiscUtils.map(
                        "op", "apppush_sendbyuids",
                        "env", RuntimeMode.getCurrentStage(),
                        "messageId", getRequestString("messageId"),
                        "uids", getRequestString(REQ_APP_MSG_UIDS)
                ));

        String testOnly = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "crm_push_test_only");
        if (Objects.equals(testOnly, "1")) {
            logger.info("crm push send_by_userids, {}, {}", getRequestString("messageId"), getRequestString(REQ_APP_MSG_UIDS));
            return MapMessage.successMessage();
        }

        String messageId = getRequestString("messageId");
        AppPushWfMessage pushMessage = crmAppPushLoaderClient.findById(messageId);
        if(pushMessage == null){
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "pushId错误");
            return resultMap;
        }
        String userIdStrs = getRequestString(REQ_APP_MSG_UIDS);
        String[] userIds = userIdStrs.split(",");
        List<Long> userIdList = new LinkedList<>();
        for(String userId : userIds){
            if(StringUtils.isNotEmpty(userId)){
                userIdList.add(Long.valueOf(userId.trim()));
            }
        }
        if(CollectionUtils.isEmpty(userIdList)){
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "userIds错误");
            return resultMap;
        }

        MapMessage message = crmAppPushServiceClient.publish(pushMessage, userIdList);
        if(message.isSuccess()){
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_MESSAGE,"发送成功" );
        }else {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, "发送失败");
        }
        return resultMap;

    }

}
