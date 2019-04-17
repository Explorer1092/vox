package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.galaxy.service.wechat.api.entity.WechatQRCodeMessage;
import com.voxlearning.galaxy.service.wechat.api.mapper.WechatInfoContext;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatLoader;
import com.voxlearning.galaxy.service.wechat.api.service.DPWechatService;
import com.voxlearning.galaxy.service.wechat.api.util.ParentWechatInfoProvider;
import com.voxlearning.galaxy.service.wechat.api.util.StudyTogetherWechatInfoProvider;
import com.voxlearning.galaxy.service.wechat.api.util.WechatQRCodeMessageIdentifyUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xin.xin
 * @since 10/18/18
 **/
@Controller
@RequestMapping(value = "/site/wechat/qrcode/")
public class SiteWechatQRCodeController extends SiteAbstractController {
    private static final Pattern pattern = Pattern.compile("(?<=\\{\\{)(\\S+)(?=\\}\\})");

    @ImportService(interfaceClass = DPWechatLoader.class)
    private DPWechatLoader dpWechatLoader;
    @ImportService(interfaceClass = DPWechatService.class)
    private DPWechatService dpWechatService;

    /**
     * 二维码消息列表页
     */
    @RequestMapping(value = "/messages.vpage", method = RequestMethod.GET)
    public String messages() {
        return "site/wechat/qrcode_messages";
    }

    @RequestMapping(value = "/messages/list.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMessages() {
        List<WechatQRCodeMessage> messages = dpWechatLoader.getAllWechatQRCodeMessageForCRM();
        if (CollectionUtils.isEmpty(messages)) {
            return MapMessage.successMessage();
        }
        return MapMessage.successMessage().add("messages", messages);
    }

    @RequestMapping(value = "/message/add.vpage", method = RequestMethod.GET)
    public String addMessage(Model model) {
        String identify = getRequestString("idf");
        String type = getRequestString("type");
        if (StringUtils.isNotBlank(identify)) {
            WechatQRCodeMessage message = dpWechatLoader.getWechatQRCodeMessage(type, identify);
            if (null != message) {
                model.addAttribute("type", type);
                model.addAttribute("identify", identify);
                model.addAttribute("id", message.getId());
            }
        }
        return "site/wechat/qrcode_message_add";
    }

    @RequestMapping(value = "/message/add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addMessagePost() {
        String type = getRequestString("type");
        Boolean limit = getRequestBool("limit");
        Integer sceneId = getRequestInt("sceneId");
        String content = getRequestString("content");
        String url = getRequestString("url");
        Boolean needLogin = getRequestBool("needLogin");
        String operationName = getRequestString("operation");

        try {
            WechatInfoContext context = getWechatInfoContext(type);
            if (null == context || StringUtils.isBlank(context.getName())) {
                return MapMessage.errorMessage("非法的微信类型");
            }

            if (limit) {
                //永久二维码
                if (0 == sceneId) {
                    //字符参数二维码
                    //每小时只能加一条
                    try {
                        AtomicLockManager.instance().acquireLock("WECHAT_QRCODE_MESSAGE_LOCK_" + type, 3600);
                    } catch (CannotAcquireLockException ignore) {
                        return MapMessage.errorMessage("字符型二维码配置每小时只能加一条，请1小时后再试");
                    }

                    String identify = WechatQRCodeMessageIdentifyUtils.limitStringIdentify();
                    WechatQRCodeMessage message = dpWechatLoader.getWechatQRCodeMessage(type, identify);
                    if (null != message) {
                        return MapMessage.errorMessage("添加失败，标识已存在");
                    }

                    dpWechatService.addWechatLimitQRCodeMessage(context, content, url, needLogin, operationName);
                } else {
                    if (sceneId > 100000) {
                        return MapMessage.errorMessage("场景值不能大于100000");
                    }

                    String identify = WechatQRCodeMessageIdentifyUtils.limitSceneIdIdentify(sceneId);
                    WechatQRCodeMessage message = dpWechatLoader.getWechatQRCodeMessage(type, identify);
                    if (null != message) {
                        return MapMessage.errorMessage("添加失败，标识已存在");
                    }

                    dpWechatService.addWechatLimitQRCodeMessage(context, sceneId, content, url, needLogin, operationName);
                }
            } else {
                if (0 == sceneId) {
                    //字符参数二维码
                    //每小时只能加一条
                    try {
                        AtomicLockManager.instance().acquireLock("WECHAT_QRCODE_MESSAGE_LOCK_" + type, 3600);
                    } catch (CannotAcquireLockException ignore) {
                        return MapMessage.errorMessage("字符型二维码配置每小时只能加一条，请1小时后再试");
                    }

                    String identify = WechatQRCodeMessageIdentifyUtils.stringIdentify();
                    WechatQRCodeMessage message = dpWechatLoader.getWechatQRCodeMessage(type, identify);
                    if (null != message) {
                        return MapMessage.errorMessage("添加失败，标识已存在");
                    }

                    dpWechatService.addWechatQRCodeMessage(context, content, url, needLogin, operationName);
                } else {
                    String identify = WechatQRCodeMessageIdentifyUtils.sceneIdIdentify(sceneId);
                    WechatQRCodeMessage message = dpWechatLoader.getWechatQRCodeMessage(type, identify);
                    if (null != message) {
                        return MapMessage.errorMessage("添加失败，标识已存在");
                    }

                    dpWechatService.addWechatQRCodeMessage(context, sceneId, content, url, needLogin, operationName);
                }
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("type:{},content:{},url:{},needLogin:{},operation:{}", type, content, url, needLogin, operationName, ex.getMessage());
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    @RequestMapping(value = "/message/update.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage updateMessage() {
        String type = getRequestString("type");
        String content = getRequestString("content");
        String url = getRequestString("url");
        Boolean needLogin = getRequestBool("needLogin");
        String operationName = getRequestString("operation");
        String id = getRequestString("id");
        String identify = getRequestString("identify");

        try {
            WechatQRCodeMessage message = dpWechatLoader.getWechatQRCodeMessage(type, identify);
            if (null == message) {
                return MapMessage.errorMessage("未查询到消息配置");
            }
            if (!message.getId().equals(id)) {
                return MapMessage.errorMessage("消息标记有重复");
            }

            message.setContent(content);
            message.setUrl(url);
            message.setNeedLogin(needLogin);
            message.setOperation(operationName);
            dpWechatService.updateWechatQRCodeMessage(message);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("id:{},type:{},content:{},url:{},needLogin:{},operation:{}", id, type, content, url, needLogin, operationName, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private WechatInfoContext getWechatInfoContext(String type) {
        if ("PARENT".equals(type)) {
            return ParentWechatInfoProvider.INSTANCE.wechatInfoContext();
        } else if ("YIQIXUE".equals(type)) {
            return StudyTogetherWechatInfoProvider.INSTANCE.wechatInfoContext();
        }
        return null;
    }

    @RequestMapping(value = "/message/download.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage downloadQRCode() {
        String type = getRequestString("type");
        String identify = getRequestString("identify");

        try {
            WechatQRCodeMessage message = dpWechatLoader.getWechatQRCodeMessage(type, identify);
            if (null == message) {
                return MapMessage.errorMessage("未查询到二维码消息配置");
            }

            if (WechatQRCodeMessageIdentifyUtils.isLimitSceneIdIdentify(identify) || WechatQRCodeMessageIdentifyUtils.isSceneIdIdentify(identify)) {
                String imgUrl = getGalaxySiteUrl() + "/qrcode/query.vpage?type=" + message.getType() + "&idf=" + identify;
                return MapMessage.successMessage().add("url", imgUrl);
            } else if (WechatQRCodeMessageIdentifyUtils.isLimitStringIdentify(identify) || WechatQRCodeMessageIdentifyUtils.isStringIdentify(identify)) {
                StringBuilder text = new StringBuilder("请使用以下链接获取二维码的url：" + getGalaxySiteUrl() + "/qrcode/query.vpage?type=" + message.getType() + "&idf=" + identify);
                Matcher matcher = pattern.matcher(message.getContent());
                while (matcher.find()) {
                    String key = matcher.group();
                    text.append("&").append(key).append("=##value##");
                }
                return MapMessage.successMessage().add("text", text.toString());
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("type:{},identify:{}", type, identify, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }

    private String getGalaxySiteUrl() {
        return ProductConfig.get("galaxy.domain");
    }

    @RequestMapping(value = "/message/get.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getMessage() {
        String type = getRequestString("type");
        String identify = getRequestString("idf");

        try {
            WechatQRCodeMessage message = dpWechatLoader.getWechatQRCodeMessage(type, identify);
            if (null == message) {
                return MapMessage.errorMessage("未查询到消息配置");
            }
            boolean limit = WechatQRCodeMessageIdentifyUtils.isLimitSceneIdIdentify(message.getIdentify()) || WechatQRCodeMessageIdentifyUtils.isLimitStringIdentify(message.getIdentify());
            Integer sceneId = 0;
            if (WechatQRCodeMessageIdentifyUtils.isLimitSceneIdIdentify(message.getIdentify())) {
                sceneId = WechatQRCodeMessageIdentifyUtils.getLimitSceneId(message.getIdentify());
            } else if (WechatQRCodeMessageIdentifyUtils.isSceneIdIdentify(message.getIdentify())) {
                sceneId = WechatQRCodeMessageIdentifyUtils.getSceneId(message.getIdentify());
            }
            return MapMessage.successMessage().add("message", message).add("limit", limit).add("sceneId", sceneId);
        } catch (Exception ex) {
            logger.error("type:{},identify:{}", type, identify, ex);
            return MapMessage.errorMessage(ex.getMessage());
        }
    }
}
