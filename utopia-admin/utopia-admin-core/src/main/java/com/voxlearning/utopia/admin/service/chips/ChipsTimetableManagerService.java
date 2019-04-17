package com.voxlearning.utopia.admin.service.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.data.ChipsTimetablePojo;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentLoader;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishContentService;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2018/10/17
 */
@Service
public class ChipsTimetableManagerService {

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;
    @ImportService(interfaceClass = ChipsEnglishContentLoader.class)
    private ChipsEnglishContentLoader chipsEnglishContentLoader;

    @ImportService(interfaceClass = ChipsEnglishContentService.class)
    private ChipsEnglishContentService chipsEnglishContentService;

    public List<ChipsTimetablePojo> loadAllChipsProduct() {
        List<OrderProduct> allProductList = userOrderLoaderClient.loadAllOrderProductIncludeOfflineForCrm();
        if (CollectionUtils.isEmpty(allProductList)) {
            return Collections.emptyList();
        }
        List<OrderProduct> chipsProductList = allProductList.stream().filter(p -> OrderProductServiceType.safeParse(p.getProductType()) == OrderProductServiceType.ChipsEnglish).collect(Collectors.toList());
        return chipsProductList.stream().map(p -> {
            ChipsTimetablePojo pojo = buildCoursePojo(p.getName(), p.getId());
            return pojo;
        }).collect(Collectors.toList());
    }

    @NotNull
    private ChipsTimetablePojo buildCoursePojo(String productName, String productId) {
        ChipsTimetablePojo coursePojo = new ChipsTimetablePojo();
        coursePojo.setProductId(productId);
        coursePojo.setProductName(productName);
        ChipsEnglishProductTimetable timetable = chipsEnglishContentLoader.loadChipsEnglishProductTimetableById(productId);
        if (timetable != null) {
            coursePojo.setBeginDate(DateUtils.dateToString(timetable.getBeginDate(), DateUtils.FORMAT_SQL_DATE));
            coursePojo.setEndDate(DateUtils.dateToString(timetable.getEndDate(), DateUtils.FORMAT_SQL_DATE));
            coursePojo.setAllUnitNum(timetable.getCourses() == null ? 0 : timetable.getCourses().size());
        }

        return coursePojo;
    }

    /**
     * book_unit_count
     *
     * @param productId
     * @return
     */
    public ChipsTimetablePojo loadChipsTimetablePojo(String productId) {
        ChipsEnglishProductTimetable timetable = chipsEnglishContentLoader.loadChipsEnglishProductTimetableById(productId);
        ChipsTimetablePojo pojo = new ChipsTimetablePojo();
        OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
        pojo.setProductName(orderProduct == null ? "" : orderProduct.getName());
        if (timetable == null || CollectionUtils.isEmpty(timetable.getCourses())) {
            List<ChipsTimetablePojo.EditPojo> editPojoList = buildEditPojo(productId);
            pojo.setEditPojoList(editPojoList);
            pojo.setProductId(productId);
            if (timetable != null) {
                pojo.setBeginDate(DateUtils.dateToString(timetable.getBeginDate(), DateUtils.FORMAT_SQL_DATE));
                pojo.setEndDate(DateUtils.dateToString(timetable.getEndDate(), DateUtils.FORMAT_SQL_DATE));
            }
        } else {
            List<ChipsEnglishProductTimetable.Course> courseList = timetable.getCourses();
            pojo.setBeginDate(DateUtils.dateToString(timetable.getBeginDate(), DateUtils.FORMAT_SQL_DATE));
            pojo.setEndDate(DateUtils.dateToString(timetable.getEndDate(), DateUtils.FORMAT_SQL_DATE));
            pojo.setProductId(productId);
            pojo.setEditPojoList(buildEditPojo(productId, courseList));
        }
        return pojo;
    }

    private List<ChipsTimetablePojo.EditPojo> buildEditPojo(String productId, List<ChipsEnglishProductTimetable.Course> courseList) {
        if (StringUtils.isBlank(productId)) {
            return Collections.emptyList();
        }
        Map<String, List<ChipsEnglishProductTimetable.Course>> bookToCourseMap = new HashMap<>();
        courseList.forEach(course -> {
            List<ChipsEnglishProductTimetable.Course> list = bookToCourseMap.get(course.getBookId());
            if (list == null) {
                list = new ArrayList<>();
                bookToCourseMap.put(course.getBookId(), list);
            }
            list.add(course);
        });
        List<ChipsTimetablePojo.EditPojo> editPojoList = new ArrayList<>();
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        itemList.forEach(item -> {
            ChipsTimetablePojo.EditPojo pojo = new ChipsTimetablePojo.EditPojo();
            if (item == null || StringUtils.isBlank(item.getAppItemId())) {
                return;
            }
            pojo.setBookId(item.getAppItemId());
            if (bookToCourseMap.get(item.getAppItemId()) != null) {
                List<String> dateList = bookToCourseMap.get(item.getAppItemId()).stream().map(c -> DateUtils.dateToString(c.getBeginDate(), DateUtils.FORMAT_SQL_DATE)).collect(Collectors.toList());
                pojo.setDateList(StringUtils.join(dateList.toArray(), ","));
            }
            pojo.setUnitNum(getUnitNum(item.getAppItemId()));
            pojo.setBookName(getBookName(item.getAppItemId()));
            editPojoList.add(pojo);
        });
        return editPojoList;
    }

    private List<ChipsTimetablePojo.EditPojo> buildEditPojo(List<ChipsEnglishProductTimetable.Course> courseList) {
        Map<String, List<ChipsEnglishProductTimetable.Course>> bookToCourseMap = new HashMap<>();
        courseList.forEach(course -> {
            List<ChipsEnglishProductTimetable.Course> list = bookToCourseMap.get(course.getBookId());
            if (list == null) {
                list = new ArrayList<>();
                bookToCourseMap.put(course.getBookId(), list);
            }
            list.add(course);
        });
        List<ChipsTimetablePojo.EditPojo> editPojoList = new ArrayList<>();
        for (Map.Entry<String, List<ChipsEnglishProductTimetable.Course>> entry : bookToCourseMap.entrySet()) {
            ChipsTimetablePojo.EditPojo pojo = new ChipsTimetablePojo.EditPojo();
            pojo.setBookId(entry.getKey());
            List<String> dateList = entry.getValue().stream().map(c -> DateUtils.dateToString(c.getBeginDate(), DateUtils.FORMAT_SQL_DATE)).collect(Collectors.toList());
            pojo.setDateList(StringUtils.join(dateList.toArray(), ","));
            pojo.setUnitNum(getUnitNum(entry.getKey()));
            pojo.setBookName(getBookName(entry.getKey()));
            editPojoList.add(pojo);
        }
        return editPojoList;
    }

    private String getBookName(String bookId) {
        if (StringUtils.isEmpty(bookId)) {
            return null;
        }
        Map<String, StoneData> stoneDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(Collections.singleton(bookId));
        return Optional.ofNullable(stoneDataMap).filter(MapUtils::isNotEmpty).map(m -> m.get(bookId)).map(StoneBookData::newInstance)
                .filter(e -> e.getJsonData() != null).map(e -> e.getJsonData().getName()).orElse(null);
    }

    private List<ChipsTimetablePojo.EditPojo> buildEditPojo(String productId) {
        List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
        return itemList.stream().map(item -> {
            List<StoneUnitData> unitList = chipsEnglishContentLoader.fetchUnitListExcludeTrialV2(item.getAppItemId());
            ChipsTimetablePojo.EditPojo pojo = new ChipsTimetablePojo.EditPojo();
            pojo.setUnitNum(unitList.size());
            pojo.setBookId(item.getAppItemId());
            pojo.setBookName(getBookName(item.getAppItemId()));
            return pojo;
        }).collect(Collectors.toList());
    }

    private Integer getUnitNum(String bookId) {
        return chipsEnglishContentLoader.fetchUnitListExcludeTrialV2(bookId).size();
    }

    public MapMessage save(Map<String, List<Date>> bookDateMap, String productId, String beginDate, String endDate) {
        ChipsEnglishProductTimetable timetable = new ChipsEnglishProductTimetable();
        timetable.setId(productId);
        timetable.setBeginDate(DateUtils.stringToDate(beginDate, DateUtils.FORMAT_SQL_DATE));
        timetable.setEndDate(DateUtils.stringToDate(endDate, DateUtils.FORMAT_SQL_DATE));
        List<ChipsEnglishProductTimetable.Course> courseList = new ArrayList<>();
        for (Map.Entry<String, List<Date>> entry : bookDateMap.entrySet()) {
            List<Date> dateList = entry.getValue();
            List<StoneUnitData> unitList = chipsEnglishContentLoader.fetchUnitListExcludeTrialV2(entry.getKey());
            if (unitList.size() != dateList.size()) {
                return MapMessage.errorMessage().add("info", "unit size " + unitList.size() + " not equale date size: " + dateList.size());
            }
            for (int i = 0; i < unitList.size(); i++) {
                ChipsEnglishProductTimetable.Course course = new ChipsEnglishProductTimetable.Course();
                course.setBeginDate(dateList.get(i));
                course.setBookId(entry.getKey());
                course.setUnitId(unitList.get(i).getId());
                courseList.add(course);
            }
        }
        timetable.setCourses(courseList);
        chipsEnglishContentService.upsertChipsEnglishProductTimetable(timetable);
        return MapMessage.successMessage();
    }

    public MapMessage checkValidParm(String beginDateStr, String endDateStr, Map<String, List<Date>> bookDateMap) {
        if (StringUtils.isBlank(beginDateStr)) {
            return MapMessage.errorMessage().add("info", "开始日期为空");
        }
        if (StringUtils.isBlank(endDateStr)) {
            return MapMessage.errorMessage().add("info", "结束日期为空");
        }
        Date beginDate = DateUtils.stringToDate(beginDateStr, DateUtils.FORMAT_SQL_DATE);
        Date endDate = DateUtils.stringToDate(endDateStr, DateUtils.FORMAT_SQL_DATE);
        if (endDate.before(beginDate)) {
            return MapMessage.errorMessage().add("info", endDateStr + " before " + beginDateStr);
        }

        TreeMap<Date, Date> dateMap = new TreeMap<>();
        for (Map.Entry<String, List<Date>> entry : bookDateMap.entrySet()) {
            String bookId = entry.getKey();
            List<Date> dateList = entry.getValue();
            List<StoneUnitData> unitList = chipsEnglishContentLoader.fetchUnitListExcludeTrialV2(bookId);
            if (dateList.size() != unitList.size()) {
                return MapMessage.errorMessage().add("info", "unit size " + unitList.size() + " not equale date size: " + dateList.size());
            }
            if (lessThanMin(dateList, beginDate)) {
                return MapMessage.errorMessage().add("info", DateUtils.dateToString(dateList.get(0), DateUtils.FORMAT_SQL_DATE) + " before " + beginDateStr);
            }
            if (greatThanMax(dateList, endDate)) {
                return MapMessage.errorMessage().add("info", DateUtils.dateToString(dateList.get(dateList.size() - 1), DateUtils.FORMAT_SQL_DATE) + " after " + endDateStr);
            }
            if (CollectionUtils.isNotEmpty(dateList)) {
                dateMap.put(dateList.get(0), dateList.get(dateList.size() - 1));
            }
        }
        MapMessage message = isDateRangeConflict(dateMap);
        if (!message.isSuccess()) {
            return message;
        }
        return MapMessage.successMessage();
    }

    private MapMessage isDateRangeConflict(TreeMap<Date, Date> dateMap) {
        if (MapUtils.isEmpty(dateMap)) {
            return MapMessage.successMessage();
        }
        Date lastEndDate = null;
        for (Map.Entry<Date, Date> entry : dateMap.entrySet()) {
            if (lastEndDate != null && !lastEndDate.before(entry.getKey())) {
                return MapMessage.errorMessage().add("info", "上课时间有冲突" + lastEndDate + ";" + entry.getKey());
            }
            lastEndDate = entry.getValue();
        }
        return MapMessage.successMessage();
    }

    private boolean lessThanMin(List<Date> dateList, Date minDate) {
        if (CollectionUtils.isEmpty(dateList)) {
            return false;
        }
        return dateList.get(0).before(minDate);
    }

    private boolean greatThanMax(List<Date> dateList, Date maxDate) {
        if (CollectionUtils.isEmpty(dateList)) {
            return false;
        }
        return dateList.get(dateList.size() - 1).after(maxDate);
    }

    public List<String> buildUnitDateList(int size, String beginDate, boolean skipSaturday, boolean skipSunday) {
        if (size < 1) {
            return Collections.emptyList();
        }
        Date begin = DateUtils.stringToDate(beginDate, DateUtils.FORMAT_SQL_DATE);
        Calendar instance = Calendar.getInstance();
        instance.setTime(begin);
        List<String> dateList = new ArrayList<>();
        skipDays(instance, skipSaturday, skipSunday);
        dateList.add(DateUtils.dateToString(instance.getTime(), DateUtils.FORMAT_SQL_DATE));
        for (int i = 1; i < size; i++) {
            instance.add(Calendar.DAY_OF_WEEK, 1);
            skipDays(instance, skipSaturday, skipSunday);
            dateList.add(DateUtils.dateToString(instance.getTime(), DateUtils.FORMAT_SQL_DATE));
        }
        return dateList;
    }

    private void skipDays(Calendar calendar, boolean skipSaturday, boolean skipSunday) {
        if (!skipSaturday && !skipSunday) {//都为false
            return;
        }
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (skipSaturday && skipSunday) {//都为true
            if (day == 7) {
                calendar.add(Calendar.DAY_OF_WEEK, 2);
            }
            if (day == 1) {
                calendar.add(Calendar.DAY_OF_WEEK, 1);
            }
            return;
        }
        if (skipSaturday) {
            if (day == 7) {
                calendar.add(Calendar.DAY_OF_WEEK, 1);
            }
            return;
        }
        if (day == 1) {
            calendar.add(Calendar.DAY_OF_WEEK, 1);
        }
    }
}
