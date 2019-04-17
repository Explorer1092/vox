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

package com.voxlearning.utopia.admin.athena;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.athena.api.SearchEngineService;
import com.voxlearning.athena.api.tag.SelfStudyTagService;
import com.voxlearning.athena.api.tag.UserTagService;
import com.voxlearning.athena.bean.LabelDataObject;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/6/15
 */
@Named("com.voxlearning.utopia.admin.athena.SearchEngineServiceClient")
public class SearchEngineServiceClient implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(SearchEngineServiceClient.class);

    private static final String LABEL_CACHE_KEY = "VALAR_LABEL_MORGHULIS";  // 缓存标签的KEY，应该不会有冲突吧

    // 用于去掉用户标签上的权重
    private static final Long LABEL_FILTER_FLAG = 0xFFFFFFFFFFFF0000L;

    @Getter
    @ImportService(interfaceClass = SearchEngineService.class)
    private SearchEngineService remoteReference;

    @Getter
    @ImportService(interfaceClass = UserTagService.class)
    private UserTagService userTagService;

    private UtopiaCache flushable;

    @Override
    public void afterPropertiesSet() throws Exception {
        flushable = CacheSystem.CBS.getCache("flushable");
    }

    public List<LabelDataObject> getLabelTree() {
        try {
            List<LabelDataObject> labelData = loadLabelDataFromCache();
            if (labelData == null) {
                labelData = new ArrayList<>();
                List<LabelDataObject> staticTagList = userTagService.getTagTree4Ad();
                if (CollectionUtils.isNotEmpty(staticTagList)) {
                    labelData.addAll(staticTagList);
                }

                addLabelDataToCache(labelData);
            }
            return labelData;
        } catch (Exception ex) {
            logger.error("Failed to load label tree from cache or Athena.", ex);
            return Collections.emptyList();
        }
    }

    public Map<Long, String> getLabelMap() {
        try {
            Map<Long, String> labelMap = loadLabelMapFromCache();
            if (labelMap == null) {
                labelMap = parseLabelTree(getLabelTree());
                addLabelDataToCache(labelMap);
            }
            return labelMap;
        } catch (Exception ex) {
            logger.error("Failed to load label map from cache or Athena.", ex);
            return Collections.emptyMap();
        }
    }

    public Set<String> getUserLabelSet(Long userId) {
        try {
            Set<String> userLabel = loadUserLabelFromCache(userId);
            if (userLabel == null) {
                userLabel = new HashSet<>();
//                String uid = String.valueOf(userId);
                Set<String> staticUserLabels = userTagService.getUserTag4Ad(userId);
                if (CollectionUtils.isNotEmpty(staticUserLabels)) {
//                    staticUserLabels = staticUserLabels.stream()
//                            .map(val -> SafeConverter.toLong(val) & LABEL_FILTER_FLAG)
//                            .map(SafeConverter::toString)
//                            .collect(Collectors.toSet());
                    userLabel.addAll(staticUserLabels);
                }

                addUserLabelToCache(userId, userLabel);
            }
            return userLabel;
        } catch (Exception ex) {
            logger.error("Failed to get user label set from cache or Athena, uid={}", userId, ex);
            return Collections.emptySet();
        }
    }

    //=========================================================================
    //==============          Load From Cache              ====================
    //=========================================================================
    private List<LabelDataObject> loadLabelDataFromCache() {
        String cacheKey = CacheKeyGenerator.generateCacheKey(LABEL_CACHE_KEY, new String[]{"T2"}, new Object[]{DateUtils.dateToString(new Date(), "yyyyMMdd")});
        CacheObject<List<LabelDataObject>> cacheObject = flushable.get(cacheKey);
        if (cacheObject == null) {
            return null;
        }
        return cacheObject.getValue();
    }

    private Map<Long, String> loadLabelMapFromCache() {
        String cacheKey = CacheKeyGenerator.generateCacheKey(LABEL_CACHE_KEY, new String[]{"M2"}, new Object[]{DateUtils.dateToString(new Date(), "yyyyMMdd")});
        CacheObject<Map<Long, String>> cacheObject = flushable.get(cacheKey);
        if (cacheObject == null) {
            return null;
        }
        return cacheObject.getValue();
    }

    private Set<String> loadUserLabelFromCache(Long userId) {
        String cacheKey = CacheKeyGenerator.generateCacheKey(LABEL_CACHE_KEY, new String[]{"U2"}, new Object[]{userId});
        CacheObject<Set<String>> cacheObject = flushable.get(cacheKey);
        if (cacheObject == null) {
            return null;
        }
        return cacheObject.getValue();
    }

    //=========================================================================
    //==============            Add To Cache               ====================
    //=========================================================================
    private void addLabelDataToCache(Map<Long, String> labelMap) {
        String cacheKey = CacheKeyGenerator.generateCacheKey(LABEL_CACHE_KEY, new String[]{"M2"}, new Object[]{DateUtils.dateToString(new Date(), "yyyyMMdd")});
        flushable.add(cacheKey, 180, labelMap);
    }

    private void addLabelDataToCache(List<LabelDataObject> labelData) {
        String cacheKey = CacheKeyGenerator.generateCacheKey(LABEL_CACHE_KEY, new String[]{"T2"}, new Object[]{DateUtils.dateToString(new Date(), "yyyyMMdd")});
        flushable.add(cacheKey, 180, labelData);
    }

    private void addUserLabelToCache(Long userId, Set<String> userLabel) {
        String cacheKey = CacheKeyGenerator.generateCacheKey(LABEL_CACHE_KEY, new String[]{"U2"}, new Object[]{userId});
        flushable.add(cacheKey, 180, userLabel);
    }

    //=========================================================================
    //==============           Private Methods             ====================
    //=========================================================================
    private Map<Long, String> parseLabelTree(List<LabelDataObject> labelTree) {
        Map<Long, String> labelMap = new HashMap<>();
        for (LabelDataObject label : labelTree) {
            labelMap.put(label.getKey(), label.getTitle());
            if (CollectionUtils.isNotEmpty(label.getChildren())) {
                labelMap.putAll(parseLabelTree(label.getChildren()));
            }
        }
        return labelMap;
    }
}
