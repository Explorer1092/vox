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

package com.voxlearning.utopia.admin.controller.site;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.CommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Yuechen.Wang on 2016/1/29.
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/commonconfig")
public class CommonConfigController extends AbstractAdminSystemController {

    @Inject private RaikouSystem raikouSystem;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    private Map<String, ConfigCategory> categoryMap = ConfigCategory.categoryMap();

    @RequestMapping(value = "configlist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        String category = getRequestString("qCategory");
        String configKey = getRequestString("qConfigKey");
        Integer pageNumber = Integer.max(1, getRequestInt("pageNumber", 1));
        Pageable pageable = new PageRequest(pageNumber - 1, 10);

        if (StringUtils.isNotBlank(category)) {
            Stream<CommonConfig> stream = crmConfigService.$loadCommonConfigs().stream()
                    .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                    .sorted((o1, o2) -> {
                        long u1 = SafeConverter.toLong(o1.getUpdateDatetime());
                        long u2 = SafeConverter.toLong(o2.getUpdateDatetime());
                        return Long.compare(u2, u1);
                    })
                    .filter(e -> StringUtils.equals(e.getCategoryName(), category));

            if (StringUtils.isNotBlank(configKey)) {
                stream = stream.filter(e -> e.getConfigKeyName() != null)
                        .filter(e -> e.getConfigKeyName().contains(configKey.trim()));
            }
            Page<Map<String, Object>> page = PageableUtils.listToPage(stream.map(this::mapConfig).collect(Collectors.toList()), pageable);

            model.addAttribute("configPage", page);
        }
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("category", category);
        model.addAttribute("configKey", configKey);
        model.addAttribute("categoryMap", categoryMap);
        return "/site/commonconfig/list";
    }

    @RequestMapping(value = "saveconfig.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage saveCommonConfig() {
        MapMessage message;
        String category = getRequestParameter("category", null);
        String configKey = getRequestParameter("configKey", null);
        String configValue = getRequestParameter("configValue", null);
        Integer regionCode = getRequestInt("regionCode", 0);
        String description = getRequestString("description");
        try {
            long id = getRequestLong("id", 0L);
            message = validateParams(id, category, configKey, configValue, regionCode);
            if (!message.isSuccess()) {
                return message;
            }
            Long userId = getCurrentAdminUser().getFakeUserId();
            String userName = getCurrentAdminUser().getAdminUserName();
            CommonConfig config = new CommonConfig();
            config.setCategoryName(category);
            config.setConfigKeyName(configKey);
            config.setConfigKeyValue(configValue);
            config.setConfigRegionCode(regionCode);
            config.setDescription(description);
            config.setLatestUpdateUserId(userId);
            config.setLatestUpdateUserName(userName);
            if (id == 0L) {
                config.setId(null);
                config = crmConfigService.$upsertCommonConfig(config);
                addAdminLog("新增配置", "", "Success:" + (config != null));
                message = new MapMessage().setSuccess(config != null);
            } else {
                config.setId(id);
                config = crmConfigService.$upsertCommonConfig(config);
                addAdminLog("更新配置", id, "Success:" + (config != null));
                message = new MapMessage().setSuccess(config != null);
            }
        } catch (Exception ex) {
            logger.error("Failed Save Common Config, cat={}, (k,v)=({},{}), region={}", category, configKey, configValue, regionCode, ex);
            return MapMessage.errorMessage("通用配置保存失败：" + ex.getMessage());
        }
        return message;
    }

    @RequestMapping(value = "deleteconfig.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage delCommonConfig() {
        Long id = getRequestLong("id", 0L);
        if (id == 0L) {
            return MapMessage.errorMessage();
        }
        boolean ret = crmConfigService.$disableCommonConfig(id);
        addAdminLog("删除配置", id, "Success:" + ret);
        return new MapMessage().setSuccess(ret);
    }

    private MapMessage validateParams(Long id, String category, String configKey, String configValue, Integer regionCode) {
        String regStr = "^([a-zA-Z_])([a-zA-Z0-9_.]){0,29}$";
        if (StringUtils.isBlank(category) || StringUtils.isBlank(configKey) || StringUtils.isBlank(configValue)) {
            return MapMessage.errorMessage("目录名称、配置的Key、配置的Value以及地区编码不得为空！");
        }
        if (regionCode != 0 && raikouSystem.loadRegion(regionCode) == null) {
            return MapMessage.errorMessage("不存在地区编码为{}的地区! ", regionCode);
        }
        if (ConfigCategory.safeParse(category) == ConfigCategory.UNKNOWN) {
            return MapMessage.errorMessage("目录名称错误，请重新选择！");
        }
        if (!category.matches(regStr) || !configKey.matches(regStr)) {
            return MapMessage.errorMessage("目录名称/配置的Key 只能由字母、数字以及点(.)下划线(_)组成，最长三十个字符！");
        }
        if (configValue.length() > 450) {
            return MapMessage.errorMessage("配置的Value过长，请重新编辑！");
        }
        if (checkConfigConflict(id, category, configKey, regionCode)) {
            return MapMessage.errorMessage(StringUtils.formatMessage("已经存在一个【目录名:{}, 配置Key:{}, 地区:{}】的配置\n不能重复添加！", category, configKey, regionCode));
        }
        return MapMessage.successMessage();
    }

    /**
     * 校验不能存在相同目录、相同配置Key以及相同地区的配置
     *
     * @param id         配置Id
     * @param category   目录
     * @param configKey  配置Key
     * @param regionCode 地区编码
     * @return
     */
    private boolean checkConfigConflict(Long id, String category, String configKey, Integer regionCode) {
        // 无效的参数
        if (StringUtils.isBlank(category) || StringUtils.isBlank(configKey) || regionCode == null) {
            return true;
        }
        List<CommonConfig> configList = crmConfigService.$loadCommonConfigs().stream()
                .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                .filter(e -> StringUtils.equals(e.getCategoryName(), category))
                .sorted((o1, o2) -> {
                    long u1 = SafeConverter.toLong(o1.getUpdateDatetime());
                    long u2 = SafeConverter.toLong(o2.getUpdateDatetime());
                    return Long.compare(u2, u1);
                })
                .collect(Collectors.toList());
        for (CommonConfig config : configList) {
            if (!Objects.equals(id, config.getId())
                    && configKey.equals(config.getConfigKeyName())
                    && Objects.equals(regionCode, config.getConfigRegionCode())) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> mapConfig(CommonConfig config) {
        Map<String, Object> mapper = new HashMap<>();
        mapper.put("id", config.getId());
        mapper.put("categoryName", config.getCategoryName());
        mapper.put("configKeyName", config.getConfigKeyName());
        mapper.put("configKeyValue", config.getConfigKeyValue());
        mapper.put("configRegionCode", config.getConfigRegionCode());
        mapper.put("description", config.getDescription());
        mapper.put("createDatetime", DateUtils.dateToString(config.getCreateDatetime()).replace(" ", "<br>"));
        mapper.put("updateDatetime", DateUtils.dateToString(config.getCreateDatetime()).replace(" ", "<br>"));
        mapper.put("latestUpdateUserName", config.getLatestUpdateUserName());
        mapper.put("category", categoryMap.get(config.getCategoryName()).getNote().replace("_", "<br>"));
        return mapper;
    }
}
