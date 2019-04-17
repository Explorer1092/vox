package com.voxlearning.washington.controller.mobile.student;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.utopia.core.ObjectIdEntity;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankCategory;
import com.voxlearning.utopia.service.afenti.api.constant.PicBookRankType;
import com.voxlearning.utopia.service.afenti.api.data.PicBookPurchaseProp;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBook;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBookResult;
import com.voxlearning.utopia.service.afenti.api.mapper.PicBookContext;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookAchieve;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress;
import com.voxlearning.utopia.service.afenti.client.UserPicBookServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.content.api.mapper.NewClazzBookRefMapper;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.newhomework.api.entity.JournalNewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkQueueServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.api.service.UserOrderService;
import com.voxlearning.utopia.service.question.api.constant.ApplyToType;
import com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel;
import com.voxlearning.utopia.service.question.api.entity.BasePictureBook;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.api.mapper.QuestionScoreResult;
import com.voxlearning.utopia.service.question.api.mapper.UserAnswerMapper;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParent;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.api.mappers.NewAdMapper;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.washington.cache.WashingtonCacheSystem;
import com.voxlearning.washington.controller.open.AbstractStudentApiController;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;
import static com.voxlearning.alps.lang.util.MapMessage.errorMessage;
import static com.voxlearning.utopia.service.question.api.constant.PictureBookNewClazzLevel.L1B;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

/**
 * Controller of 英文绘本应用
 */
@Controller
@RequestMapping("/v2/studentMobile/levelreading")
public class MobilePicBookController extends AbstractStudentApiController {

    private static final int PAGE_SIZE = 18;
    private static final int HOME_MY_BOOK_LIST_SIZE = 15;
    private static final String picbook_u3d_mirror_host_config = "picbook_u3d_mirror_host_config";
    private static final String levelKey = "PicBookLevelConfig";
    private static final String levelKey2 = "level_list";

    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;
    @Inject private UserPicBookServiceClient userPicBookServiceClient;
    @Inject private PictureBookLoaderClient picBookLoaderCli;
    @Inject private NewClazzBookLoaderClient newClazzBookLoaderCli;
    @Inject private NewHomeworkQueueServiceClient homeworkQueueSrvCli;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;
    @Inject private WashingtonCacheSystem cs;

    @ImportService(interfaceClass = UserOrderService.class)
    private UserOrderService usrOrderSrv;

    @Getter
    @AlpsQueueProducer(queue = "galaxy.parent.wish.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer wishProducer;

    @PostConstruct
    public void prepareStressTesting() {

    }

    @RequestMapping(value = "/module.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadModule() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String bookId = getRequestString("bookId");
            Validate.notBlank(bookId, "绘本id不存在!");

            Integer module = getRequestInt("module");
            Validate.isTrue(module != 0, "模块为空!");

            validateRequest("bookId", "module");

            PictureBookPlus pictureBook = userPicBookServiceClient.loadPicBook(bookId, pictureBookPlusServiceClient);
            if (pictureBook == null)
                return errorMessage("绘本已下线，无法下载").setErrorCode(RES_RESULT_INTERNAL_ERROR_CODE);

            // 付费绘本校验状态
            boolean isCharge = Optional.ofNullable(pictureBook.getFreeMap())
                    .map(m -> m.getOrDefault(ApplyToType.SELF, 0) == 2)
                    .orElse(false);
            // 当前绘本是否拥有
            boolean isPay = true; // 免费绘本默认为true
            if (isCharge) {
                String orderProductType = getRequestString(REQ_APP_KEY);
                AppPayMapper payMapper = userOrderLoaderClient.getUserAppPaidStatus(orderProductType, currStu.getId());
                if (payMapper == null || !payMapper.containsAppItemId(bookId)) {
                    isPay = false;
                    // 当前无效 如果是第一模块可以放过，有试读， 其他模块直接拦截
                    if (module != 1) {
                        return errorMessage("绘本已退款，请刷新页面后重试~").setErrorCode(RES_RESULT_INTERNAL_ERROR_CODE);
                    }
                }
            }

            MapMessage resultMsg = userPicBookServiceClient.loadPicBookContentInModule(bookId, module, pictureBookPlusServiceClient);
            // 判断U3D版本是否支持排序题 填空题
            if (resultMsg.isSuccess() && module == 4) {
                String ver = getClientVersion();
                Collection<NewQuestion> questions = (Collection<NewQuestion>) resultMsg.get("content");
                if (CollectionUtils.isNotEmpty(questions)) {
                    if (StringUtils.isBlank(ver)) {
                        // 老版本 判断排序+填空
                        NewQuestion question = questions.stream().filter(q -> (q.getContent().getSubContents().stream()
                                .filter(qq -> qq.getSubContentTypeId() == 9 || qq.getSubContentTypeId() == 4 || qq.getSubContentTypeId() == 10).findFirst().orElse(null)) != null)
                                .findFirst().orElse(null);
                        if (question != null) {
                            return MapMessage.errorMessage("暂不支持该题型").setErrorCode(RES_RESULT_INTERNAL_ERROR_CODE);
                        }
                    } else {
                        // 新版本 判断填空题
                        NewQuestion question = questions.stream().filter(q -> (q.getContent().getSubContents().stream()
                                .filter(qq -> qq.getSubContentTypeId() == 4 || qq.getSubContentTypeId() == 10).findFirst().orElse(null)) != null)
                                .findFirst().orElse(null);
                        if (question != null) {
                            return MapMessage.errorMessage("暂不支持该题型").setErrorCode(RES_RESULT_UPGRADE_ERROR_CODE);
                        }
                    }

                }
            }

            // 模块的进度信息l
            UserPicBookProgress progress = userPicBookServiceClient.loadUserPicBookProgress(currStu.getId(), bookId);
            Map<String, Object> moduleDetail = Optional.ofNullable(progress)
                    .map(p -> p.getModule(module))
                    .orElse(new HashMap<>());

            resultMsg.add("module", moduleDetail).add("isPay", isPay);
            return resultMsg;
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("Get book module data error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    /**
     * 获得绘本详情信息Mapper
     *
     * @param userId      用户ID
     * @param typeStr     appKey
     * @param bookIds     绘本ID列表
     * @param cartItemIds 购物车ID列表
     * @return
     */
    private Function<PictureBookPlus, Map<String, Object>> getPicBookMapper(Long userId,
                                                                            String typeStr,
                                                                            Collection<String> bookIds,
                                                                            List<String> cartItemIds,
                                                                            PageBlockContent levelConfig) {
        // 正常的数据都是每个绘本只存在一个，为了容下错，用groupBy
        Map<String, List<UserPicBook>> userPicBookMap = userPicBookServiceClient.loadAllUserPicBooks(userId, typeStr)
                .stream()
                .collect(groupingBy(upb -> upb.getBookId()));
        // 是否已经加入购物体
        OrderProductServiceType type = OrderProductServiceType.safeParse(typeStr);
        List<String> carItemIds = Optional.ofNullable(cartItemIds).orElse(userPicBookServiceClient.loadShoppingCartBookIds(userId, type));
        // 获得用户所有的进度数据
        Map<String, UserPicBookProgress> progressMap = userPicBookServiceClient.loadAllUserPicBookProgress(userId);
        // 获得绘本对应的商品记录
        Map<String, List<OrderProduct>> productMap = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIds);
        // 获得绘本的主题
        Map<String, String> topicMap = picBookLoaderCli.loadAllPictureBookTopics()
                .stream()
                .collect(toMap(t -> t.getId(), t -> t.getName()));
        // 获得用户购买历史信息
        AppPayMapper payMapper = userOrderLoaderClient.getUserAppPaidStatus(typeStr, userId, true);

        // 付费绘本开通根据系列送的道具列表
//        Map<String, List<PicBookPurchaseProp>> propMap = userPicBookServiceClient.getRewardListByBookIds(new ArrayList<>(bookIds));

        return pb -> {
            String bookId = pb.getId();


            boolean isFree = userPicBookServiceClient.isFreePicBook(pb);
            boolean bought = payMapper != null && payMapper.containsAppItemId(bookId);

            UserPicBook userPicBook = Optional.ofNullable(userPicBookMap.get(bookId))
                    .orElse(emptyList())
                    .stream()
                    .findFirst()
                    .orElse(null);

            // 绘本的进度
            UserPicBookProgress progress = progressMap.get(bookId);
            boolean hadRead = (bought || isFree) && progress != null && progress.isModuleFinished(1);

            // 已经读过的书，要初始化其它模块的数据。
            // 把条件扩大，免费或者是已购买的都给详细
            if (isFree || bought) {
                if (progress == null) {
                    progress = new UserPicBookProgress();
                    progress.setUserId(userId);
                    progress.setBookId(bookId);
                }

                // 有的绘本可能没有习题模块
                int moduleNum = CollectionUtils.isEmpty(pb.getPracticeQuestions()) ? 3 : 4;
                progress.fill(moduleNum);
            }

            // 获得绘本的价格
            double price = Optional.ofNullable(productMap.get(bookId))
                    .orElse(emptyList())
                    .stream()
                    // 保留两位小数，向上约起
                    .map(op -> op.getPrice().setScale(2, RoundingMode.UP))
                    .map(BigDecimal::doubleValue)
                    .findFirst()
                    .orElse(0d);

            // 如果付费绘本的价格是零，说明还没有导入付费信息。过滤掉
            if (!isFree && price == 0) {
                return null;
            }

            // 分数
            int score = Optional.ofNullable(userPicBook).map(b -> b.getScore()).orElse(0);
            // 主题
            List<String> topicNames = Optional.ofNullable(pb.getTopicIds())
                    .orElse(emptyList())
                    .stream()
                    .map(topicMap::get)
                    .filter(Objects::nonNull)
                    .collect(toList());

            // 时长，各个模块算算，加加
            int spendTime = pb.getRecommendTime() + pb.getOralSeconds();
            spendTime += pb.allOftenUsedWords().size() * 10 + pb.getPracticeQuestions().size() * 20;
            spendTime = spendTime / 60 + 1;

            // 绘本的阅读等级
            String readLvl = pb.getNewClazzLevels()
                    .stream()
                    .map(cl -> userPicBookServiceClient.loadClazzLevelName(cl.name()))
                    .reduce((acc, item) -> acc + "," + item)
                    .orElse("");


            String seriesId = pb.getSeriesId();
            String bookType = "";
            // 付费绘本开通根据系列送的道具列表
            List<PicBookPurchaseProp> propList = new ArrayList<>();
//            if (!isFree) {
//                propList = propMap.get(pb.getId());
//            }
            if (levelConfig != null) {
                Map<String, Object> configMap = JsonUtils.convertJsonObjectToMap(levelConfig.getContent());
                if (configMap != null) {
                    List<String> jx = (List<String>) configMap.get("jx");
                    List<String> yz = (List<String>) configMap.get("yz");
                    List<String> tj = (List<String>) configMap.get("tj");

                    if (CollectionUtils.isNotEmpty(jx) && jx.contains(seriesId)) {
                        bookType = "精选绘本";
                    } else if (CollectionUtils.isNotEmpty(yz) && yz.contains(seriesId)) {
                        bookType = "优质绘本";
                    } else if (CollectionUtils.isNotEmpty(tj) && tj.contains(seriesId)) {
                        bookType = "推荐绘本";
                    }
                }
            }

            return MapUtils.m(
                    "hadRead", hadRead,
                    "bought", bought,
                    "free", isFree,
                    "id", pb.getId(),
                    "bookType", bookType,
                    "detail", progress,
                    "screenMode", pb.getScreenMode(),
                    "isSpellingPb", pb.getIsSpellingPb(),
                    "hadPractise", CollectionUtils.isNotEmpty(pb.getPracticeQuestions()),
                    "inShoppingCart", carItemIds.contains(bookId),
                    "keyWords", toInt(pb.getWordsCount()),
                    "totalWords", toInt(pb.getWordsLength()),
                    "spendTime", spendTime,
                    "coverUrl", replaceCdnHost(pb.getCoverThumbnailUrl()),
                    "coverUrlAndroid", pb.getCoverThumbnailUrlAndroid(),
                    "coverUrlIos", pb.getCoverThumbnailUrlIos(),
                    "coverUrlLarge", pb.getCoverUrl(),
                    //"materialUrl", pb.getMaterialUrl(),
                    "name", pb.getEname(),
                    "topicIds", topicNames,
                    "price", price,
                    "readLvl", readLvl,
                    "score", score,
                    "freePage", isFree ? 0 : 1,// 付费绘本如果没买可免费试读页数
                    "rewardList", propList);
        };
    }


    private String replaceCdnHost(String url) {
        String newHost = commonConfigServiceClient.getCommonConfigBuffer()
                .loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), picbook_u3d_mirror_host_config);
        if (StringUtils.isBlank(newHost)) {
            return url;
        }
        String regEx = "^http(s)?://(.*?)/"; //定义HTML标签的正则表达式
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.replaceAll(newHost) + "?x-oss-process=image/quality,q_75/resize,w_300";

    }


    @RequestMapping(value = "/books.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage books() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            String bookIdsStr = getRequestString("bookIds");
            Validate.notBlank(bookIdsStr, "非法参数!");

            validateRequest("bookIds");

            List<String> bookIds = Arrays.stream(bookIdsStr.split(",")).collect(toList());
            Function<PictureBookPlus, Map<String, Object>> mapFunc = getPicBookMapper(currStu.getId(), appKey, bookIds, null, getLevelConfigContent());
            List<Map<String, Object>> bookMapper = userPicBookServiceClient.loadPicBooks(bookIds, pictureBookPlusServiceClient)
                    .stream()
                    .map(mapFunc)
                    .collect(toList());

            return MapMessage.successMessage().add("books", bookMapper);
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    /**
     * 上报答题结果给大数据
     *
     * @param stuDetail
     * @param data
     * @param subject
     * @param appKey
     */
    private void reportToBigData(StudentDetail stuDetail,
                                 List<UserPicBookResult> data,
                                 Subject subject,
                                 String appKey) {
        try {
            GroupMapper group = deprecatedGroupLoaderClient.loadStudentGroups(stuDetail.getId(), false)
                    .stream()
                    .filter(g -> g.getSubject() == subject).findFirst()
                    .orElse(null);

            Function<UserPicBookResult, JournalNewHomeworkProcessResult> resultMapper = record -> {
                List<List<String>> inputAnswer = record.parseAnswer();

                JournalNewHomeworkProcessResult result = new JournalNewHomeworkProcessResult();
                result.setCreateAt(new Date());
                result.setUpdateAt(new Date());
                result.setStudyType(StudyType.levelReading);
                result.setClazzId(stuDetail.getClazz().getId());

                result.setClazzGroupId(group == null ? 0L : group.getId());
                result.setClazzLevel(stuDetail.getClazzLevelAsInteger());
                result.setBookId(record.getBookId()); // ser.setBook_id(request.getBookId());
                //result.setUnitId(request.getUnitId()); // ser.setUnit_id(request.getUnitId());
                result.setUserId(stuDetail.getId());
                result.setQuestionId(record.getQuestionId());
                result.setDuration(SafeConverter.toLong(record.getDuration())); // ser.setCmt_timelen(request.getFinishTime());
                result.setSubject(subject); // ser.setSubject(Subject.of(request.getSubject()));
                result.setUserAnswers(inputAnswer); // ser.setAnswers(request.getAnswer());
                result.setClientType("mobile");
                result.setClientName(appKey); // ser.setClient_name(request.getClientType());
                result.setAlgoW("");
                result.setAlgoV("");
                result.setClientId(Long.toString(stuDetail.getId()));

                // 如果是跟读题，要处理下oralDetail等参数
                if (record.getModule() == 3) {
                    result.setGrasp(true);
                    result.setVoiceScoringMode("Normal");

                    NewHomeworkProcessResult.OralDetail detail = new NewHomeworkProcessResult.OralDetail();
                    detail.setMacScore(record.getScore());
                    detail.setOralScore(record.getScore());

                    result.setOralDetails(singletonList(singletonList(detail)));
                    result.setScore(SafeConverter.toDouble(record.getScore()));
                } else if (record.getModule() == 4) {
                    UserAnswerMapper uam = new UserAnswerMapper(record.getQuestionId(), 1D, inputAnswer);
                    uam.setUserAgent(getRequest().getHeader("User-Agent"));
                    uam.setUserId(stuDetail.getId());
                    uam.setHomeworkType(StudyType.levelReading.name());
                    QuestionScoreResult qsr = scoreCalculationLoaderClient.loadQuestionScoreResult(uam);
                    if (qsr == null || qsr.getSubScoreResults() == null) {
                        logger.error("Afenti GenerateCastleResultContext Error,userId={},questionID={},userAnswer={}", stuDetail.getId(), record.getQuestionId(), record.getAnswer());
                        return null;
                    }

                    // 小题的分数
                    List<List<Boolean>> subMaster = qsr.getSubScoreResults()
                            .stream()
                            .map(r -> r.getIsRight())
                            .collect(toList());

                    result.setGrasp(qsr.getIsRight());
                    result.setSubGrasp(subMaster); // ser.setSatag(subMaster);
                }

                return result;
            };

            List<JournalNewHomeworkProcessResult> resultList = data.stream()
                    // 只有第三、四模块有questionId，才需要报给大数据
                    .filter(r -> StringUtils.isNotBlank(r.getQuestionId()))
                    .map(resultMapper)
                    .filter(Objects::nonNull)
                    .collect(toList());

            homeworkQueueSrvCli.saveJournalNewHomeworkProcessResults(resultList);
        } catch (Exception e) {
            // 如果出错先吞掉，不影响report后面的逻辑
            logger.error("PicBook:上报大数据出错", e);
        }
    }

    @RequestMapping(value = "/report.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage report() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String data = getRequestString("data");
            Validate.notBlank(data, "参数错误!");

            validateRequest("data");

            List<UserPicBookResult> reportData = JsonUtils.fromJsonToList(data, UserPicBookResult.class);
            Validate.noNullElements(reportData, "上报数据丢失!");

            String bookId = reportData.get(0).getBookId();
            PictureBookPlus picBook = userPicBookServiceClient.loadPicBook(bookId, pictureBookPlusServiceClient);
            validateNotNull(picBook, "绘本不存在!");

            // 填上用户ID字段
            reportData.forEach(rd -> rd.setUserId(currStu.getId()));

            String appKey = getRequestString(REQ_APP_KEY);
            // 上报给大数据
            reportToBigData(currStu, reportData, Subject.fromSubjectId(picBook.getSubjectId()), appKey);

            // 上传数据后，返回最新的进度信息，供前端刷新进度界面
            return userPicBookServiceClient.report(reportData);
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    private List<Map<String, Object>> getCartItemMapper(Long userId, String appKey, List<String> bookIds) {
        // 倒序显示，从近到远
        Collections.reverse(bookIds);

        Function<PictureBookPlus, Map<String, Object>> cartItemFunc = getPicBookMapper(userId, appKey, bookIds, bookIds, getLevelConfigContent());
        return userPicBookServiceClient.loadPicBooks(bookIds, pictureBookPlusServiceClient)
                .stream()
                .map(cartItemFunc)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private PageBlockContent getLevelConfigContent() {
        return pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName(levelKey)
                .stream()
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .filter(p -> Objects.equals(levelKey2, p.getBlockName()))
                .findFirst().orElse(null);
    }


    @RequestMapping(value = "/shopping_cart.vpage")
    @ResponseBody
    public MapMessage loadShoppingCart() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            validateRequest();

            OrderProductServiceType type = OrderProductServiceType.safeParse(appKey);
            List<String> cartItemIds = userPicBookServiceClient.loadShoppingCartBookIds(currStu.getId(), type);
            List<Map<String, Object>> items = getCartItemMapper(currStu.getId(), appKey, cartItemIds);

            return MapMessage.successMessage()
                    .add("items", items)
                    .add("name", currStu.fetchRealname());
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("LevelReading:Get shopping cart items error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    @RequestMapping(value = "/add_cart_item.vpage")
    @ResponseBody
    public MapMessage addShoppingCartItem() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            String bookId = getRequestString("bookId");
            Validate.notBlank(bookId, "绘本id不存在!");

            validateRequest("bookId");
            Long userId = currStu.getId();
            PictureBookPlus picBook = userPicBookServiceClient.loadPicBook(bookId, pictureBookPlusServiceClient);

            // 不能重复购买
            AppPayMapper payMapper = userOrderLoaderClient.getUserAppPaidStatus(appKey, userId, true);
            if (payMapper.containsAppItemId(bookId)) {
                return MapMessage.errorMessage("产品" + picBook.getEname() + "已购买，请刷新页面重试~")
                        .setErrorCode(RES_RESULT_INTERNAL_ERROR_CODE);
            }

            OrderProductServiceType type = OrderProductServiceType.safeParse(appKey);
            MapMessage resultMsg = userPicBookServiceClient.addShoppingCartItem(currStu.getId(), type, bookId);

            Function<PictureBookPlus, Map<String, Object>> picBookMapper = getPicBookMapper(userId, appKey, singletonList(bookId), null, getLevelConfigContent());
            resultMsg.add("newItem", picBookMapper.apply(picBook));

            return resultMsg;
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            return errorMessage(RES_RESULT_GENERAL_ERROR).setErrorCode(RES_RESULT_INTERNAL_ERROR_CODE);
        }
    }

    @RequestMapping(value = "/remove_cart_item.vpage")
    @ResponseBody
    public MapMessage removeShoppingCartItem() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            String bookIds = getRequestString("bookIds");
            Validate.notBlank(bookIds, "绘本id不存在!");
            validateRequest("bookIds");

            List<String> bookIdList = Arrays.asList(bookIds.split(","));

            OrderProductServiceType type = OrderProductServiceType.safeParse(appKey);
            List<String> cartBookIds = userPicBookServiceClient.removeShoppingCartItem(currStu.getId(), type, bookIdList);
            List<Map<String, Object>> items = getCartItemMapper(currStu.getId(), appKey, cartBookIds);

            return MapMessage.successMessage().add("items", items);
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    @RequestMapping(value = "/create_order.vpage")
    @ResponseBody
    public MapMessage createOrder() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String bookIdsStr = getRequestString("bookIds");
            Validate.notBlank(bookIdsStr, "商品id为空!");
            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            Long userId = currStu.getId();
            VendorAppsUserRef appsUserRef = vendorLoaderClient.loadVendorAppUserRef(appKey, userId);
            validateNotNull(appsUserRef, "session is missing!");

            String refer = getRequestString("refer");
            validateRequest("bookIds", "refer");

            List<String> bookIds = Arrays.stream(bookIdsStr.split(",")).collect(toList());
            Validate.noNullElements(bookIds, "订单中未包含绘本!");

            List<String> productIds = userOrderLoaderClient.loadOrderProductByAppItemIds(bookIds)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(ObjectIdEntity::getId)
                    .collect(toList());
            if(CollectionUtils.isEmpty(productIds)){
                return MapMessage.errorMessage("该商品不存在！").setErrorCode(RES_RESULT_INTERNAL_ERROR_CODE);
            }
            PictureBookPlus picBook = userPicBookServiceClient.loadPicBook(bookIds.get(0), pictureBookPlusServiceClient);
            validateNotNull(picBook, "购买的绘本已经下线!");

            // 不能重复购买
            AppPayMapper payMapper = userOrderLoaderClient.getUserAppPaidStatus(appKey, userId, true);
            if (payMapper.containsAppItemId(picBook.getId())) {
                return MapMessage.errorMessage("产品" + picBook.getEname() + "已购买，请刷新页面重试~")
                        .setErrorCode(RES_RESULT_INTERNAL_ERROR_CODE);
            }

            String showName = "小U绘本（" + bookIds.size() + "本）";

            MapMessage resultMsg = usrOrderSrv.createAppOrder(currStu.getId(), appKey, productIds, showName, refer);
            if (resultMsg.isSuccess()) {
                String orderToken = MapUtils.getString(resultMsg, "orderToken");
                String orderId = MapUtils.getString(resultMsg, "orderId");
                String sessionKey = appsUserRef.getSessionKey();

                Map<String, String> urlParams = new HashMap<>();
                urlParams.put("oid", orderId);
                urlParams.put("returUrl", "");
                urlParams.put("rel", "");
                urlParams.put("sendnotification", "1");
                VendorApps appInfo = getApiRequestApp();
                validateNotNull(appInfo, "应用信息为空!");

                String sig = DigestSignUtils.signMd5(urlParams, appInfo.getSecretKey());
                urlParams.put("sig", sig);
//                urlParams.put("hideTopTitle", "true");

                String domain = ProductConfig.getMainSiteBaseUrl();
                String url = UrlUtils.buildUrlQuery(domain + "/view/mobile/parent/17my_shell/affirm.vpage", urlParams);
                resultMsg.add("confirmOrderUrl", url);
            } else {
                resultMsg.setErrorCode(RES_RESULT_INTERNAL_ERROR_CODE);
            }

            return resultMsg;
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    @RequestMapping(value = "/home.vpage")
    @ResponseBody
    public MapMessage loadHomePageDetail() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            OrderProductServiceType type = OrderProductServiceType.safeParse(appKey);
            Validate.isTrue(type != OrderProductServiceType.Unknown, "appKey错误!");
            validateRequest();

            Subject subject = userPicBookServiceClient.loadTypeSubject(type);
            Long stuId = currStu.getId();

            Map<String, String> clazzLevelMap = userPicBookServiceClient.getClazzLevelMap();
            // 阅读等级列表
            List<Map<String, Object>> readLevelMapper = clazzLevelMap.entrySet()
                    .stream()
                    .map(entry -> MapUtils.m("code", entry.getKey(), "value", entry.getValue()))
                    .collect(toList());

            MapMessage resultMsg = MapMessage.successMessage();
            resultMsg.add("readLvl", readLevelMapper);

            // 我的绘本部分，15本，按照购买顺序由近到远
            List<PictureBookPlus> myPicBook = userPicBookServiceClient.loadAllUserPicBooksOrdered(stuId, appKey, pictureBookPlusServiceClient)
                    .stream()
                    .limit(HOME_MY_BOOK_LIST_SIZE)
                    .collect(toList());

            List<String> myPicBookIds = myPicBook.stream().map(pb -> pb.getId()).collect(toList());
            // 初始化PB到mapper的Function
            Function<PictureBookPlus, Map<String, Object>> myPicBookFunc = getPicBookMapper(stuId, appKey, myPicBookIds, null, getLevelConfigContent());
            List<Map<String, Object>> myPicBookMapper = myPicBook.stream()
                    .map(myPicBookFunc)
                    .filter(Objects::nonNull)
                    .collect(toList());

            resultMsg.add("myBooks", myPicBookMapper);

            // 一周阅读成就，时长转成分为单位。向上取整..
            UserPicBookAchieve achieve = userPicBookServiceClient.loadThisWeekAchieve(stuId);
            int learnTimeInMin = (int) Math.ceil(achieve.getLearnTime() / 1000d / 60);

            Map<String, Object> achieveMapper = MapUtils.m(
                    "readingNum", achieve.getReadingNum(),
                    "newWordsNum", achieve.getNewWordsNum(),
                    "averageScore", achieve.getAverageScore(),
                    "learnTime", learnTimeInMin);

            resultMsg.add("achieve", achieveMapper);
            // 购物车数量
            resultMsg.add("shoppingCartItemNum", userPicBookServiceClient.loadShoppingCartBookIds(stuId, type).size());

            // 推荐绘本部分
            // 如果缓存中有,拿缓存的，如果是空，则取教材对应的阅读等级
            List<PictureBookNewClazzLevel> cachedReadLvl = userPicBookServiceClient.loadCachedUserLevel(stuId);
            // 如果用户没有选过阅读等级，通过教材来确认
            Supplier<List<PictureBookNewClazzLevel>> clazzLvlSupplier = () -> {
                Long groupId = deprecatedGroupLoaderClient.loadStudentGroups(singleton(stuId), false)
                        .get(stuId)
                        .stream()
                        .filter(gm -> gm.getSubject() == subject)
                        .map(GroupMapper::getId)
                        .findFirst()
                        .orElse(null);

                Clazz stuClazz = currStu.getClazz();
                Long schoolId = stuClazz == null ? 0L : stuClazz.getSchoolId();
                ClazzLevel clazzLevel = currStu.getClazzLevel();

                NewClazzBookRefMapper clazzBookMapper = newClazzBookLoaderCli.findNewClazzBookRefWithDefault(groupId, subject, clazzLevel, schoolId);
                List<PictureBookNewClazzLevel> newLevels = Optional.ofNullable(clazzBookMapper)
                        .map(bm -> userPicBookServiceClient.getReadLvlFromBookId(bm.getBookId()))
                        .orElse(Collections.singletonList(L1B));

                // 确定完新等级后，要存进去
                userPicBookServiceClient.modifyUserLevelCache(stuId, newLevels);
                return newLevels;
            };

            List<PictureBookNewClazzLevel> readLvl = Optional.ofNullable(cachedReadLvl).orElseGet(clazzLvlSupplier);

            PicBookContext context = new PicBookContext();
            context.setUserId(stuId);
            context.setClazzLevels(readLvl);
            context.setSubjectId(subject.getId());
            context.setType(type.name());

            List<PictureBookPlus> featuredBooks = userPicBookServiceClient.loadFeaturedBooks(context, pictureBookPlusServiceClient);
            List<String> featuredBookIds = featuredBooks.stream()
                    .filter(Objects::nonNull)
                    .map(b -> b.getId())
                    .collect(toList());

            // 阅读等级可能是多个
            List<Map<String, Object>> currLevel = readLvl.stream()
                    .map(cl -> MapUtils.m("code", cl.name(), "name", clazzLevelMap.get(cl.name())))
                    .collect(toList());

            // 当前阅读等级
            resultMsg.add("currReadLevel", currLevel);

            // 推荐绘本列表
            Function<PictureBookPlus, Map<String, Object>> featurePicBookFunc = getPicBookMapper(stuId, appKey, featuredBookIds, null, getLevelConfigContent());
            List<Map<String, Object>> featuredBooksMapper = featuredBooks.stream()
                    .map(featurePicBookFunc)
                    .filter(Objects::nonNull)
                    .collect(toList());

            resultMsg.add("featuredBooks", featuredBooksMapper);

            // 是否显示排行榜入口
            resultMsg.add("rankShow", true);

            // 不显示排行榜奖励弹窗。
            resultMsg.add("rankRewardShow", false);
            // 取上周的排行奖励信息
//            int week = WeekRange.current().previous().getWeekOfYear();
//            PicBookRankReward rankReward = userPicBookServiceClient.getRemoteReference().loadUserWeekRankReward(stuId, week);
//            // 是否显示阅读成就排行榜奖励弹窗
//            resultMsg.add("rankRewardShow", rankReward != null && rankReward.getShow());
//            if (rankReward != null && rankReward.getShow()) {
//                //奖励弹窗内容
//                resultMsg.add("rankReward", rankReward);
//            }
            return resultMsg;
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("PicBook:Load home page error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    @RequestMapping(value = "/change_reading_lvl.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage changeReadingLvl() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            String readingLvlName = getRequestString("reading_lvl");
            Validate.notBlank(readingLvlName, "参数错误!");
            validateRequest("reading_lvl");

            PictureBookNewClazzLevel readingLvl = PictureBookNewClazzLevel.safeValueOf(readingLvlName);
            Validate.isTrue(readingLvl != PictureBookNewClazzLevel.UNKNOWN, "非法参数!");

            boolean result = userPicBookServiceClient.modifyUserLevelCache(currStu.getId(), Collections.singletonList(readingLvl));
            return MapMessage.successMessage().add("result", result);
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    /**
     * 绘本馆
     */
    @RequestMapping(value = "/pic_books_library.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadPicBookLibrary() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            Integer pageNumber = getRequestInt("page_number");
            Validate.isTrue(pageNumber > 0, "非法的参数!");

            // 排序规则
            String orderBy = Optional.ofNullable(getRequestParameter("order_by", null)).orElse("multiple");
            // 绘本类别
            String bookType = Optional.ofNullable(getRequestParameter("book_type", null)).orElse("allBook");
            String ver = getClientVersion();
            if (StringUtils.isNotBlank(ver) && VersionUtil.compareVersion(ver, "1.1.1.100") >= 0) {
                validateRequest("page_number", "order_by", "book_type");
            } else {
                validateRequest("page_number", "order_by");
            }
            OrderProductServiceType type = OrderProductServiceType.safeParse(appKey);

            Subject subject = userPicBookServiceClient.loadTypeSubject(type);

            PicBookContext context = new PicBookContext();
            context.setType(type.name());
            context.setBookType(bookType);
            context.setOrderBy(orderBy);
            context.setUserId(currStu.getId());
            context.setSubjectId(subject.getId());

            PageBlockContent levelConfig = getLevelConfigContent();

            List<PictureBookPlus> picBooks = userPicBookServiceClient.loadPicBooks(context, levelConfig, pictureBookPlusServiceClient);


            Pageable pageable = PageableUtils.startFromOne(pageNumber, PAGE_SIZE);
            Page<PictureBookPlus> pageResult = PageableUtils.listToPage(picBooks, pageable);

            // 绘本的ID列表
            List<String> bookIds = pageResult.getContent()
                    .stream()
                    .map(BasePictureBook::getId)
                    .collect(toList());

            Function<PictureBookPlus, Map<String, Object>> picBookMapper = getPicBookMapper(currStu.getId(), appKey, bookIds, null, levelConfig);
            List<Map<String, Object>> booksMapper = pageResult.getContent()
                    .stream()
                    .map(picBookMapper)
                    .filter(Objects::nonNull)
                    .collect(toList());

            MapMessage resultMsg = MapMessage.successMessage();
            resultMsg.add("books", booksMapper);
            resultMsg.add("isLast", pageResult.isLast());
            resultMsg.add("pageNumber", pageNumber);

            // 排序项
            List<Map<String, Object>> soringOptions = new ArrayList<>();
            soringOptions.add(MapUtils.m("code", "multiple", "name", "综合排序"));
//            soringOptions.add(MapUtils.m("code", "priceAsc", "name", "价格由低到高"));
//            soringOptions.add(MapUtils.m("code", "priceDesc", "name", "价格由高到低"));
            soringOptions.add(MapUtils.m("code", "salesVolDesc", "name", "销量"));
            resultMsg.add("soringOptions", soringOptions);
            // 绘本类型
            List<Map<String, Object>> pictureBookOptions = new ArrayList<>();
            pictureBookOptions.add(MapUtils.m("code", "allBook", "name", "全部绘本"));
            pictureBookOptions.add(MapUtils.m("code", "selection", "name", "精选绘本"));
            pictureBookOptions.add(MapUtils.m("code", "highQuality", "name", "优质绘本"));
            pictureBookOptions.add(MapUtils.m("code", "recommend", "name", "推荐绘本"));
            resultMsg.add("pictureBookOptions", pictureBookOptions);

            return resultMsg;
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("PicBook:Load library error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    /**
     * 小U绘本3期，广告位接口
     *
     * @return
     */

    @RequestMapping(value = "/advertising.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadAdvertising() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            String slotId = getRequestString("slot_id");
            Validate.isTrue("321101".equals(slotId) || "321102".equals(slotId), "非法的参数!");
            validateRequest("slot_id");

            List<NewAdMapper> newAdMappers = userAdvertisementServiceClient.getUserAdvertisementService().loadNewAdvertisementData(currStu.getId(), slotId, null);
            List<Map<String, Object>> adList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(newAdMappers)) {
                for (NewAdMapper adMapper : newAdMappers) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("imgUrl", getUserAvatarImgUrl(adMapper.getImg()));
                    data.put("introText", adMapper.getContent());
                    data.put("btnText", adMapper.getBtnContent());
                    data.put("detailUrl", adMapper.getUrl());
                    data.put("id", adMapper.getId());
                    adList.add(data);
                }
            }
            return MapMessage.successMessage().add("adPopup", adList);
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("advertising:Load library error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    @RequestMapping(value = "/my_books.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage loadMyBooks() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            Integer pageNumber = getRequestInt("page_number");
            Validate.isTrue(pageNumber > 0, "非法的参数!");
            validateRequest("page_number");

            Long userId = currStu.getId();

            List<PictureBookPlus> userPicBooks = userPicBookServiceClient.loadAllUserPicBooksOrdered(userId, appKey, pictureBookPlusServiceClient);
            List<String> userPicBookIds = userPicBooks.stream().map(upb -> upb.getId()).collect(toList());

            Pageable pageable = PageableUtils.startFromOne(pageNumber, PAGE_SIZE);
            Page<PictureBookPlus> pageResult = PageableUtils.listToPage(userPicBooks, pageable);

            Function<PictureBookPlus, Map<String, Object>> picBookMapper = getPicBookMapper(userId, appKey, userPicBookIds, null, getLevelConfigContent());
            List<Map<String, Object>> booksMapper = pageResult.getContent()
                    .stream()
                    .map(picBookMapper)
                    .filter(Objects::nonNull)
                    .collect(toList());

            MapMessage resultMsg = MapMessage.successMessage();
            resultMsg.add("books", booksMapper);
            resultMsg.add("isLast", pageResult.isLast());
            resultMsg.add("pageNumber", pageNumber);

            return resultMsg;
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("PicBook:Load my books error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    // 排行榜信息
    @RequestMapping(value = "/ranks.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ranks() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            Integer rankCategory = getRequestInt("rankCategory");
            PicBookRankCategory category = PicBookRankCategory.of(rankCategory);
            Validate.isTrue(category != null, "排行榜类型错误!");
            Integer rankType = getRequestInt("rankType");
            PicBookRankType type = PicBookRankType.of(rankType);
            Validate.isTrue(type != null, "排行榜范围类型错误!");

            validateNotNull(currStu.getClazz(), "用户班级为空");
            validateNotNull(currStu.getClazz().getSchoolId(), "用户学校为空");

            validateRequest("rankCategory", "rankType");

            return userPicBookServiceClient.getRemoteReference().loadUserRanksInfo(currStu.getId(), category, type, getCdnBaseUrlAvatarWithSep());
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("PicBook:Load ranks error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    // 排行榜奖励规则
    @RequestMapping(value = "/rankrewards.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage rankRewards() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            // 暂时写死吧 以后肯定是要改需求 没这么简单
            List<Map<String, Object>> schoolRewards = new ArrayList<>();
//            Map<String, Object> rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第1名");
//            rewardMap.put("score", 10);
//            schoolRewards.add(rewardMap);
//
//            rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第2名");
//            rewardMap.put("score", 8);
//            schoolRewards.add(rewardMap);
//
//            rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第3名");
//            rewardMap.put("score", 5);
//            schoolRewards.add(rewardMap);
//
//            rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第4-10名");
//            rewardMap.put("score", 2);
//            schoolRewards.add(rewardMap);
//
            List<Map<String, Object>> rewards = new ArrayList<>();
//            rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第1名");
//            rewardMap.put("score", 500);
//            rewards.add(rewardMap);
//
//            rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第2名");
//            rewardMap.put("score", 300);
//            rewards.add(rewardMap);
//
//            rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第3名");
//            rewardMap.put("score", 100);
//            rewards.add(rewardMap);
//
//            rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第4-20名");
//            rewardMap.put("score", 30);
//            rewards.add(rewardMap);
//
//            rewardMap = new HashMap<>();
//            rewardMap.put("rankName", "第21-100名");
//            rewardMap.put("score", 10);
//            rewards.add(rewardMap);

//            return MapMessage.successMessage().add("schoolRewards", schoolRewards).add("rewards", rewards)
//                    .add("scoreImg", "https://oss-image.17zuoye.com/wonderland/reward/img/2017/07/20/20170720173210259463.png");
            return MapMessage.successMessage().add("schoolRewards", schoolRewards).add("rewards", rewards).add("scoreImg", "");
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("PicBook:Load rankRewards error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    // 购买成功后调用接口
    @RequestMapping(value = "/orderreward.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage orderReward() {
        try {
            StudentDetail currStu = getApiRequestStudentDetail();
            validateNotNull(currStu, "非法的请求!");

            String appKey = getRequestString(REQ_APP_KEY);
            Validate.notBlank(appKey, "appKey为空!");

            String bookIdsStr = getRequestString("bookIds");
            Validate.notBlank(bookIdsStr, "非法参数!");

            validateRequest("bookIds");

            List<String> bookIds = Arrays.stream(bookIdsStr.split(",")).collect(toList());
            Function<PictureBookPlus, Map<String, Object>> mapFunc = getPicBookMapper(currStu.getId(), appKey, bookIds, null, getLevelConfigContent());
            List<Map<String, Object>> bookMapper = userPicBookServiceClient.loadPicBooks(bookIds, pictureBookPlusServiceClient)
                    .stream()
                    .map(mapFunc)
                    .collect(toList());

            //  处理付费后赠送的道具列表总和
            List<PicBookPurchaseProp> totalList = new ArrayList<>();
//            for (Map<String, Object> book : bookMapper) {
//                List<PicBookPurchaseProp> rewardList = (List<PicBookPurchaseProp>) book.get("rewardList");
//                if (CollectionUtils.isNotEmpty(rewardList)) {
//                    for (PicBookPurchaseProp prop : rewardList) {
//                        PicBookPurchaseProp exist = totalList.stream().filter(p -> StringUtils.equals(prop.getName(), p.getName()))
//                                .findFirst().orElse(null);
//                        if (exist == null) {
//                            totalList.add(prop);
//                        } else {
//                            int addAfter = exist.getNum() + prop.getNum();
//                            exist.setNum(addAfter);
//                        }
//                    }
//                }
//            }
            return MapMessage.successMessage().add("books", bookMapper).add("rewardList", totalList);
        } catch (IllegalArgumentException e) {
            return errorMessage(e.getMessage());
        } catch (Throwable t) {
            logger.error("PicBook:Load orderReward error!", t);
            return errorMessage(RES_RESULT_GENERAL_ERROR);
        }
    }

    // 许愿
    @RequestMapping(value = "/wish.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage wish() {
        // 校验参数
        try {
            validateRequired("appKey", "应用");
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage();
        }

        // 请求校验
        try {
            List<String> params = new ArrayList<>();
            params.add("appKey");
            params.add("refer");
            if (StringUtils.isNotBlank(getRequestString("productId"))) {
                params.add("productId");
            } else if (StringUtils.isNotBlank(getRequestString("appItemId"))) {
                params.add("appItemId");
            }
            validateRequest(params.toArray(new String[0]));
        } catch (IllegalArgumentException e) {
            return MapMessage.errorMessage();
        }

        StudentDetail student = getApiRequestStudentDetail();
        String appKey = getRequestString("appKey");
        String refer = getRequestString("refer");
        String productId = getRequestString("productId");
        String appItemId = getRequestString("appItemId");
        String pattern = "{}/view/mobile/student/activity/guide_parent/index.vpage?type={}&appKey={}&from={}";

        // 查询孩子是否有绑定了家长
        List<StudentParent> parents = parentLoaderClient.loadStudentParents(student.getId());
        if (CollectionUtils.isNotEmpty(parents)) {
            // 查询缓存
            Set<String> wishes = fetchWishes(student.getId());

            // 判断产品
            List<String> pids = fetchProductIds(productId, appItemId);
            if (CollectionUtils.isEmpty(pids)) {
                if (wishes.contains(appKey)) {
                    logCollect(student.getId(), "wish_success_cached", appKey, refer, new ArrayList<>(), new ArrayList<>());
                } else {
                    wishProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(
                            MapUtils.m("sid", student.getId(), "appKey", appKey, "refer", refer))));
                    recordWish(student.getId(), appKey);
                    logCollect(student.getId(), "wish_success", appKey, refer, new ArrayList<>(), new ArrayList<>());
                }
            } else {
                Set<String> cached = new HashSet<>();
                Set<String> missed = new HashSet<>();
                for (String pid : pids) {
                    String wish = StringUtils.join(new Object[]{appKey, pid}, "_");
                    if (wishes.contains(wish)) {
                        cached.add(pid);
                    } else {
                        wishProducer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(MapUtils.m(
                                "sid", student.getId(), "appKey", appKey, "productId", pid, "refer", refer))));
                        recordWish(student.getId(), wish);
                        missed.add(pid);
                    }
                }
                if (CollectionUtils.isEmpty(cached)) {
                    logCollect(student.getId(), "wish_success", appKey, refer, missed, cached);
                }
                if (CollectionUtils.isEmpty(missed)) {
                    logCollect(student.getId(), "wish_success_cached", appKey, refer, missed, cached);
                }
                if (CollectionUtils.isNotEmpty(cached) && CollectionUtils.isNotEmpty(missed)) {
                    logCollect(student.getId(), "wish_success_mix", appKey, refer, missed, cached);
                }
            }
            return MapMessage.successMessage().add("link", StringUtils.formatMessage(pattern, fetchMainsiteUrlByCurrentSchema(), "binding", appKey, refer));
        } else {
            logCollect(student.getId(), "wish_failed_no_parent", appKey, refer, new ArrayList<>(), new ArrayList<>());
            return MapMessage.successMessage().add("link", StringUtils.formatMessage(pattern, fetchMainsiteUrlByCurrentSchema(), "unbinding", appKey, refer));
        }
    }

    private List<String> fetchProductIds(String productId, String appItemId) {
        if (StringUtils.isBlank(productId) && StringUtils.isBlank(appItemId))
            return Collections.emptyList();

        if (StringUtils.isNotBlank(productId)) {
            return Arrays.asList(StringUtils.split(productId, ","));
        } else {
            List<String> ids = Arrays.asList(StringUtils.split(appItemId, ","));
            return userOrderLoaderClient.loadOrderProductByAppItemIds(ids)
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(ObjectIdEntity::getId)
                    .collect(toList());
        }
    }

    private void logCollect(Long studentId, String op, String ak, String refer, Collection<String> pids, Collection<String> cpids) {
        LogCollector.info("backend_va", MapUtils.map(
                "env", RuntimeMode.getCurrentStage(),
                "studentId", studentId,
                "ak", ak,
                "refer", refer,
                "productId", pids,
                "cachedProductId", cpids,
                "activity", "",
                "op", op,
                "time", System.currentTimeMillis()
        ));
    }

    private Set<String> fetchWishes(Long studentId) {
        String key = "MAKE_A_WISH_I_WANT_BLABLA:" + studentId;
        CacheObject<Set<String>> cacheObject = cs.CBS.persistence.get(key);
        Set<String> cached = cacheObject.getValue();
        return cached == null ? Collections.emptySet() : cached;
    }

    private void recordWish(Long studentId, String wish) {
        if (null == studentId || StringUtils.isBlank(wish)) return;

        String key = "MAKE_A_WISH_I_WANT_BLABLA:" + studentId;
        CacheObject<Set<String>> cacheObject = cs.CBS.persistence.get(key);
        if (cacheObject != null && cacheObject.getValue() != null) {
            cs.CBS.persistence.cas(key, DateUtils.getCurrentToDayEndSecond(), cacheObject,
                    currentValue -> {
                        currentValue = new HashSet<>(currentValue);
                        currentValue.add(wish);
                        return currentValue;
                    });
        } else {
            cs.CBS.persistence.add(key, DateUtils.getCurrentToDayEndSecond(), Collections.singleton(wish));
        }
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        params.put("app_key", "ELevelReading");
        params.put("session_key", "6be6a1d94d8bb49aa2de84500a81616a");
        params.put("appKey", "ELevelReading");
        params.put("refer", "1111");
        params.put("productId","5a80241c777487abe373075d,5a98dc657774878944fa8d2f");
        String sig = DigestSignUtils.signMd5(params, "kUG1a03XbehV");
        params.put("sig", sig);

//        String url = "http://10.203.5.47:8081/v2/studentMobile/levelreading/wish.vpage";
        String url = "http://www.test.17zuoye.net/v2/studentMobile/levelreading/wish.vpage";

        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(UrlUtils.buildUrlQuery(url, params)).execute();
        System.out.println(response);
    }
}
