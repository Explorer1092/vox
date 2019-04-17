package com.voxlearning.utopia.cnedu.listener.helper;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.monitor.FlightController;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import sun.applet.resources.MsgAppletViewer_it;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CneduReportHelper {
    private static final String MAC_NAME = "HmacSHA1";

    private static final Map<String, SsoConnections> APP_ID_MAP = new HashMap<>();
    static {
        // C
        APP_ID_MAP.put("pc", SsoConnections.Cnedu);
        // 小学学生APP
        APP_ID_MAP.put("junior_role_student_", SsoConnections.CneduStudent);
        APP_ID_MAP.put("junior_role_student_android", SsoConnections.CneduStudent);
        APP_ID_MAP.put("junior_role_student_ios", SsoConnections.CneduStudentIOS);

        // 小学老师APP
        APP_ID_MAP.put("junior_role_teacher_", SsoConnections.CneduTeacher);
        APP_ID_MAP.put("junior_role_teacher_android", SsoConnections.CneduTeacher);
        APP_ID_MAP.put("junior_role_teacher_ios", SsoConnections.CneduTeacherIOS);

        // 中学学学生APP
        APP_ID_MAP.put("middle_role_student_", SsoConnections.CneduJuniorStu);
        APP_ID_MAP.put("middle_role_student_android", SsoConnections.CneduJuniorStu);
        APP_ID_MAP.put("middle_role_student_ios", SsoConnections.CneduJuniorStuIOS);

        // 小学老师APP
        APP_ID_MAP.put("middle_role_teacher_", SsoConnections.CneduJuniorTea);
        APP_ID_MAP.put("middle_role_teacher_android", SsoConnections.CneduJuniorTea);
        APP_ID_MAP.put("middle_role_teacher_ios", SsoConnections.CneduJuniorTeaIOS);
    }

    private static MapMessage reportLoginInfo(String httpUrl, Long userId, String userName, Long schoolId, String schoolName, String identity,
                                              Integer provCode, String provName,
                                              Integer cityCode, String cityName,
                                              Integer countyCode, String countyName,
                                              String appType) {

        Map<String, String> httpParams = new HashMap<>();
        httpParams.put("userId", SafeConverter.toString(userId));
        httpParams.put("name", userName);
        httpParams.put("loginAccount", SafeConverter.toString(userId));
        httpParams.put("type", "0");
        httpParams.put("orgId", SafeConverter.toString(schoolId));
        httpParams.put("orgName", schoolName);
        httpParams.put("userIdentity", identity);
        httpParams.put("provinceCode", SafeConverter.toString(provCode));
        httpParams.put("province", provName);
        httpParams.put("cityCode", SafeConverter.toString(cityCode));
        httpParams.put("city", cityName);
        httpParams.put("areaCode", SafeConverter.toString(countyCode));
        httpParams.put("area", countyName);

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(httpUrl).turnOffLogException().json(httpParams).execute();

        if (response.hasHttpClientException()) {
            // 如果是http connection timeout retry again
            String exceptionInfo = response.getHttpClientExceptionMessage();
            if (StringUtils.isNoneBlank(exceptionInfo) && exceptionInfo.contains("connect timed out")) {
                response = HttpRequestExecutor.defaultInstance().post(httpUrl).json(httpParams).execute();
                if (response.hasHttpClientException()) {
                    log.error("Failed to report LoginInfo @cnedu, {}", response.getHttpClientExceptionMessage());
                    return MapMessage.errorMessage("Failed to report LoginInfo");
                }
            } else {
                // log.error("Failed to report LoginInfo @cnedu, {}", response.getHttpClientExceptionMessage());
                return MapMessage.errorMessage("Failed to report LoginInfo");
            }
        }

        if (response.getStatusCode() == 200) {
            String validateResponse = response.getResponseString();
            Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
            if (apiResult == null || !apiResult.containsKey("retCode") || !apiResult.get("retCode").equals("000000")) {
                //log.warn("report login info failed, request:{}, response:{}, request:{}", JsonUtils.toJson(httpParams), validateResponse, httpUrl);
                LogCollector.info("backend-general", MiscUtils.map(
                        "env", RuntimeMode.getCurrentStage(),
                        "op", "CneduReportHelperFailed",
                        "request",  JsonUtils.toJson(httpParams),
                        "response", validateResponse,
                        "apiUrl", httpUrl,
                        "appType", appType
                ));

                return MapMessage.errorMessage(validateResponse);
            }

            LogCollector.info("backend-general", MiscUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "op", "CneduReportHelperSucceed",
                    "request",  JsonUtils.toJson(httpParams),
                    "response", validateResponse,
                    "apiUrl", httpUrl,
                    "appType", appType
            ));
        }

        return MapMessage.successMessage();
    }

    private static String ckAccessToken(String appId) {
        return "login_report_at7_" + appId;
    }

    private static String loadCachedAccessToken(SsoConnections connections) {
        String ck = ckAccessToken(connections.getClientId());
        Map apiResult = CacheSystem.CBS.getCache("flushable").load(ck);
        if (apiResult == null || apiResult.size() == 0) {
            return "";
        }

        Long validTime = SafeConverter.toLong(apiResult.get("validTime"));
        if (validTime < System.currentTimeMillis() - 5000) {
            return "";
        }

        return SafeConverter.toString(apiResult.get("accessToken"));
    }

    public static String getAccessToken(SsoConnections connections) {
        // load cached access token first
        String cachedAccessToken = loadCachedAccessToken(connections);
        if (StringUtils.isNoneBlank(cachedAccessToken)) {
            //log.info("return cached access token:{}, for {}", cachedAccessToken, connections.getClientId());
            return cachedAccessToken;
        }

        return internalGetAccessToken(connections);
    }

    private static String internalGetAccessToken(SsoConnections connections) {
        try {
            final String sendUrl = getAccessTokenGetUrl();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put("appId", connections.getClientId());
            String curTime = String.valueOf(new Date().getTime());
            httpParams.put("timeStamp", curTime);
            String encryptText = connections.getClientId() + connections.getSecretId() + curTime;
            String keyinfo = HmacSHA1Encrypt(encryptText, connections.getSecretId());
            httpParams.put("keyInfo", keyinfo);
            httpParams.put("sysCode", "0");

            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(sendUrl).json(httpParams).execute();

            if (response.getStatusCode() == 200) {
                String validateResponse = response.getResponseString();
                Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
                if (apiResult == null || !apiResult.containsKey("retCode") || !apiResult.get("retCode").equals("000000")) {
                    log.warn("get access token failed, response:{}", validateResponse);
                    return "";
                } else {
                    Map map = (Map) apiResult.get("data");

                    // temp save result
                    CacheSystem.CBS.getCache("flushable").set(ckAccessToken(connections.getClientId()), 7200, map);

                    return SafeConverter.toString(map.get("accessToken"));
                }
            }
        } catch (Exception e) {
            log.error("get access token failed.", e);
        }

        return "";
    }

    private static String HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes("UTF-8");
        //根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
        SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
        //生成一个指定 Mac 算法 的 Mac 对象
        Mac mac = Mac.getInstance(MAC_NAME);
        //用给定密钥初始化 Mac 对象
        mac.init(secretKey);
        byte[] text = encryptText.getBytes("UTF-8");
        //完成 Mac 操作
        byte[] resultBytes = mac.doFinal(text);
        return bytesToHexString(resultBytes);
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int value = src[i];
            int v1 = value / 16;
            int v2 = value % 16;
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static String getLoginInfoReportUrl() {
        return "http://gateway.system.eduyun.cn:40032/cert/independentAppRegister?accessToken=";
    }

    private static String getAccessTokenGetUrl() {
        return "http://gateway.system.eduyun.cn:40032/apigateway/getAccessToken";
    }

    private static SsoConnections getConnectionByRoleType(RoleType roleType, OperationSourceType loginSource, SchoolLevel schoolLevel, String appType) {
        // PC 的统一走一个
        if (loginSource == OperationSourceType.pc) {
            return APP_ID_MAP.get(loginSource.name());
        }

        String key = StringUtils.join(schoolLevel.name(), "_", roleType.name(), "_", appType).toLowerCase();
        if (!APP_ID_MAP.containsKey(key)) {
            log.warn("unknown role type found with key {}", key);
            return null;
        }

        return APP_ID_MAP.get(key);
    }

    public static void sendStudentLoginInfo2Cnedu(StudentDetail detail, ExRegion region, OperationSourceType loginSource, String appType) {
        if (detail == null || detail.getClazz() == null || region == null) {
            return;
        }

        SchoolLevel schoolLevel = SchoolLevel.JUNIOR;
        if (detail.isJuniorStudent() || detail.isSeniorStudent()) {
            schoolLevel = SchoolLevel.MIDDLE;
        }

        SsoConnections connections = getConnectionByRoleType(RoleType.ROLE_STUDENT, loginSource, schoolLevel, appType);
        if (connections == null) {
            return;
        }

        String accessToken = getAccessToken(connections);

        if (StringUtils.isBlank(accessToken)) {
            return;
        }

        // send login report
        String reportUrl = getLoginInfoReportUrl() + accessToken;
        reportLoginInfo(reportUrl, detail.getId(), detail.fetchRealname(),
                detail.getClazz().getSchoolId(), detail.getStudentSchoolName(), "0",
                region.getProvinceCode(), region.getProvinceName(),
                region.getCityCode(), region.getCityName(),
                region.getCountyCode(), region.getCountyName(),
                appType
        );

    }

    public static void sendTeacherLoginInfo2Cnedu(TeacherDetail detail, ExRegion region, OperationSourceType loginSource, String appType) {
        if (detail == null || detail.getTeacherSchoolId() == null || region == null) {
            return;
        }

        SchoolLevel schoolLevel = SchoolLevel.JUNIOR;
        if (detail.isJuniorTeacher() || detail.isSeniorTeacher()) {
            schoolLevel = SchoolLevel.MIDDLE;
        }

        SsoConnections connections = getConnectionByRoleType(RoleType.ROLE_TEACHER, loginSource, schoolLevel, appType);
        if (connections == null) {
            return;
        }

        String accessToken = getAccessToken(connections);

        if (StringUtils.isBlank(accessToken)) {
            return;
        }

        // send login report
        String reportUrl = getLoginInfoReportUrl() + accessToken;
        reportLoginInfo(reportUrl, detail.getId(), detail.fetchRealname(),
                detail.getTeacherSchoolId(), detail.getTeacherSchoolName(), "1",
                region.getProvinceCode(), region.getProvinceName(),
                region.getCityCode(), region.getCityName(),
                region.getCountyCode(), region.getCountyName(),
                appType
        );
    }

//    public static void sendParentLoginInfo2Cnedu(User parent, StudentDetail detail, ExRegion region, OperationSourceType loginSource) {
//        if (parent == null || detail == null || detail.getClazz() == null || region == null) {
//            return;
//        }
//
//        SsoConnections connections = getConnectionByRoleType(RoleType.ROLE_PARENT, loginSource);
//        String accessToken = getAccessToken(connections);
//
//        if (StringUtils.isBlank(accessToken)) {
//            return;
//        }
//
//        // send login report
//        String reportUrl = getLoginInfoReportUrl() + accessToken;
//        reportLoginInfo(reportUrl, parent.getId(), parent.fetchRealname(),
//                detail.getClazz().getSchoolId(), detail.getStudentSchoolName(), "2",
//                region.getProvinceCode(), region.getProvinceName(),
//                region.getCityCode(), region.getCityName(),
//                region.getCountyCode(), region.getCountyName()
//        );
//    }

    public static void main(String[] args) {
        StudentDetail detail = new StudentDetail();
        detail.setId(30002L);
        detail.setStudentSchoolName("一起作业测试学校");
        Clazz clazz = new Clazz();
        clazz.setSchoolId(34529L);
        clazz.setEduSystem(EduSystemType.P6);
        detail.setClazz(clazz);
        detail.getProfile().setRealname("测试");

        ExRegion exRegion = new ExRegion();
        exRegion.setProvinceCode(110000);
        exRegion.setProvinceName("北京市");
        exRegion.setCityCode(110100);
        exRegion.setCityName("北京市");
        exRegion.setCountyCode(110101);
        exRegion.setCountyName("海淀区");


        sendStudentLoginInfo2Cnedu(detail, exRegion, OperationSourceType.app, "ios");


//        SsoConnections connections = getConnectionByRoleType(RoleType.ROLE_TEACHER);
//        String accessToken = getAccessToken(connections);
//        System.out.println(accessToken);
//
//        String httpUrl = "http://gateway.system.eduyun.cn:40015/baseInfo/getAreaList?accessToken=" + accessToken;
//
//        Map<String, String> httpParams = new HashMap<>();
//        httpParams.put("parentCode ", "0");
//
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(httpUrl).json(httpParams).execute();
//        System.out.println(response.getResponseString());


        //String httpUrl = getLoginInfoReportUrl() + accessToken;
        //validateTicket(httpUrl,"ek01NzJjMmUzODItNDRhYy00ZDY2LTgyZTMtODI0NGFjY2Q2NmYxMTUyOTQ3OTIxNDIwNw==");
    }
}
