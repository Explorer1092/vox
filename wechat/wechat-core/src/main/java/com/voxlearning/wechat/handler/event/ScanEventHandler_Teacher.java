/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.handler.event;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.support.ObjectCacheKeyGenerator;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.service.campaign.api.constant.CampaignType;
import com.voxlearning.utopia.service.campaign.client.WechatUserCampaignServiceClient;
import com.voxlearning.utopia.service.clazz.client.ClazzIntegralServiceClient;
import com.voxlearning.utopia.service.user.api.constants.ClazzIntegralType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.ClazzIntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaceInviteRecord;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.builder.ServiceMessageBuilder;
import com.voxlearning.wechat.constants.EventType;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.handler.AbstractHandler;
import com.voxlearning.wechat.support.WechatConfig;
import com.voxlearning.wechat.support.utils.MessageFields;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Xin Xin
 * @since 10/21/15
 */
@Slf4j
public class ScanEventHandler_Teacher extends AbstractHandler {
    private final List<String> SUBSCRIBE_CODES = Arrays.asList("1", "2", "3", "4", "5");
    private final List<String> O2O_CODES = Arrays.asList("O2O_MB_BJ", "O2O_MB_CD", "O2O_MB_GZ", "O2O_MB_SH", "O2O_MB_SZ", "O2O_MB_TJHX",
            "O2O_MB_WH", "O2O_MB_TJHB", "O2O_MB_GDFS");

    @Setter private ClazzIntegralServiceClient clazzIntegralServiceClient;
    @Setter private WechatUserCampaignServiceClient wechatUserCampaignServiceClient;

    @Override
    public String getFingerprint() {
        return WechatType.TEACHER.name() + ":" + MessageFields.FIELD_EVENT + ":" + MessageFields.FIELD_EVENT_KEY + ":" + MessageFields.FIELD_TICKET;
    }

    @Override
    public String handle(MessageContext context) {
        Objects.requireNonNull(context, "context must not be null.");

        EventType type = EventType.of(context.getEvent());
        String eventKey = context.getEventKey().replace("qrscene_", "");
        if (StringUtils.isBlank(eventKey)) {
            ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
            rb.buildMsgType(MessageType.TEXT);
            rb.buildContent("无效的参数");
            return rb.toString();
        }

        switch (type) {
            case SCAN:
                return handleOnScan(context);
            case SUBSCRIBE:
                return handleOnScan(context);
        }
        return "success";
    }

    private String handleOnScan(MessageContext context) {
        String eventKey = context.getEventKey().replace("qrscene_", "");
        if (eventKey.startsWith("FACE_2_FACE_")) {
            //扫的是面对面的二维码，不管是关注的还是未关注的用户都推一个到注册的页面
            String inviterId = eventKey.replace("FACE_2_FACE_", "");

            WechatFaceInviteRecord record = new WechatFaceInviteRecord();
            record.setInviter(Long.valueOf(inviterId));
            record.setOpenId(context.getFromUserName());
            wechatServiceClient.saveWechatFaceInviteRecord(record);

            ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
            sb.buildArticleMsg("点我去注册！参与活动领30元话费！", "", WechatConfig.getBaseSiteUrl() + "/teacher/static/images/campaign/f2finvite.jpg", WechatConfig.getBaseSiteUrl() + "/teacher/regist/index.vpage?_from=cm&woid=" + context.getFromUserName());
            messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);
            return "success";
        } else if (SUBSCRIBE_CODES.contains(eventKey)) {
            //扫的是关注二维码
            //发一条客服消息、一条被动回复文本消息
            sendMsgForSubscribe(context);
            return "success";
        } else if (O2O_CODES.contains(eventKey)) {
            //扫描o2o model b 的二维码
            String key = "wechat-o2o-modelb-" + context.getFromUserName();
            CacheSystem.CBS.getCacheBuilder().getCache("persistence").set(key, 30 * 24 * 3600, eventKey);

            //发一条客服消息、一条被动回复文本消息
            sendMsgForSubscribe(context);
            return "success";
        } else if (eventKey.equals("6") || eventKey.equals("WZW_2015_PROMOT")) {
            //专门为数博会推送的消息
            ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
            sb.buildArticleMsg("欢迎关注一起作业", "一起作业网专注K12中小学线上教学领域，为老师、学生和家长三方提供基于互联网的在线作业和能力提升的平台", WechatConfig.getBaseSiteUrl() + "/teacher/static/images/campaign/sbh.png", "http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=210705413&idx=1&sn=989e1d36553610b816764eebec9af455#rd");
            messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);
            return "success";
        } else if (eventKey.equals("SUBSCRIBE_OPERATION")) {
            //月刊
            ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
            sb.buildArticleMsg("【首次揭秘】月刊背后的故事！", "", "https://mmbiz.qlogo.cn/mmbiz/1LxbKSTZB7GaGYKzfWP6c8xeVZGfpmHCVNKcDAL2fYyQtUNicx1lgJ73FrVOLnCW1VUEtZXObzrdIBJNVtUtyxQ/0?wx_fmt=jpeg", "http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=214063548&idx=1&sn=16ce40d00f3b047a25884100b99afc3f#rd");
            sb.buildArticleMsg("【关爱】让“爱”拨动学生心灵的弦", "", "https://mmbiz.qlogo.cn/mmbiz/1LxbKSTZB7GaGYKzfWP6c8xeVZGfpmHCmyJx7sadIYZTNzkCat8CypLwSharPHT0D7Sibj6AvFyaq8cURiatrC7Q/0?wx_fmt=jpeg", "http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=214063548&idx=2&sn=1498b9c118c9763cab8aa8efa52e97c5#rd");
            sb.buildArticleMsg("【课件】课件之痛，不痛不痛啦", "", "https://mmbiz.qlogo.cn/mmbiz/1LxbKSTZB7GaGYKzfWP6c8xeVZGfpmHCws9RUdnQflbakJzex09zhDOml9930MWGvfoDCVeTAWAyU5fQsvPM8w/0?wx_fmt=jpeg", "http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=214063548&idx=3&sn=cf72f5a857ab5ea9668cacb15ec70769#rd");
            sb.buildArticleMsg("【复习】高效复习，贵在得法", "", "https://mmbiz.qlogo.cn/mmbiz/1LxbKSTZB7GaGYKzfWP6c8xeVZGfpmHCn7AicrXALfw5YaJG18oQhMEIpGiamMvNZhksyE1nZMpr8EEwuG735UrA/0?wx_fmt=jpeg", "http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=214063548&idx=4&sn=d563ff7b7059ec3ee890fa25f39f4c59#rd");
            sb.buildArticleMsg("【口语】三大妙招，打破“哑巴英语”的魔咒！", "", "https://mmbiz.qlogo.cn/mmbiz/1LxbKSTZB7GaGYKzfWP6c8xeVZGfpmHCpgib7sXNOu7j0d7qj4fhD8weRkorFfuGY5trbOFicrY7ejRCBMCDlD7g/0?wx_fmt=jpeg", "http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=214063548&idx=5&sn=994beb5fb97f4b395e9c5cabf42235b4#rd");
            sb.buildArticleMsg("【课堂】调节课堂气氛的5个环节式创意设计", "", "https://mmbiz.qlogo.cn/mmbiz/1LxbKSTZB7GaGYKzfWP6c8xeVZGfpmHCH2HeBWo4y20kDMDQTL5HibvNlcic0cswkxXYomg2YSVIPrh9plh97WAg/0?wx_fmt=jpeg", "http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=214063548&idx=7&sn=da9b458f290a6e1ad7d27d7486e4e24b#rd");
            sb.buildArticleMsg("【批评】尊重学生“五不批评”", "", "https://mmbiz.qlogo.cn/mmbiz/1LxbKSTZB7GaGYKzfWP6c8xeVZGfpmHCH2HeBWo4y20kDMDQTL5HibvNlcic0cswkxXYomg2YSVIPrh9plh97WAg/0?wx_fmt=jpeg", "http://mp.weixin.qq.com/s?__biz=MjM5MzAxMTYwMA==&mid=214063548&idx=7&sn=da9b458f290a6e1ad7d27d7486e4e24b#rd");
            messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);
            return "success";
        }

        //判断这个用户是否是扫描了邀请的二维码过来的，如果是则推送有奖邀请的消息
        String key = "RECOMMEND_INVITE_FLAG_" + eventKey;
        CacheObject<String> value = CacheSystem.CBS.getCacheBuilder().getCache("persistence").get(key);
        boolean needInvite = false;
        if (null != value && !StringUtils.isBlank(value.getValue())) {
            ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
            sb.buildArticleMsg("邀请老师送话费", "多多益善，邀请越多，话费越多，上不封顶", WechatConfig.getBaseSiteUrl() + "/teacher/static/public/invite/images/invite_cm.jpg", WechatConfig.getBaseSiteUrl() + "/teacher/ucenter/invite/share.vpage?_from=article");
            messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);

            needInvite = true;
        } else {
            String k = "TEACHER_INVITEE_REGIST_SUBSCRIBE_" + eventKey;
            k = MemcachedKeyConstants.WECHAT_MEMCACHE_KEY_PREFIX + "_" + ObjectCacheKeyGenerator.generate(k);
            CacheObject<Object> object = CacheSystem.CBS.getCacheBuilder().getCache("flushable").get(k);
            if (null != object && null != object.getValue()) {
                ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
                sb.buildTxtMsg("欢迎加入一起作业大家庭，认证您的老师身份，获得20元话费！<a href=\"" + WechatConfig.getBaseSiteUrl() + "/teacher/regist/selectschool.vpage?_from=cm_invitee_regist&woid=" + context.getFromUserName() + "\">立刻认证</a>");
                messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);
            } else {
                sendArticleMsgForSubscribe(context);
            }
        }

        //检查是不是已绑定
        User user = wechatLoaderClient.loadWechatUser(context.getFromUserName());
        if (null != user) {
            //微信号已绑定17zuoye帐号
            if (needInvite) {
                sendArticleMsgForSubscribe(context);
            }

            ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
            sb.buildTxtMsg("已绑定老师号，请点击下方按钮布置作业或检查作业");
            messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);
        } else {
            //未绑定
            if (bindTeacher(Long.valueOf(eventKey), context.getFromUserName(), "qrcode_teacher")) {
                sendMsgForSubscribe(context);
            } else {
                ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
                sb.buildTxtMsg("绑定失败，请点击下方按钮绑定登陆绑定老师号");
                messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);
            }
        }

        return "success";
    }

    private void sendMsgForSubscribe(MessageContext context) {
        //发一条客服消息、一条被动回复文本消息
        sendArticleMsgForSubscribe(context);
        sendTxtMsgForSubscribe(context);
    }

    private void sendArticleMsgForSubscribe(MessageContext context) {
        ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
        sb.buildArticleMsg("欢迎加入一起作业，点此注册", "", WechatConfig.getBaseSiteUrl() + "/teacher/static/images/campaign/welcome.jpg", WechatConfig.getBaseSiteUrl() + "/teacher/login.vpage?_from=subtui&woid=" + context.getFromUserName());
        messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);
    }

    private void sendTxtMsgForSubscribe(MessageContext context) {
        ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
        sb.buildTxtMsg("老师您来啦~欢迎加入17作业大家庭\n\n    <a href=\"http://x.eqxiu.com/s/mxMrtVTc\">【我是老师】</a> 点击这里\n\n    <a href=\"http://mp.weixin.qq.com/s?__biz=MjM5NjE5OTc0MQ==&mid=548556088&idx=1&sn=333190188ea09907b6583fdc149d2a2e#rd\">【我是家长】</a> 专属福利\n\n如果遇到任何疑问，可以随时联系我哦~\n\u2199快点左下角小键盘\ue415");
        messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);
    }

    @SneakyThrows(InterruptedException.class)
    private boolean bindTeacher(Long userId, String openId, String source) {
        CampaignType campaignType = wechatUserCampaignServiceClient.getWechatUserCampaignService().loadUserCampaign(userId).get();
        if (null != campaignType) {
            source += "_" + campaignType;
        }

        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null) {
            return false;
        }
        boolean haveBindBefore = wechatLoaderClient.haveBindBefore(userId, WechatType.TEACHER.getType());
        if (!haveBindBefore && teacher.fetchCertificationState() == AuthenticationState.SUCCESS) {
            //如果老师第一次绑定，奖励老师当月因为检查作业获取的智慧教室学豆，如果没有则送100智慧教室学豆
            //必须认证老师才能获取！！

            List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(userId).stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(e -> !e.isTerminalClazz())
                    .collect(Collectors.toList());
            for (Clazz clazz : clazzList) {
                String couchBaseKey = MemcachedKeyConstants.CHECK_HOMEWORK_ADD_SMART_CLASS_INTEGRAL + "_" + userId + "_" + clazz.getId();
                CacheObject<String> cacheObject = CacheSystem.CBS.getCacheBuilder().getCache("persistence").get(couchBaseKey);
                int integral = 0;
                if (cacheObject != null) {
                    String response = StringUtils.trim(cacheObject.getValue());
                    if (StringUtils.isNotEmpty(response)) {
                        integral += NumberUtils.toInt(response);
                    }
                }
                if (integral > 0) {
                    GroupMapper group = groupLoaderClient.loadTeacherGroupByTeacherIdAndClazzId(teacher.getId(), clazz.getId(), false);
                    if (group != null) {
                        ClazzIntegralHistory history = new ClazzIntegralHistory();
                        history.setGroupId(group.getId());
                        history.setClazzIntegralType(ClazzIntegralType.老师首次绑定微信奖励.getType());
                        history.setIntegral(integral);
                        history.setComment(ClazzIntegralType.老师首次绑定微信奖励.getDescription());
                        history.setAddIntegralUserId(teacher.getId());
                        clazzIntegralServiceClient.getClazzIntegralService()
                                .changeClazzIntegral(history)
                                .awaitUninterruptibly();
                    } else {
                        log.error("teacher group is null.teacherId:{},clazzId:{}", teacher.getId(), clazz.getId());
                    }
                }
                //需要把couchbase里预存的学豆清掉
                CacheSystem.CBS.getCacheBuilder().getCache("persistence").delete(couchBaseKey);
            }
        }

        wechatServiceClient.bindUserAndWechat(userId, openId, source, WechatType.TEACHER.getType(), null);

        return true;
    }
}
