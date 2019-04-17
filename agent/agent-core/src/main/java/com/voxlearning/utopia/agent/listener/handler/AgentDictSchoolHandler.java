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

package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentDictSchool;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentDictSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.loader.agent.AgentUserSchoolLoaderClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentDictSchoolServiceClient;
import com.voxlearning.utopia.service.crm.consumer.service.agent.AgentUserSchoolServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Objects;

/**
 * 字典表数据更新操作
 * Created by yaguang.wang on 2017/1/17.
 */
@Named
public class AgentDictSchoolHandler extends SpringContainerSupport {

    @Inject private RaikouSystem raikouSystem;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private AgentDictSchoolServiceClient agentDictSchoolServiceClient;
    @Inject private AgentDictSchoolLoaderClient agentDictSchoolLoaderClient;
    @Inject private AgentUserSchoolLoaderClient agentUserSchoolLoaderClient;
    @Inject private AgentUserSchoolServiceClient agentUserSchoolServiceClient;
    @Inject private BaseOrgService baseOrgService;
    @Inject private BaseDictService baseDictService;

    public void runningRanking() {
        schoolDictConfigVerifyInfo();
    }

    private void schoolDictConfigVerifyInfo() {
        List<AgentDictSchool> agentSchoolDictList = agentDictSchoolLoaderClient.findAllDictSchool();
        if (CollectionUtils.isEmpty(agentSchoolDictList)) {
            return;
        }
        agentSchoolDictList.forEach((AgentDictSchool p) -> {
            try {
                if (p != null) {
                    School school = raikouSystem.loadSchool(p.getSchoolId());
                    if (school == null) {
                        flushOrgService(p.getSchoolId());
                        p.setDisabled(true);
                        agentDictSchoolServiceClient.upsert(p);
                        return;
                    }
                    Integer regionCode = school.getRegionCode();
                    ExRegion exRegion = raikouSystem.loadRegion(regionCode);
                    if (exRegion == null) {
                        return;
                    }
                    // 更新用户权限下面的schoolLevel
                    if (!Objects.equals(school.getLevel(), p.getSchoolLevel())) {
                        baseOrgService.getUserSchoolBySchool(school.getId()).forEach(p1 -> {
                            p1.setSchoolLevel(school.getLevel());
                            baseOrgService.updateUserSchool(p1.getId(), p1);
                        });
                    }

                    if (!Objects.equals(regionCode, p.getCountyCode()) || !Objects.equals(school.getLevel(), p.getSchoolLevel()) ||
                            !Objects.equals(ConversionUtils.toString(exRegion.getCityName()) + ConversionUtils.toString(exRegion.getCountyName()), p.getCountyName())) {
                        p.setCountyCode(regionCode);
                        p.setCountyName(ConversionUtils.toString(exRegion.getCityName()) + ConversionUtils.toString(exRegion.getCountyName()));
                        p.setSchoolLevel(school.getLevel());
                        agentDictSchoolServiceClient.upsert(p);
                    }
                }
            } catch (Exception ex) {
                logger.error("update school dict of regionCode and regionName is failed dictId=" + ConversionUtils.toString(p.getId()), ex);
            }
        });
    }

    private void flushOrgService(Long schoolId) {
        List<AgentUserSchool> userSchoolList = agentUserSchoolLoaderClient.findBySchoolId(schoolId);
        if (CollectionUtils.isNotEmpty(userSchoolList)) {
            userSchoolList.forEach(p -> {
                p.setDisabled(true);
                agentUserSchoolServiceClient.update(p.getId(), p);
            });
        }
    }
}
