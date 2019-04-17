package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.api.AiOrderProductService;
import com.voxlearning.utopia.service.ai.constant.AIBookStatus;
import com.voxlearning.utopia.service.ai.data.AIUserBookInfo;
import com.voxlearning.utopia.service.ai.data.ChipsUserCourseMapper;
import com.voxlearning.utopia.service.ai.data.StoneBookData;
import com.voxlearning.utopia.service.ai.entity.AIUserLessonBookRef;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.entity.ChipsUserCourse;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserLessonBookRefPersistence;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.impl.persistence.support.AIUserLessonBookSupport;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.ai.util.OrderProductUtil;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guangqing
 * @since 2018/8/15
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = AiOrderProductService.class, version = @ServiceVersion(version = "20190222")),
        @ExposeService(interfaceClass = AiOrderProductService.class, version = @ServiceVersion(version = "20181226"))
})
public class AiOrderProductServiceImpl implements AiOrderProductService {

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private AIUserLessonBookSupport aiUserLessonBookSupport;

    @Inject
    private AIUserLessonBookRefPersistence aiUserLessonBookRefPersistence;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private ChipsUserService chipsUserService;

    private static String USER_BOOK_ID_SEP = "-";


    @Override
    @Deprecated
    public OrderProduct loadCurrentValidProduct() {
        return userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .filter(e -> {
                    ChipsEnglishProductTimetable config = chipsEnglishProductTimetableDao.load(e.getId());
                    if (config == null || config.getBeginDate() == null || config.getEndDate() == null) {
                        return false;
                    }
                    Date now = new Date();
                    return now.after(config.getBeginDate()) && now.before(config.getEndDate());
                }).findFirst().orElse(null);
    }

    @Override
    public OrderProduct loadBeginPaidShortProduct(Long userId) {
        if (userId == null || userId == 0L) {
            return null;
        }
        Set<String> productIds = chipsUserService.loadUserEffectiveCourse(userId).stream().map(ChipsUserCourse::getProductId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(productIds)) {
            return null;
        }
        Date now = new Date();
        return userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .filter(e -> productIds.contains(e.getId()))
                .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                .filter(e -> {
                    ChipsEnglishProductTimetable config = chipsEnglishProductTimetableDao.load(e.getId());
                    if (config == null || config.getBeginDate() == null || config.getEndDate() == null) {
                        return false;
                    }
                    return OrderProductUtil.isShortProduct(e) && now.before(config.getBeginDate());
                }).findFirst().orElse(null);
    }

    @Override
    public OrderProduct loadCurrentValidPaidShortProduct(Long userId) {
        if (userId == null || userId == 0L) {
            return null;
        }
        Set<String> productIds = chipsUserService.loadUserBoughtProduct(userId);
        if (CollectionUtils.isEmpty(productIds)) {
            return null;
        }
        Date now = new Date();
        return userOrderLoaderClient.loadAllOrderProductIncludeOffline().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                .filter(e -> productIds.contains(e.getId()))
                .filter(e -> StringUtils.isNotBlank(e.getAttributes()))
                .filter(e -> {
                    ChipsEnglishProductTimetable config = chipsEnglishProductTimetableDao.load(e.getId());
                    if (config == null || config.getBeginDate() == null || config.getEndDate() == null) {
                        return false;
                    }
                    return OrderProductUtil.isShortProduct(e) && now.after(config.getBeginDate()) && now.before(config.getEndDate());
                }).findFirst().orElse(null);
    }

    @Override
    public MapMessage loadUserCourseInfo(Long userId) {
        if (userId == null) {
            return MapMessage.errorMessage().add("result", "402").add("message", "参数为空");
        }

        List<ChipsUserCourse> chipsUserCourses = chipsUserService.loadUserEffectiveCourse(userId);
        if (CollectionUtils.isEmpty(chipsUserCourses)) {
            return MapMessage.successMessage().add("result", "success").add("courseList", Collections.emptyList());
        }

        Map<String, List<OrderProductItem>> orderProductItemMap = Optional.of(chipsUserCourses)
                .filter(CollectionUtils::isNotEmpty)
                .map(e -> e.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet()))
                .map(ids -> userOrderLoaderClient.loadOrderProductItems(ids))
                .map(map -> {
                    Map<String, List<OrderProductItem>> productItemMap = new HashMap<>();
                    chipsUserCourses.stream().collect(Collectors.groupingBy(ChipsUserCourse::getProductId)).forEach((k, v) -> {
                        List<OrderProductItem> itemList = new ArrayList<>();
                        v.stream().map(ChipsUserCourse::getProductItemId).forEach(e -> itemList.add(map.get(e)));
                        productItemMap.put(k, itemList);
                    });
                    return productItemMap;
                })
                .orElse(Collections.emptyMap());
        Set<String> bookIds = new HashSet<>();
        orderProductItemMap.values().stream().filter(CollectionUtils::isNotEmpty).map(e -> e.stream().map(OrderProductItem::getAppItemId).collect(Collectors.toSet())).forEach(bookIds::addAll);
        Map<String, StoneData> bookDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(bookIds);

        AIUserLessonBookRef userLessonBookRef = aiUserLessonBookSupport.fetchUserCurrentBook(userId);

        Date now = new Date();
        List<AIUserBookInfo> result = new ArrayList<>();

        Set<String> productIds = chipsUserCourses.stream().sorted(Comparator.comparing(ChipsUserCourse::getCreateTime)).map(ChipsUserCourse::getProductId).collect(Collectors.toSet());
        for (String productId : productIds) {
            ChipsEnglishProductTimetable config = chipsEnglishProductTimetableDao.load(productId);
            Date beginDate = config.getBeginDate();
            Date endDate = config.getEndDate();
            if (beginDate == null || endDate == null) {
                continue;
            }
            List<OrderProductItem> orderProductItemList = orderProductItemMap.get(productId);
            if (CollectionUtils.isEmpty(orderProductItemList)) {
                continue;
            }

            AIBookStatus status;
            if (now.before(beginDate)) {
                status = AIBookStatus.UnBegin;
            } else if (now.after(endDate)) {
                status = AIBookStatus.Finished;
            } else {
                status = AIBookStatus.InTime;
            }
            orderProductItemList.stream()
                    .sorted(Comparator.comparing(OrderProductItem::getUpdateDatetime).reversed())
                    .filter(e -> StringUtils.isNotBlank(e.getAppItemId()))
                    .forEach(e -> {
                        StoneBookData bookData = Optional.ofNullable(bookDataMap)
                                .map(e1 -> e1.get(e.getAppItemId()))
                                .map(StoneBookData::newInstance)
                                .filter(f -> f.getJsonData() != null)
                                .orElse(null);
                        if (bookData != null) {
                            AIUserBookInfo info = new AIUserBookInfo();
                            info.setId(productId + USER_BOOK_ID_SEP + e.getAppItemId());
                            info.setName(Optional.of(bookData)
                                    .map(StoneBookData::getJsonData)
                                    .map(StoneBookData.Book::getName)
                                    .orElse(""));
                            String image = Optional.of(bookData)
                                    .map(StoneBookData::getJsonData)
                                    .map(StoneBookData.Book::getCover_image)
                                    .orElse("");
                            info.setImage(image);
                            info.setStatus(status);
                            boolean active = userLessonBookRef != null &&
                                    userLessonBookRef.getBookId().equals(e.getAppItemId()) &&
                                    userLessonBookRef.getProductId().equals(productId);
                            info.setActive(active);
                            result.add(info);
                        }
                    });
        }

        return MapMessage.successMessage().add("result", "success").add("courseList", result);
    }

    @Override
    public MapMessage changeUserBookRef(Long userId, String id) {
        if (userId == null || StringUtils.isBlank(id)) {
            return MapMessage.errorMessage().add("result", "402").add("message", "参数为空");
        }
        String[] strings = id.split(USER_BOOK_ID_SEP);
        if (strings.length < 2) {
            return MapMessage.errorMessage().add("result", "403").add("message", "参数不合法");
        }
        String productId = strings[0];
        String bookId = strings[1];
        OrderProductItem item = userOrderLoaderClient.loadAllOrderProductItems().stream().filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish).filter(e -> e.getAppItemId().equalsIgnoreCase(bookId))
                .findFirst().orElse(null);
        if (item == null) {
            return MapMessage.errorMessage().add("result", "404").add("message", "教材未找到");
        }

        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(productId);
        if (timetable == null || timetable.getBeginDate() == null) {
            return MapMessage.errorMessage().add("result", "404").add("message", "课表未找到");
        }

        Date now = new Date();
        if (timetable.getBeginDate().before(now) && (CollectionUtils.isEmpty(timetable.getCourses()) ||
                timetable.getCourses().stream().filter(e -> StringUtils.isNotBlank(e.getBookId()) && e.getBookId().equalsIgnoreCase(bookId))
                        .findFirst().orElse(null) == null)) {
            return MapMessage.errorMessage().add("result", "404").add("message", "课程内容正在加班加点制作中，敬请期待");
        }

        aiUserLessonBookRefPersistence.deleteByUser(userId);

        AIUserLessonBookRef bookRef = new AIUserLessonBookRef();
        bookRef.setUserId(userId);
        bookRef.setBookId(bookId);
        bookRef.setBookName(item.getName());
        bookRef.setProductId(productId);
        bookRef.setCreateTime(now);
        bookRef.setUpdateTime(now);
        bookRef.setDisabled(false);
        aiUserLessonBookRefPersistence.insertOrUpdate(bookRef);

        return MapMessage.successMessage().add("result", "success");
    }

    @Override
    public AIUserLessonBookRef loadUserBookRef(Long userId) {
        return aiUserLessonBookSupport.fetchUserCurrentBook(userId);
    }

    /**
     * @param typeList "1"-已完结,"2"-当前,"3"-未开始
     * @return 如果typeList 是空 返回所有的
     */
    @Override
    public List<OrderProduct> loadProductByType(List<String> typeList) {
        List<OrderProduct> productList = userOrderLoaderClient.loadAllOrderProductIncludeOfflineForCrm().stream()
                .filter(e -> Boolean.FALSE.equals(e.getDisabled()))
                .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(typeList)) {
            return Collections.emptyList();
        }
        return productList.stream().filter(p -> filterProduct(p, typeList)).collect(Collectors.toList());
    }

    @Override
    public List<ChipsUserCourseMapper> loadUserAllCourseInfo(Long userId) {
        if (userId == null || userId == 0L) {
            return Collections.emptyList();
        }
        // 获取用户全部课程 包含过期的课程， 不包含退款的课程
        List<ChipsUserCourse> userCourses = chipsUserService.loadUserAllCourse(userId);
        if (CollectionUtils.isEmpty(userCourses)) {
            return Collections.emptyList();
        }
        Set<String> productIds = userCourses.stream().map(ChipsUserCourse::getProductId).collect(Collectors.toSet());
        Set<String> productItemIds = userCourses.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet());

        Map<String, OrderProduct> orderProductMap = userOrderLoaderClient.loadOrderProducts(productIds);
        Map<String, OrderProductItem> orderProductItemMap = userOrderLoaderClient.loadOrderProductItems(productItemIds);
        Set<String> bookIds = orderProductItemMap.values().stream().filter(e -> StringUtils.isNotBlank(e.getAppItemId()))
                .map(OrderProductItem::getAppItemId).collect(Collectors.toSet());
        Map<String, StoneData> bookDataMap = stoneDataLoaderClient.loadStoneDataIncludeDisabled(bookIds);

        List<ChipsUserCourseMapper> chipsUserCourseMappers = new ArrayList<>();
        Date now = new Date();
        for (ChipsUserCourse course : userCourses) {
            ChipsUserCourseMapper mapper = new ChipsUserCourseMapper();
            mapper.setId(course.getId());
            mapper.setProductId(course.getProductId());
            mapper.setOperation(course.getOperation().name());
            mapper.setServiceBeginDate(DateUtils.dateToString(course.getServiceBeginDate()));
            mapper.setServiceEndDate(DateUtils.dateToString(course.getServiceEndDate()));

            ChipsEnglishProductTimetable config = chipsEnglishProductTimetableDao.load(course.getProductId());
            Date beginDate = config.getBeginDate();
            Date endDate = config.getEndDate();
            if (beginDate == null || endDate == null) {
                continue;
            }
            AIBookStatus status;
            if (now.before(beginDate)) {
                status = AIBookStatus.UnBegin;
            } else if (now.after(endDate)) {
                status = AIBookStatus.Finished;
            } else {
                status = AIBookStatus.InTime;
            }
            mapper.setStatus(status.name());

            OrderProduct product = orderProductMap.get(course.getProductId());
            if (product != null) {
                mapper.setProductName(product.getName());
            }

            OrderProductItem orderProductItem = orderProductItemMap.get(course.getProductItemId());
            if (orderProductItem != null) {
                StoneData book = bookDataMap.get(orderProductItem.getAppItemId() == null ? "" : orderProductItem.getAppItemId());
                if (book != null) {
                    mapper.setBookId(book.getId());
                    mapper.setBookName(book.getCustomName());
                }
            }
            chipsUserCourseMappers.add(mapper);
        }
        return chipsUserCourseMappers;
    }

    /**
     * @param typeList "1"-已完结,"2"-当前,"3"-未开始,beginDate,endDate yyyy-mm-dd 格式
     */
    private boolean filterProduct(OrderProduct product, List<String> typeList) {
        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(product.getId());
        if (timetable == null) {
            return false;
        }
        Date now = DayRange.current().getStartDate();

        for (String type : typeList) {
            if ("1".equals(type)) {
                if (timetable.getEndDate() != null && now.after(timetable.getEndDate())) {
                    return true;
                }
            }
            if ("2".equals(type)) {
                if (timetable.getBeginDate() != null && timetable.getEndDate() != null && !now.before(timetable.getBeginDate()) && !now.after(timetable.getEndDate())) {
                    return true;
                }
            }
            if ("3".equals(type)) {
                if (timetable.getBeginDate() != null && now.before(timetable.getBeginDate())) {
                    return true;
                }
            }
        }
        return false;
    }
}
