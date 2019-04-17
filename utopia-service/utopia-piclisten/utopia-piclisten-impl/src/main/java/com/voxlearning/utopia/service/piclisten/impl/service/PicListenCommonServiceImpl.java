package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DateRangePrecision;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.athena.api.StuAuthQueryService;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.client.AsyncOrderCacheServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.piclisten.cache.PiclistenCache;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2018-01-10 下午4:13
 **/
@Named
@ExposeServices({
        @ExposeService(interfaceClass = PicListenCommonService.class, version = @ServiceVersion(version = "2018-01-10")),
        @ExposeService(interfaceClass = PicListenCommonService.class, version = @ServiceVersion(version = "2018-09-26"))
}
)
public class PicListenCommonServiceImpl extends SpringContainerSupport implements PicListenCommonService {

    @Inject private RaikouSystem raikouSystem;

    @Inject
    private AsyncPiclistenCacheServiceImpl asyncPiclistenCacheService;

    @Inject
    private AsyncOrderCacheServiceClient asyncOrderCacheServiceClient;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @ImportService(interfaceClass = StuAuthQueryService.class)
    private StuAuthQueryService stuAuthQueryService;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Override
    public Boolean userIsAuthForPicListen(User user) {
        if (user == null)
            return false;
        user = raikouSystem.loadUser(user.getId());
        if (user == null)
            return false;
        if (user.isParent())
            return isParentAuth(user.getId());
        if (user.isStudent())
            return isStudentAuth(user.getId());
        return false;
    }

    @Override
    public Map<String, DayRange> parentBuyBookPicListenLastDayMap(Long parentId, boolean keepExpired) {
        List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(parentId);
        userActivatedProducts = userActivatedProducts.stream().filter(t -> OrderProductServiceType.PicListenBook == OrderProductServiceType.safeParse(t.getProductServiceType())).collect(Collectors.toList());
        Map<String, List<UserActivatedProduct>> item2ActiveListMap = userActivatedProducts.stream().collect(Collectors.groupingBy(UserActivatedProduct::getProductItemId));
        List<String> productItemIds = userActivatedProducts.stream().sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime()))
                .map(UserActivatedProduct::getProductItemId).collect(Collectors.toList());
        Map<String, OrderProductItem> orderProductItemMap = userOrderLoaderClient.loadOrderProductItems(productItemIds);

        Map<String, DayRange> map = new LinkedHashMap<>();
        productItemIds.forEach(itemId -> {
            OrderProductItem orderProductItem = orderProductItemMap.get(itemId);
            if (orderProductItem == null)
                return;
            List<UserActivatedProduct> userActivatedProductList = item2ActiveListMap.get(itemId);
            if (CollectionUtils.isEmpty(userActivatedProductList))
                return;
            UserActivatedProduct userActivatedProduct = userActivatedProductList.stream().sorted((o1, o2) -> o2.getServiceEndTime().compareTo(o1.getServiceEndTime()))
                    .findFirst().orElse(null);
            if (userActivatedProduct == null)
                return;
            if (!keepExpired && userActivatedProduct.getServiceEndTime().before(new Date()))
                return;
            map.put(orderProductItem.getAppItemId(), DayRange.newInstance(userActivatedProduct.getServiceEndTime().getTime(), DateRangePrecision.MILLISECOND));
        });

        return map;
    }

    @Override
    public Map<String, DayRange> parentBuyWalkManLastDayMap(Long parentId, boolean keepExpired) {
        List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(parentId);
        userActivatedProducts = userActivatedProducts.stream().filter(t -> OrderProductServiceType.WalkerMan == OrderProductServiceType.safeParse(t.getProductServiceType())).collect(Collectors.toList());
        Map<String, List<UserActivatedProduct>> item2ActiveListMap = userActivatedProducts.stream().collect(Collectors.groupingBy(UserActivatedProduct::getProductItemId));
        List<String> productItemIds = userActivatedProducts.stream().sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime()))
                .map(UserActivatedProduct::getProductItemId).collect(Collectors.toList());
        Map<String, OrderProductItem> orderProductItemMap = userOrderLoaderClient.loadOrderProductItems(productItemIds);
        Map<String, DayRange> map = new LinkedHashMap<>();
        productItemIds.forEach(itemId -> {
            OrderProductItem orderProductItem = orderProductItemMap.get(itemId);
            if (orderProductItem == null)
                return;
            List<UserActivatedProduct> userActivatedProductList = item2ActiveListMap.get(itemId);
            if (CollectionUtils.isEmpty(userActivatedProductList))
                return;
            UserActivatedProduct userActivatedProduct = userActivatedProductList.stream().sorted((o1, o2) -> o2.getServiceEndTime().compareTo(o1.getServiceEndTime()))
                    .findFirst().orElse(null);
            if (userActivatedProduct == null)
                return;
            if (!keepExpired && userActivatedProduct.getServiceEndTime().before(new Date()))
                return;
            map.put(orderProductItem.getAppItemId(), DayRange.newInstance(userActivatedProduct.getServiceEndTime().getTime(), DateRangePrecision.MILLISECOND));
        });
        return map;
    }


    @Override
    public DayRange parentBuyScoreLastDay(Long parentId) {
        List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(parentId);
        userActivatedProducts = userActivatedProducts.stream().filter(t -> OrderProductServiceType.FollowRead == OrderProductServiceType.safeParse(t.getProductServiceType())).collect(Collectors.toList());
        UserActivatedProduct userActivatedProduct = userActivatedProducts.stream().sorted((o1, o2) -> o2.getServiceEndTime().compareTo(o1.getServiceEndTime())).findFirst().orElse(null);
        if (userActivatedProduct == null)
            return null;
        if (userActivatedProduct.getServiceEndTime() == null)
            return null;
        if (userActivatedProduct.getServiceEndTime().before(new Date()))
            return null;
        return DayRange.newInstance(userActivatedProduct.getServiceEndTime().getTime(), DateRangePrecision.MILLISECOND);
    }

    /**
     * 是否购买点读
     *
     * @param parentId
     * @return
     */
    @Override
    public Boolean parentHasBuyScore(Long parentId) {
        List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(parentId);
        userActivatedProducts = userActivatedProducts.stream().filter(t -> OrderProductServiceType.FollowRead == OrderProductServiceType.safeParse(t.getProductServiceType())).collect(Collectors.toList());
        return userActivatedProducts.stream().anyMatch(t -> t.getServiceEndTime().after(new Date()));
    }


    /**
     * @param user
     * @param keepExpired 如果为 true，则如果已经过期，map 中同样会有相关信息；如果为 false，map.get(bookId)为 null
     * @return
     */
    @Override
    public Map<String, PicListenBookPayInfo> userBuyBookPicListenLastDayMap(User user, boolean keepExpired) {
        if (user == null)
            return Collections.emptyMap();
        if (user.isStudent())
            return studentPicListenBuyInfoMap(user.getId(), keepExpired);
        if (user.isParent())
            return parentPicListenBuyInfoMap(user.getId(), keepExpired);
        return Collections.emptyMap();
    }

    private static String purchaseCountKey = "PicListenPurchaseCounter";

    public void addPiclistenPurchaseCount(long delta) {
        if (delta <= 0) {
            return;
        }
        Long incr = SafeConverter.toLong(PiclistenCache.getPersistenceCache().incr(purchaseCountKey, delta, 1L, 0));
        if (incr == 1) {
            Long aLong = SafeConverter.toLong(asyncOrderCacheServiceClient.getAsyncOrderCacheService().PicListenBookPurchaseCacheManager_loadBuyCount().getUninterruptibly());
            if (aLong != 0) {
                PiclistenCache.getPersistenceCache().incr(purchaseCountKey, aLong, 1L, 0);
            }
        }
    }

    @Override
    public Long loadPicListenPurchaseCount() {
        return SafeConverter.toLong(PiclistenCache.getPersistenceCache().load(purchaseCountKey));
    }


    public Map<String, PicListenBookPayInfo> studentPicListenBuyInfoMap(Long studentId, boolean keepExpired) {
        if (studentId == null)
            return Collections.emptyMap();
        List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(studentId);
        List<Long> parentIdList = studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toList());
        Map<String, DayRange> map = new HashMap<>();
        Map<String, Long> bookId2ParentIdMap = new HashMap<>();
        parentIdList.forEach(t -> {
            Map<String, DayRange> dayRangeMap = this.parentBuyBookPicListenLastDayMap(t, keepExpired);
            if (MapUtils.isEmpty(dayRangeMap))
                return;
            dayRangeMap.forEach((booKId, dayRange) -> {
                DayRange dayRange1 = map.get(booKId);
                if (dayRange1 == null || dayRange.getEndTime() > dayRange1.getEndTime()) {
                    map.put(booKId, dayRange);
                    bookId2ParentIdMap.put(booKId, t);
                }
            });
        });
        Map<String, PicListenBookPayInfo> picListenBookPayInfoMap = new HashMap<>();
        map.forEach((bookId, dayRange) -> {
            Long parentId = bookId2ParentIdMap.get(bookId);
            if (parentId == null)
                return;
            PicListenBookPayInfo info = PicListenBookPayInfo.newInstance(parentId, dayRange);
            picListenBookPayInfoMap.put(bookId, info);
        });
        return picListenBookPayInfoMap;
    }


    public Map<String, PicListenBookPayInfo> parentPicListenBuyInfoMap(Long parentId, boolean keepExpired) {
        Map<String, DayRange> dayRangeMap = this.parentBuyBookPicListenLastDayMap(parentId, keepExpired);
        Map<String, PicListenBookPayInfo> map = new HashMap<>();
        dayRangeMap.forEach((bookId, dayRange) -> {
            PicListenBookPayInfo info = PicListenBookPayInfo.newInstance(parentId, dayRange);
            map.put(bookId, info);
        });
        return map;
    }

    /**
     * 加了1小时的缓存
     *
     * @param parentId
     * @return
     */
    private Boolean isParentAuth(Long parentId) {
        String key = CacheKeyGenerator.generateCacheKey("parentAuth", new String[]{"pid"}, new Object[]{parentId});
        CacheObject<Object> cacheObject = PiclistenCache.getPersistenceCache().get(key);
        if (cacheObject == null || cacheObject.getValue() == null) {
            Boolean isParentAuth = innerIsParentAuth(parentId);
            PiclistenCache.getPersistenceCache().set(key, 3600, isParentAuth);
            return isParentAuth;
        } else {
            return SafeConverter.toBoolean(cacheObject.getValue());
        }
    }

    private Boolean innerIsParentAuth(Long parentId) {
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        if (CollectionUtils.isEmpty(studentParentRefs))
            return false;
        List<Long> studentIdList = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
        try {
            List<Long> authedStudentIds = stuAuthQueryService.filerAuthedStudents(studentIdList, SchoolLevel.JUNIOR);
            return CollectionUtils.isNotEmpty(authedStudentIds);
        } catch (Exception e) {
            logger.warn("athena api : stuAuthQueryService.filerAuthedStudents failed !!", e);
            return false;
        }
    }

    public boolean isStudentAuth(Long studentId) {
        List<Long> longs = stuAuthQueryService.filerAuthedStudents(Collections.singleton(studentId), SchoolLevel.JUNIOR);
        return CollectionUtils.isNotEmpty(longs);
    }

}
