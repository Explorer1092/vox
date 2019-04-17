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
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.util.AdminOssManageUtils;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.entity.ClientAppUpgradeCtl;
import com.voxlearning.utopia.service.config.api.entity.ClientAppUpgradeCtlResponse;
import com.voxlearning.utopia.service.config.client.ClientApplicationConfigServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.voxlearning.utopia.service.crm.api.constants.agent.CrmClientAppUpdType;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 客户端版本升级控制
 * Created by fanshuo.iu on 2015/5/7.
 */
@Controller
@Slf4j
@RequestMapping(value = "/site/clientappverion")
public class SiteClientAppUpgradeCtlController extends SiteAbstractController {

    @ImportService(interfaceClass = CRMConfigService.class)
    private CRMConfigService crmConfigService;

    private static List<String> imageSuffix = Arrays.asList("bmp", "gif", "jpeg", "jpg", "png"); // 此处维护上传图片的后缀

    @Inject private ClientApplicationConfigServiceClient clientApplicationConfigServiceClient;

    @RequestMapping(value = "list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String list(Model model) {
        List<ClientAppUpgradeCtl> clientAppUpgradeCtls = clientApplicationConfigServiceClient.getClientApplicationConfigService()
                .loadAllClientAppUpgradeCtlsFromDB()
                .getUninterruptibly()
                .stream()
                .sorted((o1, o2) -> {
                    int r1 = SafeConverter.toInt(o1.getRank());
                    int r2 = SafeConverter.toInt(o2.getRank());
                    return Integer.compare(r2, r1);
                })
                .collect(Collectors.toList());
        model.addAttribute("vers", clientAppUpgradeCtls);


        model.addAttribute("enum",CrmClientAppUpdType .values());
        return "site/clientapp/list";
    }

    @RequestMapping(value = "toeditpage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String edit(Model model) {
        String id = getRequestString("id");
        if (!StringUtils.isBlank(id)) {
            ClientAppUpgradeCtl inst = clientApplicationConfigServiceClient.getClientApplicationConfigService()
                    .loadAllClientAppUpgradeCtlsFromDB()
                    .getUninterruptibly()
                    .stream()
                    .filter(e -> StringUtils.equals(e.getId(), id))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("ver", inst);
        }
        return "site/clientapp/edit";
    }

    @RequestMapping(value = "totestpage.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String totest() {
        return "site/clientapp/test";
    }

    @RequestMapping(value = "toremove.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String remove(Model model) {
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)) {
            crmConfigService.$removeClientAppUpgradeCtl(id);
        }
        model.addAttribute("vers", clientApplicationConfigServiceClient.getClientApplicationConfigService()
                .loadAllClientAppUpgradeCtlsFromDB()
                .getUninterruptibly());
        model.addAttribute("enum",CrmClientAppUpdType .values());
        return "site/clientapp/list";
    }

    @RequestMapping(value = "topublish.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String publish(Model model) {
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)) {
            ClientAppUpgradeCtl document = new ClientAppUpgradeCtl();
            document.setId(id);
            document.setStatus("published");
            crmConfigService.$updateClientAppUpgradeCtl(document);
        }

        model.addAttribute("vers", clientApplicationConfigServiceClient.getClientApplicationConfigService()
                .loadAllClientAppUpgradeCtlsFromDB()
                .getUninterruptibly());
        model.addAttribute("enum",CrmClientAppUpdType .values());
        return "site/clientapp/list";
    }

    @RequestMapping(value = "todraft.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String draft(Model model) {
        String id = getRequestString("id");
        if (StringUtils.isNotBlank(id)) {
            ClientAppUpgradeCtl document = new ClientAppUpgradeCtl();
            document.setId(id);
            document.setStatus("draft");
            crmConfigService.$updateClientAppUpgradeCtl(document);
        }

        model.addAttribute("vers", clientApplicationConfigServiceClient.getClientApplicationConfigService()
                .loadAllClientAppUpgradeCtlsFromDB()
                .getUninterruptibly());
        model.addAttribute("enum",CrmClientAppUpdType .values());
        return "site/clientapp/list";
    }

    @RequestMapping(value = "test.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage test(@RequestBody ClientAppUpgradeCtl entity) {
        try {
            // 根据输入参数构造请求的URL
            String sendUrl = "http://www.test.17zuoye.net";
//            String sendUrl = "http://localhost:8081";
            if (RuntimeMode.isStaging()) {
                sendUrl = "http://www.staging.17zuoye.net";
            } else if (RuntimeMode.isProduction()) {
                sendUrl = "http://www.17zuoye.com";
            }

            StringBuilder urlBuilder = new StringBuilder(sendUrl);
            urlBuilder.append("/client/app3/upgrade.vpage?apkVer=").append(entity.getApkVer());
            urlBuilder.append("&androidVerCode=").append(entity.getAndroidVerCode());
            urlBuilder.append("&channel=").append(entity.getChannel());
            urlBuilder.append("&sdkVer=").append(entity.getSdkVer());
            urlBuilder.append("&sysVer=").append(entity.getSysVer());
            urlBuilder.append("&region=").append(entity.getRegion());
            urlBuilder.append("&school=").append(entity.getSchool());
            urlBuilder.append("&clazz=").append(entity.getClazz());
            urlBuilder.append("&productId=").append(entity.getProductId());
            urlBuilder.append("&clazzLevel=").append(entity.getClazzLevel());
            urlBuilder.append("&user=").append(entity.getUser());
            urlBuilder.append("&userType=").append(entity.getUserType());
            urlBuilder.append("&imei=").append(entity.getImei());
            urlBuilder.append("&brand=").append(entity.getBrand());
            urlBuilder.append("&model=").append(entity.getModel());
            urlBuilder.append("&mobile=").append(entity.getMobile());
            urlBuilder.append("&productName=").append(entity.getProductName());
            urlBuilder.append("&apkName=").append(entity.getApkName());
            urlBuilder.append("&test=true");

            String r = HttpRequestExecutor.defaultInstance()
                    .get(urlBuilder.toString())
                    .execute().getResponseString();
            String content = JsonUtils.toJsonPretty(JsonUtils.fromJson(r));
            return MapMessage.successMessage("操作成功").add("content", content);

        } catch (Exception ignore) {
            return MapMessage.errorMessage("后台异常，请联系管理员");
        }
    }

    @RequestMapping(value = "addver.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage add(@RequestBody Map entity) {
        ClientAppUpgradeCtl clientAppUpgradeCtl = new ClientAppUpgradeCtl();
        clientAppUpgradeCtl.setProductId(entity.get("productId").toString());
        clientAppUpgradeCtl.setApkVer(entity.get("apkVerOld").toString());
        // clientAppUpgradeCtl.setAndroidVerCode(entity.get("androidVerCode").toString());
        clientAppUpgradeCtl.setChannel(entity.get("channel").toString());
        clientAppUpgradeCtl.setSdkVer(entity.get("sdkVer").toString());
        clientAppUpgradeCtl.setSysVer(entity.get("sysVer").toString());
        clientAppUpgradeCtl.setBrand(entity.get("brand").toString());
        // clientAppUpgradeCtl.setMobile(entity.get("mobile").toString());
        clientAppUpgradeCtl.setModel(entity.get("model").toString());
        clientAppUpgradeCtl.setRegion(entity.get("region").toString());
        clientAppUpgradeCtl.setImei(entity.get("imei").toString());
        clientAppUpgradeCtl.setTime(entity.get("time").toString());
        clientAppUpgradeCtl.setClazz(entity.get("clazz").toString());
        clientAppUpgradeCtl.setClazzLevel(entity.get("clazzLevel").toString());
        clientAppUpgradeCtl.setAccountStatus(SafeConverter.toString(entity.get("accountStatus"), ""));
        clientAppUpgradeCtl.setKtwelve(SafeConverter.toString(entity.get("ktwelve"), ""));
        clientAppUpgradeCtl.setSchool(entity.get("school").toString());
        clientAppUpgradeCtl.setUser(entity.get("user").toString());
        clientAppUpgradeCtl.setUserType(entity.get("userType").toString());
        //clientAppUpgradeCtl.setRate(entity.get("rate").toString());
        //clientAppUpgradeCtl.setCount(entity.get("count").toString());
        clientAppUpgradeCtl.setOwnerAppPid(SafeConverter.toString(entity.get("ownerAppPid")));
        clientAppUpgradeCtl.setOwnerAppApkVer(SafeConverter.toString(entity.get("ownerAppApkVer")));

        clientAppUpgradeCtl.setRank(SafeConverter.toInt(entity.get("rank")));
        clientAppUpgradeCtl.setIsManual(SafeConverter.toBoolean(entity.get("isManual")));

        ClientAppUpgradeCtlResponse clientAppUpgradeCtlResponse = new ClientAppUpgradeCtlResponse();
        clientAppUpgradeCtlResponse.setApkVer(entity.get("apkVerNew").toString());
        clientAppUpgradeCtlResponse.setApkSize(entity.get("apkSize").toString());
        clientAppUpgradeCtlResponse.setApkUrl(entity.get("apkUrl").toString().replaceAll("\\s*", ""));
        clientAppUpgradeCtlResponse.setApkMD5(entity.get("apkMD5").toString());
        clientAppUpgradeCtlResponse.setUpgradeType(entity.get("upgradeType").toString());
        clientAppUpgradeCtlResponse.setDescription(entity.get("description").toString());
        clientAppUpgradeCtlResponse.setUpdateTime(entity.get("updateTime").toString());
        clientAppUpgradeCtlResponse.setPackageName(entity.get("packageName").toString());
        clientAppUpgradeCtlResponse.setScheme(entity.get("scheme").toString());
        clientAppUpgradeCtlResponse.setProductId(entity.get("productIdNew").toString());
        clientAppUpgradeCtlResponse.setImage(entity.get("image").toString());
        clientAppUpgradeCtl.setResponse(clientAppUpgradeCtlResponse);

        String regex = "(([=><]|!=|>=|<=)\\d+\\.\\d+\\.\\d+\\.\\d+|\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+\\.\\d+\\.\\d+\\.\\d+)";
        String regexChannl = "(=|!=).+";
        String regexSdk = "(([=><]|!=|>=|<=).+|.+:.+)";
        String regexRegion = "(=|!=)\\d{6}";
        String regexData = "(=|!=)\\d+";
        String regexDate = "((>=|<=)[1-2][0-9][0-9][0-9]-([1][0-2]|0?[1-9])-([12][0-9]|3[01]|0?[1-9]) ([01][0-9]|[2][0-3]):[0-5][0-9])";
        String regexDataSimple = "((>=|<=).+|.+#.+)";

        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getApkVer()) && !clientAppUpgradeCtl.getApkVer().matches(regex)){
            return MapMessage.errorMessage("客户端版本号信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getChannel()) && !clientAppUpgradeCtl.getChannel().matches(regexChannl)){
            return MapMessage.errorMessage("渠道号信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getSdkVer()) && !clientAppUpgradeCtl.getSdkVer().matches(regexSdk)){
            return MapMessage.errorMessage("SDK版本信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getSysVer()) && !clientAppUpgradeCtl.getSysVer().matches(regexSdk)){
            return MapMessage.errorMessage("系统版本信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getBrand()) && !clientAppUpgradeCtl.getBrand().matches(regexChannl)){
            return MapMessage.errorMessage("手机厂商信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getModel()) && !clientAppUpgradeCtl.getModel().matches(regexChannl)){
            return MapMessage.errorMessage("手机型号信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getRegion()) && !clientAppUpgradeCtl.getRegion().matches(regexRegion)){
            return MapMessage.errorMessage("区域编码信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getTime()) && !clientAppUpgradeCtl.getTime().matches(regexDataSimple)){
            return MapMessage.errorMessage("时间段信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getSchool()) && !clientAppUpgradeCtl.getSchool().matches(regexData)){
            return MapMessage.errorMessage("学校ID信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getClazz()) && !clientAppUpgradeCtl.getClazz().matches(regexData)){
            return MapMessage.errorMessage("班级ID信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getClazzLevel()) && !clientAppUpgradeCtl.getClazzLevel().matches(regexData)){
            return MapMessage.errorMessage("年级信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getUser()) && !clientAppUpgradeCtl.getUser().matches(regexData)){
            return MapMessage.errorMessage("用户ID信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getImei()) && !clientAppUpgradeCtl.getImei().matches(regexChannl)){
            return MapMessage.errorMessage("手机IMEI号码信息不对");
        }
        if (StringUtils.isNoneBlank(clientAppUpgradeCtl.getOwnerAppApkVer()) && !clientAppUpgradeCtl.getOwnerAppApkVer().matches(regex)){
            return MapMessage.errorMessage("宿主AP版本信息不对");
        }

        // android 插件配置必须要要有宿主app配置
        if (clientAppUpgradeCtl.getProductId().matches("1007\\d+")) {
            if (StringUtils.isBlank(clientAppUpgradeCtl.getOwnerAppPid()) || StringUtils.isBlank(clientAppUpgradeCtl.getOwnerAppApkVer())) {
                return MapMessage.errorMessage("安卓插件配置必须指定宿主app信息");
            }

            if (!clientAppUpgradeCtl.getOwnerAppApkVer().matches(".*\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                return MapMessage.errorMessage("宿主app版本信息不对");
            }
        }

        try {
            if (entity.get("id") != null) {
                clientAppUpgradeCtl.setStatus(entity.get("status").toString());
                clientAppUpgradeCtl.setId(entity.get("id").toString());
            } else {
                clientAppUpgradeCtl.setStatus("draft");
            }
            crmConfigService.$upsertClientAppUpgradeCtl(clientAppUpgradeCtl);
        } catch (Exception ignore) {
            return MapMessage.errorMessage("后台异常，请联系管理员");
        }
        return MapMessage.successMessage("操作成功");
    }

    @RequestMapping(value = "uploadimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage uploadImage(MultipartFile file) {
        try {
            MapMessage validMsg = validateImage(file);
            if (!validMsg.isSuccess()) {
                return validMsg;
            }
            String fileName = AdminOssManageUtils.upload(file, "upgrade");
            if (StringUtils.isBlank(fileName)) {
                return MapMessage.errorMessage("图片保存失败！");
            }
            return MapMessage.successMessage().add("fileName", fileName);
        } catch (Exception ex) {
            logger.error("Failed to upload img, ex={}", ex);
            return MapMessage.errorMessage("上传图片失败：" + ex.getMessage());
        }
    }

    @RequestMapping(value = "clearimage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage clearImage() {
        String verId = getRequestString("verId");
        if (StringUtils.isBlank(verId)) {
            return MapMessage.errorMessage("无效的参数！");
        }
        try {
            ClientAppUpgradeCtl document = new ClientAppUpgradeCtl();
            document.setId(verId);
            document.getResponse().setImage("");
            crmConfigService.$updateClientAppUpgradeCtl(document);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed to clear img, ex={}", ex);
            return MapMessage.errorMessage("清除图片失败：" + ex.getMessage());
        }
    }

    private MapMessage validateImage(MultipartFile file) {
        String ext = StringUtils.substringAfterLast(file.getOriginalFilename(), ".").toLowerCase();
        if (!imageSuffix.contains(ext)) {
            return MapMessage.errorMessage("图片格式不正确");
        } else {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                int height = image.getHeight();
                int width = image.getWidth();
                return new MapMessage().setSuccess(ClientAppUpgradeCtlResponse.matchImageScale(width, height))
                        .setInfo("图片尺寸不正确！");
            } catch (Exception ex) {
                logger.error("Failed validate Img, ex={}", ex);
                return MapMessage.errorMessage("图片校验异常！");
            }
        }
    }

}
