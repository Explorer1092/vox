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

package com.voxlearning.washington.controller.open.v2.teacher;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.business.api.constant.TeacherCardType;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.mapper.TeacherCardMapper;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * teacher home page api controller
 * Created by alex on 2017/1/3.
 */
@Controller
@RequestMapping(value = "/v2/teacher")
@Slf4j
public class TeacherHomeApiController extends AbstractTeacherApiController {

    @Inject
    private UserAdvertisementServiceClient userAdvertisementServiceClient;

    // 老师端首页banner，教学工具，热门活动列表接口
    @RequestMapping(value = "/home/index.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage getTeacherHomeInfo() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequest();
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

        Teacher teacher = getCurrentTeacher();

        String appKey = getRequestString(REQ_APP_KEY);

        //获取首页banner
        resultMap.add(RES_BANNER_LIST, getTopBanner(teacher, appKey, appVersion));

        //布置作业
        resultMap.add(RES_HWFUN_LIST, getHwfunList(teacher, appVersion));

        // 老师首页教学工具
        resultMap.add(RES_TEACHING_TOOLS, getTeachingTools(teacher, appVersion));

        // 老师首页热门活动 redmine 51508, 161版本后已经没有这个东东了
        if (VersionUtil.compareVersion(appVersion, "1.6.1.0") < 0) {
            resultMap.add(RES_ACTIVITIES, getActivities(teacher));
        }

        // 小学老师首页任务卡片
        if (teacher.isPrimarySchool()) {
            resultMap.add(RES_CARD_LIST, getCardList(teacher));
        }

        return resultMap;

    }

    // 获取首页Banner
    private List<Map<String, Object>> getTopBanner(Teacher teacher, String appKey, String appVersion) {
        String slotId = "120110";
        if ("17JuniorTea".equals(appKey)) {
            slotId = "150101";
        } else {
            if (VersionUtil.compareVersion(appVersion, "1.7.6.0") >= 0) {
                slotId = "120122";
            } else if (VersionUtil.compareVersion(appVersion, "1.7.3.0") >= 0) {
                slotId = "120120";
            } else if (VersionUtil.compareVersion(appVersion, "1.6.1.0") >= 0) {
                slotId = "120113";
            }
        }

        List<Map<String, Object>> adMapperList = new ArrayList<>();
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(teacher.getId(), slotId, getRequestString(REQ_SYS), appVersion);
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String ua = getRequest().getHeader("User-Agent");
        String sys = getRequestString(REQ_SYS);
        Integer index = 0;
        for (NewAdMapper p : newAdMappers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("img", combineCdbUrl(p.getImg()));
            item.put("resourceUrl", AdvertiseRedirectUtils.redirectUrl(p.getId(), index, ver, sys, "", 0L));
            adMapperList.add(item);
            //曝光打点
            if (Boolean.TRUE.equals(p.getLogCollected())) {
                LogCollector.info("sys_new_ad_show_logs",
                        MapUtils.map(
                                "user_id", teacher.getId(),
                                "env", RuntimeMode.getCurrentStage(),
                                "version", ver,
                                "aid", p.getId(),
                                "acode", SafeConverter.toString(p.getCode()),
                                "index", index,
                                "slotId", slotId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", ua,
                                "system", sys
                        ));
            }
            index++;
        }
        return adMapperList;
    }

    private List<Map<String, Object>> getHwfunList(Teacher teacher, String appVersion) {
        String slotId = "120104";
        if (VersionUtil.compareVersion(appVersion, "1.6.7.0") >= 0) {
            slotId = "120105";
        }
        List<Map<String, Object>> hwfunList = new ArrayList<>();
        List<NewAdMapper> toolList = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(teacher.getId(), slotId, getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));

        for (NewAdMapper p : toolList) {
            // 旧版本过滤假期作业
            if ((teacher.isPrimarySchool() || teacher.isInfantTeacher())
                    && "SET_WINTERVACATION_HOMEWORK".equals(p.getUrl())
                    && VersionUtil.compareVersion(appVersion, "1.7.8.0") < 0) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("img", combineCdbUrl(p.getImg()));
            item.put("resourceUrl", p.getUrl());
            item.put("title", p.getName());
            item.put("description", p.getDescription());
            hwfunList.add(item);
        }

        return hwfunList;
    }

    private List<Map<String, Object>> getTeachingTools(Teacher teacher, String appVersion) {
        List<Map<String, Object>> teachingTools = new ArrayList<>();

        String slotId = "120111";
        if (VersionUtil.compareVersion(appVersion, "1.7.3.0") >= 0) {
            slotId = "120121";
        }

        List<NewAdMapper> toolList = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(teacher.getId(), slotId, getRequestString(REQ_SYS), appVersion);

        // FIXME 小学老师端1.5.3以上版本才能显示SET_GOAL_HOMEWORK
        if (teacher.isPrimarySchool() && CollectionUtils.isNotEmpty(toolList) && VersionUtil.compareVersion(appVersion, "1.5.3.0") < 0) {
            toolList = toolList.stream()
                    .filter(p -> StringUtils.isNoneBlank(p.getUrl()) && !p.getUrl().equals("SET_GOAL_HOMEWORK"))
                    .collect(Collectors.toList());
        }

        // 小学老师端1.5.6以上版本才能显示期末复习
        if (teacher.isPrimarySchool() && CollectionUtils.isNotEmpty(toolList) && VersionUtil.compareVersion(appVersion, "1.5.6.0") < 0) {
            toolList = toolList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getUrl()) && !p.getUrl().contains("view/newexam/termindex"))
                    .collect(Collectors.toList());
        }

        // 小学老师端1.5.7以上版本才能显示暑假作业
        if (teacher.isPrimarySchool() && CollectionUtils.isNotEmpty(toolList) && VersionUtil.compareVersion(appVersion, "1.5.7.0") < 0) {
            toolList = toolList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getUrl()) && !p.getUrl().contains("view/vacationhomework/summerhomework"))
                    .collect(Collectors.toList());
        }

        // Feature #54231 小学老师端1.6.4版本以上支持跳转到 布置作业、检查作业、我的班级
        if (teacher.isPrimarySchool() && CollectionUtils.isNotEmpty(toolList) && VersionUtil.compareVersion(appVersion, "1.6.4.0") < 0) {
            List<String> specialTypes = Arrays.asList(
                    "SET_HOMEWORK", "CHECK_HOMEWORK", "CLAZZ_MANAGER"
            ); // 这几个标识字段是客户端提供的
            toolList = toolList.stream()
                    .filter(p -> StringUtils.isNotBlank(p.getUrl()) && !specialTypes.contains(p.getUrl()))
                    .collect(Collectors.toList());
        }

        for (NewAdMapper p : toolList) {
            // 旧版本过滤假期作业
            if ((teacher.isPrimarySchool() || teacher.isInfantTeacher())
                    && "SET_WINTERVACATION_HOMEWORK".equals(p.getUrl())
                    && VersionUtil.compareVersion(appVersion, "1.7.8.0") < 0) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("img", combineCdbUrl(p.getImg()));
            item.put("resourceUrl", p.getUrl());
            item.put("title", p.getName());
            item.put("description", p.getDescription());
            teachingTools.add(item);
        }

        return teachingTools;
    }

    // 首页更多
    private List<Map<String, Object>> getActivities(Teacher teacher) {
        List<Map<String, Object>> activities = new ArrayList<>();
        List<NewAdMapper> actList = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(teacher.getId(), "120112", getRequestString(REQ_SYS), getRequestString(REQ_APP_NATIVE_VERSION));

        for (NewAdMapper act : actList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("img", combineCdbUrl(act.getImg()));
            item.put("resourceUrl", act.getUrl());
            item.put("title", act.getName());
            item.put("description", act.getDescription());
            activities.add(item);
        }

        return activities;
    }

    private List<TeacherCardMapper> getCardList(Teacher teacher) {
        try {
            String sys = getRequestString(REQ_SYS);
            String ver = getRequestString(REQ_APP_NATIVE_VERSION);
            String imgDomain = getCdnBaseUrlStaticSharedWithSep();
            String domain = getWebRequestContext().getWebAppBaseUrl();
            List<TeacherCardMapper> cardList = businessTeacherServiceClient.loadTeacherCardList(teacher, sys, ver, imgDomain);
            // 只有一个卡片时添加一个"没有更多"的占位卡片
            if (cardList.size() == 1) {
                TeacherCardMapper noneMapper = new TeacherCardMapper();
                noneMapper.setCardName("没有更多任务啦");
                noneMapper.setCardType(TeacherCardType.NONE);
                noneMapper.setImgUrl("resources/app/17teacher/res/non-task_banner.png");
                cardList.add(noneMapper);
            }
            // 将图片和详情页相对路径转为绝对路径
            // 如果为空或者已经是绝对路径，则不处理
            for (TeacherCardMapper cardMapper : cardList) {
                String imgUrl = cardMapper.getImgUrl();
                String detailUrl = cardMapper.getDetailUrl();
                if (StringUtils.isNotEmpty(imgUrl) && !imgUrl.startsWith("http")) {
                    cardMapper.setImgUrl(imgDomain + imgUrl);
                }
                if (StringUtils.isNotEmpty(detailUrl) && !detailUrl.startsWith("http")) {
                    cardMapper.setDetailUrl(domain + detailUrl);
                }
            }
            return cardList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
