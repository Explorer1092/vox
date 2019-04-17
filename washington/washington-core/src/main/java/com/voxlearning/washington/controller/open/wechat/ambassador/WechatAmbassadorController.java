/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.wechat.ambassador;

import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.AmbassadorCompetitionScoreType;
import com.voxlearning.utopia.api.constant.AmbassadorLevel;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorLevelDetail;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserTagEventType;
import com.voxlearning.utopia.service.user.api.constants.UserTagType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by XiaoPeng.Yang on 15-4-23.
 * 校园大使微信号
 */
@Controller
@RequestMapping(value = "/open/wechat/ambassador")
@Slf4j
public class WechatAmbassadorController extends AbstractOpenController {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;
    @Inject private AmbassadorServiceClient ambassadorServiceClient;

    //将老师与微信帐号绑定
    @RequestMapping(value = "/bindambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext bindUser(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String openId = ConversionUtils.toString(openAuthContext.getParams().get("openId"));
        Long userId = ConversionUtils.toLong(openAuthContext.getParams().get("userId"));
        String source = ConversionUtils.toString(openAuthContext.getParams().get("s"));
        if (null == openId) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的openId或userId");
            return openAuthContext;
        }
        try {
            boolean haveBindBefore = wechatLoaderClient.haveBindBefore(userId, WechatType.AMBASSADOR.getType());
            MapMessage message = wechatServiceClient.bindUserAndWechat(userId, openId, source, WechatType.AMBASSADOR.getType(), null);
            if (message.isSuccess() && null != message.get("id")) {
                Long id = (Long) message.get("id");
                // 记录校园大使（实习）点亮标签
                ambassadorServiceClient.getAmbassadorService().recordAmbassadorMentor(userId,
                        MiscUtils.map(UserTagType.AMBASSADOR_BIND_WECHAT, UserTagEventType.AMBASSADOR_BIND_WECHAT));
                // 如果没绑定过 第一次加经验值
                if (!haveBindBefore) {
                    // 加经验值
                    ambassadorServiceClient.getAmbassadorService().addAmbassadorScore(userId, 0L, AmbassadorCompetitionScoreType.BIND_WECHAT);
                }
                openAuthContext.setCode("200");
                openAuthContext.add("rid", id);
            } else {
                openAuthContext.setCode("400");
                openAuthContext.setError("绑定失败");
            }
        } catch (Exception ex) {
            log.error("bind user and wechat failed,[userId:{},openId:{},msg:{}]", userId, openId, ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("绑定失败");
        }
        return openAuthContext;
    }

    //获取学校信息
    @RequestMapping(value = "/getambassadorinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getAmbassadorInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        Long userId = ConversionUtils.toLong(openAuthContext.getParams().get("uid"));
        TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
        openAuthContext.add("isAmbassador", false);
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacher.getId())
                .getUninterruptibly();
        openAuthContext.add("school", school);
        openAuthContext.add("teacherName", teacher.fetchRealname());
        openAuthContext.add("imgUrl", getUserAvatarImgUrl(teacher.fetchImageUrl()));
        if (teacher.isSchoolAmbassador()) {
            openAuthContext.add("isAmbassador", true);
            //校园大使级别
            AmbassadorLevelDetail levelDetail = ambassadorLoaderClient.getAmbassadorLoader().loadAmbassadorLevelDetail(teacher.getId());
            if (levelDetail == null) {
                levelDetail = new AmbassadorLevelDetail();
                levelDetail.setAmbassadorId(currentUserId());
                levelDetail.setLevel(AmbassadorLevel.SHI_XI);
            }
            openAuthContext.add("ambassadorLevel", levelDetail);
        }
        openAuthContext.setCode("200");
        return openAuthContext;
    }

    //申请校园大使
    @RequestMapping(value = "/applyambassador.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext applyAmbassador(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请在PC端申请，微信暂不支持此功能！");
        return openAuthContext;
    }

    //暑假作业活动页面
    @RequestMapping(value = "/vacation.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext vacation(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("功能已下线");
        return openAuthContext;

    }

}