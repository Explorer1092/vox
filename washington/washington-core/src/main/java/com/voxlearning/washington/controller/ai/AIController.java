package com.voxlearning.washington.controller.ai;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.api.*;
import com.voxlearning.utopia.service.ai.constant.ChipsEnglishTeacher;
import com.voxlearning.utopia.service.ai.context.AIUserQuestionContext;
import com.voxlearning.utopia.service.ai.data.AIUserQuestionResultRequest;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.content.consumer.WordStockLoaderClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.SensitiveUserDataServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.RES_MESSAGE;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT;

/**
 * Created by Summer on 2018/3/27
 */
@Controller
@RequestMapping("/ai")
public class AIController extends AbstractAiController {

    @Inject
    private WordStockLoaderClient wordStockLoaderClient;

    @ImportService(interfaceClass = AiOrderProductService.class)
    private AiOrderProductService aiOrderProductService;

    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;


    @ImportService(interfaceClass = ChipsOrderProductLoader.class)
    private ChipsOrderProductLoader chipsOrderProductLoader;

    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @ImportService(interfaceClass = ChipsEnglishClazzLoader.class)
    private ChipsEnglishClazzLoader chipsEnglishClazzLoader;

    // 获取今日课程
    @RequestMapping(value = "/1.0/daily.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage daily() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no user!");
        }
        String version = getRequestString("ver");
        String checkVersion = Optional.ofNullable(chipsEnglishConfigServiceClient.loadChipsEnglishConfigByName(APP_VERSION))
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .map(ChipsEnglishPageContentConfig::getValue)
                .orElse("");
        if (StringUtils.isNoneBlank(checkVersion, version) && VersionUtil.compareVersion(version, checkVersion) < 0) {
            String url =  ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/parent_ai/download";
            return MapMessage.successMessage().add(RES_RESULT, "success").add("data", "401").add("redirect", url);
        }
        String unitId = getRequestString("unitId");
        MapMessage message = aiLoaderClient.getRemoteReference().loadDailyClass(user, unitId);
        message.putIfAbsent("userName", Optional.ofNullable(user.getProfile()).map(UserProfile::getNickName).orElse(""));
        if (message.isSuccess()) {
            message.putIfAbsent(RES_RESULT, "success");
            List<UserOrder> userOrderList = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId());
            if (CollectionUtils.isEmpty(userOrderList)) {
                return message.add("bottomBe", Collections.emptyMap());
            }
            Set<String> products = userOrderList.stream().map(UserOrder::getProductId).collect(Collectors.toSet());
            Map<String, OrderProduct> orderProductMap = userOrderLoaderClient.loadOrderProducts(products);
            if (MapUtils.isEmpty(orderProductMap)) {
                return message.add("bottomBe", Collections.emptyMap());
            }
            //有没有购买过
            boolean buyOfficial = orderProductMap.values().stream()
                    .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                    .filter(e -> {
                        Map<String, Object> map = JsonUtils.fromJson(e.getAttributes());
                        if (MapUtils.isEmpty(map)) {
                            return false;
                        }
                        int grade = SafeConverter.toInt(map.get("grade"));
                        if (grade > 0) {
                            return true;
                        }
                        return false;
                    })
                    .findFirst().orElse(null) != null;
            //课程结束日期
            Date endDate = orderProductMap.values().stream()
                    .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                    .filter(e -> {
                        Map<String, Object> map = JsonUtils.fromJson(e.getAttributes());
                        if (MapUtils.isEmpty(map)) {
                            return false;
                        }
                        int grade = SafeConverter.toInt(map.get("grade"));
                        if (grade <= 0) {
                            return true;
                        }
                        return false;
                    })
                    .findFirst()
                    .map(e -> JsonUtils.fromJson(e.getAttributes()))
                    .map(e -> SafeConverter.toDate(e.get("endDate")))
                    .orElse(null);
            return message.add("bottomBe", StringUtils.isBlank(unitId) ? bottomAdvice(buyOfficial, endDate) : Collections.emptyMap());
        } else {
            message.add(RES_RESULT, "400");
            message.add(RES_MESSAGE, message.getInfo());
            return message;
        }
    }


    // 课程详情
    @RequestMapping(value = "/1.0/classdetail.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage classDetail() {
        String unitId = getRequestString("unitId");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("param error!");
        }
        MapMessage message = aiLoaderClient.getRemoteReference().loadClassDetail(user, unitId);
        if (message.isSuccess()) {
            message.add(RES_RESULT, "success");
            return message;
        } else {
            message.add(RES_RESULT, "400");
            message.add(RES_MESSAGE, message.getInfo());
            return message;
        }
    }

    // 课程详情
    @RequestMapping(value = "/1.0/lesson/questions.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage questions() {
        String id = getRequestString("id");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("param error!");
        }
        try {
            return aiLoaderClient.getRemoteReference().loadQuestions(user, id);
        } catch (Exception e) {
            logger.error("loadQuestions error. lessonId :{} ,userId:{}", id, user.getId(), e);
            return MapMessage.errorMessage("服务器异常").set("result", "404");
        }

    }

    // 提交答题结果
    @RequestMapping(value = "/1.0/processresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage processResult() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("param error!");
        }
        String json = getRequestString("data");
        AIUserQuestionResultRequest request = JsonUtils.fromJson(json, AIUserQuestionResultRequest.class);
        if (request == null) {
            return MapMessage.errorMessage("error data!").add(RES_RESULT, "400");
        }
        if (CollectionUtils.isNotEmpty(request.getWeekPoints())) {
            for (QuestionWeekPoint weekPoint : request.getWeekPoints()) {
                String content = weekPoint.getContent();
                try {
                    WordStock right = wordStockLoaderClient.loadWordStocksByEntext(content).stream()
                            .filter(e -> StringUtils.isNotBlank(e.getAudioUS()))
                            .findFirst().orElse(null);
                    if (right == null) {
                        continue;
                    }
                    weekPoint.setSuggestUrl(right.getAudioUS());
                } catch (Exception e) {
                    logger.error("loadWordStocksByEntext error. content:{}", content, e);
                }

            }
        }
        AIUserQuestionContext context = new AIUserQuestionContext();
        context.setData(json);
        context.setAiUserQuestionResultRequest(request);
        context.setUser(user);
        MapMessage result = new MapMessage();
        try {
            MapMessage message = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("processAIQuestionResult")
                    .keys(user.getId(), request.getQid())
                    .callback(() -> aiServiceClient.getRemoteReference().processAIQuestionResult(context))
                    .build()
                    .execute();
            if (message.isSuccess()) {
                message.add(RES_RESULT, "success");
                return message;
            } else {
                message.add(RES_RESULT, "400");
                message.add(RES_MESSAGE, message.getInfo());
                return message;
            }
        } catch (CannotAcquireLockException ex) {
            result.add(RES_RESULT, "400");
            result.add(RES_MESSAGE, "正在处理中");
            return result;
        } catch (Exception ex) {
            logger.error("processAIQuestionResult error. studentId:{}, cause: {}", user.getId(), ex.getMessage(),ex);
            result.add(RES_RESULT, "400");
            result.add(RES_MESSAGE, "服务异常");
            return result;
        }
    }

    // 提交视频结果
    @RequestMapping(value = "/1.0/question/video.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage questionVideoResult(@RequestParam(value = "qid", required = false) String qid,
                                          @RequestParam(value = "lessonId", required = false) String lessonId,
                                          @RequestParam(value = "video", required = false) String[] videos) {
        User user = currentUser();
        if (user == null || StringUtils.isBlank(qid) || videos == null || videos.length <= 0) {
            return MapMessage.errorMessage("param error!").add(RES_RESULT, "400").add(RES_MESSAGE, "参数异常");
        }

        MapMessage result = new MapMessage();
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("processAIQuestionResult")
                    .keys(user.getId(), qid)
                    .callback(() -> aiServiceClient.getRemoteReference().handleUserVideo(user, lessonId, Arrays.asList(videos)))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            result.add(RES_RESULT, "400");
            result.add(RES_MESSAGE, "正在处理中");
            return result;
        } catch (Exception ex) {
            logger.error("questionVideoResult error. studentId:{}, qid:{}, video:{}", user.getId(), qid, videos, ex);
            result.add(RES_RESULT, "400");
            result.add(RES_MESSAGE, "服务异常");
            return result;
        }
    }

    // 课程总结
    @RequestMapping(value = "/1.0/lessonresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage lessonResult() {
        String lessonId = getRequestString("lessonId");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(lessonId)) {
            return MapMessage.errorMessage("param error!");
        }
        return aiLoaderClient.getRemoteReference().loadLessonResult(user, lessonId);
    }

    // 单元总结
    @RequestMapping(value = "/1.0/unitresult.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage unitResult() {
        String unitId = getRequestString("unitId");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("param error!");
        }
        return aiLoaderClient.getRemoteReference().loadUnitResult(user.getId(), unitId);
    }

    /**
     * @see ChipsEnglishContentController#unitPlay()
     */
    @Deprecated
    // 我的剧本
    @RequestMapping(value = "/1.0/lesson/play.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage lessonPlay() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("没有登录");
        }

//        List<ChipEnglishInvitation> chipEnglishInvitations = aiLoaderClient.getRemoteReference().loadInvitationByInviterId(user.getId());
//        if (CollectionUtils.isEmpty(chipEnglishInvitations)) {
//            return MapMessage.errorMessage("没有权限");
//        }
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.errorMessage("参数异常");
        }

        return aiLoaderClient.getRemoteReference().loadLessonPlay(id);
    }
    // 任务地图
    @RequestMapping(value = "/1.0/maplist.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage mapList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("param error!");
        }
        return aiLoaderClient.getRemoteReference().loadUserMapList(user.getId());
    }
    /**
    　* @Description: app视频页面
    　* @author zhiqi.yao
    　* @date 2018/4/20 16:15
    */
    @RequestMapping(value = "/1.0/videos.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage vides() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no user!");
        }
        MapMessage message = aiLoaderClient.getRemoteReference().loadVideo(user.getId());
        if (message.isSuccess()) {
            message.add(RES_RESULT, "success");
            return message;
        } else {
            message.add(RES_RESULT, "400");
            message.add(RES_MESSAGE, message.getInfo());
            return message;
        }
    }
    /**
    　* @Description: app视频页面讲义列表
    　* @author zhiqi.yao
    　* @date 2018/4/23 11:02
    */
    @RequestMapping(value = "/1.0/handouts.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage handoutsList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no user!");
        }
        MapMessage message = aiLoaderClient.getRemoteReference().loadHandoutsList(user.getId());
        if (message.isSuccess()) {
            message.add(RES_RESULT, "success");
            return message;
        } else {
            message.add(RES_RESULT, "400");
            message.add(RES_MESSAGE, message.getInfo());
            return message;
        }
    }

    @RequestMapping(value = "/1.0/orderstatus.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage orderStatus() {
        User user = currentUser();
        if (user == null) {
            return failMessage("no user");
        }
        return wrapper(me -> me.putAll(chipsOrderProductLoader.loadOnSaleShortLevelProductInfo(user.getId())));
    }

    @RequestMapping(value = "/1.0/warmup/collect.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage warmUpCollect() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }

        String input = getRequestString("input");
        String lessonId = getRequestString("lessonId");
        String unitId = getRequestString("unitId");
        String qId = getRequestString("qId");
        if (StringUtils.isAnyBlank(input, lessonId,lessonId,unitId,qId)) {
            return MapMessage.errorMessage("input param error!").set("result", "400");
        }

        MapMessage message =aiServiceClient.getRemoteReference().collectWarmUpResult(user, input, lessonId,unitId,qId);
        if (message.isSuccess()) {
            message.add(RES_RESULT, "success");

        } else {
            message.add(RES_RESULT, "400");
            message.add(RES_MESSAGE, message.getInfo());

        }
        return message;
    }


    // 情景对话
    @RequestMapping(value = "/dialogue/scene.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage dialogueScene() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }

        String input = getRequestString("input");
        String userid = getRequestString("userid");
        if (StringUtils.isAnyBlank(input, userid)) {
            return MapMessage.errorMessage("input param error!").set("result", "400");
        }
        String lessonId = getRequestString("lessonId");
        return aiServiceClient.loadAndRecordDialogueTalk(user, userid, input, lessonId);
    }
    //任务对话
    @RequestMapping(value = "/dialogue/task.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage taskScene() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }

        String input = getRequestString("input");
        String name = getRequestString("name");
        String userid = getRequestString("userid");
        if (StringUtils.isAnyBlank(input, userid, name)) {
            return MapMessage.errorMessage("input or name param error!").set("result", "400");
        }
        String lessonId = getRequestString("lessonId");
        return aiServiceClient.loadAndRecordTaskTalk(user, userid, input, name, lessonId);
    }

    //我的班级
    @RequestMapping(value = "/clazz/my.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage myVitualClazz() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }

        String orderId = getRequestString("orderId");
        ChipsEnglishClass chipsEnglishClass;
        String mobile = "";
        if (StringUtils.isNotBlank(orderId)) {
            UserOrder userOrder = userOrderLoaderClient.loadUserOrder(orderId);
            if (user.isStudent()) {
                List<StudentParent> studentParentList = parentLoaderClient.loadStudentParents(user.getId());
                if (CollectionUtils.isEmpty(studentParentList) || userOrder == null ||
                        !studentParentList.stream().map(StudentParent::getParentUser).map(User::getId).collect(Collectors.toSet()).contains(userOrder.getUserId())) {
                    return MapMessage.errorMessage("订单号错误").set("result", "400");
                }
            }

            chipsEnglishClass = Optional.ofNullable(userOrder)
                    .map(e -> {
                        List<UserOrderProductRef> userOrderProductRefs = userOrderLoaderClient.loadOrderProducts(e.getUserId(), e.getId());

                        if (CollectionUtils.isEmpty(userOrderProductRefs)) {
                            return e.getProductId();
                        }
                        return userOrderProductRefs.get(0).getProductId();
                    })
                    .map(e -> chipsEnglishClazzService.loadClazzIdByUserAndProduct(userOrder.getUserId(), e))
                    .orElse(null);
            mobile = Optional.ofNullable(userOrder)
                    .map(UserOrder::getUserId)
                    .map(sensitiveUserDataServiceClient::loadUserMobile)
                    .filter(e -> StringUtils.isNotBlank(e) && e.length() > 7)
                    .map(e -> e.substring(0, 3) + "****" + e.substring(e.length() - 4, e.length()))
                    .orElse("");
        } else {
            chipsEnglishClass = chipsEnglishClazzService.loadMyDefaultClass(user.getId());
        }

        String wxCode = Optional.ofNullable(chipsEnglishClass)
                .map(ChipsEnglishClass::getTeacherInfo)
                .map(ChipsEnglishTeacher::getWxCode)
                .orElse("");
        String qrCode = Optional.ofNullable(chipsEnglishClass)
                .map(ChipsEnglishClass::getTeacherInfo)
                .map(ChipsEnglishTeacher::getQrImage)
                .orElse("");
        String companyQrCode = Optional.ofNullable(chipsEnglishClass)
                .map(ChipsEnglishClass::getTeacherInfo)
                .map(ChipsEnglishTeacher::getCompanyQrImage)
                .orElse("");
        String techerImage = Optional.ofNullable(chipsEnglishClass)
                .map(ChipsEnglishClass::getTeacherInfo)
                .map(te -> chipsEnglishClazzLoader.loadChipsEnglishTeacherByName(te.name()).stream().findFirst().orElse(null))
                .map(AiChipsEnglishTeacher::getHeadPortrait)
                .orElse("");
        return MapMessage.successMessage()
                .add("clazz", chipsEnglishClass != null && "Winston".equalsIgnoreCase(chipsEnglishClass.getTeacher()) ? 2 : 1)
                .add("wxCode", wxCode)
                .add("mobile", mobile)
                .add("companyQrCode", companyQrCode)
                .add("teacherAvatar", techerImage)
                .add("teacherName", Optional.ofNullable(chipsEnglishClass).map(ChipsEnglishClass::getTeacherInfo).map(ChipsEnglishTeacher::name).orElse(""))
                .add("qrCode", qrCode);
    }

    //我的开课信息
    @RequestMapping(value = "/course/info.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage courseInfo() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }
        Date now = new Date();
        Set<String> products = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId()).stream().map(UserOrder::getProductId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(products)) {
            return MapMessage.successMessage().set("beginDate", DateUtils.dateToString(now, "MM月dd日"));
        }

        String productId = Optional.ofNullable(aiOrderProductService.loadUserBookRef(user.getId()))
                .map(e -> userOrderLoaderClient.loadOrderProductById(e.getProductId()))
                .map(o -> o.getId())
                .orElse(null);

        Date beginDate = null;

        if (!StringUtils.isEmpty(productId)) {
            ChipsEnglishProductTimetable timetable = chipsEnglishContentLoader.loadChipsEnglishProductTimetableById(productId);
            if (timetable != null) {
                beginDate = timetable.getBeginDate();
            }
        }

//        Date beginDate = Optional.ofNullable(aiOrderProductService.loadUserBookRef(user.getId()))
//                .map(e -> userOrderLoaderClient.loadOrderProductById(e.getProductId()))
//                .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
//                .map(e -> JsonUtils.fromJson(e.getAttributes()))
//                .filter(e -> MapUtils.isNotEmpty(e))
//                .map(e -> SafeConverter.toDate(e.get("beginDate")))
//                .orElse(null);
        return MapMessage.successMessage().set("beginDate", beginDate != null ? DateUtils.dateToString(beginDate, "MM月dd日") : "");
    }

    //正价课产品列表
    @RequestMapping(value = "/product/official/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage productList() {
        User user = currentUser();
        if (user == null) {
            return MapMessage.errorMessage("no login!").set("result", "400");
        }
        List<OrderProduct> orderProductList = userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream()
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                .filter(e -> {
                    Map<String, Object> map = JsonUtils.fromJson(e.getAttributes());
                    if (MapUtils.isEmpty(map)) {
                        return false;
                    }
                    Date beginDate = SafeConverter.toDate(map.get("beginDate"));
                    if (beginDate == null) {
                        return false;
                    }
                    if (SafeConverter.toInt(map.get("grade")) > 0) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderProductList)) {
            return MapMessage.successMessage().add("productList", Collections.emptyList());
        }
        AppPayMapper appPayMapper = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.ChipsEnglish.name(), user.getId(), true);

        Set<String> productIds = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user.getId()).stream().map(UserOrder::getProductId).collect(Collectors.toSet());
        Map<String, List<OrderProductItem>> productItemMap = userOrderLoaderClient.loadProductItemsByProductIds(orderProductList.stream().map(OrderProduct::getId).collect(Collectors.toSet()));

        List<Map<String, Object>> list = new ArrayList<>();
        for(OrderProduct orderProduct : orderProductList) {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", orderProduct.getId());
            map.put("productName", orderProduct.getName());
            map.put("price", orderProduct.getPrice());
            map.put("originalPrice", orderProduct.getOriginalPrice());
            map.put("beginDate", SafeConverter.toDate(JsonUtils.fromJson(orderProduct.getAttributes()).get("beginDate")));
            map.put("endDate", SafeConverter.toDate(JsonUtils.fromJson(orderProduct.getAttributes()).get("endDate")));
            boolean buy = false;
            if (productIds.contains(orderProduct.getId())) {
                buy = true;
            }
            if (!buy && MapUtils.isNotEmpty(productItemMap) && CollectionUtils.isNotEmpty(productItemMap.get(orderProduct.getId()))) {
                List<OrderProductItem> productItems = productItemMap.get(orderProduct.getId());
                buy = appPayMapper != null &&
                        productItems.stream().filter(e -> appPayMapper.getValidItems().contains(e.getId())).findFirst().orElse(null) != null;
            }
            map.put("bought", buy);
            list.add(map);
        }
        return MapMessage.successMessage().add("product", list);
    }

    private Map<String, Object> bottomAdvice(boolean buyOffical, Date endDate) {
//        if (buyOffical || endDate == null) {
//            return Collections.emptyMap();
//        }
//        Date now = new Date();
//        if (endDate != null && endDate.before(now)) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("image", "http://cdn.17zuoye.com/fs-resource/5b6be1bf498ca48907e24eca.png");
//            map.put("linkUrl", "https://wechat.test.17zuoye.net/chips/center/robinnormal.vpage");
//            return map;
//        }
//        if (DateUtils.addDays(now, 3).after(endDate)) {
//            Map<String, Object> map = new HashMap<>();
//            map.put("image", "http://cdn.17zuoye.com/fs-resource/5b6be1bf498ca48907e24eca.png");
//            map.put("linkUrl", "https://wechat.test.17zuoye.net/chips/center/robinnormal.vpage");
//            return map;
//        }
        return Collections.emptyMap();
    }


    // 今日总结页面 打卡分享成功后回调，
    // 记录用户打卡分享记录。
    @RequestMapping(value = "/recordshare.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage recordShare() {
        String unitId = getRequestString("unitId");
        User user = currentUser();
        if (user == null || StringUtils.isBlank(unitId)) {
            return MapMessage.errorMessage("param error!");
        }
        MapMessage result = new MapMessage();
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("recordUserShare")
                    .keys(user.getId())
                    .callback(() -> aiServiceClient.getRemoteReference().recordUserShare(unitId, user))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            result.add(RES_RESULT, "400");
            result.add(RES_MESSAGE, "正在处理中");
            return result;
        }
    }

    // 给用户补打卡单元 未经准许， 请勿使用
    @RequestMapping(value = "/recordsharedoor.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage recordShareDoor() {
        String unitId = getRequestString("unitId");
        String bookId = getRequestString("bookId");
        User user = currentUser();
        if (user == null || StringUtils.isAnyBlank(unitId, bookId)) {
            return MapMessage.errorMessage("param error!");
        }
        MapMessage result = new MapMessage();
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("recordUserShare")
                    .keys(user.getId())
                    .callback(() -> aiServiceClient.getRemoteReference().recordUserShareDoor(bookId, unitId, user))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            result.add(RES_RESULT, "400");
            result.add(RES_MESSAGE, "正在处理中");
            return result;
        }
    }
}
