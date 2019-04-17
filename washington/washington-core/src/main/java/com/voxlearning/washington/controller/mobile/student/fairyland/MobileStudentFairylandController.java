package com.voxlearning.washington.controller.mobile.student.fairyland;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.prometheus.service.data.api.client.PictureBookPlusServiceClient;
import com.voxlearning.utopia.api.constant.AppSystemType;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.data.PicBookPurchaseProp;
import com.voxlearning.utopia.service.afenti.api.entity.UserPicBook;
import com.voxlearning.utopia.service.afenti.api.mapper.UserPicBookProgress;
import com.voxlearning.utopia.service.afenti.client.UserPicBookServiceClient;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.question.api.entity.Dubbing;
import com.voxlearning.utopia.service.question.api.entity.PictureBookPlus;
import com.voxlearning.utopia.service.question.consumer.DubbingLoaderClient;
import com.voxlearning.utopia.service.question.consumer.PictureBookLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.StudentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.FairylandProductStatus;
import com.voxlearning.utopia.service.vendor.api.entity.FairylandProduct;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.FairylandProductServiceClient;
import com.voxlearning.utopia.service.wonderland.api.constant.WonderlandErrorType;
import com.voxlearning.utopia.service.wonderland.api.entity.WonderlandTimesCard;
import com.voxlearning.washington.controller.mobile.AbstractMobileController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.common.Mode.STAGING;
import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;
import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;
import static com.voxlearning.utopia.service.vendor.api.constant.FairyLandPlatform.STUDENT_APP;
import static com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType.APPS;
import static com.voxlearning.utopia.service.vendor.api.constant.FairylandProductType.INNER_APPS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

/**
 * 学生app端课外乐园首页
 *
 * @author Ruib
 * @since 2017/2/12
 */
@Slf4j
@Controller
@RequestMapping(value = "/studentMobile/fairyland")
public class MobileStudentFairylandController extends AbstractMobileController {
    @Inject private FairylandProductServiceClient fairylandProductServiceClient;
    @Inject private UserPicBookServiceClient userPicBookServiceClient;
    @Inject private PictureBookPlusServiceClient pictureBookPlusServiceClient;
    @Inject private PictureBookLoaderClient picBookLoaderCli;
    @Inject private DubbingLoaderClient dubbingLoaderClient;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @RequestMapping(value = "homepage.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage homepage() {
        if (studentUnLogin()) return WonderlandErrorType.NEED_LOGIN.result();

        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) return WonderlandErrorType.NEED_LOGIN.result();

        boolean isBlack = userBlacklistServiceClient.isInBlackListByStudent(Collections.singletonList(student))
                .getOrDefault(student.getId(), false);
        boolean closeFairyland = false;
        boolean closeVap = false;

        StudentExtAttribute studentExtAttribute = studentLoaderClient.loadStudentExtAttribute(student.getId());
        if (studentExtAttribute != null) {
            closeFairyland = studentExtAttribute.fairylandClosed();
            closeVap = studentExtAttribute.vapClosed();
        }


        //灰度标记
        boolean grayFlag = false;
        try {
            grayFlag = grayFunctionManagerClient.getStudentGrayFunctionManager()
                    .isWebGrayFunctionAvailable(student, "FairylandStudy", "StuTasks");
        } catch (Exception ignore) {
        }

        return MapMessage.successMessage()
                .add("black", isBlack)
                .add("closeFairyland", closeFairyland)
                .add("closeVap", closeVap)
                .add("grayFlag", grayFlag)
                .add("currentTime", new Date().getTime());
    }

    @RequestMapping(value = "applist.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage appList() {
        if (studentUnLogin()) return WonderlandErrorType.NEED_LOGIN.result();

        StudentDetail student = currentStudentDetail();
        if (student.getClazz() == null) return WonderlandErrorType.NEED_LOGIN.result();

        String version = getRequestString("version"); // 壳的版本号
        AppSystemType os = getAppSystemType(); // 手机操作系统

        // 获取学生可见的应用
        List<VendorApps> apps = businessVendorServiceClient.getStudentMobileAvailableApps(student, version, os);
        Set<String> aks = apps.stream().map(VendorApps::getAppKey).collect(Collectors.toSet());
        Map<String, VendorApps> ak_app_map = apps.stream().collect(Collectors.toMap(VendorApps::getAppKey, p -> p));

        // 获取产品
        List<FairylandProduct> products = fairylandProductServiceClient.getFairylandProductBuffer()
                .loadFairylandProducts(STUDENT_APP, null)
                .stream()
                .filter((p) -> (FairylandProductStatus.ONLINE.name().equals(p.getStatus())))
                .filter((p) -> aks.contains(p.getAppKey()))
                .collect(Collectors.toList());

        List<Map<String, Object>> results = new ArrayList<>();
        if (CollectionUtils.isEmpty(products)) return MapMessage.successMessage().add("appsInfo", results);

        // 获取使用数量的描述
        Set<String> aks_exclude = products.stream().map(FairylandProduct::getAppKey).collect(Collectors.toSet());
        Map<String, String> desc = businessVendorServiceClient.fetchUserUseNumDesc(new ArrayList<>(aks_exclude), student);

        // 获取支付状态
        Set<String> aks_include = products.stream().map(FairylandProduct::getAppKey).collect(Collectors.toSet());
        aks_include.add(AfentiChineseImproved.name());
        aks_include.add(AfentiMathImproved.name());
        aks_include.add(AfentiExamImproved.name());
        aks_include.add(AfentiExamVideo.name());
        aks_include.add(AfentiMathVideo.name());
        aks_include.add(AfentiChineseVideo.name());
        Map<String, AppPayMapper> status = userOrderLoaderClient.getUserAppPaidStatus(new ArrayList<>(aks_include), student.getId(), false);

        // 获取绘本订单。。。
        Map<OrderProductServiceType, List<UserOrder>> story_book_order = userOrderLoaderClient.loadUserPaidOrders(
                Arrays.asList(ChineseStoryBook.name(), EnglishStoryBook.name()), student.getId())
                .stream()
                .collect(Collectors.groupingBy(o -> OrderProductServiceType.safeParse(o.getOrderProductServiceType()), Collectors.toList()));

        for (FairylandProduct product : products) {
            Map<String, Object> result = new HashMap<>();
            result.put("appKey", product.getAppKey());
            result.put("productName", product.getProductName());
            result.put("productDesc", product.getProductDesc());
            result.put("productIcon", getCdnBaseUrlAvatarWithSep() + "gridfs/" + product.getProductIcon());
            result.put("operationMessage", desc.getOrDefault(product.getAppKey(), null));
            result.put("hotFlag", product.getHotFlag());
            result.put("newFlag", product.getNewFlag());
            result.put("recommendFlag", product.getRecommendFlag());
            result.put("catalogDesc", product.getCatalogDesc());
            result.put("productType", product.getProductType());

            // app类型需要增加额外信息
            if (product.getProductType().equals(APPS.name())) {
                // 浏览器内核与支持屏幕转向
                if (ak_app_map.containsKey(product.getAppKey())) {
                    result.put("orientation", ak_app_map.get(product.getAppKey()).getOrientation());
                    result.put("browser", ak_app_map.get(product.getAppKey()).getBrowser());
                }
                result.put("launchUrl", product.fetchRedirectUrl(RuntimeMode.current()));
            }

            if (product.getProductType().equals(INNER_APPS.name())) {
                result.put("launchUrl", RuntimeMode.current() == STAGING ? product.getStagingLaunchUrl() : product.getLaunchUrl());
            }

            // 购买状态及购买开通人数：　0 未购买, 1 购买过期, 2 正在使用, 3 购买了但还没到开始时间
            int appStatus = 0;
            int dayToExpire = 0;

            if (MapUtils.isNotEmpty(status)) {
                switch (product.getAppKey()) {
                    case "AfentiExam": { // 小U需要查询两个ak来确定状态
                        AppPayMapper aem = status.getOrDefault(AfentiExam.name(), null);
                        AppPayMapper aeim = status.getOrDefault(AfentiExamImproved.name(), null);
                        if (aem != null && aeim != null) {
                            appStatus = fetchAfentiPaymentStatus(aem.getAppStatus(), aeim.getAppStatus());
                            dayToExpire = fetchAfentiDayToExpire(aem, aeim);
                        }
                        break;
                    }
                    case "AfentiMath": { // 小U需要查询两个ak来确定状态
                        AppPayMapper amm = status.getOrDefault(AfentiMath.name(), null);
                        AppPayMapper amim = status.getOrDefault(AfentiMathImproved.name(), null);
                        if (amm != null && amim != null) {
                            appStatus = fetchAfentiPaymentStatus(amm.getAppStatus(), amim.getAppStatus());
                            dayToExpire = fetchAfentiDayToExpire(amm, amim);
                        }
                        break;
                    }
                    case "AfentiChinese": { // 小U需要查询两个ak来确定状态
                        AppPayMapper acm = status.getOrDefault(AfentiChinese.name(), null);
                        AppPayMapper acim = status.getOrDefault(AfentiChineseImproved.name(), null);
                        if (acm != null && acim != null) {
                            appStatus = fetchAfentiPaymentStatus(acm.getAppStatus(), acim.getAppStatus());
                            dayToExpire = fetchAfentiDayToExpire(acm, acim);
                        }
                        break;
                    }
                    case "AfentiExamVideo":
                    case "AfentiMathVideo":
                    case "AfentiChineseVideo": { // 错题宝单独需要处理,购买不同学科的只需要显示一个Feature #48431
                        Set<String> ctb = new HashSet<>(Arrays.asList(AfentiExamVideo.name(), AfentiMathVideo.name(),
                                AfentiChineseVideo.name()));

                        List<AppPayMapper> list = status.values().stream().filter(m -> ctb.contains(m.getProductType()))
                                .sorted((o1, o2) -> Integer.compare(o2.getAppStatus(), o1.getAppStatus()))
                                .collect(Collectors.toList());

                        if (CollectionUtils.isNotEmpty(list)) {
                            appStatus = list.get(0).getAppStatus();
                            dayToExpire = list.get(0).isActive() ? SafeConverter.toInt(list.get(0).getDayToExpire()) : 0;
                        }
                        break;
                    }
                    case "ValueAddedLiveTimesCard": {
                        // 小鹰学堂 开通状态需要查询剩余次数, 剩余次数为０的时候　显示为hot 反之不显示 Feature #48431，分为购买状态与过期状态
                        WonderlandTimesCard timesCard = wonderlandLoader.loadWonderlandTimesCard(student.getId(),
                                ValueAddedLiveTimesCard.name()).getUninterruptibly();
                        if (timesCard != null && SafeConverter.toInt(timesCard.getTimes()) > 0) {
                            appStatus = 2;
                            dayToExpire = SafeConverter.toInt(timesCard.getTimes());
                            result.put("hotFlag", false);
                        } else {
                            appStatus = 1;
                            result.put("hotFlag", true);
                        }
                        break;
                    }
                    case "ChineseStoryBook":
                    case "EnglishStoryBook": {
                        OrderProductServiceType ak = OrderProductServiceType.safeParse(product.getAppKey());
                        UserOrder userOrder = story_book_order.getOrDefault(ak, new ArrayList<>())
                                .stream()
                                .filter(p -> p.getPaymentStatus() == PaymentStatus.Paid && p.getOrderStatus() == OrderStatus.Confirmed)
                                .findFirst()
                                .orElse(null);
                        appStatus = userOrder == null ? 0 : 2;
                        break;
                    }
                    case "ELevelReading": //小U绘本不显示状态
                    default: {
                        AppPayMapper appPayMapper = status.getOrDefault(product.getAppKey(), null);
                        if (appPayMapper != null) {
                            dayToExpire = appPayMapper.isActive() ? SafeConverter.toInt(appPayMapper.getDayToExpire() + 1) : 0;
                            appStatus = appPayMapper.getAppStatus();
                        }
                    }
                }
            }

            result.put("appStatus", appStatus);
            result.put("dayToExpire", dayToExpire);
            results.add(result);
        }

        return MapMessage.successMessage().add("appsInfo", results);
    }


    // 前端需要012三个状态，12在黑名单下要显示出来，0不用，阿分题有012三个状态，提高版有0123三个状态，需要整合
    private int fetchAfentiPaymentStatus(int afenti, int improved) {
        if (afenti == 2) return 2;
        if (improved == 3) return 1;
        return Math.max(afenti, improved);
    }

    private int fetchAfentiDayToExpire(AppPayMapper afenti, AppPayMapper improved) {
        if (afenti.getAppStatus() == 2) {
            return SafeConverter.toInt(afenti.getDayToExpire() + 1);
        } else if (improved.getAppStatus() == 2) {
            return SafeConverter.toInt(improved.getDayToExpire() + 1);
        } else {
            return 0;
        }
    }

    @RequestMapping(value = "lvrdb.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage lvrdb() {
        if (studentUnLogin()) return WonderlandErrorType.NEED_LOGIN.result();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int i = calendar.get(Calendar.DAY_OF_WEEK);

        String key_config = "discovery_elv_" + i;
        String clv_config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), key_config);

        List<String> elvids = new ArrayList<>();
        if (StringUtils.isNotBlank(clv_config)) elvids = Arrays.asList(StringUtils.split(clv_config, ","));
        Collections.shuffle(elvids);

        String key_dubbing = "discovery_dubbing_" + i;
        String dubbing_config = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(
                ConfigCategory.PRIMARY_PLATFORM_STUDENT.name(), key_dubbing);

        Map<String, Object> dbid_lvid_map = new HashMap<>();
        if (StringUtils.isNotBlank(dubbing_config)) dbid_lvid_map = JsonUtils.fromJson(dubbing_config);

        return MapMessage.successMessage().add("elv", fetchElv(currentUserId(), elvids))
                .add("dubbing", fetchDubbing(dbid_lvid_map));
    }

    private List<Map<String, Object>> fetchDubbing(Map<String, Object> dbid_lvid_map) {
        if (MapUtils.isEmpty(dbid_lvid_map)) return Collections.emptyList();

        Map<String, Dubbing> dubbings = dubbingLoaderClient.loadDubbingByDocIds(dbid_lvid_map.keySet());

        List<Map<String, Object>> results = new ArrayList<>();

        for (String id : dbid_lvid_map.keySet()) {
            Dubbing dubbing = dubbings.getOrDefault(id, null);
            if (dubbing == null) continue;

            Map<String, Object> result = new HashMap<>();
            result.put("id", SafeConverter.toString(dbid_lvid_map.get(id)));
            result.put("coverUrl", dubbing.getCoverUrl());
            result.put("trial", true);
            result.put("name", dubbing.getVideoName());

            results.add(result);
        }

        return results;
    }

    private List<Map<String, Object>> fetchElv(Long studentId, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) return Collections.emptyList();

        Map<String, PictureBookPlus> books = userPicBookServiceClient.loadPicBooks(ids, pictureBookPlusServiceClient)
                .stream().collect(Collectors.toMap(PictureBookPlus::getId, Function.identity()));
        Function<PictureBookPlus, Map<String, Object>> function = getPicBookMapper(studentId, "ELevelReading", ids, null, getLevelConfigContent());

        List<Map<String, Object>> results = new ArrayList<>();
        for (String id : ids) {
            PictureBookPlus book = books.getOrDefault(id, null);
            if (book == null) continue;
            Map<String, Object> map = function.apply(book);
            if (MapUtils.isEmpty(map)) continue;

            Map<String, Object> result = new HashMap<>();
            result.put("name", "ELevelReading");
            result.put("params", MapUtils.m("jump_page", 2, "picturebook_info", map));
            result.put("coverUrl", map.getOrDefault("coverUrl", ""));

            results.add(result);
        }

        return results;
    }

    private PageBlockContent getLevelConfigContent() {
        return pageBlockContentServiceClient.getPageBlockContentBuffer().findByPageName("PicBookLevelConfig")
                .stream()
                .filter(e -> e.getDisabled() == null || !e.getDisabled())
                .filter(p -> Objects.equals("level_list", p.getBlockName()))
                .findFirst().orElse(null);
    }

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
                .loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.getType(), "picbook_u3d_mirror_host_config");
        if (StringUtils.isBlank(newHost)) {
            return url;
        }
        String regEx = "^http(s)?://(.*?)/"; //定义HTML标签的正则表达式
        Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        return matcher.replaceAll(newHost) + "?x-oss-process=image/quality,q_75/resize,w_300";

    }
}
