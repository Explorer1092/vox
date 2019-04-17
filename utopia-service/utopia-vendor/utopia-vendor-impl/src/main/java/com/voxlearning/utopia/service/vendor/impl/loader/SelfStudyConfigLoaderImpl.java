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

package com.voxlearning.utopia.service.vendor.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.vendor.api.SelfStudyConfigLoader;
import com.voxlearning.utopia.service.vendor.api.entity.SelfStudyBasicConfig;
import com.voxlearning.utopia.service.vendor.api.entity.SelfStudyConfigWrapper;
import com.voxlearning.utopia.service.vendor.api.entity.SelfStudyOperativeConfig;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by jiangpeng on 16/8/5.
 */
@Named
@Service(interfaceClass = SelfStudyConfigLoader.class)
@ExposeService(interfaceClass = SelfStudyConfigLoader.class)
public class SelfStudyConfigLoaderImpl extends SpringContainerSupport implements SelfStudyConfigLoader {


    private final static String PAGE_KEY = "SelfStudyConfig";

    private String BASIC_PAGE_NAME = "SelfStudyBasicConfig";
    private String OP_PAGE_NAME = "SelfStudyOpConfig";

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Override
    public MapMessage loadSelfStudyShowConfigByClazzLevel(Integer clazzLevel) {

        List<SelfStudyConfigWrapper> resultWrapperList = internalLoadSelfStudyShowConfig(clazzLevel, null, RequestSource.H5);
        if (CollectionUtils.isEmpty(resultWrapperList))
            return MapMessage.errorMessage("no config");
        return MapMessage.successMessage().add("result", resultWrapperList);

    }


    @Override
    public List<SelfStudyConfigWrapper> loadSelfStudyConfig4LearnGrowth(Integer clazzLevel, String version) {
        List<SelfStudyConfigWrapper> resultWrapperList = internalLoadSelfStudyShowConfig(clazzLevel, version, RequestSource.APP);
        if (CollectionUtils.isEmpty(resultWrapperList))
            return Collections.emptyList();
        return resultWrapperList;
    }


    /**
     * @param clazzLevel    年级 没有孩子 传0。有孩子,孩子没班级,也传0
     * @param version
     * @param requestSource
     * @return
     */
    private List<SelfStudyConfigWrapper> internalLoadSelfStudyShowConfig(Integer clazzLevel, String version, RequestSource requestSource) {

        List<SelfStudyBasicConfig> basicConfigList = pageBlockContentServiceClient.loadConfigList(PAGE_KEY, BASIC_PAGE_NAME, SelfStudyBasicConfig.class);
        //根据requestSource version 是否支持有孩子过滤配置。
        basicConfigList = basicConfigList.stream().
                filter(config ->
                        (SelfStudyBasicConfig.Status.ONLINE == config.getStatusEnum())
                                && (CollectionUtils.isEmpty(config.getRequestSources()) || config.getRequestSources().stream().anyMatch(s -> requestSource.name().equals(s)))
                                && (StringUtils.isBlank(version) || StringUtils.isBlank(config.getStartVersion()) || VersionUtil.compareVersion(version, config.getStartVersion()) >= 0)
                )
                .sorted((o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder())).collect(Collectors.toList());

        List<SelfStudyOperativeConfig> operativeConfigList = pageBlockContentServiceClient.loadConfigList(PAGE_KEY, OP_PAGE_NAME, SelfStudyOperativeConfig.class);
        Date now = new Date();
        Map<String, List<SelfStudyOperativeConfig>> basicConfigId2OpConfigListMap = operativeConfigList.stream().filter(op -> {
            if ((op.getStartDate() != null && now.before(op.toStartDate()))
                    || (op.getEndDate() != null && now.after(op.toEndDate())))
                return false;
            if (CollectionUtils.isNotEmpty(op.getClazzLevelList()) && !op.getClazzLevelList().contains(clazzLevel))
                return false;
            return true;
        }).collect(Collectors.groupingBy(SelfStudyOperativeConfig::getToolId));

        List<SelfStudyConfigWrapper> resultWrapperList = new ArrayList<>();
        basicConfigList.forEach(bc -> {
            SelfStudyConfigWrapper wrapper = SelfStudyConfigWrapper.newInstance(bc);

            //以下是运营文案和红点
            List<SelfStudyOperativeConfig> opConfigList = basicConfigId2OpConfigListMap.get(bc.getId());
            if (CollectionUtils.isNotEmpty(opConfigList)) {
                SelfStudyOperativeConfig operativeConfig = opConfigList.stream().sorted((o1, o2) -> o1.toCreateDate().compareTo(o2.toCreateDate()))
                        .findFirst().orElse(null);
                if (operativeConfig != null) {
                    wrapper.addOperativeConfig(operativeConfig);
                }
            }
            resultWrapperList.add(wrapper);
        });
        return resultWrapperList;
    }

    private List<SelfStudyOperativeConfig> getOpConfigList(PageBlockContent opConfig) {
        if (opConfig == null)
            return Collections.emptyList();
        try {
            String content = opConfig.getContent();
            List<SelfStudyOperativeConfig> selfStudyOperativeConfigs = JsonUtils.fromJsonToList(content.replaceAll("\n|\r|\t", "").trim(), SelfStudyOperativeConfig.class);
            return selfStudyOperativeConfigs == null ? Collections.emptyList() : selfStudyOperativeConfigs;
        } catch (Exception e) {
            logger.warn("selfStudyOpConfig json format error!!");
            return Collections.emptyList();
        }
    }

    private List<SelfStudyBasicConfig> getBasicConfigList(PageBlockContent basicConfig) {
        try {
            String content = basicConfig.getContent();
            List<SelfStudyBasicConfig> selfStudyBasicConfigs = JsonUtils.fromJsonToList(content.replaceAll("\n|\r|\t", "").trim(), SelfStudyBasicConfig.class);
            return selfStudyBasicConfigs == null ? Collections.emptyList() : selfStudyBasicConfigs;
        } catch (Exception e) {
            logger.warn("selfStudyBasicConfig json format error!!");
            return Collections.emptyList();
        }
    }
}
