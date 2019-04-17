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

package com.voxlearning.washington.controller.open.wechat.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaceInviteRecord;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashSet;

import static com.voxlearning.utopia.api.constant.OperationSourceType.wechat;
import static com.voxlearning.utopia.service.user.api.constants.InvitationType.TEACHER_INVITE_TEACHER_SMS_BY_WECHAT;

/**
 * 微信端老师邀请
 * Created by Shuai Huan on 2015/3/19.
 */
@Controller
@RequestMapping(value = "/open/wechat/teacher/invite")
@Slf4j
public class WechatTeacherInviteController extends AbstractOpenController {
    @Inject
    private GrayFunctionManagerClient grayFunctionManagerClient;

    @Inject private WechatCodeServiceClient wechatCodeServiceClient;
    @Inject private WechatServiceClient wechatServiceClient;

    // 是否可以邀请初中老师
    @RequestMapping(value = "isInviteJuniorAvailable.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext isInviteJuniorAvailable(HttpServletRequest request) {
        OpenAuthContext ctx = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(ctx.getParams().get("userId"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            ctx.setCode("400");
            ctx.setError("invalid parameters");
            return ctx;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        boolean isPrimarySchool = teacherDetail.isPrimarySchool();
        boolean isAvailable = isPrimarySchool && grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, "JMS", "Invitation");
        ctx.setCode("200");
        ctx.setError("未开放注册");
        ctx.add("isAvailable", isAvailable);
        return ctx;
    }

    // isWebGrayFunction
    @RequestMapping(value = "isWebGrayFunction.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext isWebGrayFunction(HttpServletRequest request) {
        OpenAuthContext ctx = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(ctx.getParams().get("userId"), Long.MIN_VALUE);
        String mainFunc = SafeConverter.toString(ctx.getParams().get("main"));
        String subFunc = SafeConverter.toString(ctx.getParams().get("sub"));
        if (teacherId == Long.MIN_VALUE) {
            ctx.setCode("400");
            ctx.setError("invalid parameters");
            return ctx;
        }
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        boolean isAvailable = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(teacherDetail, mainFunc, subFunc);
        ctx.setCode("200");
        ctx.add("isAvailable", isAvailable);
        return ctx;
    }

    // 小学邀请初中
    @RequestMapping(value = "invitepstjmst.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext invite_pst_jmst(HttpServletRequest request) {
        OpenAuthContext ctx = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(ctx.getParams().get("userId"), Long.MIN_VALUE);
        String mobile = SafeConverter.toString(ctx.getParams().get("mobile"));
        String realname = SafeConverter.toString(ctx.getParams().get("realname"));
        String schoolName = SafeConverter.toString(ctx.getParams().get("schoolName"));
        Subject subject = Subject.ofWithUnknown(SafeConverter.toString(ctx.getParams().get("subject")));

        if (teacherId == Long.MIN_VALUE || StringUtils.isBlank(mobile) || StringUtils.isBlank(realname)
                || StringUtils.isBlank(schoolName) || subject == Subject.UNKNOWN) {
            ctx.setCode("400");
            ctx.setError("invalid parameters");
            return ctx;
        }

        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(teacherId);
        if (teacher == null) {
            ctx.setCode("400");
            ctx.setError("invalid parameters");
            return ctx;
        }

        try {
            MapMessage mesg = atomicLockManager.wrapAtomic(deprecatedInvitationServiceClient)
                    .keyPrefix("TEACHER_INVITE_TEACHER_SMS_BY_WECHAT")
                    .keys(teacher.getId(), mobile, realname)
                    .proxy()
                    .invite(teacher, mobile, realname, subject, new HashSet<>(), schoolName,
                            TEACHER_INVITE_TEACHER_SMS_BY_WECHAT, wechat);

            if (mesg.isSuccess()) {
                ctx.setCode("200");
            } else {
                ctx.setCode("400");
                ctx.setError(mesg.getInfo());
            }
        } catch (DuplicatedOperationException e) {
            ctx.setCode("400");
            ctx.setError("正在处理。。。");
            return ctx;
        } catch (Exception e) {
            logger.error("wechat invite error:teacherId:{},mobile:{}", teacherId, mobile, e.getMessage(), e);
            ctx.setCode("400");
            ctx.setError("邀请失败！");
            return ctx;
        }
        return ctx;
    }

    @RequestMapping(value = "getnewinvitationprocess.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getNewInvitationProgress(HttpServletRequest request) {
        // 新邀请的进度获取
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        int pageNumber = SafeConverter.toInt(openAuthContext.getParams().get("pn"), 1);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        Pageable pageable = new PageRequest(pageNumber - 1, 5);

        openAuthContext.add("pn", pageNumber);
        openAuthContext.add("pagination", newInvitationServiceClient.fetchInvitationProgress(teacherId, pageable));
        return openAuthContext;
    }

    @RequestMapping(value = "fetchrecommendinviteflag.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext fetchRecommendInviteFlag(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        CacheObject<String> value = washingtonCacheSystem.CBS.flushable.get(MemcachedKeyConstants.RECOMMEND_INVITE_FLAG + "_" + teacherId);
        openAuthContext.setCode("200");
        openAuthContext.add("needinvite", (value != null && StringUtils.isNotEmpty(value.getValue())));
        return openAuthContext;
    }

    @RequestMapping(value = "rewardinviter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext rewardInviter(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        String key = MemcachedKeyConstants.INVITE_TEACHER_SHARE_COUNT + "_" + teacherId;
        if (washingtonCacheSystem.CBS.unflushable.add(key, DateUtils.getCurrentToDayEndSecond(), "dummy")) {
            IntegralHistory integralHistory = new IntegralHistory(teacherId, IntegralType.老师邀请分享到朋友圈微信好友获得金币, 100);
            integralHistory.setComment("您成功分享邀请红包，获得奖励10园丁豆");
            userIntegralService.changeIntegral(integralHistory);
        }
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    // 面对面二维码生成
    @RequestMapping(value = "f2fqrcode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext f2fqrcode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);

        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        if (teacherId == Long.MIN_VALUE) {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid parameters");
            return openAuthContext;
        }
        try {
            // 首先查看缓存中是否有缓存该老师的动态二维码
            String key = "WECAHT_TEACHER_F2F_DYNAMIC_QRCODE_" + teacherId;
            Object cached = wechatServiceClient.getWechatService()
                    .getFromPersistenceCache(key).getUninterruptibly();
            if (cached != null && cached instanceof String && StringUtils.isNotBlank((String) cached)) {
                // 有缓存的动态二维码地址；
                openAuthContext.setCode("200");
                openAuthContext.add("qrcode_url", cached);
            } else {
                // 重新生成动态二维码，并将结果缓存
                String qrcodeUrl = wechatCodeServiceClient.getWechatCodeService()
                        .generateF2FQrcode(key, WechatType.TEACHER)
                        .getUninterruptibly();
                // 这里缓存的和微信缓存的时间稍微增加一秒，在那一秒发生的可能会报错
                wechatServiceClient.getWechatService()
                        .addIntoPersistenceCache(key, (int) (DateUtils.calculateDateDay(new Date(), 30).getTime() / 1000), qrcodeUrl)
                        .awaitUninterruptibly();
                openAuthContext.add("qrcode_url", qrcodeUrl);
            }
        } catch (UtopiaRuntimeException ex) {
            logger.warn("生成二维码失败，msg:{}", ex.getMessage());
            openAuthContext.setCode("400");
            openAuthContext.setError("生成二维码失败");
        } catch (Exception ex) {
            logger.error("生成二维码失败,msg:{}", ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("生成二维码失败");
        }
        return openAuthContext;
    }

    // 面对面邀请 被邀请的人执行扫码
    @RequestMapping(value = "savefacerecord.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext saveFaceRecord(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long teacherId = SafeConverter.toLong(openAuthContext.getParams().get("userId"), Long.MIN_VALUE);
        String openId = ConversionUtils.toString(openAuthContext.getParams().get("openid"));
        if (teacherId == Long.MIN_VALUE || StringUtils.isBlank(openId)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("参数错误");
            return openAuthContext;
        }
        try {
            //保存扫码记录
            WechatFaceInviteRecord record = new WechatFaceInviteRecord();
            record.setInviter(teacherId);
            record.setOpenId(openId);
            wechatServiceClient.saveWechatFaceInviteRecord(record);
            openAuthContext.setCode("200");
            openAuthContext.add("info", "保存成功");
        } catch (Exception ex) {
            logger.error("保存面对面邀请扫码失败,msg:{}", ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("操作失败");
        }
        return openAuthContext;
    }

    // 面对面邀请 通过openid查询邀请人ID
    @RequestMapping(value = "loadfaceinviter.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadFaceInviter(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String openId = ConversionUtils.toString(openAuthContext.getParams().get("openid"));
        if (StringUtils.isBlank(openId)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("参数错误");
            return openAuthContext;
        }
        WechatFaceInviteRecord record = wechatLoaderClient.loadWechatFaceInviteLastRecord(openId);
        if (record == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无邀请记录");
        } else {
            openAuthContext.setCode("200");
            openAuthContext.add("inviter", record.getInviter());
        }
        return openAuthContext;
    }

}
