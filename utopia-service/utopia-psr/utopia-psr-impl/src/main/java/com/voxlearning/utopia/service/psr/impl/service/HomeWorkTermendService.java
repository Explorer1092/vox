package com.voxlearning.utopia.service.psr.impl.service;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.Unit;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.EnglishQuestionBox;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.MathMentalQuestionBox;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.MathQuestionBox;
import com.voxlearning.utopia.service.psr.impl.dao.CouchBaseDao;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hotallen
 * Date: 2016/5/13
 * Time: 19:41
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
@Named
public class HomeWorkTermendService extends SpringContainerSupport {

    @Inject private CouchBaseDao couchBaseDao;
    @Inject private EnglishContentLoaderClient englishContentLoaderClient;

    // local cache for question boxes, the key is usually unit_id
    private static LoadingCache<String, EnglishQuestionBox> unitIdEnglishQuestionBoxLoadingCache;
    private static LoadingCache<String, MathMentalQuestionBox> unitIdMathMentalQuestionBoxLoadingCache;
    private static LoadingCache<String, MathQuestionBox> unitIdMathQuestionBoxLoadingCache;
    // local cache for questions boxes, the key is usually boxId
    private static LoadingCache<String, EnglishQuestionBox> boxIdEnglishQuestionBoxLoadingCache;
    private static LoadingCache<String, MathMentalQuestionBox> boxIdMathMentalQuestionBoxLoadingCache;
    private static LoadingCache<String, MathQuestionBox> boxIdMathQuestionBoxLoadingCache;

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public Map<String, EnglishQuestionBox> loadEnglishQuestionBoxs(Collection<String> boxIds) {
        Map<String, EnglishQuestionBox> englishQuestionBoxMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(boxIds)) {
            return englishQuestionBoxMap;
        }
        for (String boxId : boxIds) {
            EnglishQuestionBox englishQuestionBox = getFromCache(boxIdEnglishQuestionBoxLoadingCache, boxId);
            if (englishQuestionBox != null) {
                englishQuestionBoxMap.put(boxId, englishQuestionBox);
            }
        }
        return englishQuestionBoxMap;
    }

    public Map<String, MathMentalQuestionBox> loadMathMentalQuestionBoxs(Collection<String> boxIds) {
        Map<String, MathMentalQuestionBox> mathMentalQuestionBoxMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(boxIds)) {
            return mathMentalQuestionBoxMap;
        }
        for (String boxId : boxIds) {
            MathMentalQuestionBox mathMentalQuestionBox = getFromCache(boxIdMathMentalQuestionBoxLoadingCache, boxId);
            if (mathMentalQuestionBox != null) {
                mathMentalQuestionBoxMap.put(boxId, mathMentalQuestionBox);
            }
        }
        return mathMentalQuestionBoxMap;
    }

    public Map<String, MathQuestionBox> loadMathQuestionBoxs(Collection<String> boxIds) {
        Map<String, MathQuestionBox> mathQuestionBoxMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(boxIds)) {
            return mathQuestionBoxMap;
        }
        for (String boxId : boxIds) {
            MathQuestionBox mathQuestionBox = getFromCache(boxIdMathQuestionBoxLoadingCache, boxId);
            if (mathQuestionBox != null) {
                mathQuestionBoxMap.put(boxId, mathQuestionBox);
            }
        }
        return mathQuestionBoxMap;
    }

    public List<EnglishQuestionBox> getEnglishQuestionBoxes(List<Long> unitIds) {
        List<EnglishQuestionBox> englishQuestionBoxes = Lists.newArrayList();
        for (Long unitId : unitIds) {
            EnglishQuestionBox englishQuestionBox = getFromCache(unitIdEnglishQuestionBoxLoadingCache, formatKey("english", String.valueOf(unitId)));
            if (englishQuestionBox != null) englishQuestionBoxes.add(englishQuestionBox);
        }
        return englishQuestionBoxes;
    }

    public List<MathQuestionBox> getMathQuestionBoxes(List<String> unitIds) {
        List<MathQuestionBox> mathQuestionBoxes = Lists.newArrayList();
        for (String unitId : unitIds) {
            MathQuestionBox mathQuestionBox = getFromCache(unitIdMathQuestionBoxLoadingCache, formatKey("math", unitId));
            if (mathQuestionBox != null) mathQuestionBoxes.add(mathQuestionBox);
        }
        return mathQuestionBoxes;
    }

    public List<MathMentalQuestionBox> getMathMentalQuestionBoxes(List<String> unitIds) {
        List<MathMentalQuestionBox> mathMentalQuestionBoxes = Lists.newArrayList();
        for (String unitId : unitIds) {
            MathMentalQuestionBox mathMentalQuestionBox = getFromCache(unitIdMathMentalQuestionBoxLoadingCache, formatKey("math_mental", unitId));
            if (mathMentalQuestionBox != null) mathMentalQuestionBoxes.add(mathMentalQuestionBox);
        }
        return mathMentalQuestionBoxes;
    }


    private void init() {
        unitIdEnglishQuestionBoxLoadingCache = build(new CacheLoader<String, EnglishQuestionBox>() {
            @Override
            public EnglishQuestionBox load(String key) {
                return loadEnglishQuestionBoxFromCouchBase(key);
            }
        }, 2000);
        unitIdMathMentalQuestionBoxLoadingCache = build(new CacheLoader<String, MathMentalQuestionBox>() {
            @Override
            public MathMentalQuestionBox load(String key) {
                return loadMathMentalQuestionBoxFromCouchBase(key);
            }
        }, 2000);
        unitIdMathQuestionBoxLoadingCache = build(new CacheLoader<String, MathQuestionBox>() {
            @Override
            public MathQuestionBox load(String key)  {
                return loadMathQuestionBoxFromCouchBase(key);
            }
        }, 1600);

        boxIdEnglishQuestionBoxLoadingCache = build(new CacheLoader<String, EnglishQuestionBox>() {
            @Override
            public EnglishQuestionBox load(String key)  {
                return loadEnglishQuestionBoxFromCouchBase(key);
            }
        }, 2000);
        boxIdMathMentalQuestionBoxLoadingCache = build(new CacheLoader<String, MathMentalQuestionBox>() {
            @Override
            public MathMentalQuestionBox load(String key) {
                return loadMathMentalQuestionBoxFromCouchBase(key);
            }
        }, 2000);
        boxIdMathQuestionBoxLoadingCache = build(new CacheLoader<String, MathQuestionBox>() {
            @Override
            public MathQuestionBox load(String key)  {
                return loadMathQuestionBoxFromCouchBase(key);
            }
        }, 1600);

    }


    private EnglishQuestionBox loadEnglishQuestionBoxFromCouchBase(String key) {
        try {
            String content = couchBaseDao.getCouchbaseDataByKey(key);
            if (StringUtils.isNotBlank(content)) {
                EnglishQuestionBox englishQuestionBox = JSON.parseObject(content, EnglishQuestionBox.class);
                fillUnitInfo(englishQuestionBox);
                return englishQuestionBox;
            }
        } catch (Exception e) {
            logger.warn("loadEnglishQuestionBoxFromCouchBase error! e = {}, key = {}", e.getMessage(), key);
        }
        return null;
    }

    private void fillUnitInfo(EnglishQuestionBox englishQuestionBox) {
        if (englishQuestionBox == null) return;
        List<Long> unitIds = Lists.newArrayList();
        unitIds.add(englishQuestionBox.getUnitId());
        Map<Long, Unit> unitMap = englishContentLoaderClient.loadEnglishUnits(unitIds);
        for (Long unitId : unitMap.keySet()) {
            Unit unit = unitMap.get(unitId);
            if (unit == null) continue;
            if (unitId.longValue() == englishQuestionBox.getUnitId().longValue()) {
                englishQuestionBox.setUnitName(unit.getCname());
                englishQuestionBox.setBookId(unit.getBookId());
                Book book = englishContentLoaderClient.loadEnglishBookIncludeDisabled(unit.getBookId());
                if (book != null) {
                    englishQuestionBox.setBookName(book.getCname());
                }
            }
        }
    }

    private MathMentalQuestionBox loadMathMentalQuestionBoxFromCouchBase(String key) {
        try {
            String content = couchBaseDao.getCouchbaseDataByKey(key);
            if (StringUtils.isNotBlank(content)) {
                return JSON.parseObject(content, MathMentalQuestionBox.class);
            }
        } catch (Exception e) {
            logger.warn("loadMathMentalQuestionBoxFromCouchBase error! e = {}, key = {}", e.getMessage(), key);
        }
        return null;
    }

    private MathQuestionBox loadMathQuestionBoxFromCouchBase(String key) {
        try {
            String content = couchBaseDao.getCouchbaseDataByKey(key);
            if (StringUtils.isNotBlank(content)) {
                return JSON.parseObject(content, MathQuestionBox.class);
            }
        } catch (Exception e) {
            logger.warn("loadMathQuestionBoxFromCouchBase error! e = {}, key = {}", e.getMessage(), key);
        }
        return null;
    }


    private static String formatKey(String app, String unitId) {
        String prefix = "";
        if (StringUtils.equals(app, "math")) {
            prefix = "math_";
        } else if (StringUtils.equals(app, "math_mental")) {
            prefix = "math_mental_";
        } else if (StringUtils.equals(app, "english")) {
            prefix = "english_";
        }
        return prefix + unitId;
    }

    private <K, V> V getFromCache(LoadingCache<K, V> loadingCache, K key) {
        try {
            return loadingCache.getUnchecked(key);
        } catch (CacheLoader.InvalidCacheLoadException e) {}
        return null;
    }

    private <K, V> LoadingCache<K, V> build(CacheLoader<K, V> cacheLoader, int maxSize) {
        LoadingCache<K , V> cache = CacheBuilder.newBuilder()
                                                .maximumSize(maxSize)
                                                .weakKeys()
                                                .softValues()
                                                .build(cacheLoader);
        return cache;
    }

    @Override
    public void destroy() throws Exception {
        if (unitIdEnglishQuestionBoxLoadingCache != null) unitIdEnglishQuestionBoxLoadingCache.invalidateAll();
        if (unitIdMathMentalQuestionBoxLoadingCache != null) unitIdMathMentalQuestionBoxLoadingCache.invalidateAll();
        if (unitIdMathQuestionBoxLoadingCache != null) unitIdMathQuestionBoxLoadingCache.invalidateAll();
        if (boxIdEnglishQuestionBoxLoadingCache != null) boxIdEnglishQuestionBoxLoadingCache.invalidateAll();
        if (boxIdMathMentalQuestionBoxLoadingCache != null) boxIdMathMentalQuestionBoxLoadingCache.invalidateAll();
        if (boxIdMathQuestionBoxLoadingCache != null) boxIdMathQuestionBoxLoadingCache.invalidateAll();
    }
}
