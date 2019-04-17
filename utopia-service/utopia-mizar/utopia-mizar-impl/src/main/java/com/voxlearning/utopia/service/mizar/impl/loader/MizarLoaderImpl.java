/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.HashedMap;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.GEOUtils;
import com.voxlearning.utopia.service.config.client.BusinessActivityManagerClient;
import com.voxlearning.utopia.service.mizar.api.constants.*;
import com.voxlearning.utopia.service.mizar.api.entity.shop.*;
import com.voxlearning.utopia.service.mizar.api.loader.MizarLoader;
import com.voxlearning.utopia.service.mizar.api.mapper.LoadMizarShopContext;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarCourseMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.MizarShopMapper;
import com.voxlearning.utopia.service.mizar.api.mapper.TradeAreaMapper;
import com.voxlearning.utopia.service.mizar.impl.dao.shop.*;
import com.voxlearning.utopia.service.mizar.impl.service.AsyncMizarCacheServiceImpl;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.AsyncUserCacheServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * Created by Summer Yang on 2016/8/15.
 */
@Named
@Service(interfaceClass = MizarLoader.class)
@ExposeService(interfaceClass = MizarLoader.class)
@Slf4j
public class MizarLoaderImpl extends SpringContainerSupport implements MizarLoader {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncUserCacheServiceClient asyncUserCacheServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private BusinessActivityManagerClient businessActivityManagerClient;
    @Inject private AsyncMizarCacheServiceImpl asyncMizarCacheService;
    @Inject private MizarShopDao mizarShopDao;
    @Inject private MizarRatingDao mizarRatingDao;
    @Inject private MizarShopGoodsDao mizarShopGoodsDao;
    @Inject private MizarBrandDao mizarBrandDao;
    @Inject private MizarReserveRecordPersistence mizarReserveRecordPersistence;
    @Inject private MizarCategoryDao mizarCategoryDao;
    @Inject private MizarTradeAreaDao mizarTradeAreaDao;
    @Inject private MizarShopLikeDao mizarShopLikeDao;
    @Inject private MizarShopRatingDao mizarShopRatingDao;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private MizarCourseDao mizarCourseDao;
    @Inject private MizarCourseTargetDao mizarCourseTargetDao;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private SchoolExtServiceClient schoolExtServiceClient;

    //------------------------------------------------------------------------------
    //-------------------------          BRAND        ------------------------------
    //------------------------------------------------------------------------------

    @Override
    public MizarBrand loadBrandById(String brandId) {
        return mizarBrandDao.load(brandId);
    }

    @Override
    public Page<MizarBrand> loadBrandByPage(Pageable page, String brandName) {
        return mizarBrandDao.loadByPage(page, brandName);
    }

    @Override
    public Map<String, MizarBrand> loadBrandByIds(Collection<String> brandIds) {
        return mizarBrandDao.loads(brandIds);
    }

    //------------------------------------------------------------------------------
    //-------------------------           SHOP        ------------------------------
    //------------------------------------------------------------------------------

    @Override
    public MizarShop loadShopById(String shopId) {
        return mizarShopDao.load(shopId);
    }

    @Override
    public Map<String, MizarShop> loadShopByIds(Collection<String> shopIds) {
        return mizarShopDao.loads(shopIds);
    }

    @Override
    public Map<String, Object> loadShopShowMap(String shopId) {
        MizarShop shop = mizarShopDao.load(shopId);
        Map<String, Object> shopMap = new HashMap<>();
        shopMap.put("name", shop.getFullName());
        shopMap.put("address", shop.getAddress());
        shopMap.put("phone", shop.getContactPhone());
        shopMap.put("area", shop.getTradeArea()); // 商圈
        shopMap.put("secondCategory", shop.getSecondCategory());
        shopMap.put("latitude", shop.getLatitude());
        shopMap.put("longitude", shop.getLongitude());
        shopMap.put("brandId", shop.getBrandId());
        shopMap.put("isVip", shop.getVip());
        shopMap.put("shopId", shopId);
        shopMap.put("ratingCount", shop.getRatingCount());
        shopMap.put("ratingStar", shop.getRatingStar());
        shopMap.put("introduction", shop.getIntroduction()); // 机构介绍
        // 直接读点赞数量
        long likeCount = loadShopLikeCount(shopId, MizarRatingActivity.ADD_MIZAR_SHOP_LIKE.getId());
        shopMap.put("likeCount", likeCount);
        List<MizarRating> ratingList = mizarRatingDao.loadByShopId(shopId);
        if (CollectionUtils.isNotEmpty(ratingList)) {
            // 评价详情前5条
            List<Map<String, Object>> ratingMapList = new ArrayList<>();
            for (MizarRating rating : ratingList) {
                if (ratingMapList.size() < 5) {
                    Map<String, Object> ratingMap = MizarRating.toRatingMap(rating);
                    ratingMapList.add(ratingMap);
                } else {
                    break;
                }
            }
            shopMap.put("ratingMapList", ratingMapList);
        }
        // 图片取前7张
        if (CollectionUtils.isNotEmpty(shop.getPhoto())) {
            List<String> photos = shop.getPhoto();
            shopMap.put("picCount", photos.size());
            shopMap.put("firstPic", photos.get(0));
            if (shop.getPhoto().size() > 7) {
                photos = shop.getPhoto().subList(0, 7);
            }
            shopMap.put("photos", photos);
        }
        // 获取品牌数据
        MizarBrand brand = mizarBrandDao.load(shop.getBrandId());
        if (brand != null) {
            shopMap.put("brandDesc", brand.getIntroduction());
            if (CollectionUtils.isNotEmpty(shop.getFaculty())) {
                shopMap.put("faculty", shop.getFaculty()); // 读取机构自己的师资力量
            } else {
                shopMap.put("faculty", brand.getFaculty()); // 读取品牌的师资力量
            }
            shopMap.put("certificationPhotos", brand.getCertificationPhotos()); // 获奖证书照片
            shopMap.put("certificationName", brand.getCertificationName()); // 获奖描述
        } else {
            shopMap.put("faculty", shop.getFaculty()); // 读取机构自己的师资力量
        }

        shopMap.put("welcomeGift", shop.getWelcomeGift());// 到店礼

        // 全部课程
        List<MizarShopGoods> shopGoodsList = mizarShopGoodsDao.loadByShopId(shopId);
        if (CollectionUtils.isNotEmpty(shopGoodsList)) {
            shopGoodsList = shopGoodsList.stream()
                    .filter(t -> MizarGoodsStatus.ONLINE.equals(t.getStatus()))
                    .collect(toList());
        }
        if (CollectionUtils.isNotEmpty(shopGoodsList)) {
            List<Map<String, Object>> goodsList = new ArrayList<>();
            // 推荐课程
            List<Map<String, Object>> recommendGoodsList = new ArrayList<>();
            for (MizarShopGoods goods : shopGoodsList) {
                Map<String, Object> goodsMap = new HashMap<>();
                if (CollectionUtils.isNotEmpty(goods.getBannerPhoto())) {
                    goodsMap.put("goodsPic", goods.getBannerPhoto().get(0));
                }
                goodsMap.put("goodsName", goods.getGoodsName());
                goodsMap.put("goodsTag", goods.getTags());
                goodsMap.put("goodsPrice", goods.getPrice());
                goodsMap.put("originalPrice", goods.getOriginalPrice());
                goodsMap.put("goodsId", goods.getId());
                goodsMap.put("redirectUrl", goods.getRedirectUrl());
                goodsList.add(goodsMap);
                if (goods.getRecommended() != null && goods.getRecommended()) {
                    recommendGoodsList.add(goodsMap);
                }
            }
            shopMap.put("goodsList", goodsList);
            shopMap.put("recommendGoodsList", recommendGoodsList);
        }
        return shopMap;
    }

    @Override
    public Page<MizarShop> loadShopByPage(Pageable page, String shopName) {
        return mizarShopDao.loadByPage(page, shopName);
    }

    @Override
    public Page<MizarShop> loadAllShopByPage(Pageable page, String shopName) {
        return mizarShopDao.loadAllByPage(page, shopName);
    }

    @Override
    public Page<MizarShop> loadAllShopByPage(Pageable page, String shopName, Boolean vip, Boolean cooperator) {
        return mizarShopDao.loadAllByPage(page, shopName, vip, cooperator);
    }


    @Override
    public List<MizarShop> loadShopByBrand(String brandId) {
        return mizarShopDao.loadByBrandId(brandId);
    }

    @Override
    public Set<String> loadAllShopIds() {
        return mizarShopDao.loadAllShopIds();
    }

    @Override
    public Set<String> loadAllBrandIds() {
        return mizarBrandDao.loadAllBrandIds();
    }

    @Override
    public Set<String> loadAllShopGoodsIds() {
        return mizarShopGoodsDao.loadAllShopGoodsIds();
    }

    @Override
    public Set<String> loadAllRatingIds() {
        return mizarRatingDao.loadAllRatingIds();
    }

    //------------------------------------------------------------------------------
    //-------------------------          GOODS        ------------------------------
    //------------------------------------------------------------------------------

    @Override
    public MizarShopGoods loadShopGoodsById(String goodsId) {
        return mizarShopGoodsDao.load(goodsId);
    }

    @Override
    public Map<String, MizarShopGoods> loadShopGoodsByIds(Collection<String> goodsIds) {
        return mizarShopGoodsDao.loads(goodsIds);
    }

    @Override
    public Page<MizarShopGoods> loadShopGoodsByPage(Pageable page, String goodsName) {
        return mizarShopGoodsDao.loadByPage(page, goodsName);
    }

    @Override
    public List<MizarShopGoods> loadShopGoodsByShop(String shopId) {
        return mizarShopGoodsDao.loadByShopId(shopId);
    }

    @Override
    public Map<String, List<MizarShopGoods>> loadShopGoodsByShop(Collection<String> shopIds) {
        return mizarShopGoodsDao.loadByShopIds(shopIds);
    }

    @Override
    public Page<MizarShopGoods> loadPageByGoodsType(Pageable page, MizarGoodsStatus status, Collection<String> types) {
        return mizarShopGoodsDao.loadPageByGoodsType(types, page, status);
    }


    //------------------------------------------------------------------------------
    //-------------------------         RATING        ------------------------------
    //------------------------------------------------------------------------------

    @Override
    public MizarRating loadRatingById(@CacheParameter String ratingId) {
        return mizarRatingDao.load(ratingId);
    }

    @Override
    public List<MizarRating> loadShopRatings(String shopId) {
        return mizarRatingDao.loadByShopId(shopId);
    }

    @Override
    public Map<String, List<MizarRating>> loadShopRatings(Collection<String> shopIds) {
        return mizarRatingDao.loadByShopIds(shopIds);
    }

    @Override
    public PageImpl<MizarRating> loadRatingPage(String shopId, Pageable pageable) {
        List<MizarRating> ratingList = mizarRatingDao.loadByShopId(shopId);
        if (CollectionUtils.isEmpty(ratingList)) {
            return new PageImpl<>(Collections.emptyList());
        }
        long total = ratingList.size();
        if (pageable.getPageNumber() * pageable.getPageSize() > total) {
            // 请正确填写页码
            return new PageImpl<>(Collections.emptyList());
        }
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min((int) total, ((pageable.getPageNumber() + 1) * pageable.getPageSize()));
        ratingList = new LinkedList<>(ratingList.subList(start, end));
        return new PageImpl<>(
                ratingList,
                pageable,
                total);
    }


    @Override
    public PageImpl<Map<String, Object>> loadCollectionRatings(Integer activityId, Long time, Pageable pageable) {
        PageImpl<MizarRating> ratingPage = mizarRatingDao.loadPageByActivityIdAndTime(activityId, time, pageable);
        if (ratingPage == null) {
            return new PageImpl<>(new ArrayList<>());
        }
        // 拼接shop
        List<MizarRating> ratingList = ratingPage.getContent();
        if (CollectionUtils.isEmpty(ratingList)) {
            return new PageImpl<>(new ArrayList<>());
        }
        List<Map<String, Object>> contentList = new ArrayList<>();
        Set<String> shopIds = ratingList.stream().map(MizarRating::getShopId).collect(toSet());
        Map<String, MizarShop> shopMap = mizarShopDao.loads(shopIds);
        for (MizarRating rating : ratingList) {
            Map<String, Object> map = new HashMap<>();
            map.put("userName", rating.getUserName());
            map.put("userAvatar", rating.getUserAvatar());
            map.put("ratingStar", rating.getRating());
            map.put("ratingContent", rating.getRatingContent());
            map.put("photo", rating.getPhoto());
            map.put("ratingTime", DateUtils.dateToString(new Date(rating.getRatingTime()), "yyyy-MM-dd"));
            MizarShop shop = shopMap.get(rating.getShopId());
            if (shop != null) {
                map.put("shopName", shop.getFullName());
                if (CollectionUtils.isNotEmpty(shop.getPhoto())) {
                    map.put("shopLogo", shop.getPhoto().get(0));
                }
                map.put("shopStar", shop.getRatingStar());
                map.put("shopId", shop.getId());
            }
            contentList.add(map);
        }

        return new PageImpl<>(contentList, pageable, ratingPage.getTotalElements());
    }

    //------------------------------------------------------------------------------
    //-------------------------     RESERVE RECORD    ------------------------------
    //------------------------------------------------------------------------------

    @Override
    public List<MizarReserveRecord> loadShopReserveByParentId(Long parentId, String shopId) {
        return mizarReserveRecordPersistence.loadByParentIdAndShopId(parentId, shopId);
    }

    @Override
    public List<MizarReserveRecord> loadShopReserveByParentId(Long parentId) {
        return mizarReserveRecordPersistence.loadByParentId(parentId);
    }

    @Override
    public List<MizarReserveRecord> loadShopReserveByMobile(String mobile, String shopId) {
        return mizarReserveRecordPersistence.loadByMobileAndShopId(mobile, shopId);
    }

    @Override
    public Map<String, List<MizarReserveRecord>> loadGoodsReserveByMobile(Collection<String> mobile, String goodsId) {
        return mizarReserveRecordPersistence.loadByMobileAndGoodsId(mobile, goodsId);
    }

    @Override
    public List<MizarReserveRecord> loadGoodsRecords(String goodsId) {
        return mizarReserveRecordPersistence.loadGoodsRecords(goodsId);
    }

    @Override
    public Map<String, List<MizarReserveRecord>> loadShopReservations(Collection<String> shopIds) {
        return mizarReserveRecordPersistence.loadByShopIds(shopIds);
    }

    @Override
    public Map<Long, MizarReserveRecord> loadReservations(Collection<Long> ids) {
        return mizarReserveRecordPersistence.loads(ids);
    }

    @Override
    public List<MizarReserveRecord> loadReservationsByPeriod(Date start, Date end) {
        return mizarReserveRecordPersistence.loadByPeriod(start, end);
    }

    //------------------------------------------------------------------------------
    //-------------------------          OTHERS       ------------------------------
    //------------------------------------------------------------------------------
    @Override
    public List<TradeAreaMapper> loadAllTradeArea(Integer cityCode) {
        // 默认取北京的
        if (cityCode == null || cityCode == 0) {
            cityCode = 110100;
        }
        // 获取城市区域
        List<ExRegion> regions = raikouSystem.getRegionBuffer().loadChildRegions(cityCode);
        List<Integer> regionCodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(regions)) {
            regionCodes = regions.stream().map(ExRegion::getId).collect(toList());
        }
        List<MizarTradeArea> allList = mizarTradeAreaDao.findAll();
        List<MizarTradeArea> tradeAreas = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(allList)) {
            final List<Integer> finalRegionCodes = regionCodes;
            tradeAreas = allList.stream().filter(a -> finalRegionCodes.contains(a.getRegionCode())).collect(toList());
        }
        Map<Integer, List<MizarTradeArea>> areaMap = tradeAreas.stream().collect(groupingBy(MizarTradeArea::getRegionCode));
        List<TradeAreaMapper> mappers = new ArrayList<>();
        for (Map.Entry<Integer, List<MizarTradeArea>> entry : areaMap.entrySet()) {
            List<MizarTradeArea> areaList = entry.getValue();
            if (CollectionUtils.isNotEmpty(areaList)) {
                TradeAreaMapper mapper = new TradeAreaMapper();
                mapper.setRegionCode(entry.getKey());
                mapper.setRegionName(MiscUtils.firstElement(areaList).getRegionName());
                List<Map<String, Object>> tradeAreaList = new ArrayList<>();
                for (MizarTradeArea area : areaList) {
                    Map<String, Object> tradeMap = new HashedMap<>();
                    tradeMap.put("regionCode", area.getRegionCode());
                    tradeMap.put("tradeArea", area.getTradeArea());
                    tradeAreaList.add(tradeMap);
                }
                mapper.setTradeAreaList(tradeAreaList);
                mappers.add(mapper);
            }
        }
        return mappers;
    }

    @Override
    public List<Map<String, Object>> loadAllCategory() {
        List<MizarCategory> categories = mizarCategoryDao.findAll();
        Map<String, List<MizarCategory>> categoryMap = categories.stream().collect(groupingBy(MizarCategory::getFirstCategory));
        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Map.Entry<String, List<MizarCategory>> entry : categoryMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("firstCategory", entry.getKey());
            map.put("secondCategoryList", entry.getValue());
            categoryList.add(map);
        }
        return categoryList;
    }

    @Override
    public List<MizarCategory> loadAllMizarCategory() {
        return mizarCategoryDao.findAll().stream()
                .sorted((c1, c2) -> c1.getFirstCategory().compareTo(c2.getFirstCategory()))
                .collect(toList());
    }

    @Override
    public boolean likedShop(String shopId, Integer activityId, Long userId) {
        List<MizarShopLike> likeList = mizarShopLikeDao.loadByUserId(userId);
        if (CollectionUtils.isEmpty(likeList)) {
            return false;
        }
        MizarShopLike like = likeList.stream().filter(m -> Objects.equals(m.getActivityId(), activityId) &&
                StringUtils.equals(m.getShopId(), shopId)).findAny().orElse(null);
        return like != null;
    }

    @Override
    public List<Map<String, Object>> loadShopRankListByActivityId(Integer activityId) {
        List<MizarShopLike> allLikes = mizarShopLikeDao.loadByActivityId(activityId);
        if (CollectionUtils.isEmpty(allLikes)) {
            return Collections.emptyList();
        }
        Map<String, List<MizarShopLike>> likeMap = allLikes.stream().collect(groupingBy(MizarShopLike::getShopId));
        List<Map<String, Object>> rankList = new ArrayList<>();
        for (Map.Entry<String, List<MizarShopLike>> entry : likeMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("shopId", entry.getKey());
            map.put("likeCount", entry.getValue().size());
            map.put("shopName", entry.getValue().get(0).getShopName());
            map.put("firstCategory", entry.getValue().get(0).getFirstCategory());
            rankList.add(map);
        }
        // 排序
        rankList = rankList.stream().sorted((o1, o2) -> {
            int v1 = SafeConverter.toInt(o1.get("likeCount"));
            int v2 = SafeConverter.toInt(o2.get("likeCount"));
            return Integer.compare(v2, v1);
        }).collect(toList());
        int currentRank = 1;
        int currentCount = ConversionUtils.toInt(rankList.get(0).get("likeCount"));
        for (int i = 0; i < rankList.size(); i++) {
            Map<String, Object> element = rankList.get(i);
            int count = ConversionUtils.toInt(element.get("likeCount"));
            if (count >= currentCount) {
                element.put("rank", currentRank);
            } else {
                currentRank = i + 1;
                element.put("rank", currentRank);
                currentCount = count;
            }
        }
        return rankList;
    }

    @Override
    public Map<String, List<Map<String, Object>>> loadNearRankListBySchoolId(Long schoolId, Integer activityId) {
        // 获取排行榜
        List<Map<String, Object>> rankList = loadShopRankListByActivityId(activityId);
        if (CollectionUtils.isEmpty(rankList)) {
            return Collections.emptyMap();
        }
        // 获取机构信息
        Set<String> shopIds = rankList.stream().map(m -> SafeConverter.toString(m.get("shopId"))).collect(toSet());
        Map<String, MizarShop> shopMap = mizarShopDao.loads(shopIds);

        // 取5KM里内的机构
        SchoolExtInfo info = schoolExtServiceClient.getSchoolExtService()
                .loadSchoolExtInfo(schoolId)
                .getUninterruptibly();
        List<MizarShopMapper> mapperList = shopToMappers(shopMap.values(), SafeConverter.toDouble(info.getLongitude()), SafeConverter.toDouble(info.getLatitude()));
        mapperList = mapperList.stream().filter(m -> m.getDistance() != null && m.getDistance() <= 5).collect(toList());
        if (CollectionUtils.isEmpty(mapperList)) {
            return Collections.emptyMap();
        }
        // 按排名 取各类前三
        Map<String, List<MizarShopMapper>> categoryMap = mapperList.stream().collect(groupingBy(MizarShopMapper::getFirstCategory));
        Map<String, List<Map<String, Object>>> dataMap = new HashMap<>();
        for (Map.Entry<String, List<MizarShopMapper>> entry : categoryMap.entrySet()) {
            List<MizarShopMapper> mappers = entry.getValue();
            List<String> ids = mappers.stream().map(MizarShopMapper::getId).collect(toList());

            List<Map<String, Object>> ranks = rankList.stream().filter(m -> ids.contains(SafeConverter.toString(m.get("shopId")))).collect(toList());
            if (CollectionUtils.isNotEmpty(ranks) && ranks.size() > 3) {
                ranks = ranks.subList(0, 3);
            }
            Map<String, MizarShopMapper> mapperMap = mappers.stream().collect(toMap(MizarShopMapper::getId, t -> t));
            for (Map<String, Object> map : ranks) {
                MizarShopMapper mapper = mapperMap.get(SafeConverter.toString(map.get("shopId")));
                if (mapper != null) {
                    map.put("shopImg", mapper.getPhoto());
                    map.put("ratingStar", mapper.getRatingStar());
                    map.put("ratingCount", mapper.getRatingCount());
                    map.put("tradeArea", mapper.getTradeArea());
                    map.put("distance", mapper.getDistance());
                }
            }
            dataMap.put(entry.getKey(), ranks);
        }
        return dataMap;
    }

    @Override
    public Map<String, Object> loadShopReserveByShopIdAndSchoolId(Long schoolId, String shopId) {
        // 最近90天
        List<MizarReserveRecord> recordList = mizarReserveRecordPersistence.loadByShopId(shopId);
        if (CollectionUtils.isEmpty(recordList)) {
            return Collections.emptyMap();
        }
        List<Long> schoolIds = new ArrayList<>();
        List<Long> studentIds = new ArrayList<>();
        // 过滤同校50条数据
        List<MizarReserveRecord> sameRecordList50 = recordList.stream().filter(r -> Objects.equals(r.getSchoolId(), schoolId)).collect(toList());
        if (CollectionUtils.isNotEmpty(sameRecordList50)) {
            if (sameRecordList50.size() >= 50) {
                sameRecordList50 = sameRecordList50.subList(0, 50);
            }
            schoolIds.addAll(sameRecordList50.stream().map(MizarReserveRecord::getSchoolId).collect(toList()));
            studentIds.addAll(sameRecordList50.stream().map(MizarReserveRecord::getStudentId).collect(toList()));
        }
        // 过滤异校50条数据
        List<MizarReserveRecord> otherRecordList50 = recordList.stream().filter(r -> !Objects.equals(r.getSchoolId(), schoolId)).collect(toList());
        if (CollectionUtils.isNotEmpty(otherRecordList50)) {
            if (otherRecordList50.size() >= 50) {
                otherRecordList50 = otherRecordList50.subList(0, 50);
            }
            schoolIds.addAll(otherRecordList50.stream().map(MizarReserveRecord::getSchoolId).collect(toList()));
            studentIds.addAll(otherRecordList50.stream().map(MizarReserveRecord::getStudentId).collect(toList()));
        }

        List<Map<String, Object>> sameSchoolReserveList = new ArrayList<>();
        List<Map<String, Object>> otherSchoolReserveList = new ArrayList<>();
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();
        Map<Long, Clazz> clazzMap = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazzs(studentIds);
        for (MizarReserveRecord record : sameRecordList50) {
            Map<String, Object> map = new HashMap<>();
            String studentName = StringUtils.isBlank(record.getStudentName()) ? "同学" : StringUtils.substring(record.getStudentName(), 0, 1) + "同学";
            map.put("studentName", studentName);
            map.put("studentId", record.getStudentId());
            map.put("schoolName", schoolMap.get(record.getSchoolId()) == null ? "" : schoolMap.get(record.getSchoolId()).getShortName());
            map.put("clazzName", clazzMap.get(record.getStudentId()) == null ? "" : clazzMap.get(record.getStudentId()).getClazzLevel().getDescription());
            map.put("createDatetime", DateUtils.dateToString(record.getCreateDatetime(), "yyyy.MM.dd"));
            map.put("schoolId", record.getSchoolId());
            map.put("createTime", record.getCreateDatetime());
            sameSchoolReserveList.add(map);
        }
        for (MizarReserveRecord record : otherRecordList50) {
            Map<String, Object> map = new HashMap<>();
            String studentName = StringUtils.isBlank(record.getStudentName()) ? "同学" : StringUtils.substring(record.getStudentName(), 0, 1) + "同学";
            map.put("studentName", studentName);
            map.put("studentId", record.getStudentId());
            map.put("schoolName", schoolMap.get(record.getSchoolId()) == null ? "" : schoolMap.get(record.getSchoolId()).getShortName());
            map.put("clazzName", clazzMap.get(record.getStudentId()) == null ? "" : clazzMap.get(record.getStudentId()).getClazzLevel().getDescription());
            map.put("createDatetime", DateUtils.dateToString(record.getCreateDatetime(), "yyyy.MM.dd"));
            map.put("schoolId", record.getSchoolId());
            map.put("createTime", record.getCreateDatetime());
            otherSchoolReserveList.add(map);
        }
        // 根据时间倒序
        if (CollectionUtils.isNotEmpty(sameSchoolReserveList)) {
            sameSchoolReserveList.sort((o1, o2) -> {
                long t1 = o1.get("createTime") == null ? 0 : SafeConverter.toDate(o1.get("createTime")).getTime();
                long t2 = o2.get("createTime") == null ? 0 : SafeConverter.toDate(o2.get("createTime")).getTime();
                return Long.compare(t2, t1);
            });
        }
        if (CollectionUtils.isNotEmpty(otherSchoolReserveList)) {
            otherSchoolReserveList.sort((o1, o2) -> {
                long t1 = o1.get("createTime") == null ? 0 : SafeConverter.toDate(o1.get("createTime")).getTime();
                long t2 = o2.get("createTime") == null ? 0 : SafeConverter.toDate(o2.get("createTime")).getTime();
                return Long.compare(t2, t1);
            });
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("sameSchoolReserveList", sameSchoolReserveList);
        dataMap.put("otherSchoolReserveList", otherSchoolReserveList);
        return dataMap;
    }

    @Override
    public List<MizarShopMapper> loadRecommendShop(String shopId) {
        // 默认推荐同二级类目下同行政区域下其他VIP商家，根据距离排序，最多展示3家
        // 如果没有二级类目，则展示该一级类目下同行政区域下的商家
        MizarShop shop = mizarShopDao.load(shopId);
        if (shop == null || shop.getRegionCode() == null || shop.getRegionCode() == 0) {
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(shop.getFirstCategory()) && CollectionUtils.isEmpty(shop.getSecondCategory())) {
            return Collections.emptyList();
        }
        // 如果商家没有经纬度 则不推送
        if (shop.getLatitude() == null || shop.getLatitude() == 0 || shop.getLongitude() == null || shop.getLongitude() == 0) {
            return Collections.emptyList();
        }
        // 查询
        List<MizarShop> shopList = mizarShopDao.loadByParams(shop.getRegionCode(), shop.getSecondCategory(), shop.getFirstCategory(), true);
        shopList = shopList.stream().filter(s -> !StringUtils.equals(s.getId(), shopId)).collect(toList());
        if (CollectionUtils.isEmpty(shopList)) {
            return Collections.emptyList();
        }
        // 计算距离
        List<MizarShopMapper> mapperList = shopToMappers(shopList, shop.getLongitude(), shop.getLatitude());
        // 根据距离排序取前三家
        mapperList = orderShops(mapperList, "distance");
        if (mapperList.size() > 3) {
            mapperList = mapperList.subList(0, 3);
        }
        return mapperList;
    }

    @Override
    public long loadShopLikeCount(String shopId, Integer activityId) {
        return mizarShopLikeDao.loadCountByShopIdAndActivityId(shopId, activityId);
    }

    //------------------------------------------------------------------------------
    //-------------------------     COURSE    ------------------------------
    //------------------------------------------------------------------------------

    @Override
    public Page<MizarCourse> loadPageByParams(Pageable pageable, String title, String status, String category) {
        return mizarCourseDao.loadPageByParams(category, title, status, pageable);
    }

    @Override
    public MizarCourse loadMizarCourseById(String courseId) {
        return mizarCourseDao.load(courseId);
    }

    @Override
    public List<MizarCourseTarget> loadCourseTargetsByCourseId(String courseId) {
        return mizarCourseTargetDao.loadByCourseId(courseId);
    }

    @Override
    public Map<String, List<MizarCourseMapper>> loadMizarCourseIndexMapByParentId(Long parentId) {
        // 获取当前用户所有可见的列表
        List<MizarCourse> courses = loadUserCourseListIncludeOffline(parentId);
        // 过滤ONLINE的数据
        if (CollectionUtils.isEmpty(courses)) {
            return Collections.emptyMap();
        }
        // 过滤首页并且上线状态的
        courses = courses.stream().filter(c -> c.getIndexShow() != null && c.getIndexShow())
                .filter(c -> c.getStatus() != null && c.getStatus() == MizarCourse.Status.ONLINE).collect(toList());
        if (CollectionUtils.isEmpty(courses)) {
            return Collections.emptyMap();
        }
        // 排序 根据是否置顶+时间
        courses.sort((o1, o2) -> {
            Boolean b1 = SafeConverter.toBoolean(o1.getTop());
            Boolean b2 = SafeConverter.toBoolean(o2.getTop());
            if (!b2.equals(b1))
                return b2.compareTo(b1);
            Date d1 = o1.getCreateAt();
            Date d2 = o2.getCreateAt();
            return d2.compareTo(d1);
        });
        List<MizarCourseMapper> mappers = convertMappers(courses);
        return mappers.stream().collect(groupingBy(MizarCourseMapper::getCategory));
    }

    @Override
    public PageImpl<MizarCourseMapper> loadUserMizarCoursePageByCategory(MizarCourseCategory category, String tag, Long parentId, Pageable pageable) {
        // 获取当前用户所有可见的列表
        List<MizarCourse> courses = loadUserCourseListIncludeOffline(parentId);
        if (CollectionUtils.isEmpty(courses)) {
            return new PageImpl<>(new ArrayList<>());
        }
        // 过滤分类
        if (category == MizarCourseCategory.GOOD_COURSE || category == MizarCourseCategory.PARENTAL_ACTIVITY) {
            courses = courses.stream().filter(c -> StringUtils.isNotBlank(c.getCategory()) && StringUtils.equals(c.getCategory(), category.name())).collect(Collectors.toList());
        } else {
            courses = courses.stream().filter(c -> StringUtils.isNotBlank(c.getCategory()) && StringUtils.equals(c.getCategory(), category.name()))
                    .filter(c -> StringUtils.isBlank(tag) || (CollectionUtils.isNotEmpty(c.getTags()) && c.getTags().contains(tag)))
                    .filter(c -> c.getStatus() != null && c.getStatus() == MizarCourse.Status.ONLINE)
                    .collect(toList());
        }
        if (CollectionUtils.isEmpty(courses)) {
            return new PageImpl<>(new ArrayList<>());
        }
        // 排序 根据是否上线 + 置顶 + 优先级 + 时间
        courses.sort((o1, o2) -> {
            Boolean s1 = o1.getStatus() == MizarCourse.Status.ONLINE;
            Boolean s2 = o2.getStatus() == MizarCourse.Status.ONLINE;
            if (!s2.equals(s1)) {
                return s2.compareTo(s1);
            }
            Boolean b1 = SafeConverter.toBoolean(o1.getTop());
            Boolean b2 = SafeConverter.toBoolean(o2.getTop());
            if (!b2.equals(b1)) {
                return b2.compareTo(b1);
            }
            Integer p1 = o1.getPriority() == null ? 0 : o1.getPriority();
            Integer p2 = o2.getPriority() == null ? 0 : o2.getPriority();
            if (!p1.equals(p2)) {
                return p2.compareTo(p1);
            }
            Date d1 = o1.getCreateAt();
            Date d2 = o2.getCreateAt();
            return d2.compareTo(d1);
        });
        // 先分页 再添加阅读数
        long total = courses.size();
        if (pageable.getPageNumber() * pageable.getPageSize() > total) {
            // 请正确填写页码
            return new PageImpl<>(Collections.emptyList());
        }
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = Math.min((int) total, ((pageable.getPageNumber() + 1) * pageable.getPageSize()));
        courses = new LinkedList<>(courses.subList(start, end));
        List<MizarCourseMapper> mappers = convertMappers(courses);
        return new PageImpl<>(
                mappers,
                pageable,
                total);
    }

    private boolean isInDistance(double distance, double lat1, double lng1, final MizarShop mizarShop) {
        if (Objects.nonNull(mizarShop.getLatitude()) && Objects.nonNull(mizarShop.getLongitude())
                && mizarShop.getLatitude() > 0.0 && mizarShop.getLongitude() > 0.0) {
            return GEOUtils.getDistance(lat1, lng1, mizarShop.getLatitude(), mizarShop.getLongitude()) <= distance;
        } else {
            return false;
        }
    }

    private Map<String, Map<String, Object>> match(Double distance, Double longitude, Double latitude, Map<String, List<MizarShop>> mizarShopMap) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map.Entry<String, List<MizarShop>> entry : mizarShopMap.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            String brandId = entry.getKey();
            List<MizarShop> shopList = entry.getValue();
            if (CollectionUtils.isEmpty(shopList)) {
                //没有机构的跳过
                continue;
            }
            List<MizarShopMapper> mappers = shopToMappers(shopList, longitude, latitude);

            String orderBy = "distance";//按距离
            if (longitude == null || latitude == null || longitude == 0.0 || latitude == 0.0) {
                orderBy = "rating";//按评分
            } else {
                //按距离,需要过滤distance 以外的
                if (CollectionUtils.isNotEmpty(mappers)) {
                    mappers = mappers.stream().filter(o -> Objects.nonNull(o.getDistance()) && o.getDistance() <= distance).collect(toList());
                }
            }
            // 排序
            mappers = orderShops(mappers, orderBy);
            MizarShopMapper recommendedShop = MiscUtils.firstElement(mappers);
            if (CollectionUtils.isEmpty(mappers) || Objects.isNull(recommendedShop)) {
                //  log.warn("品牌id="+brandId+" 没有对应的机构");
                continue;
            }
            data.put("shopName", recommendedShop.getName());
            data.put("shopId", recommendedShop.getId());
            data.put("ratingStar", recommendedShop.getRatingStar());
            data.put("ratingCount", recommendedShop.getRatingCount());
            data.put("distance", recommendedShop.getDistance());
            data.put("welcomeGift", recommendedShop.getWelcomeGift());
            data.put("address", recommendedShop.getAddress());
            data.put("longitude", recommendedShop.getLongitude());
            data.put("latitude", recommendedShop.getLatitude());
            data.put("firstCategory", recommendedShop.getFirstCategory());
            result.put(brandId, data);
        }
        return result;
    }

    public Map<String, List<Map<String, Object>>> loadBrandList(String longitude, String latitude) {
        //loadBrand-->loadShop---> 1:1=brand-shop--> 10 km  以内的 －－> 排序
        Double dLongitude = SafeConverter.toDouble(longitude, 0.0);
        Double dLatitude = SafeConverter.toDouble(latitude, 0.0);

        Map<String, List<Map<String, Object>>> dataMap = new HashMap<>();
        List<MizarBrand> mizarBrandList = mizarBrandDao.loadBrandHall();
        if (CollectionUtils.isEmpty(mizarBrandList)) {
            dataMap.put("play", new ArrayList<>());
            dataMap.put("english", new ArrayList<>());
            dataMap.put("art", new ArrayList<>());
            return dataMap;
        }
        List<String> brandIdList = mizarBrandList.stream().map(o -> o.getId()).collect(toList());
        //查询品牌对应的机构 key-value=brandId-List<MizarShop>
        Map<String, List<MizarShop>> mizarShopMap = mizarShopDao.loadShopByIds(brandIdList);

        //如果有距离则获取距离最近的shop,如果没有距离,则获取评分最高的shop(同时过滤10km 以外的),机构排序
        double distance = 10.0;// km
        Map<String, Map<String, Object>> result = match(distance, dLongitude, dLatitude, mizarShopMap);

        //品牌排序
        mizarBrandList = mizarBrandList.stream().sorted(new Comparator<MizarBrand>() {
            @Override
            public int compare(MizarBrand o1, MizarBrand o2) {
                Integer orderIndex = SafeConverter.toInt(o1.getOrderIndex());
                Integer orderIndexOther = SafeConverter.toInt(o2.getOrderIndex());
                return Integer.compare(orderIndexOther, orderIndex);
            }
        }).collect(toList());

        //刷选出三个分类
        Map<String, List<Map<String, Object>>> resultMap = filterByCategory(mizarBrandList, result);
        dataMap.put("english", resultMap.get("english"));
        dataMap.put("art", resultMap.get("art"));
        dataMap.put("play", resultMap.get("play"));
        return dataMap;
    }

    private Map<String, List<Map<String, Object>>> filterByCategory(List<MizarBrand> mizarBrandList, Map<String, Map<String, Object>> shopMap) {
        Map<String, List<Map<String, Object>>> mapResult = new HashMap<>();
        mapResult.put("play", new ArrayList<>());
        mapResult.put("english", new ArrayList<>());
        mapResult.put("art", new ArrayList<>());
        String firstCategory = "";
        String key = "";
        for (MizarBrand brand : mizarBrandList) {
            Map<String, Object> shopDataMap = shopMap.get(brand.getId());
            if (Objects.isNull(shopDataMap)) {
                // log.warn("品牌没有对应的机构brand＝"+brand.getId());
                continue;
            }
            shopDataMap.put("brandName", brand.getBrandName());
            shopDataMap.put("brandLog", brand.getBrandLogo());
            shopDataMap.put("brandPoints", CollectionUtils.isEmpty(brand.getPoints()) ? Collections.emptyList() : brand.getPoints());
            shopDataMap.put("brandId", brand.getId());
            //得对品牌对应的机构的一级分类
            if (Objects.nonNull(shopDataMap.get("firstCategory"))) {
                firstCategory = shopDataMap.get("firstCategory").toString();
                if (StringUtils.contains(firstCategory, "游学玩乐")) {
                    key = "play";
                } else if (StringUtils.contains(firstCategory, "少儿外语")) {
                    key = "english";
                } else if (StringUtils.contains(firstCategory, "兴趣才艺")) {
                    key = "art";
                }
            }

            if (StringUtils.isNotBlank(key)) {
                List<Map<String, Object>> dataList = mapResult.get(key);
                if (Objects.isNull(dataList)) {
                    dataList = new ArrayList<>();
                    mapResult.put(key, dataList);
                }
                dataList.add(shopDataMap);
            }
        }
        return mapResult;
    }

    public Map<String, List<Map<String, Object>>> loadPpgIndexList(String longitude, String latitude) {
        return loadBrandList(longitude, latitude);
    }

    /**
     * 原来的逻辑,先保留
     *
     * @param longitude
     * @param latitude
     * @return
     */
    public Map<String, List<Map<String, Object>>> loadPpgIndexList_bak(String longitude, String latitude) {
        // 配置好品牌ID 分组
        List<String> englishBrandIds = Arrays.asList("57bbfe292f70b14d747e1e11", "57ce6b496cdb8a43148121cb",
                "57cfb61e6cdb8a1efe5dd44c", "57bbfe292f70b14d747e1e07", "57bbfe292f70b14d747e1e10", "57bbfe2a2f70b14d747e1e14");
        List<String> artBrandIds = Arrays.asList("57bbfe2a2f70b14d747e1e66", "57c92ef06cdb8a3b86f05b8c",
                "57bbfe2a2f70b14d747e1e1b", "57bbfe2a2f70b14d747e1e43", "57bbfe2a2f70b14d747e1e35", "580594656cdb8a0674757175", "57bbfe2b2f70b14d747e1e97");
        List<String> playBrandIds = Arrays.asList("57c8e9876cdb8a60431a1b5d");

        List<String> allBrandIds = new ArrayList<>();
        allBrandIds.addAll(englishBrandIds);
        allBrandIds.addAll(artBrandIds);
        allBrandIds.addAll(playBrandIds);
        // 获取品牌以及对应的机构
        Map<String, MizarBrand> brandMap = mizarBrandDao.loads(allBrandIds);
        // 拼装数据
        Map<String, List<Map<String, Object>>> dataMap = new HashMap<>();
        List<Map<String, Object>> englishDataList = new ArrayList<>();
        for (String brandId : englishBrandIds) {
            MizarBrand brand = brandMap.get(brandId);
            if (brand == null) {
                continue;
            }
            englishDataList.add(getPpgMap(brand, longitude, latitude));
        }
        List<Map<String, Object>> artDataList = new ArrayList<>();
        for (String brandId : artBrandIds) {
            MizarBrand brand = brandMap.get(brandId);
            if (brand == null) {
                continue;
            }
            artDataList.add(getPpgMap(brand, longitude, latitude));
        }
        List<Map<String, Object>> playDataList = new ArrayList<>();
        for (String brandId : playBrandIds) {
            MizarBrand brand = brandMap.get(brandId);
            if (brand == null) {
                continue;
            }
            playDataList.add(getPpgMap(brand, longitude, latitude));
        }
        dataMap.put("english", englishDataList);
        dataMap.put("art", artDataList);
        dataMap.put("play", playDataList);
        return dataMap;
    }

    @Override
    public Map<String, Object> loadBrandNearShop(String brandId, String longitude, String latitude) {
        MizarBrand mizarBrand = mizarBrandDao.load(brandId);
        if (mizarBrand == null) {
            return Collections.emptyMap();
        }
        return putBrandShopMap(brandId, longitude, latitude, new HashMap<>());
    }

    @Override
    public List<MizarShopMapper> loadBrandShopsByPosition(String brandId, String longitude, String latitude) {
        List<MizarShop> shopList = mizarShopDao.loadByBrandId(brandId);
        if (CollectionUtils.isEmpty(shopList)) {
            return Collections.emptyList();
        }
        // 距离最近的或者评分最高的排序
        Double longitudeD = null;
        Double latitudeD = null;
        if (SafeConverter.toDouble(longitude) != 0 && SafeConverter.toDouble(latitude) != 0) {
            longitudeD = SafeConverter.toDouble(longitude);
            latitudeD = SafeConverter.toDouble(latitude);
        }
        List<MizarShopMapper> mappers = shopToMappers(shopList, longitudeD, latitudeD);
        String orderBy = "distance";
        if (longitude == null || latitude == null) {
            orderBy = "rating";
        }
        // 排序
        mappers = orderShops(mappers, orderBy);
        return mappers;
    }

    private Map<String, Object> getPpgMap(MizarBrand brand, String longitude, String latitude) {
        Map<String, Object> data = new HashMap<>();
        data.put("brandName", brand.getBrandName());
        data.put("brandLog", brand.getBrandLogo());
        data.put("brandPoints", brand.getPoints());
        data.put("brandId", brand.getId());
        return putBrandShopMap(brand.getId(), longitude, latitude, data);
    }

    private Map<String, Object> putBrandShopMap(String brandId, String longitude, String latitude, Map<String, Object> data) {
        // 获取机构数据
        List<MizarShop> shopList = mizarShopDao.loadByBrandId(brandId);
        if (CollectionUtils.isNotEmpty(shopList)) {
            // 获取距离最近的或者评分最高的
            Double longitudeD = null;
            Double latitudeD = null;
            if (SafeConverter.toDouble(longitude) != 0 && SafeConverter.toDouble(latitude) != 0) {
                longitudeD = SafeConverter.toDouble(longitude);
                latitudeD = SafeConverter.toDouble(latitude);
            }
            List<MizarShopMapper> mappers = shopToMappers(shopList, longitudeD, latitudeD);
            String orderBy = "distance";
            if (longitude == null || latitude == null) {
                orderBy = "rating";
            }
            // 排序
            mappers = orderShops(mappers, orderBy);
            // 取第一个
            MizarShopMapper recommendedShop = MiscUtils.firstElement(mappers);
            data.put("shopName", recommendedShop.getName());
            data.put("shopId", recommendedShop.getId());
            data.put("ratingStar", recommendedShop.getRatingStar());
            data.put("ratingCount", recommendedShop.getRatingCount());
            data.put("distance", recommendedShop.getDistance());
            data.put("welcomeGift", recommendedShop.getWelcomeGift());
            data.put("address", recommendedShop.getAddress());
            // data.put("longitude", recommendedShop.getLatitude());
            data.put("longitude", recommendedShop.getLongitude());
            data.put("latitude", recommendedShop.getLatitude());
        }
        return data;
    }

    //------------------------------------------------------------------------------
    //-------------------------     PRIVATE METHODS   ------------------------------
    //------------------------------------------------------------------------------
    private List<MizarShopMapper> orderShops(List<MizarShopMapper> mappers, final String orderBy) {
        Collections.sort(mappers, (o1, o2) -> {
            int result = 0;
            switch (orderBy) {
                case "distance": {
                    Double distance1 = o1.getDistance() == null ? 0 : o1.getDistance();
                    Double distance2 = o2.getDistance() == null ? 0 : o2.getDistance();
                    result = distance1.compareTo(distance2);
                    break;
                }
                case "rating": {
                    Integer r1 = o1.getRatingStar() == null ? 0 : o1.getRatingStar();
                    Integer r2 = o2.getRatingStar() == null ? 0 : o2.getRatingStar();
                    result = r2.compareTo(r1);
                    break;
                }
                case "smart": {
                    // 得分越高越靠前
                    result = o2.getOrderScore().compareTo(o1.getOrderScore());
                    break;
                }
            }
            return result;
        });
        return mappers;
    }


    @Override
    public PageImpl<MizarShopMapper> loadShopPageByParam(LoadMizarShopContext context) {
        // 查询全部数据根据条件
        List<MizarShop> shopList = new ArrayList<>();
        if (context.regionCode != null && context.regionCode > 0) {
            shopList = mizarShopDao.loadByRegionCode(context.regionCode);
        } else if (StringUtils.isNoneBlank(context.longitude) && StringUtils.isNoneBlank(context.latitude)
                && !StringUtils.equals("0", context.longitude) && !StringUtils.equals("0", context.latitude)) {
            // 不传区域 如果传了经纬度 则根据经纬度匹配周边5KM的机构
            double[] rectangle = GEOUtils.getRectangle(SafeConverter.toDouble(context.longitude), SafeConverter.toDouble(context.latitude), 5000);//[lng1,lat1, lng2,lat2]
            shopList = mizarShopDao.loadByGpsPos(rectangle[0], rectangle[2], rectangle[1], rectangle[3]);
        } else {
            // 如果没传区域 也没有经纬度 则默认按照学校区域查询
            shopList = mizarShopDao.loadByRegionCode(context.schoolRegionCode);
        }
        // 拼上所有的线上机构
        shopList.addAll(mizarShopDao.loadByType(MizarShopType.ONLINE.getId()));
        if (CollectionUtils.isEmpty(shopList)) {
            return new PageImpl<>(Collections.emptyList());
        }
        //过滤条件不符合条件的机构,当没有区域时,一定没有商圈
        shopList = shopList.stream()
                .filter(p -> StringUtils.isBlank(context.shopName) || (StringUtils.isNotBlank(p.getFullName()) && p.getFullName().contains(context.shopName)))  // filter by shop name
                .filter(p -> StringUtils.isBlank(context.firstCategory) || (CollectionUtils.isNotEmpty(p.getFirstCategory()) && (p.getFirstCategory().contains(context.firstCategory))))
                .filter(p -> StringUtils.isBlank(context.secondCategory) || (CollectionUtils.isNotEmpty(p.getSecondCategory()) && (p.getSecondCategory().contains(context.secondCategory))))
                .filter(p -> StringUtils.isBlank(context.tradeArea) || Objects.equals(context.tradeArea, p.getTradeArea()))
                .filter(p -> matchClazzLevel(p.getMatchGrade(), context.clazzLevels))
                .filter(p -> Objects.nonNull(p.getType()) && (Objects.equals(0, p.getType()) || Objects.equals(1, p.getType())))//只查询这两种类型的机构
                .collect(toList());

        List<MizarShopMapper> mappers = smartOrderShop(shopList, context.longitude, context.latitude);
        mappers = orderShops(mappers, context.orderBy);
        long total = mappers.size();
        if (context.pageNum * context.pageSize > total) {
            // 请正确填写页码
            return new PageImpl<>(Collections.emptyList());
        }
        int start = context.pageNum * context.pageSize;
        int end = Math.min((int) total, ((context.pageNum + 1) * context.pageSize));
        mappers = new LinkedList<>(mappers.subList(start, end));
        // 获取是否有本校同学去过
        if (context.schoolId != null && context.schoolId != 0) {
            Set<String> shopIds = mappers.stream().map(MizarShopMapper::getId).collect(toSet());
            Map<String, List<MizarReserveRecord>> recordMap = mizarReserveRecordPersistence.loadBySchoolIdAndShopId(context.schoolId, shopIds);
            if (MapUtils.isNotEmpty(recordMap)) {
                for (MizarShopMapper mapper : mappers) {
                    if (!mapper.isVip()) {
                        continue;
                    }
                    if (CollectionUtils.isNotEmpty(recordMap.get(mapper.getId()))) {
                        List<MizarReserveRecord> reserveRecordList = recordMap.get(mapper.getId());
                        reserveRecordList = reserveRecordList.stream()
                                .filter(r -> !Objects.equals(r.getStudentId(), context.studentId)).collect(toList());
                        mapper.setSameSchoolFlag(CollectionUtils.isNotEmpty(reserveRecordList));
                    }
                }
            }
        }
        return new PageImpl<>(
                mappers,
                new PageRequest(context.pageNum, context.pageSize),
                total);
    }

    /**
     * 智能排序
     *
     * @param shopList  机构列表
     * @param longitude 经度
     * @param latitude  纬度
     * @return 机构信息包装后对象
     */
    private List<MizarShopMapper> smartOrderShop(final List<MizarShop> shopList, String longitude, String latitude) {
        List<MizarShopMapper> mappers = new ArrayList<>();
        List<String> shopIds = shopList.stream().map(MizarShop::getId).collect(toList());
        //机构大数据打分Map
        Map<String, MizarShopRating> mizarShopRatingMap = mizarShopRatingDao.loadMizarShopRatingByShopIds(shopIds);

        for (MizarShop shop : shopList) {
            MizarShopRating mizarShopRating = mizarShopRatingMap.get(shop.getId());
            MizarShopMapper mapper = new MizarShopMapper();
            int rating = mizarShopRating == null ? 0 : mizarShopRating.getRating();

            double distance = 0;
            if (shop.getLatitude() != null && shop.getLongitude() != null && !StringUtils.equals("0", longitude) && !Objects.equals("0", latitude)) {
                distance = GEOUtils.getDistance(SafeConverter.toDouble(latitude), SafeConverter.toDouble(longitude), shop.getLatitude(), shop.getLongitude());
                if (distance > 5) {
                    mapper.setOrderScore(-10 + rating);
                    mapper.setDistance(distance);
                } else if (distance > 0) {
                    mapper.setOrderScore((int) (distance * (-2) + rating));
                    mapper.setDistance(distance);
                }
            } else {
                // 通过区域查询的机构,只有大数据计算的分值
                mapper.setOrderScore(rating);
            }
            //合作等级和人工调节的分数
            int otherScore = Objects.isNull(shop.getCooperationLevel()) ? 0 : shop.getCooperationLevel() * 3
                    + (Objects.isNull(shop.getAdjustScore()) ? 0 : shop.getAdjustScore());
            mapper.setOrderScore(otherScore + (Objects.isNull(mapper.getOrderScore()) ? 0 : mapper.getOrderScore()));
            mapper.setDistance(distance);
            mapper.setId(shop.getId());
            mapper.setName(shop.getFullName());
            if (CollectionUtils.isNotEmpty(shop.getPhoto())) {
                mapper.setPhoto(shop.getPhoto().get(0));
            }
            mapper.setRatingCount(shop.getRatingCount());
            mapper.setRatingStar(shop.getRatingStar());
            mapper.setWelcomeGift(shop.getWelcomeGift());
            mapper.setTradeArea(shop.getTradeArea());
            mapper.setSecondCategory(shop.getSecondCategory());
            mapper.setVip(shop.getVip() == null ? false : shop.getVip());
            mappers.add(mapper);
        }
        return mappers;
    }


    public List<MizarCourse> loadUserCourseListIncludeOffline(Long parentId) {
        // 将用户对应的列表放到缓存
        String cacheKey = "MIZAR:CL:USER:" + parentId;
        List<MizarCourse> cacheList = CacheSystem.CBS.getCache("flushable").load(cacheKey);
        if (CollectionUtils.isNotEmpty(cacheList)) {
            return cacheList;
        }
        if (parentId == null) {
            return Collections.emptyList();
        }
        User user = userLoaderClient.loadUser(parentId);
        if (user == null || user.fetchUserType() != UserType.PARENT) {
            return Collections.emptyList();
        }
        List<MizarCourse> allCourse = mizarCourseDao.loadAll();
        if (CollectionUtils.isEmpty(allCourse)) {
            return Collections.emptyList();
        }

        // 根据家长孩子的信息过滤配置
        // 获取家长孩子
        List<User> userList = studentLoaderClient.loadParentStudents(parentId);
        List<Integer> regionCodeList = new ArrayList<>();
        List<Long> schoolIdList = new ArrayList<>();
        List<Integer> clazzLevelList = new ArrayList<>();
        for (User child : userList) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(child.getId());
            if (studentDetail == null) continue;
            CollectionUtils.addNonNullElement(regionCodeList, studentDetail.getCityCode());
            CollectionUtils.addNonNullElement(regionCodeList, studentDetail.getRootRegionCode());
            CollectionUtils.addNonNullElement(regionCodeList, studentDetail.getStudentSchoolRegionCode());
            CollectionUtils.addNonNullElement(clazzLevelList, studentDetail.getClazzLevelAsInteger());
            if (studentDetail.getClazz() != null) {
                CollectionUtils.addNonNullElement(schoolIdList, studentDetail.getClazz().getSchoolId());
            }
        }

        List<MizarCourse> filterList = new ArrayList<>();
        for (MizarCourse course : allCourse) {
            // 先校验年级 年级不满足 直接pass
            if (!checkClazzLevel(course.getClazzLevels(), clazzLevelList)) {
                continue;
            }
            // 过滤广告target
            Map<Integer, List<MizarCourseTarget>> targetMap = mizarCourseTargetDao.loadByCourseId(course.getId())
                    .stream().collect(groupingBy(MizarCourseTarget::getTargetType, toList()));
            // 校验是否投放所有用户
            List<MizarCourseTarget> targetList = targetMap.get(MizarCourseTargetType.TARGET_TYPE_ALL.getType());
            if (CollectionUtils.isNotEmpty(targetList)) {
                MizarCourseTarget target = targetList.stream().filter(t -> SafeConverter.toBoolean(t.getTargetStr()))
                        .findAny().orElse(null);
                if (target != null) {
                    filterList.add(course);
                    continue;
                }
            }

            // 校验 TARGET_TYPE_REGION 投放区域过滤
            // 这里是 || 的关系
            targetList = targetMap.get(MizarCourseTargetType.TARGET_TYPE_REGION.getType());
            if (checkRegionNew(regionCodeList, targetList)) {
                // 命中条件 直接返回
                filterList.add(course);
                continue;
            }
            // 校验 TARGET_TYPE_SCHOOL 投放学校过滤
            targetList = targetMap.get(MizarCourseTargetType.TARGET_TYPE_SCHOOL.getType());
            if (checkTargetSchool(schoolIdList, targetList)) {
                // 命中条件 直接返回
                filterList.add(course);
            }
        }
        // 加入缓存 用户量少,暂时改成5分钟
        // 500s = 8min20s
        CacheSystem.CBS.getCache("flushable").set(cacheKey, 500, filterList);
        return filterList;
    }

    private boolean checkClazzLevel(List<String> courseClazzLevels, List<Integer> clazzLevelList) {
        if (CollectionUtils.isEmpty(courseClazzLevels)) {
            return true;
        }
        for (String gradeLevel : courseClazzLevels) {
            Integer level = clazzLevelList.stream().filter(lv -> SafeConverter.toInt(gradeLevel) == lv).findAny().orElse(null);
            if (level != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setBeanName(String beanName) {
        super.setBeanName(beanName);
    }


    private List<MizarShopMapper> shopToMappers(Collection<MizarShop> shopList, Double longitude, Double latitude) {
        List<MizarShopMapper> mappers = new ArrayList<>();
        for (MizarShop shop : shopList) {
            MizarShopMapper mapper = new MizarShopMapper();
            mapper.setId(shop.getId());
            mapper.setName(shop.getFullName());
            if (CollectionUtils.isNotEmpty(shop.getPhoto())) {
                mapper.setPhoto(shop.getPhoto().get(0));
            }
            mapper.setRatingCount(shop.getRatingCount());
            mapper.setRatingStar(shop.getRatingStar());
            // 计算距离
            double distance = 0.0;
            if (shop.getLatitude() != null && shop.getLongitude() != null && longitude != null && latitude != null && longitude > 0.0 && latitude > 0.0) {
                distance = GEOUtils.getDistance(latitude, longitude, shop.getLatitude(), shop.getLongitude());
            }
            mapper.setDistance(distance);
            mapper.setWelcomeGift(shop.getWelcomeGift());
            mapper.setTradeArea(shop.getTradeArea());
            mapper.setSecondCategory(shop.getSecondCategory());
            mapper.setFirstCategory(shop.getFirstCategory() != null ? shop.getFirstCategory().stream().findFirst().orElse("") : "");
            mapper.setVip(shop.getVip() == null ? false : shop.getVip());
            mapper.setAddress(shop.getAddress());
            mapper.setLongitude(shop.getLongitude());
            mapper.setLatitude(shop.getLatitude());
            mappers.add(mapper);
        }
        return mappers;
    }

    private List<MizarCourseMapper> convertMappers(List<MizarCourse> courses) {
        if (CollectionUtils.isEmpty(courses)) {
            return Collections.emptyList();
        }
        List<MizarCourseMapper> mappers = new ArrayList<>();
        for (MizarCourse course : courses) {
            MizarCourseMapper mapper = new MizarCourseMapper();
            mapper.setBackground(course.getBackground());
            mapper.setTitle(course.getTitle());
            mapper.setSubTitle(course.getSubTitle());
            mapper.setDescription(course.getDescription());
            mapper.setId(course.getId());
            mapper.setKeynoteSpeaker(course.getKeynoteSpeaker());
            mapper.setRedirectUrl("/mizar/course/go.vpage?id=" + course.getId()); // 跳转到我们自己的转换链接
            mapper.setTags(course.getTags());
            mapper.setStatus(course.getStatus());
            mapper.setCategory(course.getCategory());
            mapper.setReadCount(asyncMizarCacheService.MizarCourseReadCountManager_loadReadCount(course.getId()).getUninterruptibly());
            mapper.setSoldOut(false);
            mapper.setSpeakerAvatar(course.getSpeakerAvatar());
            mapper.setPrice(course.getPrice());
            mapper.setClassTime(course.getClassTime());
            if (course.getActivityId() != null && course.getActivityId() != 0) {
                // 设置是否售罄
                BusinessActivity activity = businessActivityManagerClient.getBusinessActivityBuffer()
                        .load(course.getActivityId());
                if (activity != null && activity.getLimit() != null && activity.getLimit() > 0
                        && asyncUserCacheServiceClient.getAsyncUserCacheService()
                        .SeattleSoldCountManager_loadSoldCount(activity.getId())
                        .getUninterruptibly() >= activity.getLimit()) {
                    mapper.setSoldOut(true);
                }
            }
            mappers.add(mapper);
        }
        return mappers;
    }

    private boolean matchClazzLevel(String clazzLevels, Collection<Integer> clazzLevelList) {
        if (StringUtils.isBlank(clazzLevels)) {
            return true;
        }

        if (CollectionUtils.isEmpty(clazzLevelList)) {
            return false;
        }

        for (Integer clazzLevel : clazzLevelList) {
            if (clazzLevels.contains(SafeConverter.toString(clazzLevel))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRegionNew(List<Integer> regionCodeList, List<MizarCourseTarget> targetList) {
        if (CollectionUtils.isNotEmpty(targetList)) {
            for (MizarCourseTarget target : targetList) {
                for (Integer code : regionCodeList) {
                    if (StringUtils.equals(target.getTargetStr(), String.valueOf(code)))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean checkTargetSchool(List<Long> schoolIds, List<MizarCourseTarget> targetList) {
        if (CollectionUtils.isNotEmpty(targetList)) {
            MizarCourseTarget schoolTarget = targetList.stream().filter(t ->
                    StringUtils.isNotBlank(t.getTargetStr()) && schoolIds.contains(SafeConverter.toLong(t.getTargetStr())))
                    .findAny().orElse(null);
            if (schoolTarget != null) {
                return true;
            }
        }
        return false;
    }
}
