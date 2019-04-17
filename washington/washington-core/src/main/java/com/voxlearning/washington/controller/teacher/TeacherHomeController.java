package com.voxlearning.washington.controller.teacher;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.utopia.core.helper.AdvertiseRedirectUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * 新版老师端H5首页
 * 原生1.7.6版本开始的需求
 */
@Controller
@RequestMapping(value = "/teacher/home")
public class TeacherHomeController extends AbstractTeacherController {

    // 获取首页banner、布置作业功能列表、教学工具
    @RequestMapping(value = "index.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getTeacherHomeInfo() {
        MapMessage resultMap = MapMessage.successMessage();
        String appVersion = getRequestString(REQ_APP_NATIVE_VERSION);
        Teacher teacher = currentTeacher();

        // 获取首页banner
        resultMap.add(RES_BANNER_LIST, getTopBanner(teacher, appVersion));

        //布置作业
        resultMap.add(RES_HWFUN_LIST, getHwfunList(teacher, appVersion));

        // 老师首页教学工具
        resultMap.add(RES_TEACHING_TOOLS, getTeachingTools(teacher, appVersion));

        // 1206新增浮窗广告
        resultMap.add(RES_FLOATING_ADS_LIST, getFloatingAds(teacher, appVersion));

        boolean homeworkTextABTest = grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(currentTeacherDetail(), "HomeworkText", "ABTest");
        resultMap.add("homeworkTextABTest", !homeworkTextABTest);

        return resultMap;
    }

    // 获取首页Banner
    private List<Map<String, Object>> getTopBanner(Teacher teacher, String appVersion) {
        String slotId = "120110";
        if (VersionUtil.compareVersion(appVersion, "1.7.6.0") >= 0) {
            slotId = "120122";
        } else if (VersionUtil.compareVersion(appVersion, "1.7.3.0") >= 0) {
            slotId = "120120";
        } else if (VersionUtil.compareVersion(appVersion, "1.6.1.0") >= 0) {
            slotId = "120113";
        }

        List<Map<String, Object>> adMapperList = new ArrayList<>();
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String ua = getRequest().getHeader("User-Agent");
        String sys = getRequestString(REQ_SYS);
        Integer index = 0;
        List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(teacher.getId(), slotId, getRequestString(REQ_SYS), ver);
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
                .loadNewAdvertisementData(teacher.getId(), slotId, getRequestString(REQ_SYS), appVersion);

        for (NewAdMapper p : toolList) {
            // 旧版本过滤假期作业
            if ((teacher.isPrimarySchool() || teacher.isInfantTeacher())
                    && "SET_WINTERVACATION_HOMEWORK".equals(p.getUrl())
                    && VersionUtil.compareVersion(appVersion, "1.7.8.0") < 0) {
                continue;
            }
            // 旧版本过滤布置O2O作业
            if (teacher.isPrimarySchool() && "/view/mobile/teacher/junior/ocrhomework/clazz.vpage".equals(p.getUrl()) && VersionUtil.compareVersion(appVersion, "1.9.3.0") < 0) {
                continue;
            }
            // 旧版本过滤亲子古诗活动
            if (teacher.isPrimarySchool() && "/view/mobile/teacher/activity2019/poetry/index.vpage".equals(p.getUrl()) && VersionUtil.compareVersion(appVersion, "1.9.3.0") < 0) {
                continue;
            }
            // 旧版本过滤布置单元测验
            if (teacher.isPrimarySchool() && "SET_UNIT_TEST".equalsIgnoreCase(p.getUrl()) && VersionUtil.compareVersion(appVersion, "1.9.5.0") < 0) {
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

    private List<Map<String, Object>> getFloatingAds(Teacher teacher, String appVersion) {
        List<Map<String, Object>> floatingAds = new ArrayList<>();

        String slotId = "120106";
        String ver = getRequestString(REQ_APP_NATIVE_VERSION);
        String sys = getRequestString(REQ_SYS);
        String ua = getRequest().getHeader("User-Agent");

        List<NewAdMapper> floatingAdList = userAdvertisementServiceClient.getUserAdvertisementService()
                .loadNewAdvertisementData(teacher.getId(), slotId, getRequestString(REQ_SYS), appVersion);

        for (NewAdMapper p : floatingAdList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("img", combineCdbUrl(p.getImg()));
            item.put("resourceUrl", AdvertiseRedirectUtils.redirectUrl(p.getId(), 0, ver, sys, "", 0L));
            floatingAds.add(item);

            //曝光打点
            if (Boolean.TRUE.equals(p.getLogCollected())) {
                LogCollector.info("sys_new_ad_show_logs",
                        MapUtils.map(
                                "user_id", teacher.getId(),
                                "env", RuntimeMode.getCurrentStage(),
                                "version", ver,
                                "aid", p.getId(),
                                "acode", SafeConverter.toString(p.getCode()),
                                "index", 0,
                                "slotId", slotId,
                                "client_ip", getWebRequestContext().getRealRemoteAddress(),
                                "time", DateUtils.dateToString(new Date()),
                                "agent", ua,
                                "system", sys
                        ));
            }
        }

        return floatingAds;
    }
}
