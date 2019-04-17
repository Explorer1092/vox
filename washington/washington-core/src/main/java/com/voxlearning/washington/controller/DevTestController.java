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

package com.voxlearning.washington.controller;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.event.EventBusBoss;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.athena.bean.ParentResData;
import com.voxlearning.utopia.api.constant.FlowerConditionActionType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.payment.gateway.UmpayPaymentGateway;
import com.voxlearning.utopia.service.flower.api.FlowerConditionService;
import com.voxlearning.utopia.service.flower.event.FlowerConditionActionEvent;
import com.voxlearning.utopia.service.message.client.AppMessageLoaderClient;
import com.voxlearning.utopia.service.message.data.VersionedAppGlobalMessageData;
import com.voxlearning.utopia.service.newhomework.consumer.VoiceRecommendLoaderClient;
import com.voxlearning.utopia.service.order.api.loader.UserOrderLoader;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.athena.ParentSearchEngineServiceClient;
import com.voxlearning.washington.support.AbstractController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Controller
@RequestMapping("/devtest")
@Slf4j
public class DevTestController extends AbstractController {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private ParentSearchEngineServiceClient parentSearchEngineServiceClient;

    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;

    @ImportService(interfaceClass = FlowerConditionService.class)
    private FlowerConditionService flowerConditionService;

    @Resource PaymentGatewayManager paymentGatewayManager;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private VoiceRecommendLoaderClient voiceRecommendLoaderClient;
    @Inject private AppMessageLoaderClient appMessageLoaderClient;

    @ImportService(interfaceClass = UserOrderLoader.class) private UserOrderLoader userOrderLoader;

    /**
     * 为运维team提供的ping方法，对userProvider进行dubbo远程调用
     * 如果成功返回SC_OK，否则返回SC_INTERNAL_SERVER_ERROR
     *
     * @param response http servlet response
     */
    @RequestMapping(value = "ping_user_loader.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public void pingUserLoader(HttpServletResponse response) {
        FlightRecorder.closeLog();
        try {
            userLoaderClient.pingUserLoader();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "response_header.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage responseHeader(HttpServletResponse response) {
        response.addHeader("Controller-name", "DevTestController");
        response.setHeader("Date", "One world, one dream.");
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "info.vpage", method = RequestMethod.GET)
    public String info(Model model, HttpServletRequest request, HttpServletResponse response) {

        if (getWebRequestContext().isRequestFromOffice()) {
            //do sth ... tricky
        }

        String realRemoteAddr = getWebRequestContext().getRealRemoteAddr();

        model.addAttribute("realRemoteIp", realRemoteAddr);

        ArrayList<String> httpHeaderLines = new ArrayList<String>();
        for (Enumeration e = request.getHeaderNames(); e.hasMoreElements(); ) {
            String headerName = (String) e.nextElement();
            for (Enumeration f = request.getHeaders(headerName); f.hasMoreElements(); ) {
                String headerValue = (String) f.nextElement();
                httpHeaderLines.add(headerName + ": " + headerValue);
            }

        }


        model.addAttribute("httpHeaderLines", httpHeaderLines);

        model.addAttribute("testJsonArray", new Object[]{1, "A", new Date()});
        model.addAttribute("testJsonList", Arrays.asList(1, "A", new Date()));
        model.addAttribute("testJsonMap", MiscUtils.map("A", "B", 1, 2));

        log.info(JsonUtils.toJson(MapMessage.errorMessage("err")));

        log.info("Parameters:" + JsonUtils.toJson(request.getParameterMap()));

        return "devtest/info";
    }

    @RequestMapping(value = "test.vpage", method = RequestMethod.GET)
    public String test(Model model) {

        return "devtest/test";
    }

    @RequestMapping(value = "error.vpage", method = RequestMethod.GET)
    public String error(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String code = request.getParameter("code");
        if (StringUtils.isBlank(code))
            code = "200";

        int httpStatus = Integer.parseInt(code);
        response.setStatus(httpStatus);
        response.getWriter().write("message:" + code);

        return null;
    }


    @RequestMapping(value = "umpay/trans.vpage", method = RequestMethod.GET)
    @ResponseBody
    String umpayTrans(Model model, HttpServletRequest request, HttpServletResponse response) {
/*

TRADEDETAIL-START,3318,20130616,3.0,0000,生成对账文件成功
3318,331801,13800138000,371383837857972575403,20130616,20130616,1800,02,3,20130626,0,1,731
3318,331801,13800138000,371368813721910602941,20130616,20130616,1800,02,3,20130626,0,1,431
3318,331801,13800138000,371366411039912574510,20130616,20130616,1800,02,3,20130626,0,1,010
3318,331801,13800138000,371361617679808161311,20130616,20130616,1800,02,3,20130626,0,1,010
3318,331801,13800138000,371349559897015789714,20130616,20130616,1800,02,3,20130626,0,1,010
3318,331801,13800138000,371350263095744964336,20130616,20130616,1800,02,3,20130626,0,1,010
3318,331801,13800138000,371388250448202519504,20130616,20130616,1800,02,3,20130626,0,1,010
3318,200,13800138000,371381665635468341758,20130616,20130616,2000,02,3,20130616,0,1,010
3318,250,13800138000,371382805927475886856,20130616,20130616,2500,02,3,20130616,0,1,010
TRADEDETAIL-END,3318,20130616,9,17100

 */
        if (!getWebRequestContext().isRequestFromOffice()) {
            throw new RuntimeException("forbidden");
        }

        UmpayPaymentGateway umpayPaymentGateway = (UmpayPaymentGateway) paymentGatewayManager.getPaymentGateway(UmpayPaymentGateway.Name);

        String day = getRequestParameter("day", DateUtils.dateToString(DateUtils.getYesterdayStart(), "yyyyMMdd"));
        String content = umpayPaymentGateway.getBillingTrans(day);

        return content;
    }

    @RequestMapping(value = "umpay/settle.vpage", method = RequestMethod.GET)
    @ResponseBody
    String umpaySettle(Model model, HttpServletRequest request, HttpServletResponse response) {

        /*
        对方解释settle： 那个 包月的  是10天后； 按次的 是 和交易对账文件一天的

TRADEDETAIL-START,3318,20130616,3.0,0000,生成对账文件成功
3318,200,13800138000,371381665635468341758,20130616,20130616,2000,02,3,20130616,0,1,010
3318,250,13800138000,371382805927475886856,20130616,20130616,2500,02,3,20130616,0,1,010
TRADEDETAIL-END,3318,20130616,2,4500
         */
        if (!getWebRequestContext().isRequestFromOffice()) {
            throw new RuntimeException("forbidden");
        }

        UmpayPaymentGateway umpayPaymentGateway = (UmpayPaymentGateway) paymentGatewayManager.getPaymentGateway(UmpayPaymentGateway.Name);

        String day = getRequestParameter("day", DateUtils.dateToString(DateUtils.getYesterdayStart(), "yyyyMMdd"));
        String content = umpayPaymentGateway.getBillingSettle(day);

        return content;
    }

    @RequestMapping(value = "cookie/set.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String cookieSet(Model model) {
        if (!getWebRequestContext().isRequestFromOffice()) {
            throw new RuntimeException("forbidden");
        }

        String name = getRequestParameter("name", "test_cookie");
        String value = getRequestParameter("value", "test_value");
        int expire = Integer.parseInt(getRequestParameter("expire", "-1"));
        getCookieManager().setCookieTLD(name, value, expire);

        return "done";
    }

    /**
     * 跳转到单词检查的flash页面
     *
     * @return
     */
    @RequestMapping(value = "wordcheck.vpage", method = RequestMethod.GET)
    public String getWordCheck() {
        return "devtest/wordcheck";
    }

    @RequestMapping(value = "text.vpage")
    @ResponseBody
    public String text() {
        return "中文测试";
    }

    @RequestMapping(value = "json.vpage")
    @ResponseBody
    public Map json() {
        return MiscUtils.<String, Object>map().add("a", "中文").add("c", 1).add("e", null);
    }

    @RequestMapping(value = "jsonnull.vpage")
    @ResponseBody
    public Map jsonnull() {
        return null;
    }

    @RequestMapping(value = "jsonstr.vpage")
    @ResponseBody
    public String jsonstr() {
        return JsonUtils.toJson(null);
    }

    @RequestMapping(value = "voiceengine.vpage")
    public String voiceengine() {
        return "devtest/voiceengine";
    }

    @RequestMapping(value = "date.vpage", method = {RequestMethod.GET})
    public String date(Model model) {
        log.error(currentUser().getCreateTime().getClass().getName());
        model.addAttribute("d", new Date());
        return "devtest/date";
    }

    @RequestMapping(value = "ex.vpage", method = {RequestMethod.GET})
    public String ex() {
        throw new RuntimeException("This is an exception for test");
    }


    /**
     * 资讯搜索测试
     *
     * @param model
     */
    @RequestMapping(value = "jxtnewssearchtest.vpage")
    public String jxtnewssearchtest(Model model) {
        if (RuntimeMode.isTest() || RuntimeMode.isDevelopment() || RuntimeMode.isStaging()) {
            String keyWord = getRequestParameter("keyWord", "");
            if (StringUtils.isNotBlank(keyWord)) {
                //这里需要调接口，以下是测试数据
                List<String> list = new ArrayList<>();
                try {
                    Map<Integer, List<ParentResData>> result = parentSearchEngineServiceClient.getParentSearchEngineService()
                            .textSearch(keyWord, 0, 10, -1);
                    List<ParentResData> datas = result.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
                    list.addAll(datas.stream().map(pd -> pd.id).collect(Collectors.toList()));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                if (RuntimeMode.isTest() || RuntimeMode.isDevelopment()) {
                    model.addAttribute("testResult", list);
                } else if (RuntimeMode.isStaging()) {
                    model.addAttribute("stagingResult", list);
                }
            }
            return "devtest/newssearchtest";
        }
        return "";
    }

    @RequestMapping(value = "testvoid.vpage", method = {RequestMethod.GET})
    public String testvoid() {
        miscServiceClient.termBeginRecordAdjustClazz(125076L);
        return "";
    }

    @RequestMapping(value = "appGlobalMessageBufferStatus.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String appGlobalMessageBufferStatus() {
        Map<String, Object> map = new LinkedHashMap<>();
        VersionedAppGlobalMessageData data = appMessageLoaderClient.getAppGlobalMessageBuffer().dump();
        map.put("version", data.getVersion());
        map.put("size", data.getAppGlobalMessageList().size());
        return JsonUtils.toJsonPretty(map);
    }

    @RequestMapping(value = "loadTeacherSchool.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String loadTeacherSchool(@RequestParam Long teacherId) {
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherId)
                .getUninterruptibly();
        return JsonUtils.toJsonPretty(school);
    }

    @RequestMapping(value = "orderProductList.vpage", method = RequestMethod.GET)
    @ResponseBody
    public String orderProductList() {
        return JsonUtils.toJsonPretty(userOrderLoader.loadAllOrderProduct());
    }

    @RequestMapping(value = "autoSubmitVacationHomework.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage autoSubmitHomework() {
        if (RuntimeMode.isUsingProductionData()) {
            return MapMessage.errorMessage("线上数据不允许自动提交");
        }
        String homeworkId = getRequestString("hid");
        Long userId = getRequestLong("uid");
        if (StringUtils.isBlank(homeworkId)) {
            return MapMessage.errorMessage("作业id错误");
        }
        if (userId == 0) {
            return MapMessage.errorMessage("学生id错误");
        }
        String type = getRequestString("type");
        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
        return vacationHomeworkServiceClient.autoSubmitVacationHomework(homeworkId, userId, objectiveConfigType);
    }

    @RequestMapping(value = "testglobalmsg.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage ttt() {
        if (RuntimeMode.isProduction())
            return MapMessage.errorMessage("error");
        String sst = getRequestString("sst");
        mySelfStudyService.globalMsg(SelfStudyType.of(sst), getRequestString("msg"));
        return MapMessage.successMessage();
    }

    @RequestMapping(value = "testaddflowercondition.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addFlowerCondition() {
        if (RuntimeMode.isProduction())
            return MapMessage.errorMessage("error");
        FlowerConditionActionEvent event = new FlowerConditionActionEvent();
        event.setStudentId(getRequestLong("sid"));
        event.setActionType(FlowerConditionActionType.of(getRequestString("action")));
        event.setGroupId(getRequestLong("gid"));
        Teacher teacher = teacherLoaderClient.loadGroupSingleTeacher(Collections.singleton(getRequestLong("gid"))).get(getRequestLong("gid"));
        event.setTeacherId(teacher.getId());
        event.setCreationDate(new Date());
        flowerConditionService.sendFlowerConditionMessage(event);
        return MapMessage.successMessage();
    }

    private static Map<String, String> secretKeyMap = new HashMap<>();
    private static final String PARENT_APP_KEY = "17Parent";
    private static final String PARENT_SECRET_KEY = "iMMrxI3XMQtd";
    private static final String STUDENT_APP_KEY = "17Student";
    private static final String STUDENT_SECRET_KEY = "kuLwGZMJBcQj";
    private static final String TACHER_APP_KEY = "17Teacher";
    private static final String TEACHER_SECRET_KEY = "gvUKQN1EFXKp";

    static {
        Map<String, Object> m = MiscUtils.m(PARENT_APP_KEY, PARENT_SECRET_KEY, STUDENT_APP_KEY, STUDENT_SECRET_KEY, TACHER_APP_KEY, TEACHER_SECRET_KEY);
        m.entrySet().forEach(t -> {
            secretKeyMap.put(t.getKey(), SafeConverter.toString(t.getValue()));
        });
    }

    @RequestMapping(value = "mock_api_test.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mockApiRequestForTest() {
        if (RuntimeMode.isProduction())
            return MapMessage.errorMessage("error");
        Mode current = RuntimeMode.current();
        String host;
        if (current == Mode.DEVELOPMENT)
            host = "http://localhost:8081/";
        else if (current == Mode.TEST)
            host = "https://api.test.17zuoye.net/";
        else if (current == Mode.STAGING)
            host = "https://api.staging.17zuoye.net/";
        else
            return MapMessage.errorMessage();
        String apiUrl = getRequestString("apiUrl");
        Long userId = getRequestLong("uid");
        String appKey = getRequestString("appKey");
        String version = getRequestString("version");
        Map<String, Object> params1 = JsonUtils.fromJson(getRequestString("params"));
        Map<String, String> paramMap = new HashMap<>();
        params1.entrySet().forEach(t -> {
            paramMap.put(t.getKey(), SafeConverter.toString(t.getValue()));
        });

        List<VendorAppsUserRef> vendorAppsUserRefs = vendorLoaderClient.loadUserVendorApps(userId);
        VendorAppsUserRef vendorAppsUserRef = vendorAppsUserRefs.stream().filter(t -> t.getAppKey().equals(appKey)).sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .findFirst().orElse(null);
        if (vendorAppsUserRef == null)
            return MapMessage.errorMessage("no vendor appsRef");

        paramMap.put("session_key", vendorAppsUserRef.getSessionKey());
        paramMap.put("app_key", appKey);
        String secretKey = secretKeyMap.get(appKey);
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        if (StringUtils.isNotBlank(version))
            paramMap.put("ver", version);
        String url = UrlUtils.buildUrlQuery(host + apiUrl, paramMap);
        EventBusBoss boss = EventBusBoss.getInstance();
        boss.initialize();
        boss.subscribeInstalledListeners();
        boss.subscribeConfiguredListeners();
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        String responseString = response.getResponseString();
        System.out.println(responseString);
        System.out.println("Test End...");
        return JsonUtils.fromJson(responseString, MapMessage.class);
    }

    @RequestMapping(value = "testPageConfig.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage testPageConfig() {
        if (RuntimeMode.isProduction())
            return MapMessage.errorMessage("调皮");
        String pageName = getRequestString("pageName");
        String blockName = getRequestString("blockName");
        String className = getRequestString("className");
        Class<?> aClass = null;
        try {
            aClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (aClass == null)
            return MapMessage.errorMessage("can not find class");
        List<?> objects = pageBlockContentServiceClient.loadConfigList(pageName, blockName, aClass);
        return MapMessage.successMessage().add("data", objects);
    }

    @RequestMapping(value = "voiceengineconfig.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage voiceEngineConfig() {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(333905103L);
        return newHomeworkContentServiceClient.loadVoiceEngineConfig(studentDetail, ObjectiveConfigType.BASIC_APP);
    }
}
