package com.voxlearning.utopia.mizar.controller.basic;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.constants.MizarNotifyType;
import com.voxlearning.utopia.service.mizar.api.entity.notify.MizarNotify;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarFile;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarNotifyMapper;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.api.entities.SmsMessage;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by yuechen.wang on 2016/12/05.
 */
@Controller
@RequestMapping(value = "/basic/notify")
public class MizarNotifyController extends AbstractMizarController {

    @Inject private SmsServiceClient smsServiceClient;

    // 允许手动发送的消息类型
    private static final Map<String, MizarNotifyType> validType = new LinkedHashMap<String, MizarNotifyType>() {
        private static final long serialVersionUID = 2470129782493696651L;

        {
            put(MizarNotifyType.ADMIN_NOTICE.name(), MizarNotifyType.ADMIN_NOTICE);
            put(MizarNotifyType.SMS_NOTICE.name(), MizarNotifyType.SMS_NOTICE);
        }
    };

    // 消息列表
    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String notifyList(Model model) {
        int page = Integer.max(1, getRequestInt("page", 1));

        Boolean type = getRequestBool("type", null); // 查看已读未读

        List<MizarNotifyMapper> userNotifies = mizarNotifyLoaderClient.loadUserAllNotify(currentUserId())
                .stream()
                .filter(n -> type == null || type.equals(n.isRead()))
                .collect(Collectors.toList());

        model.addAttribute("notifyList", splitList(userNotifies, PAGE_SIZE));
        model.addAttribute("page", page);
        return "basic/notify/list";
    }

    // 消息编辑
    @RequestMapping(value = "edit.vpage", method = RequestMethod.GET)
    public String editNotify(Model model) {
        model.addAttribute("types", validType);

        // FIXME 先选中所有用户，未作任何过滤
        String userId = currentUserId();
        List<MizarUser> users = mizarUserLoaderClient.loadAllUsers()
                .stream()
                .filter(u -> !userId.equals(u.getId()))
                .collect(Collectors.toList());
        model.addAttribute("users", users);
        return "basic/notify/edit";
    }

    // 发送消息
    @RequestMapping(value = "send.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage sendNotify() {
        try {
            MizarNotify notify = requestEntity(MizarNotify.class);
            if (notify == null) {
                return MapMessage.errorMessage("消息生成失败！");
            }
            List<String> receivers = requestStringList("receiver");
            List<MizarUser> validUser = mizarUserLoaderClient.loadUsers(receivers).values()
                    .stream()
                    .filter(MizarUser::isValid)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(validUser)) {
                return MapMessage.errorMessage("请选择有效的接收用户！");
            }
            String fileNames = getRequestString("files");
            if (StringUtils.isNotBlank(fileNames)) {
                List<MizarFile> files = JSON.parseArray(fileNames, MizarFile.class);
                notify.setFiles(files);
            }
            // 校验
            MizarNotifyType notifyType = MizarNotifyType.parse(notify.getType());
            if (notifyType == null) {
                return MapMessage.errorMessage("请选择正确的【消息类型】！");
            }
            if (StringUtils.isBlank(notify.getTitle())) {
                return MapMessage.errorMessage("请填写【消息标题】！");
            }
            if (StringUtils.isBlank(notify.getContent())) {
                return MapMessage.errorMessage("请填写【消息内容】！");
            }
            if (MizarNotifyType.SMS_NOTICE == notifyType) {
                if (notify.getContent().length() > 70) {
                    return MapMessage.errorMessage("短信消息内容请控制在70字以内！");
                }
            }
            if (notify.getFiles() != null && notify.getFiles().size() > 3) {
                return MapMessage.errorMessage("最多只能上传三个附件！");
            }

            // 检验完成，完善信息
            notify.setCreator(currentUserId());
            notify.setSendTime(new Date());
            receivers = validUser.stream().map(MizarUser::getId).collect(Collectors.toList());
            // 发送消息
            MapMessage retMsg = mizarNotifyServiceClient.sendNotify(notify, receivers);
            if (!retMsg.isSuccess()) {
                return retMsg;
            }
            // 发送短信
            if (MizarNotifyType.SMS_NOTICE == notifyType) {
                SmsMessage smsMessage = new SmsMessage();
                smsMessage.setType(SmsType.MIZAR_SMS_NOTIFY.name());
                smsMessage.setSmsContent(notify.getContent());
                smsMessage.setSendTime(DateUtils.dateToString(notify.getSendTime(), "yyyyMMddHHmmss"));
                for (MizarUser user : validUser) {
                    String mobile = user.getMobile();
                    if (MobileRule.isMobile(mobile) && !badWordCheckerClient.containsMobileNumBadWord(mobile)) {
                        smsMessage.setMobile(mobile);
                        smsServiceClient.getSmsService().sendSms(smsMessage);
                    }
                }
            }
            return retMsg;
        } catch (Exception ex) {
            logger.error("Failed send mizar notify", ex);
            return MapMessage.errorMessage("消息发送失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "read.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage readNotify() {
        String refId = getRequestString("id");
        try {
            return mizarNotifyServiceClient.readNotify(refId);
        } catch (Exception ex) {
            logger.error("Failed read mizar notify, id={}", refId, ex);
            return MapMessage.errorMessage("设置已读失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "remove.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage removeNotify() {
        String refId = getRequestString("id");
        try {
            return mizarNotifyServiceClient.removeNotify(refId);
        } catch (Exception ex) {
            logger.error("Failed remove mizar notify, id={}", refId, ex);
            return MapMessage.errorMessage("消息删除失败：" + ex.getMessage());
        }
    }

}
