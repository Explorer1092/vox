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

package com.voxlearning.utopia.agent.controller.mobile;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.constants.AgentClientType;
import com.voxlearning.utopia.agent.constants.AgentErrorCode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;
import com.voxlearning.utopia.agent.service.memorandum.AgentMemorandumService;
import com.voxlearning.utopia.agent.service.mobile.v2.WorkRecordService;
import com.voxlearning.utopia.agent.service.region.AgentRegionService;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.ClientAppUpgradeCtl;
import com.voxlearning.utopia.service.config.client.ClientApplicationConfigServiceClient;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * @author Jia HuanYin
 * @since 2015/6/30
 */
@Controller
@RequestMapping("/mobile")
public class MobileController extends AbstractAgentController {

    public static final String IOS_DOWNLOAD_URL = "https://cdn-cnc.17zuoye.com/resources/app/market/iOS/Market_3.0.6/Market.html";
    public static final String ANDROID_DOWNLOAD_URL = "http://cdn-cnc.17zuoye.cn/resources/app/market/Android/market_release_3.0.6.1100.apk";

    @Inject private RaikouSystem raikouSystem;
    @Inject private ClientApplicationConfigServiceClient clientApplicationConfigServiceClient;

    @Inject
    private AgentRegionService agentRegionService;

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @Inject
    private WorkRecordService workRecordService;

    @Inject private AgentMemorandumService agentMemorandumService;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index() {
        // FIXME 如果修改此处的话，请务必对 MobileInterceptor 相应地方作出调整
        return "redirect:/view/mobile/crm/home/index.vpage";
    }

    @RequestMapping(value = "download.vpage")
    public String download(Model model) {
        try {
            AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext();
            String userAgent = getRequest().getHeader(context.getHeaderMap().getOrDefault("user-agent", "User-Agent")).toLowerCase();
            if (userAgent.indexOf("micromessenger") > 0) {
                return "mobile/download";
            }
            ClientAppUpgradeCtl inst = clientApplicationConfigServiceClient.getClientApplicationConfigService()
                    .loadAllClientAppUpgradeCtlsFromDB()
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> StringUtils.equals(e.getProductId(), userAgent.indexOf("iphone") > 0 ? AgentClientType.IOS.getProductId() : AgentClientType.ANDROID.getProductId()))
                    .findFirst()
                    .orElse(null);
            if (inst == null || inst.getResponse() == null || StringUtils.isBlank(inst.getResponse().getApkUrl())) {
                return errorInfoPage(AgentErrorCode.CLIENT_UPDATE_ERROR, "客户端升级配置出现异常，请联系管理员", model);
            }
            String url = inst.getResponse().getApkUrl();
            return "redirect:" + url;
        } catch (Exception ex) {
            return "redirect:" + ANDROID_DOWNLOAD_URL;
        }
    }

    // 省级区域列表
    @RequestMapping(value = "common/provinces.vpage")
    @ResponseBody
    public List<ExRegion> provinces() {
        return raikouSystem.getRegionBuffer().loadProvinces();
    }

    // 下级区域列表
    @RequestMapping(value = "common/child_regions.vpage")
    @ResponseBody
    public List<ExRegion> childRegions() {
        Integer regionCode = requestInteger("regionCode");
        return raikouSystem.getRegionBuffer().loadChildRegions(regionCode);
    }

    // 用户负责区域
    @RequestMapping(value = "common/user_regions.vpage")
    @ResponseBody
    public String userRegions() {
        AuthCurrentUser user = getCurrentUser();
        Map<Object, Object> userRegions = agentRegionService.buildUserRegionMapTree(user);
        return JsonUtils.toJson(userRegions);
    }
}
