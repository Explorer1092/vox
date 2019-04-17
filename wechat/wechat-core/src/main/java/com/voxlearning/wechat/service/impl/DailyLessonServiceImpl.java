package com.voxlearning.wechat.service.impl;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.ai.api.AiOrderProductService;
import com.voxlearning.utopia.service.ai.api.AiTodayLessonService;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.cache.UserShareVideoRankCache;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.data.ChipsRank;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.BooKConst;
import com.voxlearning.wechat.service.DailyLessonService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2018/8/6
 */
@Named
public class DailyLessonServiceImpl implements DailyLessonService {

    @ImportService(interfaceClass = AiTodayLessonService.class)
    private AiTodayLessonService aiTodayLessonService;

    @ImportService(interfaceClass = AiOrderProductService.class)
    private AiOrderProductService aiOrderProductService;
    @Inject
    private AiLoaderClient aiLoaderClient;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @Override
    public Map<String, Object> buildDataMapForPreview(String bookId, String unitId) {
        List<AITodayLesson> todayLessonList = aiTodayLessonService.findByUnitId(unitId);
        if (CollectionUtils.isEmpty(todayLessonList)) {
            return new HashMap<>();
        }
        AITodayLesson aiTodayLesson = todayLessonList.get(0);
        if (aiTodayLesson == null) {
            return new HashMap<>();
        }
        Map<String, Object> baseMap = buildAiTodayLessonBaseMap(aiTodayLesson);
        if (MapUtils.isEmpty(baseMap)) {
            return baseMap;
        }
        baseMap.putAll(buildAiTodayLessonStatisticsMap(bookId, aiTodayLesson, unitId));
        baseMap.putAll(buildAiTodayLessonExtMapForPreview(aiTodayLesson, unitId));
        baseMap.putAll(levelUserCountForPreview());
        return baseMap;
    }

    @Override
    public Map<String, Object> buildDataMap(String bookId, Long clazzId, String unitId) {
        List<AITodayLesson> todayLessonList = aiTodayLessonService.findByUnitId(unitId);
        if (CollectionUtils.isEmpty(todayLessonList)) {
            return new HashMap<>();
        }

        AITodayLesson aiTodayLesson = todayLessonList.get(0);
        if (aiTodayLesson == null) {
            return new HashMap<>();
        }
        Map<String, Object> baseMap = buildAiTodayLessonBaseMap(aiTodayLesson);
        if (MapUtils.isEmpty(baseMap)) {
            return baseMap;
        }
        baseMap.putAll(buildAiTodayLessonStatisticsMap(bookId, aiTodayLesson, unitId));
        baseMap.putAll(buildAiTodayLessonExtMap(aiTodayLesson, clazzId, unitId));
        baseMap.putAll(levelUserCount(clazzId, unitId));
        return baseMap;
    }

    /**
     * 该unit后面还有几节课
     *
     * @param unitIdList
     * @param unitId
     * @return
     */
    private int remainLessonCount(List<String> unitIdList, String unitId) {
        if (CollectionUtils.isEmpty(unitIdList)) {
            return 0;
        }
        if (StringUtils.isBlank(unitId)) {
            return unitIdList.size();
        }
        int index = 0;
        boolean flag = false;
        for (String unit : unitIdList) {
            index++;
            if (unit.equals(unitId)) {
                flag = true;
                break;
            }
        }
        if (!flag) {//如果不包含该unitId返回unitIdList.size()
            return unitIdList.size();
        }
        return unitIdList.size() - index;
    }

    /**
     * rank top 3
     *
     * @param clazzId
     * @param unitId
     * @return
     */
    private List<ChipsRank> loadTopThreeRank(Long clazzId, String unitId) {
        if (clazzId == null || clazzId == 0l || StringUtils.isBlank(unitId)) {
            return null;
        }
        List<ChipsRank> chipsRankList = UserShareVideoRankCache.load(clazzId + "", unitId);
        if (CollectionUtils.isEmpty(chipsRankList)) {
            return null;
        }
        chipsRankList = chipsRankList.stream().filter(c -> c.getRank() != null).collect(Collectors.toList());
        chipsRankList.sort(Comparator.comparing(ChipsRank::getRank));
        Set<Integer> rankSet = new HashSet<>();
        List<ChipsRank> list = new ArrayList<>();
        for (ChipsRank rank : chipsRankList) {
            if (rankSet.size() > 3) {
                break;
            }
            if (rankSet.contains(rank)) {
                list.add(rank);
                continue;
            }
            if (rankSet.size() == 3) {
                break;
            }
            rankSet.add(rank.getRank());
            list.add(rank);
        }
        return list;
    }

    private Map<String, Object>[] hotRankMapForPreview() {
        Map<String, Object>[] mapArr = new Map[3];
        for (int i = 0; i < 3; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", "xx同学");
            map.put("rank", (i + 1));
            map.put("title", "薯条英语" + DateUtils.dateToString(new Date(), "MMdd") + rankToString(i + 1));
            map.put("content", "第" + (i + 1) + "名同学的视频和点评哦。xx的视频拍的非常清楚");
            map.put("url", "url");
            mapArr[i] = map;
        }
        return mapArr;
    }

    private OrderProduct getOrderProductByClassId(Long clazzId) {
        if (clazzId == null || clazzId == 0l) {
            return null;
        }
        ChipsEnglishClass clazz = chipsEnglishClazzService.selectChipsEnglishClassById(clazzId);
        if (clazz == null || StringUtils.isBlank(clazz.getProductId())) {
            return null;
        }
        return userOrderLoaderClient.loadOrderProductById(clazz.getProductId());
    }

    private Map<String, Object>[] hotRankMap(List<ChipsRank> rankList, String unitId, Long clazzId) {
        if (CollectionUtils.isEmpty(rankList)) {
            return new Map[0];
        }
        OrderProduct orderProduct = getOrderProductByClassId(clazzId);
        if (orderProduct == null) {
            return new Map[0];
        }
        Date date = aiLoaderClient.getRemoteReference().loadUnitBeginTime(orderProduct, BooKConst.CHIPS_ENGLISH_BOOK_ID, unitId);
        Map<String, Object>[] mapArr = new Map[rankList.size()];
        for (int i = 0; i < rankList.size(); i++) {
            ChipsRank rank = rankList.get(i);
            AIUserVideo userVideo = getUserVideo(rank.getUserId(), unitId);
            Map<String, Object> map = new HashMap<>();
            map.put("name", rank.getUserName());
            map.put("rank", rank.getRank());
            map.put("title", "薯条英语" + DateUtils.dateToString(date == null ? new Date() : date, "MMdd") + rankToString(rank.getRank()));
            map.put("content", getComment(userVideo));
            map.put("url", getVidelShareUrl(userVideo));
            mapArr[i] = map;
        }
        return mapArr;
    }

    private List<Long> findUserByClazz(Long clazzId) {
        if (clazzId == null || clazzId == 0l) {
            return null;
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClazzService.selectChipsEnglishClassUserRefByClazzId(clazzId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return null;
        }
        List<Long> list = new ArrayList<>();
        userRefList.forEach(e -> list.add(e.getUserId()));
        return list;
    }

    private Map<String, Integer> levelUserCountForPreview() {
        Map<String, Integer> map = new HashMap<>();
        map.put("level_a", 40);
        map.put("level_b", 150);
        map.put("level_c", 10);
        return map;
    }

    private Map<String, Integer> levelUserCount(Long clazz, String unitId) {
        List<Long> userIdList = findUserByClazz(clazz);
        if (CollectionUtils.isEmpty(userIdList) || StringUtils.isBlank(unitId)) {
            return levelUserCountDefMap();
        }
        Map<Long, List<AIUserUnitResultPlan>> aiUserUnitResultPlanListMap = aiLoaderClient.getRemoteReference().loadUnitStudyPlan(userIdList);
        if (MapUtils.isEmpty(aiUserUnitResultPlanListMap)) {
            return levelUserCountDefMap();
        }
        Map<String, Integer> map = new HashMap<>();
        int countA = filterGrade(AIUserUnitResultPlan.Grade.A, unitId, aiUserUnitResultPlanListMap);
        int countB = filterGrade(AIUserUnitResultPlan.Grade.B, unitId, aiUserUnitResultPlanListMap);
        int countC = filterGrade(AIUserUnitResultPlan.Grade.C, unitId, aiUserUnitResultPlanListMap);
        map.put("level_a", countA);
        map.put("level_b", countB);
        map.put("level_c", countC);
        return map;
    }

    private Map<String, Integer> levelUserCountDefMap() {
        Map<String, Integer> map = new HashMap<>();
        map.put("level_a", 0);
        map.put("level_b", 0);
        map.put("level_c", 0);
        return map;
    }

    private int filterGrade(AIUserUnitResultPlan.Grade filterGrade, String unitId, Map<Long, List<AIUserUnitResultPlan>> aiUserUnitResultPlanListMap) {
        int count = 0;
        for (Map.Entry<Long, List<AIUserUnitResultPlan>> entry : aiUserUnitResultPlanListMap.entrySet()) {
            List<AIUserUnitResultPlan> aiUserUnitResultPlanList = entry.getValue();
            for (AIUserUnitResultPlan plan : aiUserUnitResultPlanList) {
                if (plan.getUnitId() == null || !unitId.equals(plan.getUnitId())) {
                    continue;
                }
                AIUserUnitResultPlan.Grade grade = plan.getGrade();
                if (grade == null) {
                    continue;
                }
                if (grade == filterGrade) {
                    count++;
                }
            }
        }
        return count;
    }

    private AIUserVideo getUserVideo(Long userId, String unitId) {
        List<AIUserVideo> videoList = aiLoaderClient.getRemoteReference().loadUserVideoListByUserId(userId);
        if (CollectionUtils.isEmpty(videoList)) {
            return null;
        }
        return videoList.stream().filter(v -> v.getUnitId().equals(unitId.trim())).findFirst().orElse(null);
    }

    public static String shareVideoUrl(String userVideoId) {
        return RuntimeMode.lt(Mode.STAGING) ? ("https://wechat.test.17zuoye.net/chips/center/chipsshare.vpage?id=" + userVideoId) :
                ("https://wechat.17zuoye.com/chips/center/chipsshare.vpage?id=" + userVideoId);
    }

    private String getVidelShareUrl(AIUserVideo userVideo) {
        if (userVideo == null) {
            return null;
        }
        return shareVideoUrl(userVideo.getId());
    }

    private String getComment(AIUserVideo userVideo) {
        if (userVideo == null) {
            return "";
        }
        return userVideo.getComment() == null ? "" : userVideo.getComment();
    }

    private String rankToString(int rank) {
        return "第" + ArabicToCnNum.intToString(rank) + "名";
    }

    /**
     * 最基础的直接读自外部接口的属性
     *
     * @param todayLesson
     * @return
     */
    private Map<String, Object> buildAiTodayLessonBaseMap(AITodayLesson todayLesson) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", todayLesson.getTitle() == null ? "" : todayLesson.getTitle());
        map.put("subject", todayLesson.getSubject() == null ? "" : todayLesson.getSubject());
        map.put("videoDesc", todayLesson.getVideoDesc() == null ? "" : todayLesson.getVideoDesc());
        map.put("videoUrl", todayLesson.getVideoUrl() == null ? "" : todayLesson.getVideoUrl());
        map.put("videoImg", todayLesson.getVideoImg() == null ? "" : todayLesson.getVideoImg());
        map.put("videoContent", todayLesson.getVideoContent() == null ? "" : todayLesson.getVideoContent());
        map.put("eggContent", todayLesson.getEggContent() == null ? "" : todayLesson.getEggContent());
        map.put("eggVideoUrl", todayLesson.getEggVideoUrl() == null ? "" : todayLesson.getEggVideoUrl());
        map.put("eggImg", todayLesson.getEggImg() == null ? "" : todayLesson.getEggImg());
        map.put("summaryLink", todayLesson.getSummaryLink() == null ? "" : todayLesson.getSummaryLink());
        map.put("tipsVideoUrl", todayLesson.getTipsVideoUrl() == null ? "" : todayLesson.getTipsVideoUrl());
        map.put("tipsVideoImg", todayLesson.getTipsVideoImg() == null ? "" : todayLesson.getTipsVideoImg());
        map.put("tipsNextClass", todayLesson.getTipsNextClass() == null ? "" : todayLesson.getTipsNextClass());
        return map;
    }

    /**
     * 需要组装的数据
     *
     * @param todayLesson
     * @param unitId
     * @return
     */
    private Map<String, Object> buildAiTodayLessonStatisticsMap(String bookId, AITodayLesson todayLesson, String unitId) {
        Map<String, Object> map = new HashMap<>();
        List<String> unitIdList = aiLoaderClient.getRemoteReference().loadAllValidUnitIdByBookIdSortWithRank(bookId);
        int remainLessonCount = remainLessonCount(unitIdList, unitId);
        int day = CollectionUtils.isEmpty(unitIdList) ? 0 : unitIdList.size() - remainLessonCount;
        String summaryContent = replaceSummaryContent(todayLesson.getSummaryContent(), remainLessonCount);
        map.put("summaryContent", summaryContent == null ? "" : summaryContent);
        map.put("day", day);
        return map;
    }

    private Map<String, Object> buildAiTodayLessonExtMap(AITodayLesson todayLesson, Long clazzId, String unitId) {
        Map<String, Object> map = new HashMap<>();
        List<ChipsRank> rankList = loadTopThreeRank(clazzId, unitId);
        Map<String, Object>[] hotRankMapArr = hotRankMap(rankList, unitId, clazzId);
        map.put("hotRankData", hotRankMapArr);
        String hotContent = replaceHotContent(todayLesson.getHotContent(), rankList);
        map.put("hotContent", hotContent == null ? "" : hotContent);
        return map;
    }

    private Map<String, Object> buildAiTodayLessonExtMapForPreview(AITodayLesson todayLesson, String unitId) {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object>[] hotRankMapArr = hotRankMapForPreview();
        map.put("hotRankData", hotRankMapArr);
        String hotContent = replaceHotContentForPreView(todayLesson.getHotContent());
        map.put("hotContent", hotContent == null ? "" : hotContent);
        return map;
    }

    /**
     * 替换{count}占位符
     *
     * @param summaryContent
     * @param remainCount
     * @return
     */
    private String replaceSummaryContent(String summaryContent, int remainCount) {
        if (StringUtils.isBlank(summaryContent)) {
            return summaryContent;
        }
        return summaryContent.replace("{count}", remainCount + "");
    }

    /**
     * 没传clazzId， 预览时临时替换
     *
     * @param hotContent
     * @return
     */
    private String replaceHotContentForPreView(String hotContent) {
        if (StringUtils.isBlank(hotContent)) {
            return hotContent;
        }
        hotContent = hotContent.replace("{name}", "@xx同学@xx同学@xx同学");
        hotContent = hotContent.replace("{count}", ArabicToCnNum.intToString(3));
        return hotContent;
    }

    /**
     * 替换{name},{count}占位符
     *
     * @param hotContent
     * @param rankList
     * @return
     */
    private String replaceHotContent(String hotContent, List<ChipsRank> rankList) {
        if (StringUtils.isBlank(hotContent)) {
            return hotContent;
        }
        if (CollectionUtils.isEmpty(rankList)) {
            hotContent = hotContent.replace("{name}", "");
            hotContent = hotContent.replace("{count}", ArabicToCnNum.intToString(0));
            return hotContent;
        }
        StringBuilder sb = new StringBuilder();
        for (ChipsRank rank : rankList) {
            sb.append("@").append(rank.getUserName());
        }
        hotContent = hotContent.replace("{name}", sb.toString());
        hotContent = hotContent.replace("{count}", ArabicToCnNum.intToString(rankList.size()));
        return hotContent;
    }


    enum ArabicToCnNum {
        Zeor(0, "零"),
        One(1, "一"),
        Two(2, "二"),
        Three(3, "三"),
        Four(4, "四"),
        Five(5, "五"),
        Six(6, "六");
        private int arabicVal;
        private String cnVal;

        ArabicToCnNum(int num, String cn) {
            arabicVal = num;
            cnVal = cn;
        }

        public static String intToString(int num) {
            ArabicToCnNum[] values = values();
            for (ArabicToCnNum arabicToCnNum : values) {
                if (arabicToCnNum.arabicVal == num) {
                    return arabicToCnNum.cnVal;
                }
            }
            return String.valueOf(num);
        }
    }

}
