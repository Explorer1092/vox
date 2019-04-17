package com.voxlearning.utopia.service.ai.impl.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.data.*;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.ai.util.DateExtentionUtil;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
public class ChipCourseSupport {
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private ChipsContentService chipsContentService;

    /**
     * @see ChipsContentService#loadTrailUnit()
     */
    @Deprecated
    public final static String TRAVEL_ENGLISH_TRIAL_UNIT = "BKC_10300231943369";

    public final static String TRAVEL_ENGLISH_BOOK_ID = "BK_10300003451674";
    public final static String TRAVEL_ENGLISH_EIGHT_UNIT_BOOK_ID = "SD_10300003146582";


    @Deprecated
    public NewBookCatalog loadTodayShortTravelStudyUnit() {
        Date now = new Date();
        OrderProduct orderProduct = userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream().filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                .filter(e -> {
                    ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(e.getId());
                    Date beginDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getBeginDate).orElse(new Date());
                    Date endDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getEndDate).orElse(new Date());
                    if (endDate == null || beginDate == null) {
                        return false;
                    }
                    return now.after(beginDate) && now.before(endDate);
                })
                .findFirst()
                .orElse(null);
        if (orderProduct == null) {
            return null;
        }
        return fetchDayUnit(new Date(), true, orderProduct, TRAVEL_ENGLISH_BOOK_ID);
    }

    public NewBookCatalog fetchDayUnit(Date current, boolean overLast, OrderProduct orderProduct, String bookId) {
        Date begin = Optional.ofNullable(chipsEnglishProductTimetableDao.load(orderProduct.getId())).map(ChipsEnglishProductTimetable::getBeginDate).orElse(new Date());
        if (begin == null) {
            return null;
        }
        List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(orderProduct.getId()).stream()
                .filter(e -> StringUtils.isNotBlank(e.getAppItemId()))
                .filter(e -> bookId.equals(e.getAppItemId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderProductItemList)) {
            return null;
        }
        return fetchDayUnit(current, overLast, begin, bookId);
    }

    public NewBookCatalog fetchDayUnit(Date current, boolean overLast, Date begin, String bookId) {
        if (DateExtentionUtil.isWeekend(current)) {
            return null;
        }
        // 获取教材课单元
        List<NewBookCatalog> units = fetchUnitListExcludeTrial(bookId);
        if (CollectionUtils.isEmpty(units)) {
            return null;
        }
        // 排序
        units.sort(Comparator.comparing(NewBookCatalog::getRank));
        for (int i = 0; i < units.size(); i++) {
            units.get(i).setRank(i + 1);
        }

        int days = DateExtentionUtil.daysDiffExcludeWeekend(begin, current);

        int index = overLast ? (days - 1) : Math.min(days - 1, units.size() - 1);
        return index >= 0 && index < units.size() ? units.get(index) : null;
    }

    /**
     * @param bookId
     * @return
     * @see ChipCourseSupport#fetchUnitListExcludeTrialV2
     */
    public List<NewBookCatalog> fetchUnitListExcludeTrial(String bookId) {
        if (StringUtils.isBlank(bookId)) {//
            return Collections.emptyList();
        }
        // 获取教材课单元
        List<NewBookCatalog> units = newContentLoaderClient.loadChildrenSingle(bookId, BookCatalogType.UNIT);
        if (CollectionUtils.isEmpty(units)) {
            return Collections.emptyList();
        }
        units = units.stream().filter(e -> !chipsContentService.isTrailUnit(e.getId())).collect(Collectors.toList());
        units.sort(Comparator.comparing(NewBookCatalog::getRank));
        return units;
    }


    public List<StoneUnitData> fetchUnitListExcludeTrialV2(String bookId) {
        if (StringUtils.isBlank(bookId)) {//
            return Collections.emptyList();
        }
        StoneBookData bookData = Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Arrays.asList(bookId)))
                .map(e -> e.get(bookId))
                .map(StoneBookData::newInstance)
                .filter(e -> e.getJsonData() != null && CollectionUtils.isNotEmpty(e.getJsonData().getChildren()))
                .orElse(null);
        if (bookData == null) {
            return Collections.emptyList();
        }

        return Optional.ofNullable(bookData)
                .map(e -> e.getJsonData().getChildren().stream().filter(e1 -> !chipsContentService.isTrailUnit(e1.getStone_data_id())).map(StoneBookData.Node::getStone_data_id).collect(Collectors.toList()))
                .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(e))
                .map(e -> e.values())
                .map(e -> {
                    List<StoneUnitData> res = new ArrayList<>();
                    e.stream().forEach(e1 -> res.add(StoneUnitData.newInstance(e1)));
                    return res;
                })
                .orElse(Collections.emptyList());
    }

    public Map<String, Date> fetchUnitDateV2(List<StoneUnitData> unitList, Date beginDate, boolean checkSaturday) {
        if (CollectionUtils.isEmpty(unitList) || beginDate == null) {
            return Collections.emptyMap();
        }

        Map<String, Date> dateMap = new HashMap<>();

        Calendar instance = Calendar.getInstance();
        instance.setTime(beginDate);

        for (StoneUnitData unit : unitList) {
            if (checkSaturday && instance.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                instance.add(Calendar.DAY_OF_WEEK, 2);
            } else if (instance.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                instance.add(Calendar.DAY_OF_WEEK, 1);
            }
            dateMap.put(unit.getId(), instance.getTime());
            instance.add(Calendar.DAY_OF_WEEK, 1);
        }
        return dateMap;
    }

    public List<StoneData> fetchTaskRoleStoneData(StoneTalkNpcQuestionData npcQuestion) {
        if (npcQuestion == null) {
            return Collections.emptyList();
        }
        List<String> contentIds = npcQuestion.getJsonData().getContent_ids();
        Map<String, StoneData> quesitionMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(contentIds);

        if (MapUtils.isEmpty(quesitionMap) || quesitionMap.size() != contentIds.size()) {
            return Collections.emptyList();
        }

        List<StoneData> questions = new ArrayList<>();
        for (String qid : contentIds) {
            questions.add(quesitionMap.get(qid));
        }
        return questions;
    }

    public List<StoneData> fetchTaskRoleStoneData(String lessonId, String roleName) {
        StoneTalkNpcQuestionData data = fetchTaskNpcStoneData(lessonId, roleName);
        return fetchTaskRoleStoneData(data);
    }

    public StoneTalkNpcQuestionData fetchTaskNpcStoneData(String lessonId, String roleName) {
        return Optional.ofNullable(stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singletonList(lessonId)))
                .map(e -> e.get(lessonId))
                .map(StoneLessonData::newInstance)
                .map(StoneLessonData::getJsonData)
                .filter(e -> CollectionUtils.isNotEmpty(e.getContent_ids()))
                .map(e -> stoneDataLoaderClient.loadStoneDataIncludeDisabled(e.getContent_ids()))
                .map(e -> e.values().stream()
                        .map(StoneTalkNpcQuestionData::newInstance)
                        .filter(e1 -> e1.getJsonData() != null && roleName.equalsIgnoreCase(e1.getJsonData().getNpc_name()))
                        .filter(e1 -> CollectionUtils.isNotEmpty(e1.getJsonData().getContent_ids()))
                        .findFirst().orElse(null))
                .orElse(null);
    }
}
