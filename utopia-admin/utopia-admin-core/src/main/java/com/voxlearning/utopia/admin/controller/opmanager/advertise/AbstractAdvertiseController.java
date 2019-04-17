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

package com.voxlearning.utopia.admin.controller.opmanager.advertise;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.admin.controller.opmanager.OpManagerAbstractController;
import com.voxlearning.utopia.service.config.api.CRMConfigService;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementDetail;
import com.voxlearning.utopia.service.config.api.entity.AdvertisementTarget;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;

/**
 * @author Longlong Yu
 * @since 下午6:05,13-11-22.
 */
@Controller
@RequestMapping("/opmanager/advertisement")
public abstract class AbstractAdvertiseController extends OpManagerAbstractController {

    static final String ADVERTISE_AUDITOR_CATEGORY = PRIMARY_PLATFORM_GENERAL.getType();
    static final String ADVERTISE_AUDITOR_LV1 = "ADVERTISE_AUDITOR_LV1";
    static final String ADVERTISE_AUDITOR_LV2 = "ADVERTISE_AUDITOR_LV2";
    static final String ADVERTISE_AUDITOR_LV3 = "ADVERTISE_AUDITOR_LV3";
    static final String ADVERTISE_MAIL_CC = "ADVERTISE_MAIL_CC";

    static final String MAIL_SUFFIX = "@17zuoye.com;";
    static final String TRACE_AD_LOG_QUERY = "WHERE `OPERATION`='AD_TRACE' AND `TARGET_ID`=? ORDER BY `CREATE_DATETIME` DESC";

    static final int PRIVILEGE_LV1 = 1;
    static final int PRIVILEGE_LV2 = 2;
    static final int PRIVILEGE_LV3 = 3;
    static final int ORDINARY = 0;

    protected static final Map<String, String> AUDITOR_SLOT = new HashMap<>();
    static {
        AUDITOR_SLOT.put("zhilong.hu",    "\\d+");   // 可以审核所有广告位广告
        AUDITOR_SLOT.put("wenlong.meng",  "\\d+");   // 可以审核所有广告位广告
        AUDITOR_SLOT.put("shuai.huan",    "\\d+");   // 可以审核所有广告位广告
        AUDITOR_SLOT.put("caijuan.gao",   "\\d+");   // 可以审核所有广告位广告
        AUDITOR_SLOT.put("chunlong.shen", "\\d+");   // 可以审核所有广告位广告
        AUDITOR_SLOT.put("yizhou.zhang",  "\\d+");   // 可以审核所有广告位广告
        AUDITOR_SLOT.put("xiupeng.li",    "\\d+");   // 可以审核所有广告位广告

        AUDITOR_SLOT.put("fang.zhang",    "[13]\\d+");   // 学生、老师广告位
        AUDITOR_SLOT.put("jiamin.lin",    "[13]\\d+");   // 学生、老师广告位
        AUDITOR_SLOT.put("ruiying.xu",    "2\\d+");      // 家长广告位

        AUDITOR_SLOT.put("dan.wu",        "[123][45]\\d+");  // 中学广告位
        AUDITOR_SLOT.put("hang.li",       "[123][45]\\d+");  // 中学广告位
        AUDITOR_SLOT.put("bo.li",         "[123][45]\\d+");  // 中学广告位
        AUDITOR_SLOT.put("hantao.liu",    "[123][45]\\d+");  // 中学广告位
        AUDITOR_SLOT.put("yan.dai",       "[123][45]\\d+");  // 中学广告位
        AUDITOR_SLOT.put("hongyang.liu",  "[123][45]\\d+");  // 中学广告位
        
        AUDITOR_SLOT.put("chen.qi",       "3[123]\\d+");     // 小学学生广告位
        AUDITOR_SLOT.put("boliang.li",    "3[123]\\d+");     // 小学学生广告位
        AUDITOR_SLOT.put("jun.wang.a",    "3[123]\\d+");     // 小学学生广告位

        AUDITOR_SLOT.put("zhengang.cai",  "[2][123]\\d+");   // 小学学生家长广告位
        AUDITOR_SLOT.put("shan.wang.a",   "[2][123]\\d+");   // 小学学生家长广告位
        AUDITOR_SLOT.put("xiya.wang",     "[2][123]\\d+");   // 小学学生家长广告位
        AUDITOR_SLOT.put("xincheng.bi",   "[13][123]\\d+");  // 小学学生老师广告位
        AUDITOR_SLOT.put("xu.yan",        "1[123]\\d+");   // 小学老师广告位
        AUDITOR_SLOT.put("xiaoqing.chang",   "9[123]\\d+");   // 天玑广告位
        AUDITOR_SLOT.put("dongwei.xiao",     "9[123]\\d+");   // 天玑广告位
        AUDITOR_SLOT.put("lina.qian",        "9[123]\\d+");   // 天玑广告位
        // 测试用权限
        AUDITOR_SLOT.put("shuilian.yu",    "110102|120111|120122|120121");
        AUDITOR_SLOT.put("min.yang",    "120201|3[1-5][0-9]{4}");
        AUDITOR_SLOT.put("dongwei.xiao",    "[9][9][0-9]{4}");
    }

    // FIXME 因为CrmUploader的缘故，只能支持到jpg格式
    static List<String> IMG_SUFFIX = Arrays.asList("bmp", "gif", "jpeg", "jpg", "png"); // 此处维护上传图片的后缀
    static final String DEFAULT_LINE_SEPARATOR = "\n";

    @ImportService(interfaceClass = CRMConfigService.class)
    protected CRMConfigService crmConfigService;

    /**
     * 统一记录操作的日志的格式，便于之后查询
     */
    void saveOperationLog(Long targetId, String operation, String comment) {
        addAdminLog("AD_TRACE", targetId, operation, comment, null);
    }

    int getUserPrivilegeLevel() {
        try {
            String userName = getCurrentAdminUser().getAdminUserName();
            Map<String, String> configValues = new LinkedHashMap<>();
            crmConfigService.$loadCommonConfigs().stream()
                    .filter(e -> !SafeConverter.toBoolean(e.getDisabled()))
                    .filter(e -> ADVERTISE_AUDITOR_CATEGORY.equals(e.getCategoryName()))
                    .sorted((o1, o2) -> {
                        long u1 = SafeConverter.toLong(o1.getUpdateDatetime());
                        long u2 = SafeConverter.toLong(o2.getUpdateDatetime());
                        return Long.compare(u2, u1);
                    })
                    .forEach(e -> configValues.put(e.getConfigKeyName(), e.getConfigKeyValue()));

            if (configValues.containsKey(ADVERTISE_AUDITOR_LV3) && configValues.get(ADVERTISE_AUDITOR_LV3).contains(userName)) {
                return PRIVILEGE_LV3;
            } else if (configValues.containsKey(ADVERTISE_AUDITOR_LV2) && configValues.get(ADVERTISE_AUDITOR_LV2).contains(userName)) {
                return PRIVILEGE_LV2;
            } else if (configValues.containsKey(ADVERTISE_AUDITOR_LV1) && configValues.get(ADVERTISE_AUDITOR_LV1).contains(userName)) {
                return PRIVILEGE_LV1;
            } if (RuntimeMode.isTest()) {
                return PRIVILEGE_LV3;
            }  else {
                return ORDINARY;
            }
        } catch (Exception ex) {
            logger.error("can not find config :{}", ex);
            return ORDINARY;
        }
    }

    boolean isAdmin() {
        //return getUserPrivilegeLevel() > ORDINARY;
        return AUDITOR_SLOT.containsKey(getCurrentAdminUser().getAdminUserName());
    }

    /**
     * 管理员 或者 广告创建人本身才可以访问
     */
    boolean checkUserPrivilege(String adCreator, AdvertisementDetail adDetail) {
        String userName = getCurrentAdminUser().getAdminUserName();
        if (StringUtils.equals(userName, adCreator)) {
            return true;
        }

        if (adDetail != null) {
            String slotRegex = AUDITOR_SLOT.get(userName);
            if (StringUtils.isNoneBlank(slotRegex) && StringUtils.isNoneBlank(adDetail.getAdSlotId())) {
                return adDetail.getAdSlotId().matches(slotRegex);
            }
        }

        return false;
    }

    @RequestMapping(value = "privilege.vpage", method = RequestMethod.GET)
    @ResponseBody
    public boolean checkPrivilege() {
        return checkUserPrivilege(getRequestString("creator"), null);
    }

    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    public Map<String, String> getAuditorSlotConfig() {
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            return AUDITOR_SLOT;
        } else {
            Map<String, String> slotConfig = new HashMap<>();
            slotConfig.putAll(AUDITOR_SLOT);

            String configValues = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "ADVERTISE_AUDITOR_LV3");

            String[] users = configValues.trim().split(",");

            for(int a = 0;a<users.length;a++){
                slotConfig.put(users[a], "\\d+");
            }

            if (RuntimeMode.isTest()){
//                slotConfig.put("^[a-zA-Z0-9_.-]{4,16}$", "\\d+");
                slotConfig.put(getCurrentAdminUser().getAdminUserName(), "\\d+");
            }
            return slotConfig;
        }

    }
}
