package com.voxlearning.washington.controller.client.upgrade;

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.config.api.entity.ClientAppUpgradeCtl;
import com.voxlearning.utopia.service.region.api.constant.RegionType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.washington.support.AbstractController;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * client upgrade abstract controller.
 * <p>
 * Created by alex on 2018/11/26.
 */
abstract public class AbstractUpgradeController extends AbstractController {

    // =====================================================================================
    // APP升级检查策略用到的常量定义
    protected static final String REQ_PRODUCT_ID = "productId";
    protected static final String REQ_PRODUCT_NAME = "productName";
    protected static final String REQ_PLUGINS = "plugins";
    protected static final String REQ_APK_NAME = "apkName";
    protected static final String REQ_APK_VERSION = "apkVer";
    protected static final String REQ_ANDROID_VERCODE = "androidVerCode";
    protected static final String REQ_SDK_VERSION = "sdkVer";
    protected static final String REQ_SYS_VERSION = "sysVer";
    protected static final String REQ_CHANNEL = "channel";
    protected static final String REQ_REGION_CODE = "region";
    protected static final String REQ_KTWELVE = "ktwelve";
    protected static final String REQ_SCHOOL = "school";
    protected static final String REQ_CLAZZ = "clazz";
    protected static final String REQ_SUBJECT = "subject";
    protected static final String REQ_CLAZZ_LEVEL = "clazzLevel";
    protected static final String REQ_USER = "user";
    protected static final String REQ_USER_TYPE = "userType";
    protected static final String REQ_IMEI = "imei";
    protected static final String REQ_BRAND = "brand";
    protected static final String REQ_MODEL = "model";
    protected static final String REQ_MOBILE = "mobile";
    protected static final String REQ_TEST = "test";
    protected static final String REQ_IS_AUTO = "isAuto";
    protected static final String REQ_MD5 = "apkMD5";

    // =====================================================================================
    // APP ProductId Name Mapping
    protected static final Map<String, String> PRODUCT_NAME_DEF = new HashMap<>();

    static {
        PRODUCT_NAME_DEF.put("dubing", "100700");     // 学生App Android 配置作业
        PRODUCT_NAME_DEF.put("arithmetic", "100701");     // 学生App Android 速算脑力王
        PRODUCT_NAME_DEF.put("papercalc", "100702");     // 学生App Android 口算拍照
        PRODUCT_NAME_DEF.put("pointread", "100703");     // 学生App Android 点读机
        PRODUCT_NAME_DEF.put("oralcomm", "100704");     // 学生App Android 口语交际
        PRODUCT_NAME_DEF.put("liveroom", "100705");     // 学生App Android 直播间
        PRODUCT_NAME_DEF.put("shutiao", "100706");     // 学生App Android 薯条
    }

    // =====================================================================================
    // APP 家长端APP productId定义
    protected static final Set<String> PRODUCT_ID_DEF_STUDENT = new HashSet<>();

    static {
        PRODUCT_ID_DEF_STUDENT.add("100");   // 小学学生 Android
        PRODUCT_ID_DEF_STUDENT.add("101");   // 小学学生 iOS
        PRODUCT_ID_DEF_STUDENT.add("110");   // 中学学生 Android
        PRODUCT_ID_DEF_STUDENT.add("111");   // 中学学生 iOS
        PRODUCT_ID_DEF_STUDENT.add("700");   // 直播 Android
        PRODUCT_ID_DEF_STUDENT.add("701");   // 直播 iOS
    }

    protected static final Set<String> PRODUCT_ID_DEF_PARENT = new HashSet<>();

    static {
        PRODUCT_ID_DEF_PARENT.add("200");   // 小学家长 Android
        PRODUCT_ID_DEF_PARENT.add("201");   // 小学家长 iOS
        PRODUCT_ID_DEF_PARENT.add("210");   // 中学家长 Android
        PRODUCT_ID_DEF_PARENT.add("211");   // 中学家长 iOS
    }

    protected static final Set<String> PRODUCT_ID_DEF_TEACHER = new HashSet<>();

    static {
        PRODUCT_ID_DEF_TEACHER.add("300");   // 小学老师 Android
        PRODUCT_ID_DEF_TEACHER.add("301");   // 小学老师 iOS
        PRODUCT_ID_DEF_TEACHER.add("310");   // 中学老师 Android
        PRODUCT_ID_DEF_TEACHER.add("311");   // 中学老师 iOS
    }


    protected static final String appProxySecretKey = "17zuoye";
    protected static final Set<String> ANDROID_PRODUCT_ID = new HashSet<>();
    protected static final Set<String> U3D_PRODUCT_ID = new HashSet<>();
    protected static final Set<String> auditUserAccountList;

    static {
        ANDROID_PRODUCT_ID.add("100");
        ANDROID_PRODUCT_ID.add("1000");
        ANDROID_PRODUCT_ID.add("200");
        ANDROID_PRODUCT_ID.add("2000");
        ANDROID_PRODUCT_ID.add("300");
        ANDROID_PRODUCT_ID.add("3000");
        ANDROID_PRODUCT_ID.add("900");
        ANDROID_PRODUCT_ID.add("400");

        U3D_PRODUCT_ID.add("500");
        U3D_PRODUCT_ID.add("501");
        U3D_PRODUCT_ID.add("101501");
        U3D_PRODUCT_ID.add("100501");
        U3D_PRODUCT_ID.add("101502");
        U3D_PRODUCT_ID.add("100502");
        U3D_PRODUCT_ID.add("101503");
        U3D_PRODUCT_ID.add("100503");
        U3D_PRODUCT_ID.add("101504");
        U3D_PRODUCT_ID.add("100504");
        U3D_PRODUCT_ID.add("101505");
        U3D_PRODUCT_ID.add("100505");
        U3D_PRODUCT_ID.add("400");
        U3D_PRODUCT_ID.add("701");
        U3D_PRODUCT_ID.add("110");
        U3D_PRODUCT_ID.add("310");

        // init audit account
        auditUserAccountList = Collections.unmodifiableSet(
                new LinkedHashSet<>(
                        Arrays.asList(
                                "3921029",
                                "12422307",
                                "20001")
                )
        );
    }

    @Inject private RaikouSystem raikouSystem;

    protected ClientAppUpgradeCtl getUpdateConfigForParentApp(String reqPid, List<ClientAppUpgradeCtl> ctls, UpgradeParam param) {
        // FIXME 普通情况 reqProductId VS configProductId, reqApkVersion VS configApkVer
        // FIXME 插件情况 pluginPid VS configProductId, pluginVer VS configAPkVer, reqProductId VS ownerAppPid, reqApkVer VS ownerApkVer
        final String reqProductId, reqApkVersion, ownerAppPid, ownerAppApkVer;
        if (!Objects.equals(reqPid, param.getProductId())) {
            reqProductId = reqPid;
            reqApkVersion = param.getIdVers().get(reqPid);
            ownerAppPid = param.getProductId();
            ownerAppApkVer = param.getApkVer();
        } else {
            reqProductId = param.getProductId();
            reqApkVersion = param.getApkVer();
            ownerAppPid = "";
            ownerAppApkVer = "";
        }

        String reqProductName = param.getProductName();
        String reqSysVersion = param.getSysVer();
        String reqChannel = param.getChannel();
        String reqRegionCode = param.getRegion();
        String reqKtwelve = param.getKtwelve();
        String reqSchool = param.getSchool();
        String reqClazz = param.getClazz();
        String reqClazzLevel = param.getClazzLevel();
        String reqUserType = param.getUserType();
        String reqUser = param.getUser();
        String reqImei = param.getImei();
        String reqBrand = param.getBrand();
        String reqModel = param.getModel();

        Set<StudentDetail> studentDetails = new HashSet<>();
        if (StringUtils.isNotBlank(reqUser) && NumberUtils.isDigits(reqUser)) {
            studentDetails = parentLoaderClient.loadParentStudentRefs(SafeConverter.toLong(reqUser))
                    .stream()
                    .map(StudentParentRef::getStudentId)
                    .collect(Collectors.toSet())
                    .stream()
                    .map(studentLoaderClient::loadStudentDetail)
                    .filter(s -> s != null)
                    .collect(Collectors.toSet());
        }
        final Set<StudentDetail> students = studentDetails;
        //家长这里的判断逻辑会复杂一点
        //1.是否登录||没有孩子 。直接把reqUser扔进去判断，取优先级最高的返回升级
        //2.有孩子的。逐个去判断孩子是否命中升级策略
        ClientAppUpgradeCtl config;
        if (StringUtils.isBlank(reqUser) || CollectionUtils.isEmpty(students)) {
            config = ctls.stream().filter(ctl -> checkUpgradeConfig(
                    ctl,
                    reqProductId,
                    reqProductName,
                    reqApkVersion,
                    reqSysVersion,
                    reqChannel,
                    reqRegionCode,
                    reqSchool,
                    reqClazz,
                    reqClazzLevel,
                    reqUserType,
                    reqUser,
                    reqImei,
                    reqBrand,
                    reqModel,
                    reqKtwelve,
                    "",
                    ownerAppPid,
                    ownerAppApkVer)).findFirst().orElse(null);
        } else {
            config = ctls.stream().filter(ctl -> students.stream().anyMatch(s -> checkUpgradeConfig(
                    ctl,
                    reqProductId,
                    reqProductName,
                    reqApkVersion,
                    reqSysVersion,
                    reqChannel,
                    s.getStudentSchoolRegionCode() != null ? s.getStudentSchoolRegionCode().toString() : null,
                    s.getClazz() != null && s.getClazz().getSchoolId() != null ? s.getClazz().getSchoolId().toString() : null,
                    s.getClazzId() != null ? s.getClazzId().toString() : null,
                    s.getClazz() != null ? s.getClazz().getClassLevel() : null,
                    reqUserType,
                    reqUser,
                    reqImei,
                    reqBrand,
                    reqModel,
                    s.getClazz() != null && s.getClazz().getEduSystem() != null && s.getClazz().getEduSystem().getKtwelve() != null ? s.getClazz().getEduSystem().getKtwelve().name() : null,
                    ctl.getAccountStatus(),
                    ownerAppPid,
                    ownerAppApkVer))).findFirst().orElse(null);
        }
        return config;
    }

    protected ClientAppUpgradeCtl getUpdateConfigForTeacherOrStudentApp(String reqPid, List<ClientAppUpgradeCtl> ctls, UpgradeParam param) {
        // FIXME 普通情况 reqProductId VS configProductId, reqApkVersion VS configApkVer
        // FIXME 插件情况 pluginPid VS configProductId, pluginVer VS configAPkVer, reqProductId VS ownerAppPid, reqApkVer VS ownerApkVer
        final String reqProductId, reqApkVersion, ownerAppPid, ownerAppApkVer;
        if (!Objects.equals(reqPid, param.getProductId())) {
            reqProductId = reqPid;
            reqApkVersion = param.getIdVers().get(reqPid);
            ownerAppPid = param.getProductId();
            ownerAppApkVer = param.getApkVer();
        } else {
            reqProductId = param.getProductId();
            reqApkVersion = param.getApkVer();
            ownerAppPid = "";
            ownerAppApkVer = "";
        }

        String reqProductName = param.getProductName();
        String reqSysVersion = param.getSysVer();
        String reqChannel = param.getChannel();
        String reqRegionCode = param.getRegion();
        String reqKtwelve = param.getKtwelve();
        String reqSchool = param.getSchool();
        String reqClazz = param.getClazz();
        String reqClazzLevel = param.getClazzLevel();
        String reqUserType = param.getUserType();
        String reqUser = param.getUser();
        String reqImei = param.getImei();
        String reqBrand = param.getBrand();
        String reqModel = param.getModel();

        //学生账号异常字段
        String reqAccountStatus = "";
        if (StringUtils.isNoneBlank(reqUser) && reqUserType.equals(String.valueOf(UserType.STUDENT.getType()))) {
            StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(SafeConverter.toLong(reqUser));
            if (studentExtAttribute != null && studentExtAttribute.getAccountStatus() != null) {
                reqAccountStatus = studentExtAttribute.getAccountStatus().name();
            }
        }

        final String reqStatus = reqAccountStatus;
        return ctls.stream().filter(ctl -> checkUpgradeConfig(ctl,
                reqProductId,
                reqProductName,
                reqApkVersion,
                reqSysVersion,
                reqChannel,
                reqRegionCode,
                reqSchool,
                reqClazz,
                reqClazzLevel,
                reqUserType,
                reqUser,
                reqImei,
                reqBrand,
                reqModel,
                reqKtwelve,
                reqStatus,
                ownerAppPid,
                ownerAppApkVer)).findFirst().orElse(null);
    }

    // 比较请求参数里面内容和配置信息里面的条件是否一致，如果一致返回TRUE，否则返回FALSE
    private boolean checkUpgradeConfig(ClientAppUpgradeCtl config,
                                       String reqProductId,
                                       String reqProductName,
                                       String reqApkVersion,
                                       String reqSysVersion,
                                       String reqChannel,
                                       String reqRegionCode,
                                       String reqSchool,
                                       String reqClazz,
                                       String reqClazzLevel,
                                       String reqUserType,
                                       String reqUser,
                                       String reqImei,
                                       String reqBrand,
                                       String reqModel,
                                       String reqKtwelve,
                                       String reqAccountStatus,
                                       String ownerAppPid,
                                       String ownerAppApkVer) {

        // 时间段
        String configTime = config.getTime();
        if (!checkUpgradeTime(configTime, System.currentTimeMillis())) {
            return false;
        }

        // 比较客户端版本号, 注意，Android plugin的情况下，有可能apkVersion是空
        String configApkVersion = config.getApkVer();
        if (!checkVersion(configApkVersion, reqApkVersion)) {
            return false;
        }

        // 比较宿主APP ID和内容
        if (StringUtils.isNoneBlank(ownerAppPid)) {
            if (!Objects.equals(ownerAppPid, config.getOwnerAppPid())) {
                return false;
            }

            if (!checkVersion(config.getOwnerAppApkVer(), ownerAppApkVer)) {
                return false;
            }
        }

        // 渠道
        String configChannel = config.getChannel();
        if (!checkString(configChannel, reqChannel)) {
            return false;
        }

        // 系统版本
        String configSysVer = config.getSysVer();
        if (!checkVersion(configSysVer, reqSysVersion)) {
            return false;
        }

        // 手机厂商
        String configBrand = config.getBrand();
        if (!checkString(configBrand, reqBrand)) {
            return false;
        }

        // 手机型号
        String configModel = config.getModel();
        if (!checkString(configModel, reqModel)) {
            return false;
        }

        // 区编码
        String configRegionCode = config.getRegion();
        if (!checkUpgradeRegion(configRegionCode, reqRegionCode)) {
            return false;
        }

        // 学校
        String configSchool = config.getSchool();
        if (!checkNumber(configSchool, reqSchool)) {
            return false;
        }

        // 年级
        String configClazzLevel = config.getClazzLevel();
        if (!checkNumber(configClazzLevel, reqClazzLevel)) {
            return false;
        }

        // 班级
        String configClazz = config.getClazz();
        if (!checkNumber(configClazz, reqClazz)) {
            return false;
        }

        // 类型
        String configUserType = config.getUserType();
        if (!checkString(configUserType, reqUserType)) {
            return false;
        }

        // 学号
        String configUser = config.getUser();
        if (!checkNumber(configUser, reqUser)) {
            return false;
        }

        // 手机串号
        String configImei = config.getImei();
        if (!checkString(configImei, reqImei)) {
            return false;
        }

        //中小学
        if (!checkKtwelve(config.getKtwelve(), reqKtwelve)) {
            return false;
        }

        //账号异常
        if (!checkAccountStatus(config.getAccountStatus(), reqAccountStatus)) {
            return false;
        }

        return true;
    }

    // 数值型配置比较，支持 =, != > < >= <= :
    private boolean checkNumber(String configValue, String reqValue) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        // 如果请求参数没有，直接False
        if (StringUtils.isBlank(reqValue)) {
            return false;
        }

        long reqIntValue;
        try {
            reqIntValue = Long.parseLong(reqValue.trim());
        } catch (Exception ex) {
            // 不是long，返回false
            return false;
        }

        String[] configVersionList = configValue.trim().split("&");
        boolean checkResult = false;
        for (String configVer : configVersionList) {
            if (StringUtils.isBlank(configVer)) {
                continue;
            }
            if (configVer.startsWith("!=")) {
                long configIntValue = Long.parseLong(configVer.substring(2).trim());
                if (reqIntValue == configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("<=")) {
                long configIntValue = Long.parseLong(configVer.substring(2).trim());
                if (reqIntValue > configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith(">=")) {
                long configIntValue = Long.parseLong(configVer.substring(2).trim());
                if (reqIntValue < configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("=")) {
                long configIntValue = Long.parseLong(configVer.substring(1).trim());
                if (reqIntValue == configIntValue) {
                    checkResult = true;
                    break;
                }
            } else if (configVer.startsWith(">")) {
                long configIntValue = Long.parseLong(configVer.substring(1).trim());
                if (reqIntValue <= configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("<")) {
                long configIntValue = Long.parseLong(configVer.substring(1).trim());
                if (reqIntValue >= configIntValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.indexOf(":") > 0) {
                int index = configVer.indexOf(":");
                long startValue = Long.parseLong(configVer.substring(0, index).trim());
                long endValue = Long.parseLong(configVer.substring(index + 1).trim());
                if (reqIntValue < startValue || reqIntValue > endValue) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else {
                throw new RuntimeException("Unsupported number config:" + configVer);
            }
        }

        return checkResult;
    }

    // 字符串配置比较，支持 =, !=
    private boolean checkString(String configValue, String reqValue) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        // 如果请求参数没有，直接False
        if (StringUtils.isBlank(reqValue)) {
            return false;
        }

        String[] configValueList = configValue.trim().split("&");
        boolean checkResult = false;
        for (String configVal : configValueList) {
            if (StringUtils.isBlank(configVal)) {
                continue;
            }
            if (configVal.startsWith("!=")) {
                String compareValue = configVal.substring(2).trim();
                if (compareValue.equals(reqValue.trim())) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVal.startsWith("=")) {
                String compareValue = configVal.substring(1).trim();
                if (compareValue.equals(reqValue.trim())) {
                    checkResult = true;
                    break;
                }
            }

        }
        return checkResult;
    }

    // 时间段比较，支持 >=A, <=B, A#B, A#B,C 等几种格式
    private boolean checkTime(String configValue, Long reqTime) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        // 首先对时间段按照 , 分割
        String[] configTimeList = configValue.trim().split(",");
        for (String configTime : configTimeList) {
            if (configTime.startsWith(">=")) {
                String startTime = configValue.substring(2).trim();
                Long compareTime = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (reqTime >= compareTime) {
                    return true;
                }
            } else if (configTime.startsWith("<=")) {
                String endTime = configValue.substring(2).trim();
                Long compareTime = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (reqTime <= compareTime) {
                    return true;
                }
            } else if (configTime.indexOf("#") > 0) {
                String startTime = configValue.substring(0, configTime.indexOf("#")).trim();
                Long compareStartTime = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                String endTime = configTime.substring(configTime.indexOf("#") + 1).trim();
                Long compareEndTime = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (compareStartTime <= reqTime && compareEndTime >= reqTime) {
                    return true;
                }
            } else {
                throw new RuntimeException("Unsupported time cconfig value:" + configValue);
            }
        }

        return false;
    }

    // 时间段比较，支持 >=A, <=B, A#B, A#B,C 等几种格式
    private boolean checkUpgradeTime(String configValue, Long reqTime) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }

        // 首先对时间段按照 , 分割
        String[] configTimeList = configValue.trim().split("&");
        if (configTimeList.length <= 1) {
            configTimeList = configValue.trim().split(",");
        }
        for (String configTime : configTimeList) {
            if (configTime.startsWith(">=")) {
                String startTime = configTime.substring(2).trim();
                Long compareTime = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (reqTime >= compareTime) {
                    return true;
                }
            } else if (configTime.startsWith("<=")) {
                String endTime = configTime.substring(2).trim();
                Long compareTime = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (reqTime <= compareTime) {
                    return true;
                }
            } else if (configTime.indexOf("#") > 0) {
                String startTime = configTime.substring(0, configTime.indexOf("#")).trim();
                Long compareStartTime = DateUtils.stringToDate(startTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                String endTime = configTime.substring(configTime.indexOf("#") + 1).trim();
                Long compareEndTime = DateUtils.stringToDate(endTime, DateUtils.FORMAT_SQL_DATETIME).getTime();
                if (compareStartTime <= reqTime && compareEndTime >= reqTime) {
                    return true;
                }
            } else {
                throw new RuntimeException("Unsupported time cconfig value:" + configTime);
            }
        }

        return false;
    }

    private boolean checkRegion(String configValue, String reqValue) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }
        if (StringUtils.isBlank(reqValue)) {
            return false;
        }
        int rcode;
        try {
            rcode = Integer.parseInt(reqValue);
        } catch (Exception ex) {
            // 不是int，返回false
            return false;
        }

        // 请求参数的Region
        ExRegion reqRegion = raikouSystem.loadRegion(rcode);
        if (reqRegion == null || reqRegion.fetchRegionType() != RegionType.COUNTY) {
            return false;
        }

        // 首先对区域按照 , 分割
        String[] configRegionList = configValue.trim().split(",");
        boolean checkResult = false;
        for (String configRegion : configRegionList) {
            if (StringUtils.isBlank(configRegion)) {
                continue;
            }
            String regionCode = configRegion.substring(configRegion.indexOf("=") + 1).trim();
            ExRegion compareRegion = raikouSystem.loadRegion(Integer.parseInt(regionCode));
            if (compareRegion == null) {
                continue;
            }
            if (configRegion.startsWith("=")) {
                if (compareRegion.fetchRegionType() == RegionType.COUNTY) {
                    if (reqRegion.getCountyCode() == compareRegion.getCountyCode()) {
                        checkResult = true;
                        break;
                    }
                } else if (compareRegion.fetchRegionType() == RegionType.CITY) {
                    if (reqRegion.getCityCode() == compareRegion.getCityCode()) {
                        checkResult = true;
                        break;
                    }
                } else {
                    if (reqRegion.getProvinceCode() == compareRegion.getProvinceCode()) {
                        checkResult = true;
                        break;
                    }
                }
            } else if (configRegion.startsWith("!=")) {
                if (compareRegion.fetchRegionType() == RegionType.COUNTY) {
                    if (reqRegion.getCountyCode() == compareRegion.getCountyCode()) {
                        checkResult = false;
                        break;
                    }
                } else if (compareRegion.fetchRegionType() == RegionType.CITY) {
                    if (reqRegion.getCityCode() == compareRegion.getCityCode()) {
                        checkResult = false;
                        break;
                    }
                } else if (reqRegion.getProvinceCode() == compareRegion.getProvinceCode()) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else {
                throw new RuntimeException("Unsupported region cconfig value:" + configValue);
            }
        }
        return checkResult;
    }

    private boolean checkUpgradeRegion(String configValue, String reqValue) {
        // 如果配置里面不需要检查，直接True
        if (StringUtils.isBlank(configValue)) {
            return true;
        }
        if (StringUtils.isBlank(reqValue)) {
            return false;
        }
        int rcode;
        try {
            rcode = Integer.parseInt(reqValue);
        } catch (Exception ex) {
            // 不是int，返回false
            return false;
        }

        // 请求参数的Region
        ExRegion reqRegion = raikouSystem.loadRegion(rcode);
        if (reqRegion == null || reqRegion.fetchRegionType() != RegionType.COUNTY) {
            return false;
        }

        // 首先对区域按照 , 分割
        String[] configRegionList = configValue.trim().split("&");
        if (configRegionList.length <= 1) {
            configRegionList = configValue.trim().split(",");
        }
        boolean checkResult = false;
        for (String configRegion : configRegionList) {
            if (StringUtils.isBlank(configRegion)) {
                continue;
            }
            String regionCode;
            if (configRegion.startsWith("=")) {
                regionCode = configRegion.substring(configRegion.indexOf("=") + 1).trim();
            } else if (configRegion.startsWith("!=")) {
                regionCode = configRegion.substring(configRegion.indexOf("!=") + 2).trim();
            } else {
                throw new RuntimeException("Unsupported region cconfig value:" + configRegion);
            }
            ExRegion compareRegion = raikouSystem.loadRegion(Integer.parseInt(regionCode));
            if (compareRegion == null) {
                continue;
            }
            if (configRegion.startsWith("=")) {
                if (compareRegion.fetchRegionType() == RegionType.COUNTY) {
                    if (reqRegion.getCountyCode() == compareRegion.getCountyCode()) {
                        checkResult = true;
                        break;
                    }
                } else if (compareRegion.fetchRegionType() == RegionType.CITY) {
                    if (reqRegion.getCityCode() == compareRegion.getCityCode()) {
                        checkResult = true;
                        break;
                    }
                } else {
                    if (reqRegion.getProvinceCode() == compareRegion.getProvinceCode()) {
                        checkResult = true;
                        break;
                    }
                }
            } else if (configRegion.startsWith("!=")) {
                if (compareRegion.fetchRegionType() == RegionType.COUNTY) {
                    if (reqRegion.getCountyCode() == compareRegion.getCountyCode()) {
                        checkResult = false;
                        break;
                    }
                } else if (compareRegion.fetchRegionType() == RegionType.CITY) {
                    if (reqRegion.getCityCode() == compareRegion.getCityCode()) {
                        checkResult = false;
                        break;
                    }
                } else if (reqRegion.getProvinceCode() == compareRegion.getProvinceCode()) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else {
                throw new RuntimeException("Unsupported region cconfig value:" + configRegion);
            }
        }
        return checkResult;
    }

    private boolean checkKtwelve(String configKtwelve, String reqKtwelve) {
        //没配置，直接通过
        if (StringUtils.isBlank(configKtwelve)) {
            return true;
        }
        String[] configKtwelveList = configKtwelve.trim().split("&");
        boolean checkResult = false;

        //配置了。请求没传，把请求视为小学端的。此时只要配置的是小学端就算通过
        if (StringUtils.isBlank(reqKtwelve)) {
            for (String configVal : configKtwelveList) {
                if (configVal.equals(Ktwelve.PRIMARY_SCHOOL.name())) {
                    checkResult = true;
                    break;
                }
            }
        } else {
            //配置了。请求也传了。完全匹配才通过
            for (String configVal : configKtwelveList) {
                if (configVal.equalsIgnoreCase(reqKtwelve)) {
                    checkResult = true;
                    break;
                }
            }
        }

        return checkResult;
    }

    private boolean checkAccountStatus(String configAccountStatus, String reqAccountStatus) {
        if (StringUtils.isBlank(configAccountStatus)) {
            return true;
        }

        if (StringUtils.isBlank(reqAccountStatus)) {
            return false;
        }

        boolean checkResult = false;
        //账户异常配置的是全部或者请求的与配置的相同就返回true
        if ("ALL".equals(configAccountStatus) || configAccountStatus.equalsIgnoreCase(reqAccountStatus)) {
            checkResult = true;
        }

        return checkResult;
    }

    private boolean checkVersion(String configVersion, String reqVersion) {
        // 如果配置里面不需要检查Version，直接True
        if (StringUtils.isBlank(configVersion)) {
            return true;
        }
        // 如果请求参数没有Version，直接False
        if (StringUtils.isBlank(reqVersion)) {
            return false;
        }

        String[] configVersionList = configVersion.trim().split("&");
        boolean checkResult = false;
        for (String configVer : configVersionList) {
            if (StringUtils.isBlank(configVer)) {
                continue;
            }
            if (configVer.startsWith("!=")) {
                String compareVersion = configVer.substring(2);
                if (compareVersion(reqVersion, compareVersion) == 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith(">=")) {
                String compareVersion = configVer.substring(2);
                if (compareVersion(reqVersion, compareVersion) < 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("<=")) {
                String compareVersion = configVer.substring(2);
                if (compareVersion(reqVersion, compareVersion) > 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("=")) {
                String compareVersion = configVer.substring(1);
                if (compareVersion(reqVersion, compareVersion) == 0) {
                    checkResult = true;
                    break;
                }
            } else if (configVer.startsWith(">")) {
                String compareVersion = configVer.substring(1);
                if (compareVersion(reqVersion, compareVersion) <= 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.startsWith("<")) {
                String compareVersion = configVer.substring(1);
                if (compareVersion(reqVersion, compareVersion) >= 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else if (configVer.indexOf(":") > 0) {
                int index = configVersion.indexOf(":");
                String startVersion = configVer.substring(0, index).trim();
                String endVersion = configVer.substring(index + 1).trim();
                if (compareVersion(reqVersion, startVersion) < 0 || compareVersion(reqVersion, endVersion) > 0) {
                    checkResult = false;
                    break;
                }
                checkResult = true;
            } else {
                throw new RuntimeException("Unknown app version config:" + configVer);
            }

        }
        return checkResult;
    }

    // 版本号比较，支持2，3，4版本号比较
    private static int compareVersion(String ver1, String ver2) {
        String[] ver1List = ver1.trim().split("\\.");
        String[] ver2List = ver2.trim().split("\\.");

        if (ver1List.length < 2 || ver2List.length < 2) {
            throw new RuntimeException("app version is not correct!, ver1:" + ver1 + ", ver2:" + ver2);
        }

        int ver1First = Integer.parseInt(ver1List[0]);
        int ver1Second = Integer.parseInt(ver1List[1]);
        int ver1Third = ver1List.length >= 3 ? Integer.parseInt(ver1List[2]) : -1;
        int ver1Fourth = ver1List.length >= 4 ? Integer.parseInt(ver1List[3]) : -1;
        int ver2First = Integer.parseInt(ver2List[0]);
        int ver2Second = Integer.parseInt(ver2List[1]);
        int ver2Third = ver2List.length >= 3 ? Integer.parseInt(ver2List[2]) : -1;
        int ver2Fourth = ver2List.length >= 4 ? Integer.parseInt(ver2List[3]) : -1;

        if (ver1First == ver2First && ver1Second == ver2Second && ver1Third == ver2Third && ver1Fourth == ver2Fourth) {
            return 0;
        }

        if (ver1First > ver2First) {
            return 1;
        } else if (ver1First < ver2First) {
            return -1;
        } else if (ver1Second > ver2Second) {
            return 1;
        } else if (ver1Second < ver2Second) {
            return -1;
        } else if (ver1Third > ver2Third) {
            return 1;
        } else if (ver1Third < ver2Third) {
            return -1;
        } else if (ver1Fourth > ver2Fourth) {
            return 1;
        }
        return -1;
    }

    // 关闭硬件加速的机器配置
    protected String closeHardwareAccelerationList(String productId) {
        String blockName = "hardware_acceleration_close";
        if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "200")) {
            blockName = "hardware_acceleration_close_parent";
        } else if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "300")) {
            blockName = "hardware_acceleration_close_teacher";
        } else if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "110")) {
            blockName = "hardware_acceleration_close_middle_student";
        } else if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "310")) {
            blockName = "hardware_acceleration_close_middle_teacher";
        }

        String hardwareAccelerationConfig = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", blockName);
        if (StringUtils.isBlank(hardwareAccelerationConfig)) {
            return "";
        }
        hardwareAccelerationConfig = hardwareAccelerationConfig.replace("\r", "").replace("\n", "").replace("\t", "");
        List<String> configList = JsonUtils.fromJsonToList(hardwareAccelerationConfig, String.class);
        return StringUtils.join(configList, ",");
    }

    // 关闭crosswalk引擎的配置
    protected String internalGetCloseCrossWalkList(String key) {
        String crosswalkConfig = getPageBlockContentGenerator().getPageBlockContentHtml("client_app_publish", key);
        if (StringUtils.isBlank(crosswalkConfig)) {
            return "";
        }
        crosswalkConfig = crosswalkConfig.replace("\r", "").replace("\n", "").replace("\t", "");
        List<String> configList = JsonUtils.fromJsonToList(crosswalkConfig, String.class);
        return StringUtils.join(configList, ",");
    }

    // 关闭硬件加速的机器配置
    protected String getCloseCrossWalkList(String productId) {
        String blockName = "new_crosswalk_close";

        if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "110")) {
            blockName = "crosswalk_close_middle_student";
        } else if (StringUtils.isNoneBlank(productId) && Objects.equals(productId, "310")) {
            blockName = "crosswalk_close_middle_teacher";
        }

        return internalGetCloseCrossWalkList(blockName);
    }
}
