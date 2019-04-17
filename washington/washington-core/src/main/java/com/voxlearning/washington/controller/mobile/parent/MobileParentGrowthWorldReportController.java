package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.athena.api.GrowthWorldService;
import com.voxlearning.athena.bean.GrowthWorldReportDetailCount;
import com.voxlearning.athena.bean.GrowthWorldReportTotalCount;
import com.voxlearning.athena.bean.GrowthWorldReportWeekData;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardBufferLoaderClient;
import com.voxlearning.utopia.service.parentreward.api.ParentRewardLoader;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardBusinessType;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardCategory;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardItem;
import com.voxlearning.utopia.service.parentreward.api.entity.ParentRewardLog;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.userlevel.api.mapper.UserActivationLevel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.MonthDay;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: wei.jiang
 * @Date: Created on 2017/12/14
 */
@Controller
@RequestMapping(value = "/parentMobile/growthWorldReport")
@Slf4j
public class MobileParentGrowthWorldReportController extends AbstractMobileParentController {


    @Inject
    private ParentRewardBufferLoaderClient parentRewardBufferLoaderClient;
    @ImportService(interfaceClass = GrowthWorldService.class)
    private GrowthWorldService growthWorldService;
    @ImportService(interfaceClass = ParentRewardLoader.class)
    private ParentRewardLoader parentRewardLoader;

    @RequestMapping(value = "/currentTermData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCurrentTermData() {
        long sid = getRequestLong("sid");
        boolean isWeekData = getRequestBool("is_week_data");
        long weekMondayDate = getRequestLong("week_monday");
        if (sid == 0L) {
            return MapMessage.errorMessage();
        }
        if (isWeekData && weekMondayDate == 0L) {
            return MapMessage.errorMessage();
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        List<String> weekMondays = getWeekMondays(4, isWeekData, weekMondayDate);
        if (studentDetail == null || studentDetail.getClazz() == null) {
            return MapMessage.errorMessage().setInfo("该学生未加入班级");
        }
        String schoolYear = studentDetail.getClazz().getJie();
        List<GrowthWorldReportWeekData> weekDataList = growthWorldService.loadStudentWeeksData(sid, weekMondays, schoolYear);
//        List<GrowthWorldReportWeekData> weekDataList = new ArrayList<>();
        String bigDataJson = JsonUtils.toJson(weekDataList);
        if (CollectionUtils.isEmpty(weekDataList)) {
            return MapMessage.errorMessage();
        }
        weekDataList = weekDataList.stream().filter(e -> StringUtils.isNotBlank(e.getWeekYear())).sorted(Comparator.comparing(o -> DateUtils.stringToDate(o.getWeekYear(), DateUtils.FORMAT_SQL_DATE))).collect(Collectors.toList());
        List<Map<String, Object>> weekDataReturn = new ArrayList<>();
        weekDataList.forEach(e -> {
            Map<String, Object> weekData = new HashMap<>();
            String weekDate = generateDateString(e.getWeekYear());
            weekData.put("week_date", weekDate);
            weekData.put("self_week_data", SafeConverter.toInt(e.getSelfData()));
            weekData.put("country_week_data", SafeConverter.toInt(e.getCountryData()));
            weekDataReturn.add(weekData);
        });

        //查看自学报告加家长活跃值
        userLevelService.parentViewSelfStudyReport(currentUserId());

        MapMessage message = MapMessage.successMessage().add("week_data_list", weekDataReturn).add("big_data_json", bigDataJson);

        Map<String, Object> popInfo = getActivationInfoForPopup(currentUserId(), studentDetail);
        message.add("popInfo", popInfo);
        return message;
    }

    private Map<String, Object> getActivationInfoForPopup(Long parentId, StudentDetail studentDetail) {
        Map<String, Object> popInfo = new HashMap<>();

        Long count = washingtonCacheSystem.CBS.persistence.incr("PARENT_VIEW_SELF_STUDY_REPORT_COUNT_" + currentUserId(), 1, 1, DateUtils.getCurrentToDayEndSecond());
        if (null != count && count == 1) {
            popInfo.put("pop", true);
            popInfo.put("title", "关注孩子的每一次学习");
            popInfo.put("count", count);
            popInfo.put("action", "查看自学报告");
            popInfo.put("value", 2);
            UserActivationLevel parentLevel = userLevelLoader.getParentLevel(currentUserId());
            if (null != parentLevel) {
                popInfo.put("level", parentLevel.getLevel());
                popInfo.put("levelName", parentLevel.getName());
                popInfo.put("activation", parentLevel.getValue());
                popInfo.put("maxActivation", parentLevel.getLevelEndValue() + 1);
                popInfo.put("minActivation", parentLevel.getLevelStartValue());
            }
        } else {
            popInfo.put("pop", false);
        }
        return popInfo;
    }

    @RequestMapping(value = "/totalCount.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTotalCount() {
        long sid = getRequestLong("sid");
        boolean isWeekData = getRequestBool("is_week_data");
        long weekMondayDate = getRequestLong("week_monday");
        if (sid == 0L) {
            return MapMessage.errorMessage();
        }
        if (isWeekData && weekMondayDate == 0L) {
            return MapMessage.errorMessage();
        }
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null || studentDetail.getClazz() == null) {
            return MapMessage.errorMessage().setInfo("该学生未加入班级");
        }
        String schoolYear = studentDetail.getClazz().getJie();
        List<String> weekMondays = getWeekMondays(3, isWeekData, weekMondayDate);
        List<GrowthWorldReportTotalCount> reportTotalCounts = growthWorldService.loadStudentWeeksTotalCount(sid, weekMondays, schoolYear);
        String totalCountJson = JsonUtils.toJson(reportTotalCounts);
        List<GrowthWorldReportDetailCount> reportDetailCounts = growthWorldService.loadStudentWeeksDetailByIsland(sid, weekMondays, schoolYear);
        String detailCountJson = JsonUtils.toJson(reportDetailCounts);
        if (CollectionUtils.isEmpty(reportTotalCounts)) {
            return MapMessage.errorMessage("total count list is empty!");
        }
        if (CollectionUtils.isEmpty(reportDetailCounts)) {
            return MapMessage.errorMessage("detail count list is empty");
        }
        reportTotalCounts = reportTotalCounts.stream().sorted(Comparator.comparing(o -> DateUtils.stringToDate(o.getWeekYear(), DateUtils.FORMAT_SQL_DATE))).collect(Collectors.toList());
        Map<String, Object> previousWeekData = new HashMap<>();
        GrowthWorldReportTotalCount totalCount = reportTotalCounts.get(1);
        List<Map<String, Object>> previousAbilityList = new ArrayList<>();
        List<Map<String, Object>> currentAbilityList = new ArrayList<>();
        if (isWeekData) {
            GrowthWorldReportTotalCount preTotalCount = reportTotalCounts.get(1);
            totalCount = reportTotalCounts.get(2);
            previousWeekData.put("previous_count_up", Integer.compare(totalCount.getQuestionCount() != null ? totalCount.getQuestionCount() : 0, preTotalCount.getQuestionCount() != null ? preTotalCount.getQuestionCount() : 0));
            previousWeekData.put("previous_accuracy_up", Double.compare(totalCount.getQuestionAccuracy() != null ? totalCount.getQuestionAccuracy() : 0, preTotalCount.getQuestionAccuracy() != null ? preTotalCount.getQuestionAccuracy() : 0));
        } else {
            GrowthWorldReportTotalCount currentTotalCount = reportTotalCounts.get(2);
            previousWeekData.put("current_count", SafeConverter.toInt(currentTotalCount.getQuestionCount()));
            previousWeekData.put("current_accuracy", SafeConverter.toDouble(currentTotalCount.getQuestionAccuracy()));
            previousWeekData.put("current_days", SafeConverter.toInt(currentTotalCount.getDays()));
            //取本周的能力值
            reportDetailCounts = reportDetailCounts
                    .stream()
                    .filter(e -> WeekRange.current().getStartDate()
                            .equals(DateUtils.stringToDate(e.getWeekYear(), DateUtils.FORMAT_SQL_DATE)))
                    .collect(Collectors.toList());
        }
        reportDetailCounts.forEach(e -> {
            Map<String, Object> detailMap = new HashMap<>();
            detailMap.put("name", SafeConverter.toString(e.getName()));
            detailMap.put("count", SafeConverter.toDouble(e.getAbilityCount()));
            if (DateUtils.stringToDate(e.getWeekYear(), DateUtils.FORMAT_SQL_DATE)
                    .equals(DateUtils.stringToDate(weekMondays.get(1), DateUtils.FORMAT_SQL_DATE))) {
                previousAbilityList.add(detailMap);
            } else if (DateUtils.stringToDate(e.getWeekYear(), DateUtils.FORMAT_SQL_DATE)
                    .equals(DateUtils.stringToDate(weekMondays.get(0), DateUtils.FORMAT_SQL_DATE))) {
                currentAbilityList.add(detailMap);
            }
        });
        previousWeekData.put("previous_count", SafeConverter.toInt(totalCount.getQuestionCount()));
        previousWeekData.put("previous_accuracy", SafeConverter.toDouble(totalCount.getQuestionAccuracy()));
        previousWeekData.put("previous_days", SafeConverter.toInt(totalCount.getDays()));
        previousWeekData.put("current_ability_list", currentAbilityList);
        previousWeekData.put("previous_ability_list", previousAbilityList);
        return MapMessage.successMessage().add("count_map", previousWeekData).add("total_count_json", totalCountJson).add("detail_count_json", detailCountJson);
    }


    @RequestMapping(value = "/getCountDetail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getCountDetail() {
        long sid = getRequestLong("sid");
        boolean isWeekData = getRequestBool("is_week_data");
        long weekMondayDate = getRequestLong("week_monday");
        if (sid == 0L) {
            return MapMessage.errorMessage();
        }
        if (isWeekData && weekMondayDate == 0L) {
            return MapMessage.errorMessage();
        }
        List<String> weekMondays = getWeekMondays(1, isWeekData, weekMondayDate);
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(sid);
        if (studentDetail == null || studentDetail.getClazz() == null) {
            return MapMessage.errorMessage().setInfo("该学生未加入班级");
        }
        String schoolYear = studentDetail.getClazz().getJie();
        Boolean isInBlackList = userBlacklistServiceClient.isInBlackListByStudent(Collections.singleton(studentDetail)).getOrDefault(sid, false);
        List<Map<String, Object>> returnList = new ArrayList<>();
        List<GrowthWorldReportDetailCount> isLandDetailCount = growthWorldService.loadStudentWeeksDetailByIsland(sid, weekMondays, schoolYear);
        String isLandDetailCountJson = JsonUtils.toJson(isLandDetailCount);
        GrowthWorldReportTotalCount totalCount = growthWorldService.loadStudentWeeksTotalCount(sid, weekMondays, schoolYear).stream().findFirst().orElse(null);
        String totalCountJson = JsonUtils.toJson(totalCount);
        if (RuntimeMode.le(Mode.STAGING) && (sid == 380006774L || sid == 387279747L)) {
            isLandDetailCount = mockList();
        }
        isLandDetailCount = sortDetailList(isLandDetailCount);
        //FIXME:取家长奖励
        //取时间戳传进去
        long weekMondayTime = DateUtils.stringToDate(weekMondays.get(0), DateUtils.FORMAT_SQL_DATE).getTime();
        List<ParentRewardLog> rewardLogList = parentRewardLoader.getLastWeekParentRewardLogs(sid, weekMondayTime);
        Set<ParentRewardItem> items = new HashSet<>();
        Map<String, ParentRewardItem> itemMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(rewardLogList)) {
            rewardLogList.forEach(e -> {
                ParentRewardItem parentRewardItem = parentRewardBufferLoaderClient.getParentRewardItem(e.getKey());
                ParentRewardBusinessType businessType = ParentRewardBusinessType.of(parentRewardItem.getBusiness());
                if (businessType == null) {
                    return;
                }
                switch (businessType) {
                    case GW_CHINESE_ISLAND:
                        items.add(parentRewardItem);
                        return;
                    case GW_ENGLISH_ISLAND:
                        items.add(parentRewardItem);
                        return;
                    case GW_MATH_ISLAND:
                        items.add(parentRewardItem);
                        return;
                    case GW_WISDOM_ISLAND:
                        items.add(parentRewardItem);
                        return;
                    case GW_COMPETITION_ISLAND:
                        items.add(parentRewardItem);
                        return;
                    case AFENTI_ENGLISH:
                        items.add(parentRewardItem);
                        return;
                    case AFENTI_CHINESE:
                        items.add(parentRewardItem);
                        return;
                    case AFENTI_MATH:
                        items.add(parentRewardItem);
                        return;
                    default:
                }
            });
        }
        if (CollectionUtils.isNotEmpty(items)) {
            itemMap = items.stream().collect(Collectors.toMap(ParentRewardItem::getKey, Function.identity()));
            Set<String> itemKeys = items.stream().map(ParentRewardItem::getKey).collect(Collectors.toSet());
            rewardLogList = rewardLogList.stream().filter(p -> itemKeys.contains(p.getKey())).collect(Collectors.toList());
        }
        //算出学科岛中，做题数不为0的岛的个数
        long zeroIslandCount = isLandDetailCount.stream().filter(e -> GrowthIslandType.getSubjectIslands().contains(e.getName()) && SafeConverter.toInt(e.getQuestionCount()) != 0).count();
        for (GrowthWorldReportDetailCount e : isLandDetailCount) {
            Map<String, Object> returnMap = new HashMap<>();
            GrowthIslandType typeByDesc = GrowthIslandType.getTypeByDesc(e.getName());
            returnMap.put("name", SafeConverter.toString(e.getName()));
            returnMap.put("current_data", SafeConverter.toInt(e.getQuestionCount()));
            if (isLandDetailCount.indexOf(e) == 0 && SafeConverter.toInt(e.getQuestionCount()) != 0) {
                returnMap.put("is_favorite", Boolean.TRUE);
            }
            //如果第一个岛做题量是0，则说明，整个list的做题量都是0
            if (isLandDetailCount.indexOf(e) == 1
                    && SafeConverter.toInt(isLandDetailCount.get(0).getQuestionCount()) != 0
                    && zeroIslandCount != 0L) {
                returnMap.put("is_improve", Boolean.TRUE);
                if (typeByDesc != null && typeByDesc.getStudyType() != null && !isInBlackList) {
                    Map<String, Object> productMap = generateAdvanceProduct(sid, typeByDesc);
                    returnMap.putAll(productMap);
                }
            }
            boolean showScoreLevel = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "ShowScoreLevel", "WhiteList");
            List<Map<String, Object>> rewardList = new ArrayList<>();
            if (typeByDesc != null && CollectionUtils.isNotEmpty(typeByDesc.getBusinessTypes()) && CollectionUtils.isNotEmpty(rewardLogList)) {
                Map<String, ParentRewardItem> finalItemMap = itemMap;
                Set<String> IslandItemKeys = items.stream()
                        .filter(i -> ParentRewardBusinessType.of(i.getBusiness()) != null
                                && typeByDesc.getBusinessTypes().contains(ParentRewardBusinessType.of(i.getBusiness())))
                        .map(ParentRewardItem::getKey)
                        .collect(Collectors.toSet());
                rewardLogList.stream()
                        .filter(p -> IslandItemKeys.contains(p.getKey()))
                        .sorted(Comparator.comparing(ParentRewardLog::getCreateTime))
                        .forEach(log -> {
                            Map<String, Object> rewardMap = new HashMap<>();
                            ParentRewardItem parentRewardItem = finalItemMap.get(log.getKey());
                            if (parentRewardItem != null) {
                                ParentRewardCategory category = parentRewardBufferLoaderClient.getParentRewardCategory(parentRewardItem.getCategoryId());
                                if (category != null) {
                                    rewardMap.put("url", SafeConverter.toString(parentRewardItem.getIcon()));
                                    rewardMap.put("name", getParentRewardRealBusinessName(log, parentRewardItem, category, showScoreLevel));
                                    rewardMap.put("color", parentRewardItem.getColor());
                                    rewardMap.put("date", SafeConverter.toString(DateUtils.dateToString(log.getCreateTime(), "MM-dd")));
                                    rewardList.add(rewardMap);
                                }
                            }
                        });
            }
            returnMap.put("parent_reward_list", rewardList);
            returnMap.put("parent_reward_data", rewardList.size());
            returnList.add(returnMap);
        }
        StringBuilder summaryText = new StringBuilder();
        if (isWeekData) {
            summaryText = new StringBuilder("在上周的自学中，{0}参与了{1}个学科岛的学习，");
            long count = isLandDetailCount.stream().filter(e -> SafeConverter.toInt(e.getQuestionCount()) != 0).count();
            summaryText = new StringBuilder(MessageFormat.format(summaryText.toString(), studentDetail.fetchRealnameIfBlankId(), count));
            String rewardText;
            if (rewardLogList.size() > 0) {
                rewardText = "共获得{0}次自学家长奖励，";
                rewardText = MessageFormat.format(rewardText, rewardLogList.size());
            } else {
                rewardText = "很遗憾没有获得自学家长奖励，";
            }
            summaryText.append(rewardText);
            String beyondText = "经鉴定，上周表现超过全国{0}%同年级学生";
            if (totalCount.getBeyondNum() != null) {
                beyondText = MessageFormat.format(beyondText, totalCount.getBeyondNum());
                summaryText.append(beyondText);
            }
        }
        return MapMessage.successMessage().add("previous_summary_text", summaryText.toString()).add("detail_data_list", returnList)
                .add("isLand_detail_count_json", isLandDetailCountJson)
                .add("total_count_json", totalCountJson);
    }

    private String generateDateString(String s) {
        Date startDate = DateUtils.stringToDate(s, "yyyy-MM-dd");
        //开始日期改为学期开始的时间：上学期开始日：09.01，下学期开始日：02.26
        Date endDate = DateUtils.addDays(startDate, 6);
        String weekStartDay = DateUtils.dateToString(startDate, "--MM-dd");
        String start;
        if (MonthDay.parse(weekStartDay).compareTo(MonthDay.parse("--09-01")) < 0 && MonthDay.parse(weekStartDay).compareTo(MonthDay.parse("--02-26")) >= 0) {
            start = "02.26";
        } else {
            start = "09.01";
        }
        String end = DateUtils.dateToString(endDate, "MM.dd");
        return start + " - " + end;
    }


    private List<String> getWeekMondays(int count, boolean is_week_data, long weekTime) {
        WeekRange weekRange = WeekRange.current();
        if (is_week_data) {
            weekRange = WeekRange.newInstance(weekTime);
        }
        List<String> days = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String day = DateUtils.dateToString(weekRange.getStartDate(), "yyyy-MM-dd");
            days.add(day);
            weekRange = weekRange.previous();
        }
        return days;
    }


    private Map<String, Object> generateAdvanceProduct(long sid, GrowthIslandType growthIslandType) {
        if (sid == 0L || growthIslandType.getStudyType() == null) {
            return Collections.emptyMap();
        }
        Map<String, Object> returnMap = new HashMap<>();
        Map<SelfStudyType, DayRange> selfStudyTypeDayRangeMap = parentSelfStudyPublicHelper.moneySSTLastDayMap(sid, false);
        DayRange dayRange = selfStudyTypeDayRangeMap.get(growthIslandType.getStudyType());
        if (dayRange != null && dayRange.getEndTime() > DayRange.current().getEndTime()) {
            returnMap.put("is_pay", Boolean.TRUE);
        } else {
            returnMap.put("is_pay", Boolean.FALSE);
        }
        returnMap.put("product_name", growthIslandType.getStudyType().getDesc());
        returnMap.put("app_key", growthIslandType.getStudyType().getOrderProductServiceType());
        return returnMap;
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private enum GrowthIslandType {
        gw_english_island("英语岛", Arrays.asList(ParentRewardBusinessType.GW_ENGLISH_ISLAND, ParentRewardBusinessType.AFENTI_ENGLISH), SelfStudyType.AFENTI_ENGLISH),
        gw_math_island("数学岛", Arrays.asList(ParentRewardBusinessType.GW_MATH_ISLAND, ParentRewardBusinessType.AFENTI_MATH), SelfStudyType.AFENTI_MATH),
        gw_chinese_island("语文岛", Arrays.asList(ParentRewardBusinessType.GW_CHINESE_ISLAND, ParentRewardBusinessType.AFENTI_CHINESE), SelfStudyType.AFENTI_CHINESE),
        gw_competition_island("竞技岛", Collections.singletonList(ParentRewardBusinessType.GW_COMPETITION_ISLAND), null),
        gw_wisdom_island("智慧岛", Collections.singletonList(ParentRewardBusinessType.GW_WISDOM_ISLAND), null);


        private final String desc;
        //2017-12-27:支持多中奖励BusinessType
        private final List<ParentRewardBusinessType> businessTypes;
        //private final String abilityText;
        private final SelfStudyType studyType;

        private static final Map<String, GrowthIslandType> descMap;

        static {
            descMap = Stream.of(values()).collect(Collectors.toMap(GrowthIslandType::getDesc, Function.identity()));
        }

        public static List<String> growthIslandTypeList() {
            return Stream.of(values()).map(GrowthIslandType::getDesc).collect(Collectors.toList());
        }

        public static GrowthIslandType getTypeByDesc(String desc) {
            GrowthIslandType growthIslandType = descMap.get(desc);
            return growthIslandType != null ? growthIslandType : null;
        }

        public static List<String> getSubjectIslands() {
            return Stream.of(values()).filter(e -> e != gw_wisdom_island && e != gw_competition_island).map(GrowthIslandType::getDesc).collect(Collectors.toList());
        }
    }

    private List<GrowthWorldReportDetailCount> sortDetailList(List<GrowthWorldReportDetailCount> detailCountList) {
        if (CollectionUtils.isEmpty(detailCountList)) {
            return Collections.emptyList();
        }
        Set<GrowthWorldReportDetailCount> zeroSet = detailCountList.stream().filter(e -> e.getQuestionCount() == null || e.getQuestionCount() == 0).collect(Collectors.toSet());
        //如果都是0，按默认顺序返回
        if (zeroSet.size() == detailCountList.size()) {
            return detailCountList.stream().sorted(Comparator.comparingInt(o -> GrowthIslandType.growthIslandTypeList().indexOf(o.getName()))).collect(Collectors.toList());
        }
        Comparator<GrowthWorldReportDetailCount> comparator = Comparator
                //先排做题数
                .comparing(GrowthWorldReportDetailCount::getQuestionCount)
                //再排能力值
                .thenComparing(GrowthWorldReportDetailCount::getAbilityCount)
                //再排默认顺序
                .thenComparing(o -> GrowthIslandType.growthIslandTypeList().indexOf(o.getName()))
                .reversed();
        detailCountList = detailCountList.stream().sorted(comparator).collect(Collectors.toList());
        List<GrowthWorldReportDetailCount> finalDetailCountList = detailCountList;
        //
        List<GrowthWorldReportDetailCount> subjectIslands = detailCountList
                .stream()
                .filter(e -> finalDetailCountList.indexOf(e) != 0
                        && !StringUtils.equals(GrowthIslandType.gw_competition_island.getDesc(), e.getName())
                        && !StringUtils.equals(GrowthIslandType.gw_wisdom_island.getDesc(), e.getName()))
                .collect(Collectors.toList());
        GrowthWorldReportDetailCount improveIsland = subjectIslands.stream().sorted(comparator.reversed()).findFirst().orElse(null);
        if (improveIsland != null) {
            detailCountList.remove(improveIsland);
            detailCountList.add(1, improveIsland);
        }
        return detailCountList;
    }


    private List<GrowthWorldReportDetailCount> mockList() {
        List<GrowthWorldReportDetailCount> list = new ArrayList<>();
        GrowthWorldReportDetailCount g1 = new GrowthWorldReportDetailCount();
        GrowthWorldReportDetailCount g2 = new GrowthWorldReportDetailCount();
        GrowthWorldReportDetailCount g3 = new GrowthWorldReportDetailCount();
        GrowthWorldReportDetailCount g4 = new GrowthWorldReportDetailCount();
        GrowthWorldReportDetailCount g5 = new GrowthWorldReportDetailCount();
        g1.setName("英语岛");
        g1.setQuestionCount(12);
        g1.setAbilityCount(19.0);
        g2.setName("数学岛");
        g2.setQuestionCount(9);
        g2.setAbilityCount(18.0);
        g3.setName("语文岛");
        g3.setQuestionCount(10);
        g3.setAbilityCount(21.0);
        g4.setName("竞技岛");
        g4.setQuestionCount(0);
        g4.setAbilityCount(10.0);
        g5.setName("智慧岛");
        g5.setQuestionCount(0);
        g5.setAbilityCount(10.0);
        list.add(g1);
        list.add(g2);
        list.add(g3);
        list.add(g4);
        list.add(g5);
        return list;
    }
}


