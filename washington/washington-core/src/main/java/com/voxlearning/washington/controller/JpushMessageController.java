package com.voxlearning.washington.controller;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.appalachian.org.jboss.netty.handler.codec.socks.SocksMessage;
import com.voxlearning.utopia.service.coupon.api.mapper.CouponShowMapper;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.user.api.constants.CallName;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import com.voxlearning.utopia.service.vendor.api.constant.StudentAppPushType;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.util.AppMessageUtils;
import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_MESSAGE;

@Controller
@Slf4j
@NoArgsConstructor
@RequestMapping("/push/msg")
public class JpushMessageController extends AbstractController {

    @Inject
    private AppMessageServiceClient appMessageClient;

    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    // 获取可用的优惠劵
    @RequestMapping(value = "currentPushMessage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String currentPushMessage() {
        String callbackFunction = getRequestString("jsoncallback");
        User user = currentUser();
        if(user == null){
            return callbackFunction+"("+JsonUtils.toJson(MapMessage.errorMessage("用户未登录"))+")";
        }
        //日期校验
        Date endDate = DateUtils.stringToDate("2018-10-28 23:59:59");
        if(new Date().after(endDate)){
            return callbackFunction+"("+JsonUtils.toJson(MapMessage.errorMessage("超过期限不能使用"))+")";
        }

        try{
            String messageLink = getRequestString(REQ_APP_MSG_LINK);
            Map<String, Object> extInfo = new HashMap<>();
            String content = "";
            Integer messageType = 0;
            List userIdList = new LinkedList();
            if(user.isStudent()){
                extInfo.put("url", messageLink);
                extInfo.put("t", "h5");
                extInfo.put("key", "j");
                messageType = ParentMessageType.REMINDER.getType();
                //给他的家长发
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
                List<StudentParent> parents = parentLoaderClient.loadStudentParents(user.getId());
                if(CollectionUtils.isNotEmpty(parents)){
                    for(StudentParent temp : parents){
                        if(Objects.equals(temp.getCallName(), CallName.爸爸.name()) || Objects.equals(temp.getCallName(),CallName.妈妈.name()) ){
                            userIdList.add(temp.getParentUser().getId());
                        }
                    }
                }
                if(CollectionUtils.isEmpty(userIdList)){
                    return callbackFunction+"("+JsonUtils.toJson(MapMessage.errorMessage("没有找到用户"))+")";
                }
                content = studentDetail.fetchRealname()+"给你分享了一个小秘密，点击查看 >>>>";
                appMessageClient.sendAppJpushMessageByIds(content, AppMessageSource.PARENT,userIdList,extInfo, 0L);
            }else if(user.isParent()){
                //跟他的孩子发
                extInfo.put("link", messageLink);
                extInfo.put("t", "h5");
                extInfo.put("key", "j");
                messageType = StudentAppPushType.ACTIVITY_REMIND.getType();
                List<User> students = studentLoaderClient.loadParentStudents(user.getId());
                if(CollectionUtils.isNotEmpty(students)){
                    for(User temp : students){
                        userIdList.add(temp.getId());
                    }
                }
                content = "你的家长给你分享了一个小秘密，点击查看 >>>>";
                appMessageClient.sendAppJpushMessageByIds(content,AppMessageSource.STUDENT,userIdList,extInfo, 0L);
            }
            List<AppMessage> userMessageList = AppMessageUtils.generateAppUserMessage(userIdList,messageType , "提醒", content, "", messageLink, 0, "");
            userMessageList.forEach(messageCommandServiceClient.getMessageCommandService()::createAppMessage);
            return callbackFunction+"("+JsonUtils.toJson(MapMessage.successMessage("发送成功"))+")";
        } catch (Exception e) {
            logger.error("推送消息异常",e);
            return callbackFunction+"("+JsonUtils.toJson(MapMessage.errorMessage("推送消息异常"))+")";
        }


    }





}
