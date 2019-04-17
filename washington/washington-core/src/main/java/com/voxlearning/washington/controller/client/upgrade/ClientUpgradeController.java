package com.voxlearning.washington.controller.client.upgrade;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.core.upgrade.ApkInfo;
import com.voxlearning.utopia.core.upgrade.ApkUpgradeProvider;
import com.voxlearning.utopia.core.upgrade.UpgradeCond;
import com.voxlearning.utopia.service.config.api.entity.ClientAppUpgradeCtl;
import com.voxlearning.utopia.service.config.api.entity.ClientAppUpgradeCtlResponse;
import com.voxlearning.utopia.service.config.client.ClientApplicationConfigServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 各种客户端升级用方法
 * Created by alex on 2018/11/26.
 */
@Controller
@RequestMapping("/client/upgrade")
public class ClientUpgradeController extends AbstractUpgradeController {

    @Inject
    private ClientApplicationConfigServiceClient cacsClient;

    @RequestMapping(value = "check.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage appUpgradeCheck() {

        MapMessage resultMap = new MapMessage();

        try {
            UpgradeParam param = getRequestParam();

            appendHwListIfne(param, resultMap);

            List<Map<String, Object>> upgradeList = new ArrayList<>();

            List<ClientAppUpgradeCtl> ctls = getUpgradeConfigs(param);

            if (CollectionUtils.isEmpty(ctls)) {
                resultMap.set("upgrade", false);
            } else {
                resultMap.set("upgrade", true);

                for (ClientAppUpgradeCtl ctl : ctls) {
                    Map<String, Object> item = new HashMap<>();

                    ClientAppUpgradeCtlResponse response = ctl.getResponse();

                    // 这些是iOS和android升级都需要的参数
                    item.put("productId", response.getProductId());
                    item.put("apkVer", response.getApkVer());
                    item.put("upgradeType", response.getUpgradeType());
                    item.put("description", response.getDescription());
                    item.put("updateTime", response.getUpdateTime());
                    item.put("packageName", response.getPackageName());
                    item.put("scheme", response.getScheme());
                    item.put("image", response.getImage());

                    // 如果admin配置了response则不取config里面的
                    if (StringUtils.isNotBlank(response.getApkUrl())) {
                        // 整包参数
                        item.put("apkSize", response.getApkSize());
                        item.put("apkUrl", response.getApkUrl());
                        item.put("apkMD5", response.getApkMD5().toUpperCase());
                    } else if (ANDROID_PRODUCT_ID.contains(param.getProductId()) || U3D_PRODUCT_ID.contains(param.getProductId())) {
                        //android才调差分升级接口
                        UpgradeCond cond = new UpgradeCond();
                        cond.setProductId(param.getProductId());
                        cond.setChannel(param.getChannel());
                        cond.setVersionTo(response.getApkVer());
                        cond.setClientApkMD5(param.getApkMD5());
                        ApkInfo apkInfo = ApkUpgradeProvider.getApkUpgradeInfo(cond);
                        //这下面的是android升级需要的下载地址
                        //差分包参数
                        if (apkInfo != null) {
                            if (apkInfo.isPatch()) {
                                item.put("patchSize", apkInfo.getSize());
                                item.put("patchUrl", apkInfo.getUrl());
                                item.put("patchMD5", apkInfo.getMd5().toUpperCase());
                            }
                            //整包参数
                            item.put("apkSize", response.getApkSize());
                            item.put("apkUrl", apkInfo.getCompleteApkInfo().getUrl());
                            item.put("apkMD5", apkInfo.getCompleteApkInfo().getMd5().toUpperCase());
                        } else {
                            // android命中了升级。但是没有升级包
                            com.voxlearning.alps.spi.bootstrap.LogCollector.info("client_app_upgrade_logs",
                                    MapUtils.map(
                                            REQ_PRODUCT_ID, param.getProductId(),
                                            REQ_APK_VERSION, param.getApkVer(),
                                            "target_version", response.getApkVer(),
                                            "client_md5", param.getApkMD5(),
                                            REQ_CHANNEL, param.getChannel(),
                                            REQ_USER, param.getUser(),
                                            "op", "no_patch",
                                            "env", RuntimeMode.current().name(),
                                            "upgrade", "false",
                                            "upgradeType", response.getUpgradeType(),
                                            "appClientIp", getWebRequestContext().getRealRemoteAddr()
                                    ));
                        }
                    }

                    upgradeList.add(item);
                }
            }

            resultMap.set("upgradeList", upgradeList);

            resultMap.set("result", "success");

        } catch (IllegalArgumentException e) {
            logger.error("client app upgrade check error.", e);

            resultMap.set("result", "400");
            resultMap.set("message", "请求参数错误!");
        } catch (Exception e) {
            logger.error("client app upgrade check error.", e);

            resultMap.set("result", "500");
            resultMap.set("message", "系统错误!");
        }

        return resultMap;
    }

    private void appendHwListIfne(UpgradeParam param, MapMessage resultMap) {
        String reqPid = param.getProductId();

        if (PRODUCT_ID_DEF_STUDENT.contains(reqPid) || PRODUCT_ID_DEF_PARENT.contains(reqPid)) {
            resultMap.add("closeHardwareAccelerationList", closeHardwareAccelerationList(reqPid));
            resultMap.add("closeCrosswalkList", getCloseCrossWalkList(reqPid));
            resultMap.add("wonderCloseCrosswalkList", internalGetCloseCrossWalkList("new_wonder_crosswalk_close"));
        }

        if (PRODUCT_ID_DEF_TEACHER.contains(reqPid)) {
            resultMap.add("closeHardwareAccelerationList", closeHardwareAccelerationList(reqPid));
            resultMap.add("closeCrosswalkList", getCloseCrossWalkList(reqPid));
        }
    }

    // 判断是否是合法的请求
    private UpgradeParam getRequestParam() {

        // 有一些非法请求，imei巨长无比，忽略
        String reqImei = getRequestString(REQ_IMEI);
        if (reqImei.length() > 100) {
            throw new IllegalArgumentException("req imei is too long");
        }

        // 产品ID和产品名总得有一个
        String reqProductId = getRequestString(REQ_PRODUCT_ID);
        String reqPlugins = getRequestString(REQ_PLUGINS);
        Map<String, String> plugins = JsonUtils.fromJsonToMapStringString(reqPlugins);
        if (StringUtils.isBlank(reqProductId)) {
            throw new IllegalArgumentException("product id is empty");
        }

        UpgradeParam reqParam = new UpgradeParam();

        reqParam.setProductId(reqProductId);
        reqParam.setProductName(getRequestString(REQ_PRODUCT_NAME));

        reqParam.setApkVer(getRequestString(REQ_APK_VERSION));
        reqParam.setSysVer(getRequestString(REQ_SYS_VERSION));

        reqParam.setChannel(getRequestString(REQ_CHANNEL));
        reqParam.setRegion(getRequestString(REQ_REGION_CODE));
        reqParam.setKtwelve(getRequestString(REQ_KTWELVE));

        reqParam.setSchool(getRequestString(REQ_SCHOOL));
        reqParam.setClazz(getRequestString(REQ_CLAZZ));

        reqParam.setSubject(getRequestString(REQ_SUBJECT));
        reqParam.setClazzLevel(getRequestString(REQ_CLAZZ_LEVEL));

        reqParam.setUser(getRequestString(REQ_USER));
        reqParam.setUserType(getRequestParameter(REQ_USER_TYPE, "2"));

        reqParam.setImei(getRequestString(REQ_IMEI));
        reqParam.setBrand(getRequestString(REQ_BRAND));
        reqParam.setModel(getRequestString(REQ_MODEL));

        reqParam.setTest(getRequestString(REQ_TEST));
        reqParam.setIsAuto(getRequestBool(REQ_IS_AUTO, true));
        reqParam.setApkMD5(getRequestString(REQ_MD5));

        // Android插件用
        reqParam.setPluginStr(reqPlugins);
        reqParam.setPlugins(plugins);

        List<String> reqPids = new ArrayList<>();
        Map<String, String> reqIdVers = new HashMap<>();

        // FIXME 这里逻辑有个改动，插件需要判断宿主APP版本，所以请求参数里面 productId, apkVer 都是宿主APP的信息，
        // FIXME 插件的时候请求的product参数以plugins优先
        if (MapUtils.isNotEmpty(plugins)) {
            for (String pname : plugins.keySet()) {
                reqPids.add(PRODUCT_NAME_DEF.get(pname));
                reqIdVers.put(PRODUCT_NAME_DEF.get(pname), plugins.get(pname));
            }
        } else {
            reqPids.add(reqProductId);
        }

        reqParam.setReqPids(reqPids);
        reqParam.setIdVers(reqIdVers);

        return reqParam;
    }

    // 根据请求参数找到对应的升级配置信息
    private List<ClientAppUpgradeCtl> getUpgradeConfigs(UpgradeParam param) {

        List<ClientAppUpgradeCtl> ctls = cacsClient.getClientAppUpgradeCtlBuffer().dump().getClientAppUpgradeCtls();

        //处理一下没有选择普通升级和强制升级的数据
        //前端没有选的都认为是普通升级
        ctls.stream().filter(p -> StringUtils.isBlank(p.getResponse().getUpgradeType())).forEach(p -> p.getResponse().setUpgradeType("1"));

        // 首先根据产品ID或者产品名进行过滤
        // 大部分都是产品ID，只有Android的插件升级是通过产品名，这种情况reqProductId是空，reqProductName有值
        List<String> reqPids = param.getReqPids();

        List<ClientAppUpgradeCtl> retCtls = new ArrayList<>();

        // 根据productId进行循环
        for (String reqPid : reqPids) {

            // 过滤处理，productid一样，并且是published状态
            List<ClientAppUpgradeCtl> productCtls = ctls.stream().filter(source -> {
                        boolean isSameProduct = StringUtils.equals(reqPid, source.getProductId());
                        if (isSameProduct && StringUtils.isBlank(param.getTest())) {
                            return "published".equals(source.getStatus());
                        } else {
                            return isSameProduct;
                        }
                    }
            ).collect(Collectors.toList());

            //自动升级请求：过滤手动升级的配置
            //手动升级：不做处理。直接拿全部配置
            //没有此参数视为自动升级
            if (param.getIsAuto()) {
                productCtls = productCtls.stream().filter(p -> p.getIsManual() == null || Boolean.FALSE == p.getIsManual()).collect(Collectors.toList());
            }

            //强制升级优先
            Comparator<ClientAppUpgradeCtl> c = Comparator.comparingInt(o -> SafeConverter.toInt(o.getResponse().getUpgradeType()));
            //再按rank排
            c = c.thenComparing((o1, o2) -> Integer.compare(SafeConverter.toInt(o2.getRank()), SafeConverter.toInt(o1.getRank())));
            productCtls = productCtls.stream().sorted(c).collect(Collectors.toList());

            ClientAppUpgradeCtl item;
            if (PRODUCT_ID_DEF_PARENT.contains(reqPid)) {
                item = getUpdateConfigForParentApp(reqPid, productCtls, param);
            } else {
                item = getUpdateConfigForTeacherOrStudentApp(reqPid, productCtls, param);
            }

            if (item != null) {
                retCtls.add(item);
            }
        }

        return retCtls;
    }

    public static void main(String[] args) {
        Map<String, String> param = new HashMap<>();
        param.put("dubing", "1.0.0.0");
        param.put("scaling", "1.0.0.0");
        System.out.println(JsonUtils.toJson(param));

        Map<String, String> httpPamam = new HashMap<>();
        httpPamam.put("plugins", JsonUtils.toJson(param));
        System.out.println(UrlUtils.buildUrlQuery("http://10.200.8.209:8081/client/upgrade/check.vpage", httpPamam));
    }
}
