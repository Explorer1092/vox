package com.voxlearning.utopia.service.ai.internal;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.cache.manager.ChipsClassCountCacheManager;
import com.voxlearning.utopia.service.ai.cache.manager.UserPageVisitCacheManager;
import com.voxlearning.utopia.service.ai.constant.ChipsUserDrawingTaskStatus;
import com.voxlearning.utopia.service.ai.data.StoneQuestionData;
import com.voxlearning.utopia.service.ai.entity.*;
import com.voxlearning.utopia.service.ai.exception.ProductNotExitException;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.impl.persistence.support.AIUserLessonBookSupport;
import com.voxlearning.utopia.service.ai.impl.service.AiChipsEnglishConfigServiceImpl;
import com.voxlearning.utopia.service.ai.impl.support.ConstantSupport;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.StoneData;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChipsUserService {
    @Inject
    private AIUserLessonBookSupport aiUserLessonBookSupport;
    @Inject
    private AIUserLessonBookRefPersistence aiUserLessonBookRefPersistence;

    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;

    @Inject
    private ChipsEnglishClassExtDao chipsEnglishClassExtDao;

    @Inject
    private ChipsEnglishClassUserRefPersistence chipsEnglishClassUserRefPersistence;

    @Inject
    private ChipsEnglishUserExtSplitDao chipsEnglishUserExtSplitDao;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private AiChipsEnglishConfigServiceImpl chipsEnglishConfigService;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private ChipsUserCoursePersistence chipsUserCoursePersistence;

    @Inject
    private ChipsClassCountCacheManager chipsClassCountCacheManager;

    @Inject
    private ChipsMessageService chipsMessageService;

    @Inject
    private UserPageVisitCacheManager userPageVisitCacheManager;

    @Inject
    private StoneDataLoaderClient stoneDataLoaderClient;

    private static final Logger logger = LoggerFactory.getLogger(ChipsUserService.class);

    private final static String Chips_Super_User = "Chips_Super_User";

    private final static String BOTTOM_AD_BLACK = "advertisement_black_list_cfg";


    public void processOrder(ChipsUserOrderExt chipsUserOrderExt, UserOrder userOrder) {
        Map<String, UserActivatedProduct> userActivatedProductMap = getUserActiveProduct(userOrder.getUserId());
        List<ChipsUserCourse> chipsUserCourses = loadUserEffectiveCourse(userOrder.getUserId());
        boolean marketBrand = chipsUserOrderExt != null && StringUtils.isNotBlank(chipsUserOrderExt.getSaleStaffId());

        List<UserOrderProductRef> userOrderProductRefs = userOrderLoaderClient.loadOrderProducts(userOrder.getUserId(), userOrder.getId());
        if (CollectionUtils.isEmpty(userOrderProductRefs)) {
            List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(userOrder.getProductId())
                    .stream()
                    .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                    .collect(Collectors.toList());
            doProcessOrderProduct(userOrder.getProductId(), userOrder.getProductName(), userOrder.getUserId(), userOrder.getId(),
                    userOrder.getOrderReferer(), userActivatedProductMap, itemList, chipsUserCourses, marketBrand);
        } else {
            for (UserOrderProductRef userOrderProductRef : userOrderProductRefs) {
                List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(userOrderProductRef.getProductId()).stream()
                        .filter(e -> OrderProductServiceType.safeParse(e.getProductType()) == OrderProductServiceType.ChipsEnglish)
                        .collect(Collectors.toList());
                doProcessOrderProduct(userOrderProductRef.getProductId(), userOrderProductRef.getProductName(), userOrder.getUserId(), userOrder.getId(),
                        userOrder.getOrderReferer(), userActivatedProductMap, itemList, chipsUserCourses, marketBrand);
            }
        }

    }

    public Map<String, UserActivatedProduct> getUserActiveProduct(Long userId) {
        List<UserActivatedProduct> userActivatedProductList = userOrderLoaderClient.loadUserActivatedProductList(userId).stream()
                .filter(e -> e.getDisabled() != null && !e.getDisabled())
                .filter(e -> e.getProductServiceType().equals(OrderProductServiceType.ChipsEnglish.name()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userActivatedProductList)) {
            return Collections.emptyMap();
        }
        return userActivatedProductList.stream().collect(Collectors.toMap(UserActivatedProduct::getProductItemId, e -> e));
    }

    private void doProcessOrderProduct(String productId, String productName, Long userId, String orderId, String orderRef, Map<String, UserActivatedProduct> userActiveMap, List<OrderProductItem> orderProductItemList, List<ChipsUserCourse> chipsUserCourses, boolean marketBrand) {
        try {
            if (CollectionUtils.isNotEmpty(orderProductItemList)) {
                for (OrderProductItem item : orderProductItemList) {
                    UserActivatedProduct userActivatedProduct = userActiveMap.get(item.getId());
                    if (userActivatedProduct == null) {
                        continue;
                    }
                    ChipsUserCourse ext = chipsUserCourses.stream()
                            .filter(course -> course.getOrderId().equals(orderId))
                            .filter(course -> course.getOriginalProductId().equals(productId))
                            .filter(course -> course.getOriginalProductItemId().equals(item.getId()))
                            .findFirst().orElse(null);
                    if (ext != null) {
                        continue;
                    }
                    ChipsUserCourse userCourse = ChipsUserCourse.initNewCourse(userId, orderId, productId, item.getId(), userActivatedProduct.getServiceStartTime(), userActivatedProduct.getServiceEndTime());
                    chipsUserCoursePersistence.insertOrUpdate(userCourse);
                }
            }
            List<ChipsEnglishClass> chipsEnglishClasses = chipsEnglishClassPersistence.loadByProductId(productId);
            if (CollectionUtils.isNotEmpty(chipsEnglishClasses)) {
                chipsEnglishClasses.sort(Comparator.comparing(ChipsEnglishClass::getCreateTime));

                ChipsEnglishClass chipsEnglishClass = Optional.of(chipsEnglishClasses)
                        .map(chipClass -> {
                            List<ChipsEnglishClass> selectList = chipClass.stream()
                                    .filter(e -> (marketBrand && e.getType() == ChipsEnglishClass.Type.MARKING_BRAND) || (!marketBrand && e.getType() == ChipsEnglishClass.Type.NORMAL))
                                    .collect(Collectors.toList());
                            if (CollectionUtils.isEmpty(selectList)) {
                                selectList = chipClass;
                            }
                            for(ChipsEnglishClass cla : selectList) {
                                if (cla.getUserLimit() == null) {
                                    continue;
                                }
                                Long number = chipsClassCountCacheManager.increase(cla.getId());
                                if (number == null || number.compareTo(cla.getUserLimit().longValue()) <= 0) {
                                    return cla;
                                }
                                chipsClassCountCacheManager.decrease(cla.getId());
                            }
                            chipsClassCountCacheManager.increase(selectList.get(0).getId());
                            return selectList.get(0);
                        }).filter(cla -> cla != null)
                        .orElse(chipsEnglishClasses.get(0));
                createNewChipsClazzUser(userId, chipsEnglishClass.getId(), orderRef);
            } else {
                //notify email
                chipsMessageService.notifyNoClass(productName, userId);
            }
        } catch (Exception e) {
            logger.error("doProcessOrderProduct error. productId:{}, userId:{}", productName, userId, e);
        }
    }

    private void createNewChipsClazzUser(Long userId, Long clazzId, String orderRef) {
        ChipsEnglishClassUserRef chipsEnglishClassUserRef = new ChipsEnglishClassUserRef();
        chipsEnglishClassUserRef.setChipsClassId(clazzId);
        chipsEnglishClassUserRef.setUserId(userId);
        chipsEnglishClassUserRef.setDisabled(false);
        chipsEnglishClassUserRef.setOrderRef(orderRef);
        chipsEnglishClassUserRef.setCreateTime(new Date());
        chipsEnglishClassUserRef.setUpdateTime(new Date());
        chipsEnglishClassUserRefPersistence.insertOrUpdate(chipsEnglishClassUserRef);
        ChipsEnglishUserExtSplit extSplit = new ChipsEnglishUserExtSplit();
        extSplit.setId(userId);
        chipsEnglishUserExtSplitDao.upsert(extSplit);
    }


    public ChipsEnglishClass loadClazzByUserAndBook(Long userId, String bookId) {
        AIUserLessonBookRef bookRef = aiUserLessonBookRefPersistence.loadByUserId(userId).stream()
                .filter(e -> e.getBookId().equals(bookId))
                .findFirst().orElse(null);
        if (bookRef == null) {
            return null;
        }
        return loadClazzIdByUserAndProduct(userId, bookRef.getProductId());
    }

    public ChipsEnglishProductTimetable loadTimetableByUserAndBook(Long userId, String bookId) {
        AIUserLessonBookRef bookRef = aiUserLessonBookRefPersistence.loadByUserId(userId).stream()
                .filter(e -> e.getBookId().equals(bookId))
                .findFirst().orElse(null);
        if (bookRef == null) {
            return null;
        }
        return chipsEnglishProductTimetableDao.load(bookRef.getProductId());
    }

    /**
     * 通过userid获取班级，默认从当前所选的教材对应的班级
     *
     * @param userId
     * @return
     */
    public ChipsEnglishClass loadClazzByUser(Long userId) {
        AIUserLessonBookRef bookRef = aiUserLessonBookSupport.fetchUserCurrentBook(userId);
        if (bookRef == null) {
            return null;
        }
        return loadClazzIdByUserAndProduct(userId, bookRef.getProductId());
    }


    public Set<String> loadUserBoughtProduct(Long userId) {
        return chipsUserCoursePersistence.loadByUserId(userId).stream().map(ChipsUserCourse::getProductId).collect(Collectors.toSet());
    }

    public void addUserSentenceLearnNum(Long userId) {
        chipsEnglishUserExtSplitDao.updateUserStudyNumber(userId, 1);
    }

    public List<ChipsEnglishClass> loadClazzListByUser(Long userId) {
        if (userId == null || userId == 0l) {
            return Collections.emptyList();
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return Collections.emptyList();
        }

        Set<Long> clazzIdSet = userRefList.stream().map(ChipsEnglishClassUserRef::getChipsClassId).collect(Collectors.toSet());
        Map<Long, ChipsEnglishClass> idToClazzMap = chipsEnglishClassPersistence.loads(clazzIdSet);
        if (MapUtils.isEmpty(idToClazzMap)) {
            return Collections.emptyList();
        }
        return idToClazzMap.values().stream().collect(Collectors.toList());
    }

    public ChipsEnglishClass loadClazzIdByUserAndProduct(Long userId, String productId) {
        if (userId == null || userId == 0l || StringUtils.isBlank(productId)) {
            return null;
        }
        List<ChipsEnglishClass> clazzLst = loadClazzListByUser(userId);
        return clazzLst.stream().filter(c -> c != null && StringUtils.isNotBlank(c.getProductId()) && c.getProductId().equals(productId)).findFirst().orElse(null);
    }

    public List<ChipsEnglishClassUserRef> selectChipsEnglishClassUserRefByClazzId(Long clazzId) {
        if (clazzId == null || clazzId == 0l) {
            return Collections.emptyList();
        }
        return chipsEnglishClassUserRefPersistence.loadByClassId(clazzId);
    }

    public Long loadClazzIdByUserAndUnit(Long userId, String unitId) {
        if (userId == null || userId == 0l || StringUtils.isBlank(unitId)) {
            return null;
        }
        List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByUserId(userId);
        if (CollectionUtils.isEmpty(userRefList)) {
            return null;
        }
        Set<Long> clazzIdSet = new HashSet<>();
        userRefList.stream().filter(userRef -> userRef != null && userRef.getChipsClassId() != null && userRef.getChipsClassId() != 0l).forEach(userRef -> clazzIdSet.add(userRef.getChipsClassId()));
        Map<Long, ChipsEnglishClassExt> idToExtMap = chipsEnglishClassExtDao.loads(clazzIdSet);
        if (MapUtils.isEmpty(idToExtMap)) {
            return null;
        }
        Map<Long, Set<String>> collect = idToExtMap.values().stream().filter(ext -> ext != null && CollectionUtils.isNotEmpty(ext.getCourses()))
                .collect(Collectors.toMap(ChipsEnglishClassExt::getId, ext -> ext.getCourses().stream().map(e -> e.getUnitId()).collect(Collectors.toSet())));
        for (Map.Entry<Long, Set<String>> entry : collect.entrySet()) {
            if (entry.getValue().contains(unitId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean isInWhiteList(Long userId) {
        ChipsEnglishPageContentConfig config = chipsEnglishConfigService.loadChipsConfigByName(Chips_Super_User);
        if (config == null) {
            return false;
        }
        List<String> userIds = Arrays.asList(config.getValue().split(","));
        return CollectionUtils.isNotEmpty(userIds) && userIds.contains(SafeConverter.toString(userId));
    }

    public boolean isInADBlackList(Long userId) {
        return Optional.ofNullable(chipsEnglishConfigService.loadChipsConfigByName(BOTTOM_AD_BLACK))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(e -> StringUtils.isNotBlank(e))
                .map(e -> e.contains(userId.toString()))
                .orElse(false);
    }

    public List<ChipsUserCourse> loadUserEffectiveCourse(Long userId) {
        Date now = new Date();
        return chipsUserCoursePersistence.loadByUserId(userId).stream().filter(e -> e.getServiceEndDate().after(now))
                .filter(e -> Boolean.TRUE.equals(e.getActive())).collect(Collectors.toList());
    }

    public List<ChipsUserCourse> loadUserEffectiveCourseIncludeUnactive(Long userId) {
        Date now = new Date();
        return chipsUserCoursePersistence.loadByUserId(userId).stream().filter(e -> e.getServiceEndDate().after(now)).collect(Collectors.toList());
    }

    public AIUserLessonBookRef fetchOrInitBookRef(Long userId) throws ProductNotExitException {
        List<ChipsUserCourse> chipsUserCourses = loadUserEffectiveCourse(userId);
        if (CollectionUtils.isEmpty(chipsUserCourses)) {
            throw new ProductNotExitException("user does not buy any product");
        }

        Set<String> itemIds = chipsUserCourses.stream().map(ChipsUserCourse::getProductItemId).collect(Collectors.toSet());
        Map<String, OrderProductItem> itemMap = userOrderLoaderClient.loadOrderProductItems(itemIds);
        if (MapUtils.isEmpty(itemMap)) {
            throw new ProductNotExitException("product item not exist");
        }

        Set<String> productIds = chipsUserCourses.stream().map(ChipsUserCourse::getProductId).collect(Collectors.toSet());
        AIUserLessonBookRef bookRef = aiUserLessonBookSupport.fetchUserCurrentBook(userId);
        if (bookRef == null || !productIds.contains(bookRef.getProductId())) {
            Map<String, List<OrderProductItem>> orderProductItemMap = new HashMap<>();
            chipsUserCourses.stream().collect(Collectors.groupingBy(ChipsUserCourse::getProductId)).forEach((k, v) -> {
                List<OrderProductItem> itemList = new ArrayList<>();
                v.stream().map(ChipsUserCourse::getProductItemId).forEach(e -> itemList.add(itemMap.get(e)));
                orderProductItemMap.put(k, itemList);
            });
            bookRef = aiUserLessonBookSupport.initNewUserBookV2(productIds, orderProductItemMap, userId);
        }
        return bookRef;
    }

    public List<ChipsUserCourse> loadUserAllCourse(Long userId) {
        return chipsUserCoursePersistence.loadByUserId(userId);
    }


    public List<Map<String, Object>> loadDrawingTaskPopData(List<ChipsUserDrawingTask> userDrawingTasks, Long userId) {
        if (CollectionUtils.isEmpty(userDrawingTasks)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> popData = new ArrayList<>();
        String key = ConstantSupport.DRAWING_TASK_FINISH_CACHE_KEY + userId;
        Set<Long> drawingFinish = userPageVisitCacheManager.getRecordIds(key);
        if (CollectionUtils.isNotEmpty(drawingFinish)) {
            userPageVisitCacheManager.delete(key);
            List<ChipsUserDrawingTask> popList = userDrawingTasks.stream()
                    .filter(e -> drawingFinish.contains(e.getId()))
                    .sorted(Comparator.comparing(ChipsUserDrawingTask::getUpdateTime).reversed())
                    .limit(10L)
                    .collect(Collectors.toList());
            Map<String, StoneData> popStoneData = stoneDataLoaderClient.loadStoneDataIncludeDisabled(popList.stream().map(ChipsUserDrawingTask::getDrawingId).collect(Collectors.toSet()));
            for (ChipsUserDrawingTask task : popList) {
                StoneQuestionData questionData = Optional.ofNullable(popStoneData).map(e -> e.get(task.getDrawingId()))
                        .map(StoneQuestionData::newInstance)
                        .orElse(null);
                if (questionData == null) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("drawingTaskId", task.getId());
                map.put("unitId", task.getUnitId());
                map.put("status", ChipsUserDrawingTaskStatus.finished.name());
                map.put("jsonData", questionData.getJsonData());
                popData.add(map);
            }
        }
        return popData;
    }
}
