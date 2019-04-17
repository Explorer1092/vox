package com.voxlearning.utopia.service.ai.internal;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.ai.cache.manager.*;
import com.voxlearning.utopia.service.ai.constant.ChipsMiniProgramConfig;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserQuestionResultHistoryDao;
import com.voxlearning.utopia.service.ai.impl.service.AiChipsEnglishConfigServiceImpl;
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.ai.support.MessageConfig;
import com.voxlearning.utopia.service.ai.util.CollectionExtUtil;
import com.voxlearning.utopia.service.ai.util.StringExtUntil;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * @author guangqing
 * @since 2018/9/17
 */
@Named
public class ChipsContentService {
    private final static Logger logger = LoggerFactory.getLogger(ChipsContentService.class);

    @Inject
    private AIUserQuestionResultHistoryDao aiUserQuestionResultHistoryDao;

    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private AiChipsEnglishConfigServiceImpl chipsEnglishConfigService;

    @Inject
    private CommonConfigServiceClient commonConfigServiceClient;

    @Inject
    private ChipsProductUserCountCacheManager chipsProductUserCountCacheManager;

    @Inject
    private ChipsGradeRankCacheManager chipsGradeRankCacheManager;

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private ChipsTalkLowLevelCountCacheManager chipsTalkLowLevelCountCacheManager;

    @Inject
    private ChipsClazzG3UserCountCacheManager chipsClazzG3UserCountCacheManager;

    @Inject
    private ChipsMiniProgramTokenCacheManager chipsMiniProgramTokenCacheManager;

    private static String SCORE_LIMIT = "CHIPS_ENGLISH_TALK_LIMIT_SCORE";

    private static String URL = WechatConfig.getBaseSiteUrl() + "/chips/center/planmethod.vpage?id=";

    private final static String PRODUCT_NUMBER = "product_number_cfg";

    private final static String GRADE_REPORT_CONFIG = "chips_grade_report_cfg";

    private final static String RECOMMEND_PRODUCT = "recommend_grade_product_cfg";

    private final static String OFFICIAL_PRODUCT_AD = "official_product_ad_cfg_";

    private final static String PRODUCT_CLASS_CFG = "product_class_cfg_";

    private final static String TRAIL_UNIT_CFG = "chips_trial_unit_cfg";

    private final static String BOOK_MAP = "book_unit_map_cfg";

    private final static String BOOK_MUTEX_MAP = "book_mutex_map_cfg";

    private final static String SHORT_PRODUCT_AD_PATH = "short_product_ad_path";

    private final static String SHORT_PRODUCT_TOTAL = "short_product_total_number";

    private final static String SHORT_TRAVEL_PRODUCT_CONFIG = "short_travel_product_v4_config";

    private final static String MINI_PROGRAM_TRIAL = "mini_program_trail_book_cfg";

    private final static String DRAWING_TAB_CONFIG = "chips_drawing_tab_cfg";

    private final static String DRAWING_TASK_SHARE_CONFIG = "chips_drawing_share_task_v2_cfg";

    private final static String DRAWING_TASK_SHARE_QR_CONFIG = "chips_drawing_share_task_fin_qr_cfg";

    private static String INTERLOCSTION_LEVEL_REQUEST_PATH = (RuntimeMode.current().gt(Mode.TEST) ? "http://dialogue.17zuoye.com/aiteacher/question" : "http://10.7.13.75:31001/aiteacher/question");

    private final static String CHIPS_ACTIVITY_CFG = "chips_activity_";

    private static String MINI_PROGRAM_QR_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={}&secret={}";

    private static List<String> G2_BOOKS = Arrays.asList("SD_10300000023404", "SD_10300001567359", "SD_10300000028061");

    private static List<String> G3_BOOKS = Arrays.asList("SD_10300000029165", "SD_10300001568837", "SD_10300000027375");

    private static String SHORT_LABLE = "short";

    public List<GradeReportConfig> loadGradeReportConfig() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(GRADE_REPORT_CONFIG))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> JsonUtils.fromJsonToList(e, GradeReportConfig.class))
                .orElse(Collections.emptyList());
    }

    public List<DrawingTabConfig> loadUserDrawingTab(Long userId) {
        Set<String> userBooks = Optional.ofNullable(chipsUserService.loadUserEffectiveCourse(userId))
                .map(list -> list.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet()))
                .filter(CollectionUtils::isNotEmpty)
                .map(items -> userOrderLoaderClient.loadOrderProductItems(items))
                .filter(MapUtils::isNotEmpty)
                .map(itemsMap -> itemsMap.values().stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
        Map<String, List<String>> bookMutexMap = loadBookMutexMap();
        List<DrawingTabConfig> userCfg = new ArrayList<>();
        for (DrawingTabConfig cfg : loadDrawingTabConfig()) {
            boolean buy = CollectionExtUtil.hasIntersection(userBooks, cfg.getBooks());
            if (buy) {
                userCfg.add(cfg);
                continue;
            }
            if (SHORT_LABLE.equals(cfg.getLabelCode())) {
                continue;
            }
            //查看有没有购买同系列的
            boolean hasBuySame = false;
            for (String g3Book : cfg.getBooks()) {
                List<String> mutexBooks = bookMutexMap.get(g3Book);
                if (CollectionExtUtil.hasIntersection(userBooks, mutexBooks)) {
                    hasBuySame = true;
                    break;
                }
            }

            if (hasBuySame) {
                continue;
            }

            switch (cfg.getLabelCode()) {
                case "G1": //看看G2 G3有没有买
                    if (!CollectionExtUtil.hasIntersection(userBooks, G2_BOOKS) && !CollectionExtUtil.hasIntersection(userBooks, G3_BOOKS)) {
                        userCfg.add(cfg);
                    }
                    break;
                case "G2"://看看G3有没有买
                    if (!CollectionExtUtil.hasIntersection(userBooks, G3_BOOKS)) {
                        userCfg.add(cfg);
                    }
                    break;
                case "G3":
                    userCfg.add(cfg);
                    break;
            }
        }
        return userCfg;
    }

    public List<DrawingTabConfig> loadDrawingTabConfig() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(DRAWING_TAB_CONFIG))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> JsonUtils.fromJsonToList(e, DrawingTabConfig.class))
                .orElse(Collections.emptyList());
    }

    public Map<String, Object> loadDrawingTaskShare() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(DRAWING_TASK_SHARE_CONFIG))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(JsonUtils::fromJson)
                .orElse(Collections.emptyMap());
    }

    public Map<String, Object> loadDrawingTaskShareQRCfg() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(DRAWING_TASK_SHARE_QR_CONFIG))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(JsonUtils::fromJson)
                .orElse(Collections.emptyMap());
    }

    public List<RecommendProductConfig> loadRecommendProductConfig() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(RECOMMEND_PRODUCT))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> JSONObject.parseArray(e, RecommendProductConfig.class))
                .orElse(Collections.emptyList());
    }

    public int loadProductNumber(String productId) {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(PRODUCT_NUMBER))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> JsonUtils.fromJson(e))
                .filter(e -> MapUtils.isNotEmpty(e))
                .map(e -> SafeConverter.toInt(e.get(productId)))
                .orElse(0);
    }

    public int loadShortProductTotalNumber() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(SHORT_PRODUCT_TOTAL))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> SafeConverter.toInt(e))
                .orElse(0);
    }


    public List<ShortTravelProductConfig> loadShortProductConfig() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(SHORT_TRAVEL_PRODUCT_CONFIG))
                .map(ChipsEnglishPageContentConfig::getValue)
                .map(con -> JSONObject.parseArray(con, ShortTravelProductConfig.class))
                .orElse(Collections.emptyList());
    }


    public List<ShortTravelProductConfig> loadShortProductConfig(String type) {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(SHORT_TRAVEL_PRODUCT_CONFIG + (StringUtils.isBlank(type) ? "": ("_" + type))))
                .map(ChipsEnglishPageContentConfig::getValue)
                .map(con -> JSONObject.parseArray(con, ShortTravelProductConfig.class))
                .orElse(Collections.emptyList());
    }

    public int loadShortProductRank8And9SurplusNumber() {
        int number = loadShortProductTotalNumber();
        if (number <= 0) {
            return 0;
        }
        List<ShortTravelProductConfig> shortProductList = loadShortProductConfig();
        int userNumber = chipsProductUserCountCacheManager.getTotalCount(shortProductList.stream().map(ShortTravelProductConfig::getProductId).collect(Collectors.toList())).intValue();
        return number - userNumber;
    }

    public List<Map> loadOfficialConfig(String typeName) {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(OFFICIAL_PRODUCT_AD + typeName))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> JSONObject.parseArray(e, Map.class))
                .orElse(Collections.emptyList());
    }

    public Map<String, Object> loadActivityConfig(String typeName) {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(CHIPS_ACTIVITY_CFG + typeName))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> JsonUtils.fromJson(e))
                .orElse(Collections.emptyMap());
    }

    public Map<String, List<String>> loadBookMutexMap() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(BOOK_MUTEX_MAP))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> JsonUtils.fromJson(e))
                .map(e -> {
                    Map<String, List<String>> map = new HashMap<>();
                    e.entrySet().forEach(entry -> {
                        if (entry.getValue() instanceof List) {
                            map.put(entry.getKey(), (List<String>) entry.getValue());
                        }
                    });
                    return map;
                })
                .orElse(Collections.emptyMap());
    }

    public String shortProductPath(Long userId) {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(SHORT_PRODUCT_AD_PATH))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> {
                    List<String> selectList = Arrays.stream(e.split(",")).collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(selectList)) {
                        return "/view/mobile/parent/parent_ai/be";
                    }

                    if (userId == null) {
                        return "/view/mobile/parent/parent_ai/be";
                    }

                    int index = (int) Math.floorMod(userId, selectList.size());
                    index = Math.min(selectList.size() - 1, Math.max(index, 0));
                    return selectList.get(index);
                })
                .orElse("/view/mobile/parent/parent_ai/be");
    }

    public List<ProductClassConfig> loadProductClassConfig(String productId) {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(PRODUCT_CLASS_CFG + productId))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> JSONObject.parseArray(e, ProductClassConfig.class))
                .map(list -> list.stream().filter(e -> StringUtils.isNotBlank(e.getTeacher())).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public Set<String> loadTrailUnit() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(TRAIL_UNIT_CFG))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> Arrays.stream(e.split(",")).collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    public List<String> loadMiniProgramTrailBook() {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(MINI_PROGRAM_TRIAL))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> Arrays.stream(e.split(",")).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public boolean isTrailUnit(String unitId) {
        if (StringUtils.isBlank(unitId)) {
            return false;
        }
        Set<String> unitSet = loadTrailUnit();
        return CollectionUtils.isNotEmpty(unitSet) && unitSet.contains(unitId);
    }

    public String fetchBookMapUrl(String bookId) {
        if (StringUtils.isBlank(bookId)) {
            return "";
        }
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(BOOK_MAP))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(StringUtils::isNotBlank)
                .map(JsonUtils::fromJson)
                .map(e -> {
                    String path = SafeConverter.toString(e.get(bookId));
                    if (StringUtils.isNotBlank(path)) {
                        return path;
                    }
                    return SafeConverter.toString(e.get("default"));
                }).filter(StringUtils::isNotBlank)
                .map(e -> ProductConfig.getMainSiteBaseUrl().replace("http:", "https:") + e)
                .orElse("");
    }

    public void sendDailySummaryTemplateMessage(String enInfo, String cnInfo, Long userId, String unitId, String bookId) {

        Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
        templateDataMap.put("first", new WechatTemplateData("学习总结来啦，快来看看宝贝的口语学习排行吧！\n\r", "#FF6551"));
        templateDataMap.put("keyword1", new WechatTemplateData(enInfo + " " + cnInfo, "#1BA9EF"));
        templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(new Date(), FORMAT_SQL_DATE), "#1BA9EF"));
        templateDataMap.put("remark", new WechatTemplateData("\n\r→点击查看老师评语和排行", "#FF6551"));
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("url", URL + unitId + "&book=" + bookId);
        try {
            wechatServiceClient.getWechatService()
                    .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_STUDY_DAILY_SUMMARY.name(), templateDataMap, activityMap);
        } catch (Exception e) {
            logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
        }
    }

    public StoneSceneQuestionData.Feedback processFeedback(String input, StoneSceneQuestionData questionData, Long user) {
        StoneSceneQuestionData.Feedback feedback = doProcessFeedback(input, user, questionData);
        if (feedback == null) {
            feedback = new StoneSceneQuestionData.Feedback();
            feedback.setLevel("E");
        }
        //如果level是F则放入缓存中
        if (feedback.getLevel().matches("[e|f|E|F]\\S*")) {
            Long count = chipsTalkLowLevelCountCacheManager.increase(questionData.getId(), user);
            if (count == 2L) { //第二次看一下有无F5级别的
                for (StoneSceneQuestionData.Feedback fe : questionData.getJsonData().getJsgf_content()) {
                    if ("F5".equalsIgnoreCase(fe.getLevel()) && StringUtils.isNoneBlank(fe.getVideo())) {
                        return fe;
                    }
                }
            }
            if (count > 2L) {
                feedback = new StoneSceneQuestionData.Feedback();
                feedback.setLevel("E");
            }
        }
        return feedback;
    }

    private StoneSceneQuestionData.Feedback doProcessFeedback(String input, Long userId, StoneSceneQuestionData questionData) {
        if (questionData == null || questionData.getJsonData() == null ||
                CollectionUtils.isEmpty(questionData.getJsonData().getJsgf_content())) {
            return null;
        }

        Map<String, Object> map = fetchLevelResult(questionData.getId(), input, userId);
        if (MapUtils.isEmpty(map) || !"success".equalsIgnoreCase(SafeConverter.toString(map.get("result")))) {
            return null;
        }

        String level = Optional.of(map).map(e -> JsonUtils.toJson(e.get("data")))
                .map(JsonUtils::fromJson)
                .map(e -> SafeConverter.toString(e.get("level")))
                .orElse("");
        if (StringUtils.isBlank(level)) {
            return null;
        }

        return questionData.getJsonData().getJsgf_content().stream().filter(e -> level.equalsIgnoreCase(e.getLevel())).findFirst().orElse(null);
    }

    public Map<String, Object> fetchLevelResult(String qid, String input, Long userId) {
        String userContent = fetchUserContent(input);
        try {
            long begin = System.currentTimeMillis();
            Map<Object, Object> map = new HashMap<>();
            map.put("question_id", qid);
            map.put("input", userContent);
            String response = HttpRequestExecutor.defaultInstance()
                    .post(INTERLOCSTION_LEVEL_REQUEST_PATH)
                    .headers(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"))
                    .addParameter(map)
                    .execute().getResponseString();

            if (RuntimeMode.current().lt(Mode.PRODUCTION)) {
                logger.info("aiUserTalkInteractV2  qid:{}, user:{}, userContent:{}, response:{}, input:{}", qid, userId,
                        userContent, response, input);
            }

            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", userId,
                    "mod1", qid,
                    "mod2", userContent,
                    "mod3", response,
                    "mod4", input,
                    "mod5", (System.currentTimeMillis() - begin),
                    "op", "aiUserTalkInteractV2"
            ));
            return JsonUtils.fromJson(response);
        } catch (Exception e) {
            logger.error("fetchLevelResult error, qid:{}, userContent:{}", qid, userContent, e);
            return Collections.emptyMap();
        }
    }

    private String fetchUserContent(String input) {
        int limitScore = talkInteractLimitScore();
        return Optional.ofNullable(JsonUtils.fromJson(input, AIQuestionAppraisionRequest.class))
                .filter(e -> CollectionUtils.isNotEmpty(e.getLines()))
                .map(e -> e.getLines().stream().filter(e1 -> e1.getStandardScore() != null).filter(e1 -> StringUtils.isNotBlank(e1.getSample())).findFirst().orElse(null))
                .map(e -> {
                    if (e.getStandardScore().compareTo(new BigDecimal(limitScore)) > 0) {
                        return e.getSample();
                    }
                    return null;
                }).orElse("");
    }

    public AIUserBookResult initBookResult(User user, List<AIUserUnitResultPlan> aiUserUnitResultPlans, String bookId) {
        GradeReportConfig gradeReportConfig = loadGradeReportConfig().stream().filter(e -> e.getBook().equals(bookId)).findFirst().orElse(null);
        if (gradeReportConfig == null || CollectionUtils.isEmpty(gradeReportConfig.getUnits())) {
            return null;
        }
        AIUserBookResult aiUserBookResult = new AIUserBookResult();
        aiUserBookResult.setBookId(bookId);
        aiUserBookResult.setUpdateDate(new Date());
        aiUserBookResult.setCreateDate(new Date());
        aiUserBookResult.setUserId(user.getId());
        aiUserBookResult.setId(AIUserBookResult.generateId(user.getId(), bookId));
        aiUserBookResult.setDiaglogue(new ArrayList<>());
        aiUserBookResult.setTask(new ArrayList<>());
        aiUserBookResult.setWarmUp(new ArrayList<>());
        //生成等级
        processBookResultLevel(gradeReportConfig, aiUserBookResult, user.getId(), aiUserUnitResultPlans);
        //生成总结
        processBookResultSummary(aiUserBookResult, user, aiUserUnitResultPlans);

        return aiUserBookResult;
    }

    private void processBookResultLevel(GradeReportConfig gradeReportConfig, AIUserBookResult aiUserBookResult, Long userId, List<AIUserUnitResultPlan> aiUserUnitResultPlans) {
        AIUserBookResult.Level level = AIUserBookResult.Level.One;
        Set<String> unitList = gradeReportConfig.getUnits().subList(0, Math.min(gradeReportConfig.getUnits().size(), 4)).stream().collect(Collectors.toSet());
        int w_score = processScoreByLessonType(userId, unitList, LessonType.WarmUp, LessonType.warm_up);
        int t_score = processScoreByLessonType(userId, unitList, LessonType.Task, LessonType.task_conversation);
        int d_score = processScoreByLessonType(userId, unitList, LessonType.Dialogue, LessonType.video_conversation);
        int level_score;
        int grade2Score = Optional.ofNullable(gradeReportConfig).map(GradeReportConfig::getG2Score).orElse(60);
        int grade3Score = Optional.ofNullable(gradeReportConfig).map(GradeReportConfig::getG3Score).orElse(90);
        if ((level_score = t_score + d_score) > grade2Score * 2) {
            switch (aiUserBookResult.getBookId()) {
                case "SD_10300001253782":
                    if (level_score <= grade3Score * 2) {
                        ChipsEnglishClass chipsEnglishClass = chipsUserService.loadClazzByUserAndBook(userId, aiUserBookResult.getBookId());
                        if (chipsEnglishClass == null) {
                            break;
                        }
                        List<ChipsRank> chipsRankList = chipsGradeRankCacheManager.getRankList(chipsEnglishClass.getId(), 500);
                        int rank = processScoreRankNumber(chipsRankList, userId);
                        if (rank * 5 <= chipsRankList.size() * 2) {
                            level = AIUserBookResult.Level.Two;
                        }
                    } else {
                        level = AIUserBookResult.Level.Two;
                    }
                    break;
                case "SD_10300001055259":
                case "BK_10300003451674":
                    level = AIUserBookResult.Level.Two;
                    if (level_score > grade3Score * 2) { //等级3：> G3 score && in clazz radio
                        ChipsEnglishClass chipsEnglishClass = chipsUserService.loadClazzByUserAndBook(userId, aiUserBookResult.getBookId());
                        if (chipsEnglishClass == null) {
                            break;
                        }
                        int radio = Optional.ofNullable(gradeReportConfig).map(GradeReportConfig::getG3radioPer100).orElse(5);
                        Long count = chipsClazzG3UserCountCacheManager.increase(chipsEnglishClass.getId());
                        List<ChipsEnglishClassUserRef> users = chipsUserService.selectChipsEnglishClassUserRefByClazzId(chipsEnglishClass.getId());
                        if (count.intValue() * 100 <= users.size() * radio) {
                            level = AIUserBookResult.Level.Three;
                        }
                    }
                    break;
            }
        }
        aiUserBookResult.setLevel(level);
        List<LessonScoreBean> beans = new ArrayList<>();
        beans.add(new LessonScoreBean(LessonType.WarmUp, w_score));
        beans.add(new LessonScoreBean(LessonType.Task, t_score));
        beans.add(new LessonScoreBean(LessonType.Dialogue, d_score));
        beans.sort(Comparator.comparing(LessonScoreBean::getScore));
        List<LessonType> points = new ArrayList<>();
        switch (level) {
            case One:
                points.addAll(Arrays.asList(LessonType.WarmUp, LessonType.Task, LessonType.Dialogue));
                break;
            case Two:
                points.addAll(Arrays.asList(beans.get(0).getType(), beans.get(1).getType()));
                break;
            case Three:
                points.add(beans.get(0).getType());
                break;
        }
        int score = aiUserUnitResultPlans.stream().mapToInt(AIUserUnitResultPlan::getScore).sum() / aiUserUnitResultPlans.size();
        aiUserBookResult.setScore(score);
        List<WeekScoreBean> weekScoreBeans = new ArrayList<>();
        weekScoreBeans.add(new WeekScoreBean(AIUserBookResult.Week.CS, aiUserUnitResultPlans.stream().mapToInt(AIUserUnitResultPlan::getExpress).sum()));
        weekScoreBeans.add(new WeekScoreBean(AIUserBookResult.Week.G, aiUserUnitResultPlans.stream().mapToInt(AIUserUnitResultPlan::getFluency).sum()));
        weekScoreBeans.add(new WeekScoreBean(AIUserBookResult.Week.P, aiUserUnitResultPlans.stream().mapToInt(AIUserUnitResultPlan::getPronunciation).sum()));
        weekScoreBeans.sort(Comparator.comparing(WeekScoreBean::getScore));
        for (LessonType type : points) {
            switch (type) {
                case Dialogue:
                    aiUserBookResult.getDiaglogue().add(weekScoreBeans.get(0).getType());
                    break;
                case Task:
                    aiUserBookResult.getTask().add(weekScoreBeans.get(0).getType());
                    break;
                case WarmUp:
                    aiUserBookResult.getWarmUp().add(AIUserBookResult.Week.L);
                    break;
                default:
                    break;
            }
        }
    }

    private int processScoreRankNumber(List<ChipsRank> chipsRankList, Long userId) {
        int number = 0;
        if (CollectionUtils.isEmpty(chipsRankList)) {
            return number;
        }
        chipsRankList.sort(Comparator.comparing(ChipsRank::getNumber).reversed());
        for (ChipsRank rank : chipsRankList) {
            number ++;
            if (rank.getUserId().equals(userId)) {
                break;
            }
        }
        return number;
    }

    //计算第一次完成时的平均得分
    public int processScoreByLessonType(Long userId, Set<String> unitSet, LessonType... types) {
        int totalScore = 0;
        int size = 0;
        for (LessonType type : types) {
            Map<String, List<AIUserQuestionResultHistory>> questionResultHistories = aiUserQuestionResultHistoryDao.loadByUidAndType(userId, type).stream()
                    .filter(e -> StringUtils.isNotBlank(e.getBookId()))
                    .filter(e -> unitSet.contains(e.getUnitId()))
                    .filter(e -> e.getScore() != null && e.getScore() > 0)
                    .collect(Collectors.groupingBy(AIUserQuestionResultHistory::getQid));
            for (Map.Entry<String, List<AIUserQuestionResultHistory>> entry : questionResultHistories.entrySet()) {
                AIUserQuestionResultHistory history = entry.getValue().stream().sorted(Comparator.comparing(AIUserQuestionResultHistory::getCreateDate)).findFirst().orElse(null);
                if (history != null) {
                    int score = Math.max(0, ((history.getScore() != null ? history.getScore() : 0)));
                    totalScore += score;
                }
            }
            size += questionResultHistories.size();
        }
        return totalScore / (size == 0 ? 1 : size);
    }

    private void processBookResultSummary(AIUserBookResult aiUserBookResult, User user, List<AIUserUnitResultPlan> aiUserUnitResultPlans) {
        String userCnName = Optional.ofNullable(user.getProfile())
                .map(e -> e.getNickName())
                .filter(e -> StringUtils.isNotBlank(e))
                .orElse("你的孩子");

        String userEnName = Optional.ofNullable(user.getProfile())
                .map(e -> e.getNickName())
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> upperCase(StringExtUntil.getPinyinString(e)))
                .filter(e -> StringUtils.isNotBlank(e))
                .orElse("your kid");

        String enSu, cnSu;
        try {
            StringBuilder enSummary = new StringBuilder();
            StringBuilder cnSummary = new StringBuilder();
            String level = "";
            //整体评价
            switch (aiUserBookResult.getLevel()) {
                case One:
                    level = "Ⅰ";
                    enSummary.append(MessageFormat.format(MessageConfig.book_result_level_1_en, upperCase(userEnName)));
                    cnSummary.append(MessageFormat.format(MessageConfig.book_result_level_1_cn, userCnName));
                    break;
                case Three:
                    level = "Ⅲ";

                    enSummary.append(MessageFormat.format(MessageConfig.book_result_level_3_en, upperCase(userEnName)));
                    cnSummary.append(MessageFormat.format(MessageConfig.book_result_level_3_cn, userCnName));
                    break;
                case Two:
                    level = "Ⅱ";
                    enSummary.append(MessageFormat.format(MessageConfig.book_result_level_2_en, upperCase(userEnName)));
                    cnSummary.append(MessageFormat.format(MessageConfig.book_result_level_2_cn, userCnName));
                    break;
            }
            aiUserUnitResultPlans.sort(Comparator.comparing(AIUserUnitResultPlan::getScore));

            //语言点举例
            List<String> sentences = new ArrayList<>();
            List<String> sens1 = MessageConfig.getUnitSentence(aiUserUnitResultPlans.get(aiUserUnitResultPlans.size() - 1).getUnitId());
            sentences.addAll(sens1);
            List<String> sens2 = MessageConfig.getUnitSentence(aiUserUnitResultPlans.get(aiUserUnitResultPlans.size() - 2).getUnitId());
            sentences.addAll(sens2);

            //成绩最高的单元
            Random random = new Random();
            int b_ran1 = random.nextInt(sentences.size());
            String bestSentence1 = sentences.get(b_ran1);
            sentences.remove(b_ran1);
            int b_ran2 = random.nextInt(sentences.size());
            String bestSentence2 = sentences.get(b_ran2);
            //成绩最低的单元
            sentences.clear();
            List<String> sens3 = MessageConfig.getUnitSentence(aiUserUnitResultPlans.get(0).getUnitId());
            sentences.addAll(sens3);

            List<String> sens4 = MessageConfig.getUnitSentence(aiUserUnitResultPlans.get(1).getUnitId());
            sentences.addAll(sens4);

            int w_ran1 = random.nextInt(sentences.size());
            String worstSentence1 = sentences.get(w_ran1);
            sentences.remove(w_ran1);
            int w_ran2 = random.nextInt(sentences.size());
            String worstSentence2 = sentences.get(w_ran2);

            List<String> enParams = new ArrayList<>();
            enParams.add(upperCase(userEnName));
            enParams.add(MessageConfig.getUnitEnInfo(aiUserUnitResultPlans.get(aiUserUnitResultPlans.size() - 1).getUnitId()));
            enParams.add(MessageConfig.getUnitEnInfo(aiUserUnitResultPlans.get(aiUserUnitResultPlans.size() - 2).getUnitId()));
            enParams.add(MessageConfig.getUnitEnInfo(aiUserUnitResultPlans.get(0).getUnitId()));
            enParams.add(userEnName);
            enParams.addAll(Arrays.asList(bestSentence1, bestSentence2));
            enParams.addAll(Arrays.asList(worstSentence1, worstSentence2));
            enParams.add(userEnName);
            enParams.add(level);
            enSummary.append(MessageFormat.format(MessageConfig.book_result_task_en, enParams.toArray()));

            List<String> cnParams = new ArrayList<>();
            cnParams.add(userCnName);
            cnParams.add(MessageConfig.getUnitCnInfo(aiUserUnitResultPlans.get(aiUserUnitResultPlans.size() - 1).getUnitId()));
            cnParams.add(MessageConfig.getUnitCnInfo(aiUserUnitResultPlans.get(aiUserUnitResultPlans.size() - 2).getUnitId()));
            cnParams.add(MessageConfig.getUnitCnInfo(aiUserUnitResultPlans.get(0).getUnitId()));
            cnParams.add(userCnName);
            cnParams.addAll(Arrays.asList(bestSentence1, bestSentence2));
            cnParams.addAll(Arrays.asList(worstSentence1, worstSentence2));
            cnParams.add(userCnName);
            cnParams.add(level);
            cnSummary.append(MessageFormat.format(MessageConfig.book_result_task_cn, cnParams.toArray()));

            enSu = enSummary.toString();
            cnSu = cnSummary.toString();
        } catch (Exception e) {
            enSu = MessageFormat.format(MessageConfig.book_result_task_en_common, upperCase(userEnName), userEnName);
            cnSu = MessageFormat.format(MessageConfig.book_result_task_cn_common, userCnName, userCnName);
        }

        aiUserBookResult.setCnSummary(cnSu);
        aiUserBookResult.setEnSummary(enSu);
    }

    public int talkInteractLimitScore() {
        int limit = 2;
        try {
            String limitString = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), SCORE_LIMIT);
            int value = SafeConverter.toInt(limitString);
            limit = value > 0 ? value : limit;
        } catch (Exception e) {

        }
        return limit;
    }

    private String upperCase(String str) {
        if (StringUtils.isBlank(str) || str.length() < 2) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public String getMiniProgramToken() {
        String token = chipsMiniProgramTokenCacheManager.load();
        if (StringUtils.isNotBlank(token)) {
            return token;
        }
        String tokenUrl = StringUtils.formatMessage(MINI_PROGRAM_QR_TOKEN_URL, ChipsMiniProgramConfig.APP_ID, ChipsMiniProgramConfig.APP_SECRET);
        try {
            AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING)
                    .get(tokenUrl)
                    .socketTimeout(10000)
                    .execute();
            LogCollector.info("backend-general", MapUtils.map(
                    "env", RuntimeMode.getCurrentStage(),
                    "usertoken", 0L,
                    "mod1", response.getStatusCode(),
                    "mod2", response.getResponseString(),
                    "mod3", tokenUrl,
                    "op", "chipsMiniProgramGetToken"
            ));

            switch (response.getStatusCode()) {
                case 200: {
                    Map<String, Object> map = JsonUtils.fromJson(response.getResponseString());
                    token = SafeConverter.toString(map.get("access_token"));
                    int expire = SafeConverter.toInt(map.get("expires_in"));
                    int errorCode = SafeConverter.toInt(map.get("errcode"), 0);
                    if (errorCode != 0 || StringUtils.isBlank(token)) {
                       return "";
                    }
                    chipsMiniProgramTokenCacheManager.addRecord(token, expire);
                    break;
                }
                default: {
                    return "";
                }
            }
            return token;
        } catch (Exception e) {
            logger.error("getMiniProgramToken error. ", e);
            return "";
        }
    }

    @Getter
    @Setter
    private class LessonScoreBean {
        private LessonType type;
        private Integer score;

        public LessonScoreBean(LessonType type, Integer score) {
            this.type = type;
            this.score = score;
        }
    }

    @Getter
    @Setter
    private class WeekScoreBean {
        private AIUserBookResult.Week type;
        private Integer score;

        public WeekScoreBean(AIUserBookResult.Week type, Integer score) {
            this.type = type;
            this.score = score;
        }
    }


}
