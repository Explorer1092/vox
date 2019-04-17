package com.voxlearning.washington.controller.open.v2.parent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSeries;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSku;
import com.voxlearning.galaxy.service.studycourse.api.entity.course.CourseStructSpu;
import com.voxlearning.galaxy.service.studycourse.api.mapper.StudyLesson;
import com.voxlearning.galaxy.service.studycourse.constant.CourseTypeEnum;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.parent.api.PalaceMuseumLoader;
import com.voxlearning.utopia.service.parent.api.cache.UnclePeiStudyCacheManager;
import com.voxlearning.utopia.service.parent.api.consumer.StudyTogetherServiceClient;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.PalaceMuseumUserPurchaseData;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.ParentJoinLessonRef;
import com.voxlearning.utopia.service.parent.api.entity.studytogether.StudyGroup;
import com.voxlearning.utopia.service.parent.api.mapper.studytogether.CardLessonMapper;
import com.voxlearning.utopia.service.parent.api.support.PalaceMuseumProductSupport;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.client.FairylandProductServiceClient;
import com.voxlearning.washington.controller.open.AbstractParentApiController;
import com.voxlearning.washington.mapper.studytogether.CardViewMapper;
import com.voxlearning.washington.support.CourseStructRouter;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.ApiConstants.REQ_STUDENT_ID;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_RESULT_NEED_RELOGIN_CODE;

/**
 * @author xinxin
 * @since 6/28/18
 */
@Controller
@RequestMapping(value = "/v2/parent/studyprogress/")
public class ParentStudyProgressApiV2Controller extends AbstractParentApiController {
    @Inject
    private StudyTogetherServiceClient studyTogetherServiceClient;
    @Inject
    private FairylandProductServiceClient fairylandProductServiceClient;

    @Inject
    private StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    @ImportService(interfaceClass = PalaceMuseumLoader.class)
    private PalaceMuseumLoader palaceMuseumLoader;

    private static List<String> lightLables = new ArrayList<>();
    private static List<String> tranningLables = new ArrayList<>();
    private static List<String> palaceLables = new ArrayList<>();

    private static final String TOBBITTITLE = "托比带你学数学";
    private static final String UNCLE_PEI_TITLE = "跟佩叔学英语：哈利波特与魔法石";

//    static {
//        lightLables.add("自主学习");
//        lightLables.add("随报随学");
//        tranningLables.add("老师伴学");
//        tranningLables.add("社群服务");
//        palaceLables.add("限时抢购");
//        palaceLables.add("年度超值套餐");
//    }

    @RequestMapping(value = "/training_card_list.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage newCardList() {
        User parent = getCurrentParent();
        if (null == parent) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "未登录");
        }
        if (parent.getId() == 20001) {
            return successMessage();
        }
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String cardListKey = "card_list";
        if (0 == studentId) {
            return successMessage().add("prompt", "全国千万小学生正在通过一起学巩固知识，参与学习训练营");
        }

        try {
            List<CardLessonMapperPlus> cardLessonMapperPluses = cardLessonMapperPlusList(studentId, parent);
            List<PalaceMuseumUserPurchaseData> palacePurchaseDataList = palaceMuseumLoader.getUserPurchaseDataByStudentId(studentId);
            boolean showPalaceEntry = CollectionUtils.isNotEmpty(palacePurchaseDataList);

            /**映射集合提前，不影响原有的数据结构*/
            List<CardViewMapper> viewMapperList = new ArrayList<>();

            /**tobbit购买信息加入列表*/
            CardViewMapper cardViewMapper = tobbitSell(studentId);
            if (cardViewMapper != null) {
                viewMapperList.add(cardViewMapper);
            }

            /**跟佩叔学英语*/
            CardViewMapper unclePei = unclePei(studentId);
            if (unclePei != null) {
                viewMapperList.add(unclePei);
            }

            if (!showPalaceEntry) {

                /**判断当前置顶数据如果不为null  则直接显示数据 （不去破坏其他业务逻辑）*/
                if (CollectionUtils.isEmpty(cardLessonMapperPluses) && CollectionUtils.isNotEmpty(viewMapperList)) {
                    return successMessage().add(cardListKey, viewMapperList)
                            .add("more", "/view/mobile/parent/17xue_train/my_train.vpage?rel=app_1_hzxq");
                }

                if (cardLessonMapperPluses == null) {
                    return successMessage().add(cardListKey, Collections.singleton(defaultMapper("每天10分钟的小进步，100万孩子正在参与的小课程", true)));
                }
                if (cardLessonMapperPluses.isEmpty()) {
                    return successMessage();
                }
            } else {
                if (cardLessonMapperPluses == null) {
                    cardLessonMapperPluses = new ArrayList<>();
                }
            }

            List<Long> seasonSeriesIdList = showSeasonSeriesIdList(palacePurchaseDataList);
            Map<Long, Integer> seriesFinishLessonCountMap = palaceMuseumLoader.loadPalaceSeriesFinishLessonCountMap(seasonSeriesIdList, studentId);
            Map<Long, Integer> seriesLessonCountMap = palaceMuseumSeriesLessonCountMap(seasonSeriesIdList);
            Map<Long, String> seasonSeriesStateMap = seasonSeriesStateMap(seasonSeriesIdList, seriesFinishLessonCountMap, seriesLessonCountMap);
            List<Long> openList = seasonSeriesIdList.stream().filter(t -> "open".equals(seasonSeriesStateMap.get(t))).collect(Collectors.toList());
            List<CardViewMapper> palaceMuseumOpeningMapperList = palaceMuseumOpeningMapperList(openList, seriesFinishLessonCountMap, seriesLessonCountMap);
            List<Long> waitingList = seasonSeriesIdList.stream().filter(t -> "waiting".equals(seasonSeriesStateMap.get(t))).collect(Collectors.toList());
            List<CardViewMapper> palaceMuseumWaitingMapperList = palaceMuseumWaitingMapperList(waitingList);
            List<Long> finishList = seasonSeriesIdList.stream().filter(t -> "finish".equals(seasonSeriesStateMap.get(t))).collect(Collectors.toList());
            List<CardViewMapper> palaceMuseumFinishedMapperList = palaceMuseumFinishedMapperList(finishList, seriesFinishLessonCountMap, seriesLessonCountMap);

            List<CardViewMapper> joinNoActiveMapperList = new LinkedList<>();
            List<CardViewMapper> activeOpenMapperList = new LinkedList<>();
            List<CardViewMapper> activeNotOpenMapperList = new LinkedList<>();
            List<CardViewMapper> finishMapperList = new LinkedList<>();
            for (CardLessonMapperPlus cardLessonMapperPlus : cardLessonMapperPluses) {
                if (!cardLessonMapperPlus.getIsOpen()) {
                    joinNoActiveMapperList.add(joinNoActiveMapper(cardLessonMapperPlus));
                } else if (cardLessonMapperPlus.getIsOpen() && cardLessonMapperPlus.lessonIsOpening()) {
                    activeOpenMapperList.add(activeOpenMapper(cardLessonMapperPlus));
                } else if (cardLessonMapperPlus.getIsOpen() && cardLessonMapperPlus.lessonNotOpen()) {
                    activeNotOpenMapperList.add(activeNotOpenMapper(cardLessonMapperPlus));
                } else if (cardLessonMapperPlus.lessonIsClosed()) {
                    finishMapperList.add(finishMapper(cardLessonMapperPlus));
                }
            }

            viewMapperList.addAll(joinNoActiveMapperList);
            viewMapperList.addAll(palaceMuseumOpeningMapperList);
            viewMapperList.addAll(activeOpenMapperList);
            viewMapperList.addAll(palaceMuseumWaitingMapperList);
            viewMapperList.addAll(activeNotOpenMapperList);
            viewMapperList.addAll(palaceMuseumFinishedMapperList);
            viewMapperList.addAll(finishMapperList);
            return successMessage().add(cardListKey, viewMapperList)
                    .add("more", "/view/mobile/parent/17xue_train/my_train.vpage?rel=app_1_hzxq");
        } catch (Exception ex) {
            logger.error("pid:{}", getCurrentParentId(), ex);
            return failMessage("系统异常");
        }
    }

    /**
     * 跟佩叔学英语
     */
    private CardViewMapper unclePei(Long sid) {
        Boolean userUnclePeiStudyFirst = UnclePeiStudyCacheManager.INSTANCE.getUserUnclePeiStudyFirst(sid);
        Boolean userUnclePeiStudyFinish = UnclePeiStudyCacheManager.INSTANCE.getUserUnclePeiStudyFinish(sid);
        Boolean userUnclePeiStudyAny = UnclePeiStudyCacheManager.INSTANCE.getUserUnclePeiStudyAny(sid);
        if (userUnclePeiStudyFinish) {
            return null;
        }
        if (userUnclePeiStudyFirst) {
            CardViewMapper cardViewMapper = new CardViewMapper();
            cardViewMapper.setTitle(UNCLE_PEI_TITLE);

            if (userUnclePeiStudyAny) {
                cardViewMapper.setBottom(new CardViewMapper.Bottom(new CardViewMapper.Text("#4A4A4A", "已完成"), null));
            } else {
                cardViewMapper.setBottom(new CardViewMapper.Bottom(new CardViewMapper.Text("#FF7459", "未完成"), null));
            }

            cardViewMapper.setIcon(CourseStructRouter.getRealCdnUrl(unclePeiIconUrl));

            CardViewMapper.Jump jump = new CardViewMapper.Jump();
            jump.setType(CardViewMapper.Jump.JumpType.H5);
            jump.setLink(wwwDomain + "/resources/apps/hwh5/uncle_pei/V1_0_0/index.html?from=6");
            jump.setType(CardViewMapper.Jump.JumpType.NATIVE);
            CardViewMapper.Jump.Extra extra = new CardViewMapper.Jump.Extra();
            extra.setFullScreen(true);
            extra.setHideTitle(true);
            extra.setUseNewCore(getClientSys());
            extra.setBrowser(null);
            extra.setOrientation("portrait");
            extra.setName("");
            extra.setUrl(wwwDomain + "/resources/apps/hwh5/uncle_pei/V1_0_0/index.html?from=6");
            jump.setExtra(extra);
            cardViewMapper.setJump(jump);
            return cardViewMapper;
        }
        return null;
    }

    /**
     * @return 列表模板对象
     * @Author mao.ruiwen
     * @Description 检测tobbit购买数据
     * @date : 2019/3/7 6:54 PM
     * @Param sid 学生id
     */
    private CardViewMapper tobbitSell(Long sid) {

        /**tobbit 上课内容*/
        AppPayMapper paidStatus = userOrderLoaderClient.getUserAppPaidStatus(OrderProductServiceType.Synchronousclassroom.name(), sid);

        /**检测购买状态*/
        if (null != paidStatus && paidStatus.hasPaid()) {
            Date expireTime = paidStatus.getExpireTime();
            String expiry = DateUtils.dateToString(expireTime, DateUtils.FORMAT_SQL_DATE);

            CardViewMapper courseCardView = new CardViewMapper();
            courseCardView.setTitle(TOBBITTITLE);

            /**检测tobbit购买是否已经过期*/
            if (!paidStatus.isActive()) {
                courseCardView.setBottom(new CardViewMapper.Bottom(new CardViewMapper.Text("#FF7459", "待续费"), null));
            } else {
                courseCardView.setBottom(new CardViewMapper.Bottom(new CardViewMapper.Text("#4A4A4A", "会员有效期 : " + expiry), null));
            }
            courseCardView.setIcon(CourseStructRouter.getRealCdnUrl(tobbitIconUrl));

            CardViewMapper.Jump jump = new CardViewMapper.Jump();

            String clientVersion = getClientVersion();
            boolean over260 = VersionUtil.compareVersion(clientVersion, "2.8.1.0") > 0;

            if (over260) {
                jump.setType(CardViewMapper.Jump.JumpType.NATIVE);
                CardViewMapper.Jump.Extra extra = new CardViewMapper.Jump.Extra();
                extra.setFullScreen(true);
                extra.setHideTitle(true);
                extra.setUseNewCore(getClientSys());
                extra.setBrowser(null);
                extra.setOrientation("landscape");
                extra.setName("");
                extra.setUrl(parentDomain + "/karp/toby/entry");
                jump.setExtra(extra);
            } else {
                jump.setType(CardViewMapper.Jump.JumpType.H5);
            }
            jump.setLink(parentDomain + "/karp/toby/entry");
            courseCardView.setJump(jump);

            return courseCardView;

        }

        return null;
    }

    /**
     * @return 是否过期 true 已过期
     * @Date 下午5:50 2018/9/21
     * @Param 过期时间
     */
//    private boolean checkExpiry(String expiry){
//        boolean isExpiry = false;  // 初始化是否过期，如果没有过期时间则永不过期。
//        /**验证是否过期*/
//        if(expiry != null){
//            String current = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE); //当前时间
//            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.FORMAT_SQL_DATE);
//
//            try {
//                Date cDate = simpleDateFormat.parse(current);
//                Date expiryDate = simpleDateFormat.parse(expiry);
//                Integer dateStatus = expiryDate.compareTo(cDate);
//
//                /**过期时间小于当前时间 则已过期*/
//                if(dateStatus < 0){
//                    isExpiry = true;
//                }
//            } catch (ParseException e) {
//                //logger.error("日期转换异常",e);
//                e.printStackTrace();
//            }
//        }
//
//        return isExpiry;
//    }
    private Map<Long, Integer> palaceMuseumSeriesLessonCountMap(List<Long> seasonSeriesIdList) {
        if (CollectionUtils.isEmpty(seasonSeriesIdList)) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> map = new HashMap<>();
        for (Long seriesId : seasonSeriesIdList) {
            CourseStructSeries courseStructSeries = studyCourseStructLoaderClient.loadCourseStructSeriesById(seriesId);
            if (courseStructSeries == null) {
                continue;
            }
            int count = 0;
            for (CourseStructSpu spu : courseStructSeries.getChildMap().values()) {
                for (CourseStructSku sku : spu.getChildMap().values()) {
                    count = count + sku.getTimes();
                }
            }
            map.put(seriesId, count);
        }
        return map;
    }

    private String palaceImg(Long seriesId) {
        return CourseStructRouter.getRealCdnUrl(PalaceMuseumProductSupport.seriesIdIconMap.get(seriesId));
    }


    private List<CardViewMapper> palaceMuseumOpeningMapperList(List<Long> seriesIdList, Map<Long, Integer> seriesFinishLessonCountMap, Map<Long, Integer> seriesLessonCountMap) {
        List<CardViewMapper> cardViewMapperList = new ArrayList<>(seriesIdList.size());
        for (Long seriesId : seriesIdList) {
            CourseStructSeries courseStructSeries = studyCourseStructLoaderClient.loadCourseStructSeriesById(seriesId);
            if (courseStructSeries == null) {
                continue;
            }
            String title = courseStructSeries.getName();
            CardViewMapper.Text text = new CardViewMapper.Text("#FF7459", "学习中");
            CardViewMapper.SubTitle subTitle = new CardViewMapper.SubTitle(text, null);
            CardViewMapper.Bottom bottom = palaceProgressBottom(seriesId, seriesFinishLessonCountMap, seriesLessonCountMap);
            CardViewMapper viewMapper = new CardViewMapper();
            viewMapper.setTitle(title);
            viewMapper.setSubTitle(subTitle);
            viewMapper.setLabels(palaceLables);
            viewMapper.setBottom(bottom);
            viewMapper.setIcon(palaceImg(seriesId));
            CardViewMapper.Jump jump = new CardViewMapper.Jump();
            jump.setType(CardViewMapper.Jump.JumpType.H5);
            jump.setLink(parentDomain + "/karp/gugong/index/solartermindex?series_id=" + seriesId + "&rel=app_1_hzxq&useNewCore=wk");
            viewMapper.setJump(jump);
            cardViewMapperList.add(viewMapper);
        }
        return cardViewMapperList;
    }

    private List<CardViewMapper> palaceMuseumWaitingMapperList(List<Long> seriesIdList) {
        List<CardViewMapper> cardViewMapperList = new ArrayList<>(seriesIdList.size());
        for (Long seriesId : seriesIdList) {
            CourseStructSeries courseStructSeries = studyCourseStructLoaderClient.loadCourseStructSeriesById(seriesId);
            if (courseStructSeries == null) {
                continue;
            }
            String title = courseStructSeries.getName();
            CardViewMapper.Text bottomText = new CardViewMapper.Text("#9C9C9C", "待开课");
            CardViewMapper.Bottom bottom = new CardViewMapper.Bottom(bottomText, null);

            CardViewMapper viewMapper = new CardViewMapper();
            viewMapper.setTitle(title);
            viewMapper.setLabels(palaceLables);
            viewMapper.setBottom(bottom);
            viewMapper.setIcon(palaceImg(seriesId));
            CardViewMapper.Jump jump = new CardViewMapper.Jump();
            jump.setType(CardViewMapper.Jump.JumpType.H5);
            jump.setLink(parentDomain + "/karp/gugong/index/defaultTip?ref=app_1_hzxq");
            viewMapper.setJump(jump);
            cardViewMapperList.add(viewMapper);
        }
        return cardViewMapperList;
    }

    private List<CardViewMapper> palaceMuseumFinishedMapperList(List<Long> seriesIdList, Map<Long, Integer> seriesFinishLessonCountMap, Map<Long, Integer> seriesLessonCountMap) {
        List<CardViewMapper> cardViewMapperList = new ArrayList<>(seriesIdList.size());
        for (Long seriesId : seriesIdList) {
            CourseStructSeries courseStructSeries = studyCourseStructLoaderClient.loadCourseStructSeriesById(seriesId);
            if (courseStructSeries == null) {
                continue;
            }
            String title = courseStructSeries.getName();
            CardViewMapper.Text text = new CardViewMapper.Text("#4A4A4A", "已结课");
            CardViewMapper.SubTitle subTitle = new CardViewMapper.SubTitle(text, null);
            CardViewMapper.Bottom bottom = palaceProgressBottom(seriesId, seriesFinishLessonCountMap, seriesLessonCountMap);
            CardViewMapper viewMapper = new CardViewMapper();
            viewMapper.setTitle(title);
            viewMapper.setSubTitle(subTitle);
            viewMapper.setLabels(palaceLables);
            viewMapper.setBottom(bottom);
            viewMapper.setIcon(palaceImg(seriesId));
            CardViewMapper.Jump jump = new CardViewMapper.Jump();
            jump.setType(CardViewMapper.Jump.JumpType.H5);
            jump.setLink(parentDomain + "/karp/gugong/index/solartermindex?series_id=" + seriesId + "&rel=app_1_hzxq");
            viewMapper.setJump(jump);
            cardViewMapperList.add(viewMapper);
        }
        return cardViewMapperList;
    }

    private CardViewMapper.Bottom palaceProgressBottom(Long seriesId, Map<Long, Integer> seriesFinishLessonCountMap, Map<Long, Integer> seriesLessonCountMap) {
        CardViewMapper.Text bottomText = CardViewMapper.Text.finishRate;
        Integer finishCount = seriesFinishLessonCountMap.get(seriesId);
        Integer totalCount = seriesLessonCountMap.get(seriesId);
        String rate;
        if (finishCount == null || finishCount == 0 || totalCount == null || totalCount == 0) {
            rate = "0";
        } else {
            rate = new BigDecimal(finishCount).divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP).toString();
        }
        CardViewMapper.Bottom.Progress progress = new CardViewMapper.Bottom.Progress(rate, finishCount + "/" + totalCount);
        return new CardViewMapper.Bottom(bottomText, progress);
    }


    private CourseStructSku seasonFirstSku(Long seriesId) {
        CourseStructSeries courseStructSeries = studyCourseStructLoaderClient.loadCourseStructSeriesById(seriesId);
        if (courseStructSeries == null) {
            return null;
        }
        return courseStructSeries.getChildMap().values().stream().map(t -> t.getChildMap().values().stream().findFirst().orElse(null))
                .min(Comparator.comparing(sku -> sku != null ? sku.getOpenDate() : new Date())).orElse(null);
    }

    private CourseStructSku seasonLastSku(Long seriesId) {
        CourseStructSeries courseStructSeries = studyCourseStructLoaderClient.loadCourseStructSeriesById(seriesId);
        if (courseStructSeries == null) {
            return null;
        }
        return courseStructSeries.getChildMap().values().stream().map(t -> t.getChildMap().values().stream().findFirst().orElse(null))
                .max(Comparator.comparing(sku -> sku != null ? sku.getOpenDate() : new Date())).orElse(null);
    }

    private Map<Long, String> seasonSeriesStateMap(List<Long> seriesIdList, Map<Long, Integer> seriesFinishLessonCountMap, Map<Long, Integer> seriesLessonCountMap) {

        Map<Long, String> seriesStateMap = new HashMap<>(seriesIdList.size());
        for (Long seriesId : seriesIdList) {
            String state = "waiting";
            CourseStructSku firstSku = seasonFirstSku(seriesId);
            CourseStructSku lastSku = seasonLastSku(seriesId);
            try {
                if (firstSku == null) {
                    continue;
                }
                if (lastSku == null) {
                    continue;
                }
                Date openDate = firstSku.getOpenDate();
                Date closeDate = lastSku.getCloseDate();
                long timeMillis = System.currentTimeMillis();
                if (timeMillis <= openDate.getTime()) {
                    state = "waiting";
                    continue;
                }
                if (openDate.getTime() <= timeMillis && timeMillis <= closeDate.getTime()) {
                    Integer lessonCount = seriesLessonCountMap.get(seriesId);
                    lessonCount = lessonCount == null ? 0 : lessonCount;
                    Integer finishCount = seriesFinishLessonCountMap.get(seriesId);
                    finishCount = finishCount == null ? 0 : finishCount;
                    if (finishCount >= lessonCount) {
                        state = "finish";
                    } else {
                        state = "open";
                    }
                    continue;
                }
                if (closeDate.getTime() <= timeMillis) {
                    state = "finish";
                }
            } finally {
                seriesStateMap.put(seriesId, state);
            }
        }
        return seriesStateMap;
    }

    private List<Long> showSeasonSeriesIdList(List<PalaceMuseumUserPurchaseData> purchaseDataList) {
        if (CollectionUtils.isEmpty(purchaseDataList)) {
            return Collections.emptyList();
        }
        //先看春季。。。
        List<Long> seriesIdList = new ArrayList<>(4);
        for (PalaceMuseumUserPurchaseData palaceMuseumUserPurchaseData : purchaseDataList) {
            switch (palaceMuseumUserPurchaseData.getPurchaseType()) {
                case PalaceMuseumProductSupport.SPUPREFIX:
                    //买的 spu，必定是立春
                    Long spuId = palaceMuseumUserPurchaseData.getAppItemIds().get(0);
                    CourseStructSpu courseStructSpu = studyCourseStructLoaderClient.loadCourseStructSpuById(spuId);
                    if (courseStructSpu != null) {
                        seriesIdList = addIfAbsent(seriesIdList, courseStructSpu.getSeriesId());
                    }
                    break;
                case PalaceMuseumProductSupport.SERIESPREFIX:
                    //买的 series 季节。
                    Long seriesId = palaceMuseumUserPurchaseData.getAppItemIds().get(0);
                    seriesIdList = addIfAbsent(seriesIdList, seriesId);
                    break;
                case PalaceMuseumProductSupport.ALLPREFIX:
                    //买的全套
                    for (Long id : palaceMuseumUserPurchaseData.getAppItemIds()) {
                        seriesIdList = addIfAbsent(seriesIdList, id);
                    }
                    break;
                default:
                    break;
            }
        }
        return seriesIdList;
    }

    private <T> List<T> addIfAbsent(List<T> list, T value) {
        if (list == null) {
            return list;
        }
        if (list.contains(value)) {
            return list;
        }
        list.add(value);
        return list;
    }

    private static String addIconUrl;
    private static String tobbitIconUrl;
    private static String unclePeiIconUrl;

    static {
        if (RuntimeMode.isUsingTestData()) {
            tobbitIconUrl = "TOBIT/test/2019/03/15/tobbit.png";
            addIconUrl = "/palace/test/2019/01/10/xunlianying_zhanwei_pic.png";
            unclePeiIconUrl = "unclePei/test/2019/03/14/uncle_pei.png";
        } else {
            addIconUrl = "/palace/2019/01/30/xunlianying_zhanwei_pic.png";
            tobbitIconUrl = "TOBIT/2019/03/15/tobbit.png";
            unclePeiIconUrl = "unclePei/2019/03/14/uncle_pei.png";
        }
    }

    private CardViewMapper defaultMapper(String text, boolean withUrl) {
        CardViewMapper defaultCardMapper = new CardViewMapper();
        String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STUDY_TOGETHER_JUMP_URL");
        defaultCardMapper.setTitle(text);
        defaultCardMapper.setType("add");
        CardViewMapper.Bottom bottom = new CardViewMapper.Bottom();
        CardViewMapper.Text bottomText = new CardViewMapper.Text("#FF7459", "马上报名 >");
        bottom.setText(bottomText);
        defaultCardMapper.setBottom(bottom);
        defaultCardMapper.setIcon(CourseStructRouter.getRealCdnUrl(addIconUrl));
        if (withUrl) {
            CardViewMapper.Jump jump = new CardViewMapper.Jump();
            jump.setType(CardViewMapper.Jump.JumpType.H5);
            jump.setLink(ProductConfig.getMainSiteBaseUrl() + configValue);
            defaultCardMapper.setJump(jump);
        }
        return defaultCardMapper;
    }

    private CardViewMapper.Jump convertJump(CardLessonMapperPlus cardLessonMapperPlus) {
        if (cardLessonMapperPlus == null) {
            return null;
        }
        CardViewMapper.Jump jump = new CardViewMapper.Jump();
        String type = cardLessonMapperPlus.getType();
        if ("H5".equalsIgnoreCase(type)) {
            jump.setType(CardViewMapper.Jump.JumpType.H5);
        } else if ("NATIVE".equalsIgnoreCase(type)) {
            jump.setType(CardViewMapper.Jump.JumpType.NATIVE);
        } else {
            jump.setType(CardViewMapper.Jump.JumpType.H5);
        }
        if (cardLessonMapperPlus.getLink() != null) {
            jump.setLink(cardLessonMapperPlus.getLink());
        }
        if (MapUtils.isNotEmpty(cardLessonMapperPlus.getExtra())) {
            CardViewMapper.Jump.Extra extra = JsonUtils.safeConvertMapToObject(cardLessonMapperPlus.getExtra(), CardViewMapper.Jump.Extra.class);
            jump.setExtra(extra);
        }
        return jump;
    }

    private CardViewMapper initCardMapper(CardLessonMapperPlus cardLessonMapperPlus) {
        CardViewMapper cardViewMapper = new CardViewMapper();
        cardViewMapper.setTitle(cardLessonMapperPlus.getName());
        cardViewMapper.setLabels(cardLessonMapperPlus.getLabelList());
        cardViewMapper.setIcon(cardLessonMapperPlus.getImg());
        cardViewMapper.setType("card");
        return cardViewMapper;
    }

    private CardViewMapper joinNoActiveMapper(CardLessonMapperPlus cardLessonMapperPlus) {
        CardViewMapper cardViewMapper = initCardMapper(cardLessonMapperPlus);

        CardViewMapper.Text text = new CardViewMapper.Text("#FF7459", "课程待激活");
        cardViewMapper.setSubTitle(new CardViewMapper.SubTitle(text, null));

        CardViewMapper.Text openDateText = new CardViewMapper.Text("#9C9C9C", cardLessonMapperPlus.getStartDate() + "开课");
        CardViewMapper.Bottom bottom = new CardViewMapper.Bottom(openDateText, null);
        cardViewMapper.setBottom(bottom);

        CardViewMapper.Jump jump = convertJump(cardLessonMapperPlus);
        cardViewMapper.setJump(jump);
        return cardViewMapper;
    }


    private CardViewMapper activeOpenMapper(CardLessonMapperPlus cardLessonMapperPlus) {
        CardViewMapper viewMapper = initCardMapper(cardLessonMapperPlus);
        CardViewMapper.Bottom bottom = finishRationBottom(cardLessonMapperPlus);
        viewMapper.setBottom(bottom);
        CardViewMapper.SubTitle subTitle;
        if (CourseTypeEnum.NEW_LIGHT.getType().equals(cardLessonMapperPlus.getNewStudyLesson().getSkuType())) {
            CardViewMapper.Text subTitleText = new CardViewMapper.Text("#FF7459", "学习中");
            subTitle = new CardViewMapper.SubTitle(subTitleText, null);
        } else {
            Boolean todayHasTask = cardLessonMapperPlus.getTodayHasTask();
            CardViewMapper.Text subTitleText = new CardViewMapper.Text();
            subTitleText.setColor("#4A4A4A");
            CardViewMapper.Star star = null;
            if (todayHasTask) {
                Integer todayTaskScore = cardLessonMapperPlus.getTodayTaskScore();
                if (todayTaskScore == -1) {
                    subTitleText.setContent("今日课程：未完成");
                } else {
                    subTitleText.setContent("今日课程");
                    star = new CardViewMapper.Star(3, todayTaskScore);
                }
            } else {
                subTitleText.setContent("暂无课程");
            }
            subTitle = new CardViewMapper.SubTitle(subTitleText, star);
        }
        viewMapper.setSubTitle(subTitle);
        viewMapper.setJump(convertJump(cardLessonMapperPlus));
        return viewMapper;
    }

    private CardViewMapper.Bottom finishRationBottom(CardLessonMapperPlus cardLessonMapperPlus) {
        Integer ratio = cardLessonMapperPlus.getRatio();
        String ratioText = cardLessonMapperPlus.getRatioText();
        CardViewMapper.Bottom.Progress progress = null;
        CardViewMapper.Text bottomText;
        if (StringUtils.isBlank(ratioText)) {
            String ratioStr = "0";
            if (ratio != null && ratio != 0) {
                BigDecimal divide = new BigDecimal(ratio).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
                ratioStr = divide.toString();
            }
            progress = new CardViewMapper.Bottom.Progress(ratioStr, cardLessonMapperPlus.getProgress());
            bottomText = CardViewMapper.Text.finishRate;
        } else {
            bottomText = new CardViewMapper.Text("#9C9C9C", ratioText);
        }
        return new CardViewMapper.Bottom(bottomText, progress);
    }

    private CardViewMapper activeNotOpenMapper(CardLessonMapperPlus cardLessonMapperPlus) {
        CardViewMapper viewMapper = initCardMapper(cardLessonMapperPlus);
        CardViewMapper.SubTitle subTitle = new CardViewMapper.SubTitle(
                new CardViewMapper.Text("#4A4A4A", "距离开课还有" + cardLessonMapperPlus.getLessonStartCountdown() + "天"),
                null
        );
        CardViewMapper.Bottom bottom = new CardViewMapper.Bottom(
                new CardViewMapper.Text("#9C9C9C", cardLessonMapperPlus.getStartDate() + "开课"),
                null
        );
        viewMapper.setSubTitle(subTitle);
        viewMapper.setBottom(bottom);
        viewMapper.setJump(convertJump(cardLessonMapperPlus));
        return viewMapper;
    }

    private CardViewMapper finishMapper(CardLessonMapperPlus cardLessonMapperPlus) {
        CardViewMapper viewMapper = initCardMapper(cardLessonMapperPlus);
        viewMapper.setSubTitle(new CardViewMapper.SubTitle(
                new CardViewMapper.Text("#4A4A4A", "已结课"), null
        ));
        viewMapper.setBottom(finishRationBottom(cardLessonMapperPlus));
        viewMapper.setJump(convertJump(cardLessonMapperPlus));
        return viewMapper;
    }


    private StudyLesson getStudyLesson(String lessonId) {
        return studyCourseStructLoaderClient.getStructTreeBuffer().getNativeBuffer().getStudyLesson(SafeConverter.toLong(lessonId));
    }

    @RequestMapping(value = "/training.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage training() {
        User parent = getCurrentParent();
        if (null == parent) {
            return failMessage(RES_RESULT_NEED_RELOGIN_CODE, "未登录");
        }

        if (parent.getId() == 20001) {
            return successMessage();
        }

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        if (0 == studentId) {
            return successMessage().add("prompt", "全国千万小学生正在通过家长通巩固知识，参与学习训练营");
        }
        try {
            List<CardLessonMapperPlus> cardLessonMapperPluses = cardLessonMapperPlusList(studentId, parent);
            if (cardLessonMapperPluses == null) {
//                List<PalaceMuseumUserPurchaseData> userPurchaseDataByStudentId = palaceMuseumLoader.getUserPurchaseDataByStudentId(studentId);
//                boolean showMore = CollectionUtils.isNotEmpty(userPurchaseDataByStudentId);
                String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "STUDY_TOGETHER_JUMP_URL");
                MapMessage message = successMessage()
                        .add("study_together_url", ProductConfig.getMainSiteBaseUrl() + configValue)
                        .add("prompt", "每天10分钟的小进步，100万孩子正在参与的小课程");
//                if (showMore){
                message.add("more", "/view/mobile/parent/17xue_train/my_train.vpage");
//                }
                return message;
            }
            if (cardLessonMapperPluses.isEmpty()) {
                return successMessage();
            }

            MapMessage message = successMessage()
                    .add("lessons", cardLessonMapperPluses)
                    .add("more", "/view/mobile/parent/17xue_train/my_train.vpage");
            String configValue = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.PRIMARY_PLATFORM_GENERAL.name(), "PARENT_UPGRADE_260");
            if (SafeConverter.toBoolean(configValue)) {
                String updateUrl = "https://www.17zuoye.com/view/mobile/common/download?app_type=17parent&cid=203026";
                message.add("recommend", MapUtils.m("title", "学习更多课程，建议升级至最新版本~", "url", updateUrl));
            }
            return message;
        } catch (Exception ex) {
            logger.error("pid:{}", getCurrentParentId(), ex);
            return failMessage("系统异常");
        }
    }

    private List<CardLessonMapperPlus> cardLessonMapperPlusList(Long studentId, User parent) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null || studentDetail.isJuniorStudent() || studentDetail.isSeniorStudent()) {
            return Collections.emptyList();
        }
        String clientVersion = getClientVersion();
        boolean over260 = VersionUtil.compareVersion(clientVersion, "2.6.0.0") > 0;
        Map<String, ParentJoinLessonRef> parentJoinLessonRefMap = studyTogetherServiceClient.loadParentJoinLessonRefs(parent.getId());
        List<StudyGroup> studentStudyGroupList = studyTogetherServiceClient.loadStudentActiveLessonGroups(studentId);
        Set<String> joinLessonIdSet = parentJoinLessonRefMap.keySet();
        Set<String> studentActiveLessonIdSet = studentStudyGroupList.stream().map(StudyGroup::getLessonId).collect(Collectors.toSet());
        Set<String> allLessonIdList = new HashSet<>(joinLessonIdSet);
        allLessonIdList.addAll(studentActiveLessonIdSet);
        //a
        List<StudyLesson> activeClosedLessonList = Lists.newArrayList();

        //b
        List<StudyLesson> activeStudyingLessonList = Lists.newArrayList();

        //c
        List<StudyLesson> activeNoOpenLessonList = Lists.newArrayList();

        //d
        List<StudyLesson> joinNoActiveLeesonList = Lists.newArrayList();

        for (String lessonId : allLessonIdList) {
            boolean isActive = studentActiveLessonIdSet.contains(lessonId);
            boolean isJoin = joinLessonIdSet.contains(lessonId);
            StudyLesson studyLesson = getStudyLesson(lessonId);
            if (studyLesson == null || studyLesson.getParent().getParent().isPalaceMuseumSeries()) {
                continue;
            }
            //老版本不显示真轻课
            if (!over260 && CourseTypeEnum.NEW_LIGHT.getType().equals(studyLesson.getSkuType())) {
                continue;
            }
            if (isActive) {
                if (studyLesson.isClosed()) {
                    activeClosedLessonList.add(studyLesson);
                } else if (studyLesson.isStudying()) {
                    activeStudyingLessonList.add(studyLesson);
                } else
                    activeNoOpenLessonList.add(studyLesson);
            } else if (isJoin) {
                if (studyLesson.safeIsLightSpu()) {
                    if (studyLesson.inSignUpPeriod()) {
                        joinNoActiveLeesonList.add(studyLesson);
                    }
                } else {
                    if (!studyLesson.safeIsLightSpu() && DateUtils.calculateDateDay(studyLesson.getOpenDate(), 5).after(new Date())) {
                        joinNoActiveLeesonList.add(studyLesson);
                    }
                }
            }
        }

        if (CollectionUtils.isEmpty(activeClosedLessonList) && CollectionUtils.isEmpty(activeStudyingLessonList)
                && CollectionUtils.isEmpty(activeNoOpenLessonList) && CollectionUtils.isEmpty(joinNoActiveLeesonList)) {
            return null;
        }
        Set<StudyLesson> lessonSet = Sets.newHashSet();
        if (CollectionUtils.isNotEmpty(joinNoActiveLeesonList) || CollectionUtils.isNotEmpty(activeNoOpenLessonList)
                || CollectionUtils.isNotEmpty(activeStudyingLessonList)) {
            lessonSet.addAll(joinNoActiveLeesonList);
            lessonSet.addAll(activeNoOpenLessonList);
            lessonSet.addAll(activeStudyingLessonList);
        } else {
            activeClosedLessonList.stream().sorted((o1, o2) -> o2.getCloseDate().compareTo(o1.getCloseDate())).findFirst().ifPresent(lessonSet::add);
        }

        List<CardLessonMapperPlus> mappers = generateLessonMapper(studentId, joinLessonIdSet, studentActiveLessonIdSet, lessonSet);
        return sortLessons(mappers);
    }

    @Getter
    @Setter
    private static class CardLessonMapperPlus extends CardLessonMapper {

        private static final long serialVersionUID = 4417134598274663829L;
        @JsonProperty("labels")
        private List<String> labelList;
        private String progress;

        @JsonIgnore
        private StudyLesson newStudyLesson;

        @JsonIgnore
        public boolean lessonIsClosed() {
            return "over".equalsIgnoreCase(getLessonStatus());
        }

        @JsonIgnore
        public boolean lessonIsOpening() {
            return "open".equals(getLessonStatus());
        }

        @JsonIgnore
        public boolean lessonNotOpen() {
            return "before".equals(getLessonStatus());
        }
    }


    private static String parentDomain;
    private static String wwwDomain;

    static {
        if (RuntimeMode.isProduction()) {
            parentDomain = "https://parent.17zuoye.com";
            wwwDomain = "https://www.17zuoye.com";
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            parentDomain = "https://parent.staging.17zuoye.net";
            wwwDomain = "https://www.staging.17zuoye.net";
        } else if (RuntimeModeLoader.getInstance().isUsingTestData()) {
            parentDomain = "https://parent.test.17zuoye.net";
            wwwDomain = "https://www.test.17zuoye.net";
        } else {
            parentDomain = "https://parent.17zuoye.com";
            wwwDomain = "https://www.17zuoye.com";
        }
    }


    private List<CardLessonMapperPlus> generateLessonMapper(Long studentId, Set<String> joinLessonIdSet, Set<String> studentActiveLessonIdSet, Collection<StudyLesson> lessons) {
        if (CollectionUtils.isEmpty(lessons)) {
            return new ArrayList<>();
        }

        Set<String> lessonIds = lessons.stream().map(t -> SafeConverter.toString(t.getLessonId())).collect(Collectors.toSet());
        Map<String, Integer> lessonFinishProgressMap = studyTogetherServiceClient.loadStudentLessonFinishProgress(studentId, lessonIds);
        Map<String, Integer> finishCountMap = studyTogetherServiceClient.getStudyTogetherHulkService().loadStudentSkuFinishLessonCount(studentId, lessonIds);
        List<CardLessonMapperPlus> mappers = new ArrayList<>();
        Date t = new Date();
        for (StudyLesson lesson : lessons) {
            if (lesson.getShowDate().after(t)) {
                continue;
            }
            String lessonId = SafeConverter.toString(lesson.getLessonId());
            boolean isJoin = joinLessonIdSet.contains(lessonId);
            boolean isOpen = studentActiveLessonIdSet.contains(lessonId);

            CardLessonMapperPlus mapper = new CardLessonMapperPlus();
            if (isOpen) {
                if (lesson.getCourseType() == 3) {
                    mapper.setType("H5");
                    // -----李路伟 修改-----
                    // 因为nodejs 新单页框架 对URL敏感。 但是因为客户端在拼接URL的时候也加入了一个 / 因此链接就会变成 
                    // www.test.17zuoye.net//karp  为了稳定以及前端处理逻辑单一原则 我们希望给出的URL都是 标准URL
                    // 将来解决办法 a: 客户端拼接字符串做兼容 b: 运维那边调研可以的话，nginx帮我们做301
                    mapper.setLink("karp/chinese_reading/index/read_index?useNewCore=wk&course_id=" + lessonId + "&rel=hzxqtraining");
                } else if (lesson.getCourseType() == 5) {
                    mapper.setType("H5");
                    String url = "/view/mobile/parent/math_program/index.vpage?lesson_id=";
                    if (lessonId.equals("18001") || lessonId.equals("19001") || lessonId.equals("18002") || lessonId.equals("19002")) {
                        url = "/view/mobile/parent/math_program/index_two.vpage?lesson_id=";
                    }
                    mapper.setLink(url + lessonId);
                }else if(lesson.getCourseType() == 7){
                    mapper.setType("H5");
                    String url = "/karp/course_components/index/map_project/index?useNewCore=wk&sku_id="+lessonId+"&rel=hzxqtraining";
                    mapper.setLink(parentDomain +url);
                } else {
                    //已激活，跳转到上课页面
                    mapper.setType("NATIVE");

                    //构造url
                    String project = lesson.getCourseType() == 2 ? "learnenglishtogether" : "learntogether";

                    String url = parentDomain + "/parentMobile/study_together/report/share.vpage?project=" + project + "&action=index&hash=%3FkeepHash%3Dtrue&course_id=" + lessonId + "&sid=" + studentId;

                    Map<String, Object> ext = new HashMap<>();
                    ext.put("url", url);
                    ext.put("name", lesson.getTitle());
                    ext.put("orientation", "portrait");
                    ext.put("useNewCore", "crossWalk");
                    ext.put("fullScreen", true);
                    ext.put("hideTitle", true);
                    if (clientIsAndroid()) {
                        ext.put("useNewCore", "crosswalk");
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("webview_screen_on", true);
                    ext.put("initParams", JsonUtils.toJson(map));
                    mapper.setExtra(ext);
                }
            } else if (isJoin) {
                //已报名未激活跳转到激活页
                mapper.setType("H5");
                if (lesson.safeGetActiveType() == 3) {
                    mapper.setLink("/view/mobile/parent/17xue_activate/index2.vpage?sid=" + studentId + "&lesson_id=" + lessonId);
                } else {
                    mapper.setLink("/view/mobile/parent/17xue_activate/index.vpage?sid=" + studentId + "&lesson_id=" + lessonId);
                }
            } else {
                //未报名，跳到报名页
                mapper.setType("H5");
                mapper.setLink("/view/mobile/parent/17xue_group/enroll.vpage?rel=0&lesson_id=" + lessonId);
            }
            mapper.setNewStudyLesson(lesson);
            mapper.setCloseDate(lesson.getCloseDate());
            mapper.setSignUpEndDate(lesson.getSighUpEndDate());
            mapper.setLessonId(lessonId);
            mapper.setCourseType(lesson.getCourseType());
            mapper.setName(lesson.getTitle());
            mapper.setStartDate(DateUtils.dateToString(lesson.getOpenDate(), "M月dd日"));
            mapper.setIsJoin(isJoin);
            mapper.setIsOpen(isOpen);
            mapper.setImg(CourseStructRouter.getRealCdnUrl(lesson.getIcon()));
            mapper.setLessonStatus(getStatus(lesson.getOpenDate(), lesson.getCloseDate()));
            mapper.setTodayHasTask(lesson.dateListContainsDay(DayRange.current()));
            mapper.setLessonStartCountdown(DateUtils.dayDiff(lesson.getOpenDate(), new Date()) + 1);
            if (lesson.ratioExpire()) {
                mapper.setRatioText("查看学习成果");
            } else {
                Integer ratio = lessonFinishProgressMap.getOrDefault(lessonId, 0);
                if (ratio == -1) {
                    mapper.setRatioText("完成课程可获得丰富奖励哦");
                } else {
                    if (ratio == 100) {
                        mapper.setLessonStatus("over");
                    }
                    mapper.setRatio(ratio);
                    Integer finishCount = finishCountMap.get(lessonId);
                    Integer times = lesson.getTimes();
                    mapper.setProgress(finishCount + "/" + times);
                }
            }
            Map<String, Integer> finishInfoMap = studyTogetherServiceClient.loadStudentTodayFinishInfo(lessonId, studentId).getUninterruptibly();
            if (MapUtils.isEmpty(finishInfoMap) || !finishInfoMap.containsKey("star")) {
                mapper.setTodayTaskScore(-1);
            } else {
                mapper.setTodayTaskScore(SafeConverter.toInt(finishInfoMap.get("star")));
            }
            if (lesson.safeIsLightSpu() || SafeConverter.toInt(lesson.getParent().getType()) == 1 || SafeConverter.toInt(lesson.getSkuType()) == 3) {
                mapper.setLabelList(lightLables);
            } else {
                mapper.setLabelList(tranningLables);
            }

            mappers.add(mapper);
        }
        return mappers;
    }


    private List<CardLessonMapperPlus> sortLessons(List<CardLessonMapperPlus> mappers) {
        //已激活已开课
        List<CardLessonMapperPlus> activatedAndStartedLessons = mappers.stream().filter(mapper -> mapper.getIsOpen() && "open".equals(mapper.getLessonStatus())).collect(Collectors.toList());
        //已激活未开课
        List<CardLessonMapperPlus> activatedAndNotStartedLessons = mappers.stream().filter(mapper -> mapper.getIsOpen() && "before".equals(mapper.getLessonStatus())).collect(Collectors.toList());
        //已报名未激活
        List<CardLessonMapperPlus> joinedAndNotActivatedLessons = mappers.stream().filter(mapper -> mapper.getIsJoin() && !mapper.getIsOpen()).collect(Collectors.toList());
        //已激活已结课
        List<CardLessonMapperPlus> activatedAndFinishedLessons = mappers.stream().filter(mapper -> mapper.getIsOpen() && "over".equals(mapper.getLessonStatus())).collect(Collectors.toList());

        //未结果的距离结课时间越小的排名在前
        activatedAndStartedLessons = activatedAndStartedLessons.stream()
                .sorted(Comparator.comparing(CardLessonMapper::getCloseDate))
                .collect(Collectors.toList());
        activatedAndNotStartedLessons = activatedAndNotStartedLessons.stream()
                .sorted(Comparator.comparing(CardLessonMapper::getCloseDate))
                .collect(Collectors.toList());
        joinedAndNotActivatedLessons = joinedAndNotActivatedLessons.stream()
                .sorted(Comparator.comparing(CardLessonMapper::getCloseDate))
                .collect(Collectors.toList());
        //已结课的结课时间越小的排名在前
        activatedAndFinishedLessons = activatedAndFinishedLessons.stream()
                .sorted(Comparator.comparing(CardLessonMapper::getCloseDate))
                .collect(Collectors.toList());

        List<CardLessonMapperPlus> result = new LinkedList<>();
        if (CollectionUtils.isNotEmpty(activatedAndStartedLessons)) {
            result.addAll(activatedAndStartedLessons);
        }
        if (CollectionUtils.isNotEmpty(activatedAndNotStartedLessons)) {
            result.addAll(activatedAndNotStartedLessons);
        }
        if (CollectionUtils.isNotEmpty(joinedAndNotActivatedLessons)) {
            result.addAll(joinedAndNotActivatedLessons);
        }
        if (CollectionUtils.isNotEmpty(activatedAndFinishedLessons)) {
            result.addAll(activatedAndFinishedLessons);
        }
        return result;
    }

    private String getStatus(Date start, Date end) {
        if (start == null || end == null) {
            return "over";
        }
        Date date = new Date();
        if (date.before(start)) {
            return "before";
        }
        if (date.before(end)) {
            return "open";
        }
        return "over";
    }

}
