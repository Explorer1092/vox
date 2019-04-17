package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.*;
import com.voxlearning.utopia.service.mizar.api.loader.MizarLoader;
import com.voxlearning.utopia.service.mizar.api.mapper.LoadMizarShopContext;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarCourseMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarShopMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.TradeAreaMapper;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2016/8/15.
 */
public class MizarLoaderClient {

    @ImportService(interfaceClass = MizarLoader.class) private MizarLoader mizarLoader;

    @Inject private MicroCourseLoaderClient microCourseLoaderClient;

    private static UtopiaCache persistence = CacheSystem.CBS.getCacheBuilder().getCache("persistence");

    private static String goodsSellCountKey = "MizarLoader_goods_sell_count_";

    private static String goodsDaySellCountKey = "MizarLoader_goods_day_sell_count_";

    public Long incrSellCount(String gId) {
        return persistence.incr(goodsSellCountKey + gId, 1, 1, 0);
    }

    public Long loadSellCount(String gId) {
        CacheObject<Object> objectCacheObject = persistence.get(goodsSellCountKey + gId);
        if (objectCacheObject == null) {
            return 0L;
        }
        return SafeConverter.toLong(objectCacheObject.getValue());
    }

    public Long incrDaySellCount(String gId) {
        return persistence.incr(goodsDaySellCountKey + gId, 1, 1, DateUtils.getCurrentToDayEndSecond());
    }

    public Long loadDaySellCount(String gId) {
        CacheObject<Object> objectCacheObject = persistence.get(goodsDaySellCountKey + gId);
        if (objectCacheObject == null) {
            return 0L;
        }
        return SafeConverter.toLong(objectCacheObject.getValue());
    }

    //------------------------------------------------------------------------------
    //-------------------------          BRAND        ------------------------------
    //------------------------------------------------------------------------------

    public MizarBrand loadBrandById(String brandId) {
        if (StringUtils.isBlank(brandId)) {
            return null;
        }
        return mizarLoader.loadBrandById(brandId);
    }

    public Map<String, MizarBrand> loadBrandByIds(Collection<String> brandIds) {
        if (CollectionUtils.isEmpty(brandIds)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadBrandByIds(brandIds);
    }

    public Page<MizarBrand> loadBrandByPage(Pageable page, String brandName) {
        if (page == null) {
            return null;
        }
        return mizarLoader.loadBrandByPage(page, brandName);
    }

    //------------------------------------------------------------------------------
    //-------------------------           SHOP        ------------------------------
    //------------------------------------------------------------------------------

    public MizarShop loadShopById(String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return null;
        }
        return mizarLoader.loadShopById(shopId);
    }

    public Map<String, MizarShop> loadShopByIds(Collection<String> shopIds) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadShopByIds(shopIds);
    }

    public Map<String, Object> loadShopShowMap(String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadShopShowMap(shopId);
    }

    public Page<MizarShop> loadShopByPage(Pageable page, String shopName) {
        if (page == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        return mizarLoader.loadShopByPage(page, shopName);
    }

    public Page<MizarShop> loadAllShopByPage(Pageable page, String shopName) {
        if (page == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        return mizarLoader.loadAllShopByPage(page, shopName);
    }

    public Page<MizarShop> loadAllShopByPage(Pageable page, String shopName, Boolean vip, Boolean cooperator) {
        if (page == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        return mizarLoader.loadAllShopByPage(page, shopName, vip, cooperator);
    }

    public List<MizarShop> loadShopByBrand(String brandId) {
        if (StringUtils.isBlank(brandId)) {
            return Collections.emptyList();
        }
        return mizarLoader.loadShopByBrand(brandId);
    }

    public PageImpl<MizarShopMapper> loadShopPageByParam(LoadMizarShopContext context) {
        return mizarLoader.loadShopPageByParam(context);
    }

    //------------------------------------------------------------------------------
    //-------------------------          任务调用        ------------------------------
    //------------------------------------------------------------------------------
    public Set<String> loadAllShopIds() {
        return mizarLoader.loadAllShopIds();
    }

    public Set<String> loadAllBrandIds() {
        return mizarLoader.loadAllBrandIds();
    }

    public Set<String> loadAllShopGoodsIds() {
        return mizarLoader.loadAllShopGoodsIds();
    }

    public Set<String> loadAllRatingIds() {
        return mizarLoader.loadAllRatingIds();
    }

    //------------------------------------------------------------------------------
    //-------------------------          GOODS        ------------------------------
    //------------------------------------------------------------------------------

    public MizarShopGoods loadShopGoodsById(String goodsId) {
        if (StringUtils.isBlank(goodsId)) {
            return null;
        }
        return mizarLoader.loadShopGoodsById(goodsId);
    }

    public Map<String, MizarShopGoods> loadShopGoodsByIds(Collection<String> goodsIds) {
        if (CollectionUtils.isEmpty(goodsIds)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadShopGoodsByIds(goodsIds);
    }

    public Page<MizarShopGoods> loadShopGoodsByPage(Pageable page, String goodsName) {
        if (page == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        return mizarLoader.loadShopGoodsByPage(page, goodsName);
    }

    public List<MizarShopGoods> loadShopGoodsByShop(String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        return mizarLoader.loadShopGoodsByShop(shopId);
    }

    public Map<String, List<MizarShopGoods>> loadShopGoodsByShop(Collection<String> shopIds) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadShopGoodsByShop(shopIds);
    }

    public Page<MizarShopGoods> loadPageByGoodsType(Pageable page, MizarGoodsStatus status, Collection<String> types) {
        if (page == null || CollectionUtils.isEmpty(types)) {
            return new PageImpl<>(Collections.emptyList());
        }
        return mizarLoader.loadPageByGoodsType(page, status, types);
    }

    //------------------------------------------------------------------------------
    //-------------------------         RATING        ------------------------------
    //------------------------------------------------------------------------------

    public List<MizarRating> loadShopRatings(String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        return mizarLoader.loadShopRatings(shopId);
    }

    public Map<String, List<MizarRating>> loadShopRatings(Collection<String> shopIds) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadShopRatings(shopIds);
    }

    public PageImpl<Map<String, Object>> loadCollectionRatings(Integer activityId, Long time, Pageable pageable) {
        return mizarLoader.loadCollectionRatings(activityId, time, pageable);
    }

    public PageImpl<MizarRating> loadRatingPage(String shopId, Pageable pageable) {
        return mizarLoader.loadRatingPage(shopId, pageable);
    }

    public MizarRating loadRatingById(String ratingId) {
        if (StringUtils.isBlank(ratingId)) {
            return null;
        }
        return mizarLoader.loadRatingById(ratingId);
    }

    //------------------------------------------------------------------------------
    //-------------------------     RESERVE RECORD    ------------------------------
    //------------------------------------------------------------------------------
    public List<MizarReserveRecord> loadShopReserveByParentId(Long parentId, String shopId) {
        if (parentId == null || StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        return mizarLoader.loadShopReserveByParentId(parentId, shopId);
    }

    public List<MizarReserveRecord> loadShopReserveByParentId(Long parentId) {
        if (parentId == null) {
            return Collections.emptyList();
        }
        return mizarLoader.loadShopReserveByParentId(parentId);
    }

    public List<MizarReserveRecord> loadShopReserveByMobile(String mobile, String shopId) {
        if (StringUtils.isBlank(shopId) || StringUtils.isBlank(mobile)) {
            return Collections.emptyList();
        }
        return mizarLoader.loadShopReserveByMobile(mobile, shopId);
    }

    public Map<String, List<MizarReserveRecord>> loadGoodsReserveByMobile(Collection<String> mobiles, String goodsId) {
        if (CollectionUtils.isEmpty(mobiles) || StringUtils.isBlank(goodsId)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadGoodsReserveByMobile(mobiles, goodsId);
    }
    public List<MizarReserveRecord> loadGoodsRecords(String goodsId) {
        if (StringUtils.isBlank(goodsId)) {
            return Collections.emptyList();
        }
        return mizarLoader.loadGoodsRecords(goodsId);
    }

    public Map<String, List<MizarReserveRecord>> loadShopReservations(Collection<String> shopIds) {
        if (CollectionUtils.isEmpty(shopIds)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadShopReservations(shopIds);
    }

    public Map<Long, MizarReserveRecord> loadReservations(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return mizarLoader.loadReservations(ids);
    }

    public List<MizarReserveRecord> loadReservationsByPeriod(Date start, Date end) {
        if (start == null || end == null) {
            return Collections.emptyList();
        }
        return mizarLoader.loadReservationsByPeriod(start, end);
    }

    //------------------------------------------------------------------------------
    //-------------------------          OTHERS       ------------------------------
    //------------------------------------------------------------------------------
    public List<TradeAreaMapper> loadAllTradeArea(Integer cityCode) {
        return mizarLoader.loadAllTradeArea(cityCode);
    }

    public List<Map<String, Object>> loadAllCategory() {
        return mizarLoader.loadAllCategory();
    }

    public List<MizarCategory> loadAllMizarCategory() {
        return mizarLoader.loadAllMizarCategory();
    }

    public boolean likedShop(String shopId, Integer activityId, Long userId) {
        return mizarLoader.likedShop(shopId, activityId, userId);
    }

    public List<Map<String, Object>> loadShopRankListByActivityId(Integer activityId) {
        return mizarLoader.loadShopRankListByActivityId(activityId);
    }

    public Map<String, List<Map<String, Object>>> loadNearRankListBySchoolId(Long schoolId, Integer activityId) {
        return mizarLoader.loadNearRankListBySchoolId(schoolId, activityId);
    }

    public Map<String, Object> loadShopReserveByShopIdAndSchoolId(Long schoolId, String shopId) {
        return mizarLoader.loadShopReserveByShopIdAndSchoolId(schoolId, shopId);
    }

    public List<MizarShopMapper> loadRecommendShop(String shopId) {
        return mizarLoader.loadRecommendShop(shopId);
    }


    public long loadShopLikeCount(String shopId, Integer activityId) {
        if (StringUtils.isBlank(shopId) || activityId == null) {
            return 0;
        }
        return mizarLoader.loadShopLikeCount(shopId, activityId);
    }

    public Page<MizarCourse> loadPageCourseByParams(Pageable pageable, String title, String status, String category) {
        return mizarLoader.loadPageByParams(pageable, title, status, category);
    }

    public MizarCourse loadMizarCourseById(String courseId) {
        return mizarLoader.loadMizarCourseById(courseId);
    }

    public Map<Integer, List<MizarCourseTarget>> loadCourseTargetsGroupByType(String courseId) {
        if (StringUtils.isBlank(courseId)) {
            return Collections.emptyMap();
        }
        List<MizarCourseTarget> list = loadCourseTargetsByCourseId(courseId);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(MizarCourseTarget::getTargetType, Collectors.toList()));
    }

    public List<MizarCourseTarget> loadCourseTargetsByCourseId(String courseId) {
        return mizarLoader.loadCourseTargetsByCourseId(courseId);
    }

    public Map<String, List<MizarCourseMapper>> loadMizarCourseIndexMapByParentId(Long parentId) {
        return mizarLoader.loadMizarCourseIndexMapByParentId(parentId);
    }

    public Page<MizarCourseMapper> loadUserMizarCoursePageByCategory(MizarCourseCategory category, String tag, Long parentId, Pageable pageable) {
        if (category == null || parentId == null) {
            return new PageImpl<>(new ArrayList<>());
        }
        // 直接在此处兼容微课堂，接口就不用再换了
        if (category == MizarCourseCategory.MICRO_COURSE_NORMAL || category == MizarCourseCategory.MICRO_COURSE_OPENING) {
            return microCourseLoaderClient.loadMicroCoursePage(parentId, category, tag, pageable);
        }
        return mizarLoader.loadUserMizarCoursePageByCategory(category, tag, parentId, pageable);
    }

    public Map<String, List<Map<String, Object>>> loadPpgIndexList(String longitude, String latitude) {
        return mizarLoader.loadPpgIndexList(longitude, latitude);
    }

    public Map<String, Object> loadBrandNearShop(String brandId, String longitude, String latitude) {
        return mizarLoader.loadBrandNearShop(brandId, longitude, latitude);
    }

    public List<MizarShopMapper> loadBrandShopsByPosition(String brandId, String longitude, String latitude) {
        return mizarLoader.loadBrandShopsByPosition(brandId, longitude, latitude);
    }

}
