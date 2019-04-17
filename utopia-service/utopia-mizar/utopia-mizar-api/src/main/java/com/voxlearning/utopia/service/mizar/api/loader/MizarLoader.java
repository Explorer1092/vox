package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.constants.MizarCourseCategory;
import com.voxlearning.utopia.service.mizar.api.constants.MizarGoodsStatus;
import com.voxlearning.utopia.service.mizar.api.entity.shop.*;
import com.voxlearning.utopia.service.mizar.api.mapper.LoadMizarShopContext;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarCourseMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarShopMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.TradeAreaMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer Yang on 2016/8/15.
 */
@ServiceVersion(version = "20161117")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarLoader extends IPingable {

    //------------------------------------------------------------------------------
    //-------------------------          BRAND        ------------------------------
    //------------------------------------------------------------------------------

    @CacheMethod(
            type = MizarBrand.class,
            writeCache = false
    )
    MizarBrand loadBrandById(@CacheParameter String brandId);


    Page<MizarBrand> loadBrandByPage(Pageable page, String brandName);

    @CacheMethod(
            type = MizarBrand.class,
            writeCache = false
    )
    Map<String, MizarBrand> loadBrandByIds(@CacheParameter(multiple = true) Collection<String> brandIds);

    //------------------------------------------------------------------------------
    //-------------------------           SHOP        ------------------------------
    //------------------------------------------------------------------------------

    @CacheMethod(
            type = MizarShop.class,
            writeCache = false
    )
    MizarShop loadShopById(@CacheParameter String shopId);

    @CacheMethod(
            type = MizarShop.class,
            writeCache = false
    )
    Map<String, MizarShop> loadShopByIds(@CacheParameter(multiple = true) Collection<String> shopIds);

    Map<String, Object> loadShopShowMap(String shopId);

    Page<MizarShop> loadShopByPage(Pageable page, String shopName);

    Page<MizarShop> loadAllShopByPage(Pageable page, String shopName);

    Page<MizarShop> loadAllShopByPage(Pageable page, String shopName, Boolean vip, Boolean cooperator);

    @CacheMethod(
            type = MizarShop.class,
            writeCache = false
    )
    List<MizarShop> loadShopByBrand(@CacheParameter(value = "BID") String brandId);

    PageImpl<MizarShopMapper> loadShopPageByParam(LoadMizarShopContext context);

    Set<String> loadAllShopIds();

    //------------------------------------------------------------------------------
    //-------------------------          GOODS        ------------------------------
    //------------------------------------------------------------------------------

    @CacheMethod(
            type = MizarShopGoods.class,
            writeCache = false
    )
    MizarShopGoods loadShopGoodsById(@CacheParameter String goodsId);

    @CacheMethod(
            type = MizarShopGoods.class,
            writeCache = false
    )
    Map<String, MizarShopGoods> loadShopGoodsByIds(@CacheParameter(multiple = true) Collection<String> goodsIds);

    Page<MizarShopGoods> loadShopGoodsByPage(Pageable page, String goodsName);

    @CacheMethod(
            type = MizarShopGoods.class,
            writeCache = false
    )
    List<MizarShopGoods> loadShopGoodsByShop(@CacheParameter(value = "shopId") String shopId);

    @CacheMethod(
            type = MizarShopGoods.class,
            writeCache = false
    )
    Map<String, List<MizarShopGoods>> loadShopGoodsByShop(@CacheParameter(value = "shopId", multiple = true) Collection<String> shopIds);

    Page<MizarShopGoods> loadPageByGoodsType(Pageable page, MizarGoodsStatus status, Collection<String> types);
    //------------------------------------------------------------------------------
    //-------------------------         RATING        ------------------------------
    //------------------------------------------------------------------------------

    @CacheMethod(
            type = MizarRating.class,
            writeCache = false
    )
    List<MizarRating> loadShopRatings(@CacheParameter(value = "shopId") String shopId);

    @CacheMethod(
            type = MizarRating.class,
            writeCache = false
    )
    Map<String, List<MizarRating>> loadShopRatings(@CacheParameter(value = "shopId", multiple = true) Collection<String> shopId);

    PageImpl<Map<String, Object>> loadCollectionRatings(Integer activityId, Long time, Pageable pageable);

    PageImpl<MizarRating> loadRatingPage(String shopId, Pageable pageable);

    @CacheMethod(
            type = MizarRating.class,
            writeCache = false
    )
    MizarRating loadRatingById(@CacheParameter String ratingId);

    //------------------------------------------------------------------------------
    //-------------------------     RESERVE RECORD    ------------------------------
    //------------------------------------------------------------------------------

    @CacheMethod(
            type = MizarReserveRecord.class,
            writeCache = false
    )
    List<MizarReserveRecord> loadShopReserveByParentId(@CacheParameter(value = "PID") Long parentId, @CacheParameter(value = "SID") String shopId);

    @CacheMethod(
            type = MizarReserveRecord.class,
            writeCache = false
    )
    List<MizarReserveRecord> loadShopReserveByParentId(@CacheParameter(value = "parentId") Long parentId);

    @CacheMethod(
            type = MizarReserveRecord.class,
            writeCache = false
    )
    List<MizarReserveRecord> loadShopReserveByMobile(@CacheParameter(value = "M") String mobile, @CacheParameter(value = "SID") String shopId);

    Map<String, List<MizarReserveRecord>> loadGoodsReserveByMobile(Collection<String> mobile, String goodsId);

    /**
     * 查询改商品（课程）的所有报名记录
     * @param goodsId
     * @return
     */
    List<MizarReserveRecord> loadGoodsRecords(String goodsId);

    @CacheMethod(
            type = MizarReserveRecord.class,
            writeCache = false
    )
    Map<String, List<MizarReserveRecord>> loadShopReservations(@CacheParameter(value = "shopId", multiple = true) Collection<String> shopIds);

    @CacheMethod(
            type = MizarReserveRecord.class,
            writeCache = false
    )
    Map<Long, MizarReserveRecord> loadReservations(@CacheParameter Collection<Long> ids);

    List<MizarReserveRecord> loadReservationsByPeriod(Date start, Date end);

    //------------------------------------------------------------------------------
    //-------------------------          OTHERS       ------------------------------
    //------------------------------------------------------------------------------
    List<TradeAreaMapper> loadAllTradeArea(Integer cityCode);

    List<Map<String, Object>> loadAllCategory();

    List<MizarCategory> loadAllMizarCategory();

    boolean likedShop(String shopId, Integer activityId, Long userId);

    @CacheMethod(
            type = MizarLoader.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 7200)
    )
    List<Map<String, Object>> loadShopRankListByActivityId(@CacheParameter("AID") Integer activityId);

    @CacheMethod(
            type = MizarLoader.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 7200)
    )
    Map<String, List<Map<String, Object>>> loadNearRankListBySchoolId(@CacheParameter("SID") Long schoolId, @CacheParameter("AID") Integer activityId);

    @CacheMethod(
            type = MizarLoader.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
    )
    Map<String, Object> loadShopReserveByShopIdAndSchoolId(@CacheParameter("R_SchoolId") Long schoolId, @CacheParameter("R_ShopId") String shopId);

    @CacheMethod(
            type = MizarLoader.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 7200)
    )
    List<MizarShopMapper> loadRecommendShop(@CacheParameter("RECOMMEND_SHOP") String shopId);

    @CacheMethod(
            type = MizarLoader.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 7200),
            key = "SHOP_LIKE_COUNT"
    )
    long loadShopLikeCount(@CacheParameter("shopId") String shopId, Integer activityId);

    Page<MizarCourse> loadPageByParams(Pageable pageable, String title, String status, String category);

    @CacheMethod(
            type = MizarCourse.class,
            writeCache = false
    )
    MizarCourse loadMizarCourseById(@CacheParameter String courseId);

    @CacheMethod(
            type = MizarCourseTarget.class,
            writeCache = false
    )
    List<MizarCourseTarget> loadCourseTargetsByCourseId(@CacheParameter(value = "courseId") String courseId);

    @CacheMethod(
            type = MizarLoader.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 500),
            key = "COURSE_INDEX"
    )
    Map<String, List<MizarCourseMapper>> loadMizarCourseIndexMapByParentId(@CacheParameter("UID") Long parentId);

    PageImpl<MizarCourseMapper> loadUserMizarCoursePageByCategory(MizarCourseCategory category, String tag, Long parentId, Pageable pageable);

    Map<String, List<Map<String, Object>>> loadPpgIndexList(String longitude, String latitude);

    Map<String, Object> loadBrandNearShop(String brandId, String longitude, String latitude);

    List<MizarShopMapper> loadBrandShopsByPosition(String brandId, String longitude, String latitude);

    Set<String> loadAllBrandIds();

    Set<String> loadAllShopGoodsIds();

    Set<String> loadAllRatingIds();

}
