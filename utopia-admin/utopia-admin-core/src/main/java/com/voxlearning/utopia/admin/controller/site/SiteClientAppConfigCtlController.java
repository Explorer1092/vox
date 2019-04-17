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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.ClientAppConfigCtl;
import com.voxlearning.utopia.service.config.api.entity.ClientAppConfigCtlResponse;
import com.voxlearning.utopia.service.config.client.ClientApplicationConfigServiceClient;
import com.voxlearning.utopia.service.vendor.api.constant.ClientAppConfigType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author malong
 *         CRM-JSPatch
 * @since 2016/8/26
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/clientconfig")
public class SiteClientAppConfigCtlController extends SiteAbstractController {

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    @Inject private ClientApplicationConfigServiceClient clientApplicationConfigServiceClient;

    private static final Map<String, String> templateFactory;
    private static final String listType = "_LIST";
    private static final String editType = "_EDIT";
    private static final String testType = "_TEST";
    private static final String defaultTemplate = "redirect:/site/clientconfig/list.vpage";

    static {
        templateFactory = new HashMap<>();
        templateFactory.put(ClientAppConfigType.JSPATCH.name() + listType, "site/clientconfig/jspatchlist");
        templateFactory.put(ClientAppConfigType.JSPATCH.name() + editType, "site/clientconfig/jspatchedit");
        templateFactory.put(ClientAppConfigType.JSPATCH.name() + testType, "site/clientconfig/jspatchtest");


        templateFactory.put(ClientAppConfigType.RECORD.name() + listType, "site/clientconfig/apprecordlist");
        templateFactory.put(ClientAppConfigType.RECORD.name() + editType, "site/clientconfig/apprecordedit");
        templateFactory.put(ClientAppConfigType.RECORD.name() + testType, "site/clientconfig/apprecordtest");

        templateFactory.put(ClientAppConfigType.APP_DOWNLOAD.name() + listType, "site/clientconfig/appautodownloadlist");
        templateFactory.put(ClientAppConfigType.APP_DOWNLOAD.name() + editType, "site/clientconfig/appautodownloadedit");
        templateFactory.put(ClientAppConfigType.APP_DOWNLOAD.name() + testType, "site/clientconfig/appautodownloadtest");

        templateFactory.put(ClientAppConfigType.APP_RESOURCE.name() + listType, "site/clientconfig/resourcelist");
        templateFactory.put(ClientAppConfigType.APP_RESOURCE.name() + editType, "site/clientconfig/resourceedit");
        templateFactory.put(ClientAppConfigType.APP_RESOURCE.name() + testType, "site/clientconfig/resourcetest");

        templateFactory.put(ClientAppConfigType.DYNAMIC_APP_RESOURCE.name() + listType, "site/clientconfig/dynamicresourcelist");
        templateFactory.put(ClientAppConfigType.DYNAMIC_APP_RESOURCE.name() + editType, "site/clientconfig/dynamicresourceedit");
        templateFactory.put(ClientAppConfigType.DYNAMIC_APP_RESOURCE.name() + testType, "site/clientconfig/dynamicresourcetest");
    }

    @RequestMapping(value = "/list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String list(Model model) {
        String type = getRequestString("type");
        List<ClientAppConfigCtl> clientAppConfigCtlList = clientApplicationConfigServiceClient.getClientApplicationConfigService()
                .loadAllClientAppConfigCtlsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> type.equals(e.getType()))
                .collect(Collectors.toList());
        model.addAttribute("patchList", sortList(clientAppConfigCtlList));
        return generateTemplate(type, listType);
    }

    @RequestMapping(value = "/toeditpage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String edit(Model model) {
        String id = getRequestString("id");
        String type = getRequestString("type");
        if (StringUtils.isNotBlank(id)) {
            ClientAppConfigCtl clientAppConfigCtl = clientApplicationConfigServiceClient.getClientApplicationConfigService()
                    .loadAllClientAppConfigCtlsFromDB()
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> id.equals(e.getId()))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("patch", clientAppConfigCtl);
            model.addAttribute("response", clientAppConfigCtl.getResponse());
        }

        model.addAttribute("type", type);
        return generateTemplate(type, editType);
    }

    @RequestMapping(value = "/topublish.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String publish(Model model) {
        String id = getRequestString("id");
        String type = getRequestString("type");
        if (StringUtils.isNotBlank(id)) {
            ClientAppConfigCtl clientAppConfigCtl = new ClientAppConfigCtl();
            clientAppConfigCtl.setId(id);
            clientAppConfigCtl.setStatus("published");
            crmConfigService.$upsertClientAppConfigCtl(clientAppConfigCtl);
        }
        List<ClientAppConfigCtl> clientAppConfigCtlList = clientApplicationConfigServiceClient.getClientApplicationConfigService()
                .loadAllClientAppConfigCtlsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> type.equals(e.getType()))
                .collect(Collectors.toList());
        model.addAttribute("patchList", sortList(clientAppConfigCtlList));
        return generateTemplate(type, listType);
    }

    @RequestMapping(value = "/todraft.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String draft(Model model) {
        String id = getRequestString("id");
        String type = getRequestString("type");
        if (StringUtils.isNotBlank(id)) {
            ClientAppConfigCtl clientAppConfigCtl = new ClientAppConfigCtl();
            clientAppConfigCtl.setId(id);
            clientAppConfigCtl.setStatus("draft");
            crmConfigService.$upsertClientAppConfigCtl(clientAppConfigCtl);
        }
        List<ClientAppConfigCtl> clientAppConfigCtlList = clientApplicationConfigServiceClient.getClientApplicationConfigService()
                .loadAllClientAppConfigCtlsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> type.equals(e.getType()))
                .collect(Collectors.toList());
        model.addAttribute("patchList", sortList(clientAppConfigCtlList));
        return generateTemplate(type, listType);
    }

    @RequestMapping(value = "/toremove.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String remove(Model model) {
        String id = getRequestString("id");
        String type = getRequestString("type");
        if (StringUtils.isNotBlank(id)) {
            crmConfigService.$removeClientAppConfigCtl(id);
        }
        List<ClientAppConfigCtl> clientAppConfigCtlList = clientApplicationConfigServiceClient.getClientApplicationConfigService()
                .loadAllClientAppConfigCtlsFromDB()
                .getUninterruptibly()
                .stream()
                .filter(e -> type.equals(e.getType()))
                .collect(Collectors.toList());
        model.addAttribute("patchList", sortList(clientAppConfigCtlList));
        return generateTemplate(type, listType);
    }

    @RequestMapping(value = "/totestpage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String toTest(Model model) {
        String type = getRequestString("type");
        model.addAttribute("type", type);
        return generateTemplate(type, testType);
    }

    @RequestMapping(value = "/test.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage test(@RequestBody ClientAppConfigCtl entity) {
        try {
            String sendUrl = "http://www.test.17zuoye.net";
            if (RuntimeMode.isStaging()) {
                sendUrl = "http://www.staging.17zuoye.net";
            } else if (RuntimeMode.isProduction()) {
                sendUrl = "http://www.17zuoye.com";
            }

            Map<String, String> paramMap = new HashMap<>();
            String type = entity.getType();
            ClientAppConfigType configType = ClientAppConfigType.parse(type);
            if (configType == null) {
                return MapMessage.errorMessage("无效的类型");
            }
            switch (configType) {
                case JSPATCH:
                    paramMap.put("productId", entity.getProductId());
                    paramMap.put("apkVer", entity.getApkVer());
                    paramMap.put("sysVer", entity.getSysVer());
                    paramMap.put("user", entity.getUser());
                    paramMap.put("model", entity.getModel());
                    paramMap.put("ktwelve", entity.getKtwelve());
                    sendUrl = sendUrl + "/client/getpatch.vpage";
                    break;
                case RECORD:
                    paramMap.put("productId", entity.getProductId());
                    paramMap.put("region", entity.getRegion());
                    paramMap.put("user", entity.getUser());
                    sendUrl = sendUrl + "/client/getapprecord.vpage";
                    break;
                case APP_DOWNLOAD:
                    paramMap.put("productId", entity.getProductId());
                    paramMap.put("region", entity.getRegion());
                    sendUrl = sendUrl + "/client/getappdownload.vpage";
                    break;
                case APP_RESOURCE:
                    paramMap.put("productId", entity.getProductId());
                    paramMap.put("apkVer", entity.getApkVer());
                    paramMap.put("sysVer", entity.getSysVer());
                    paramMap.put("user", entity.getUser());
                    paramMap.put("model", entity.getModel());
                    sendUrl = sendUrl + "/client/getappresource.vpage";
                    break;
                case DYNAMIC_APP_RESOURCE:
                    paramMap.put("productId", entity.getProductId());
                    paramMap.put("apkVer", entity.getApkVer());
                    paramMap.put("sysVer", entity.getSysVer());
                    paramMap.put("user", entity.getUser());
                    paramMap.put("model", entity.getModel());
                    sendUrl = sendUrl + "/client/getdynamicresource.vpage";
                    break;
            }
            String URL = UrlUtils.buildUrlQuery(sendUrl, paramMap);
            String response = HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString();
            return MapMessage.successMessage("操作成功").add("content", response);
        } catch (Exception e) {
            return MapMessage.errorMessage("操作失败");
        }

    }

    @RequestMapping(value = "addconfig.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage add(@RequestBody Map entity) {

        ClientAppConfigCtl clientAppConfigCtl = new ClientAppConfigCtl();
        clientAppConfigCtl.setType(SafeConverter.toString(entity.get("type"), ""));
        clientAppConfigCtl.setProductId(SafeConverter.toString(entity.get("productId"), ""));
        clientAppConfigCtl.setApkVer(SafeConverter.toString(entity.get("apkVer"), ""));
        clientAppConfigCtl.setSysVer(SafeConverter.toString(entity.get("sysVer"), ""));
        clientAppConfigCtl.setModel(SafeConverter.toString(entity.get("model"), ""));
        clientAppConfigCtl.setKtwelve(SafeConverter.toString(entity.get("ktwelve"), ""));
        clientAppConfigCtl.setRegion(SafeConverter.toString(entity.get("region"), ""));
        clientAppConfigCtl.setUser(SafeConverter.toString(entity.get("user"), ""));

        if (ClientAppConfigType.JSPATCH.name().equals(SafeConverter.toString(entity.get("type"), ""))) {
            clientAppConfigCtl.setIsOpen(SafeConverter.toBoolean(entity.get("isOpen")));
            clientAppConfigCtl.setComment(SafeConverter.toString(entity.get("comment"), ""));
        } else if (ClientAppConfigType.APP_RESOURCE.name().equals(SafeConverter.toString(entity.get("type"), ""))) {
            clientAppConfigCtl.setIsOpen(SafeConverter.toBoolean(entity.get("isOpen")));
        } else if (ClientAppConfigType.DYNAMIC_APP_RESOURCE.name().equals(SafeConverter.toString(entity.get("type"), ""))) {
            clientAppConfigCtl.setIsOpen(SafeConverter.toBoolean(entity.get("isOpen")));
            clientAppConfigCtl.setComment(SafeConverter.toString(entity.get("comment"), ""));
        }

        ClientAppConfigCtlResponse response = new ClientAppConfigCtlResponse();
        response.setPatchUrl(SafeConverter.toString(entity.get("patchUrl"), ""));
        response.setPatchMD5(SafeConverter.toString(entity.get("patchMD5"), ""));
        response.setRecordMode(SafeConverter.toString(entity.get("recordMode"), ""));
        response.setApkSize(SafeConverter.toString(entity.get("apkSize"), ""));
        response.setApkVer(SafeConverter.toString(entity.get("apkVerNew"), ""));

        if (ClientAppConfigType.RECORD.name().equals(SafeConverter.toString(entity.get("type"), ""))) {
            response.setYzs(SafeConverter.toString(entity.get("yzs"), ""));
        }

        clientAppConfigCtl.setResponse(response);

        try {
            if (entity.get("id") != null) {
                clientAppConfigCtl.setStatus(SafeConverter.toString(entity.get("status")));
                clientAppConfigCtl.setId(entity.get("id").toString());
            } else {
                clientAppConfigCtl.setStatus("draft");
            }
            clientAppConfigCtl = crmConfigService.$upsertClientAppConfigCtl(clientAppConfigCtl);
            //添加管理员操作日志
            addAdminLog("crm添加配置信息-" + getCurrentAdminUser().getAdminUserName(), clientAppConfigCtl.getId());
        } catch (Exception e) {
            return MapMessage.errorMessage("后台异常，请联系管理员");
        }
        return MapMessage.successMessage("操作成功");

    }

    private List<ClientAppConfigCtl> sortList(List<ClientAppConfigCtl> list) {
        return list.stream()
                .sorted((o1, o2) -> {
                    long c1 = SafeConverter.toLong(o1.getCreateDateTime());
                    long c2 = SafeConverter.toLong(o2.getCreateDateTime());
                    return Long.compare(c2, c1);
                })
                .collect(Collectors.toList());
    }

    private String generateTemplate(String key, String type) {
        String $index = StringUtils.join(key, type);
        return templateFactory.containsKey($index) ? templateFactory.get($index) : defaultTemplate;
    }
}
