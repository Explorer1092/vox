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

package com.voxlearning.utopia.service.afenti.impl.service;

import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.afenti.api.AfentiLoader;
import com.voxlearning.utopia.service.afenti.api.entity.*;
import com.voxlearning.utopia.service.afenti.impl.athena.AfentiWrongQuestionServiceClient;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiOperationalInfoService;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiPreparationVideoUtils;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.question.api.entity.Video;
import com.voxlearning.utopia.service.question.consumer.VideoLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import org.springframework.dao.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = AfentiLoader.class)
public class AfentiLoaderImpl extends UtopiaAfentiSpringBean implements AfentiLoader {
    @Inject AfentiOperationalInfoService afentiOperationalInfoService;
    @Inject GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject AfentiWrongQuestionServiceClient afentiWrongQuestionServiceClient;
    @Inject VideoLoaderClient videoLoaderClient;

    @Override
    public List<WrongQuestionLibrary> loadWrongQuestionLibraryByUserIdAndSubject(Long userId, Subject subject) {
        if (userId == null || !AfentiUtils.isSubjectAvailable(subject)) return Collections.emptyList();
        return wrongQuestionLibraryDao.findByUserIdAndSubject(userId, subject);
    }

    @Override
    public UserAfentiStats loadUserAfentiStats(Long userId) {
        if (null == userId) return null;
        UserAfentiStats inst = userAfentiStatsPersistence.load(userId);
        if (inst != null) return inst;

        inst = new UserAfentiStats().initialize();
        inst.setId(userId);
        try {
            userAfentiStatsPersistence.insert(inst);
        } catch (DuplicateKeyException ignored) {
        }
        return inst;
    }

    @Override
    public List<AfentiLearningPlanPushExamHistory> loadAfentiLearningPlanPushExamHistoryByUserId(Long userId) {
        if (null == userId) return Collections.emptyList();
        return afentiLearningPlanPushExamHistoryDao.$queryByUserId(userId);
    }

    @Override
    public List<AfentiLearningPlanPushExamHistory> loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(Long userId, String newBookId) {
        if (null == userId || StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return afentiLearningPlanPushExamHistoryDao.queryByUserIdAndNewBookId(userId, newBookId);
    }

    @Override
    public List<AfentiLearningPlanUnitRankManager> loadAfentiLearningPlanUnitRankManagerByNewBookId(String newBookId) {
        if (StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return afentiLearningPlanUnitRankManagerPersistence.findByNewBookId(newBookId);
    }

//    @Override
//    public List<String> loadBookIdsWithUnitRankManager() {
//        return afentiLearningPlanUnitRankManagerPersistence.findAllBookIds();
//    }

    @Override
    public List<String> loadBookIdsWithUnitRankManagerForPreparation() {
        return afentiLearningPlanUnitRankManagerPersistence.findAllBookIdsForPreparation();
    }

    @Override
    public List<AfentiLearningPlanUserBookRef> loadAfentiLearningPlanUserBookRefByUserIdAndSubject(Long userId, Subject subject) {
        if (null == userId || !AfentiUtils.isSubjectAvailable(subject)) return Collections.emptyList();
        return afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(userId, subject);
    }

    @Override
    public AfentiLearningPlanUserFootprint loadAfentiLearningPlanUserFootprintByUserIdAndSubject(Long userId, Subject subject) {
        if (null == userId || !AfentiUtils.isSubjectAvailable(subject)) return null;
        return afentiLearningPlanUserFootprintPersistence.findByUserIdAndSubject(userId, subject);
    }

    @Override
    public Map<Long, AfentiLearningPlanUserFootprint> loadAfentiLearningPlanUserFootprintByUserIdsAndSubject(Collection<Long> userIds, Subject subject) {
        if (CollectionUtils.isEmpty(userIds) || !AfentiUtils.isSubjectAvailable(subject)) return Collections.emptyMap();
        return afentiLearningPlanUserFootprintPersistence.findByUserIdsAndSubject(userIds, subject);
    }

    @Override
    public List<AfentiLearningPlanUserRankStat> loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(Long userId, String newBookId) {
        if (null == userId || StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return afentiLearningPlanUserRankStatPersistence.queryByUserIdAndNewBookId(userId, newBookId);
    }

    @Override
    public int loadUserTotalStar(Long userId, Subject subject) {
        if (null == userId) return 0;
        return afentiLearningPlanUserRankStatPersistence.queryTotalStar(userId, subject);
    }

//    @Override
//    public List<AfentiProductInfo> loadAllAfentiProductsIncludeOffline() {
//        return afentiProductInfoPersistence.loadAllAfentiProductInfos();
//    }

    @Override
    public List<AfentiQuizResult> loadAfentiQuizResultByUserIdAndNewBookId(@CacheParameter("UID") Long userId, @CacheParameter("NBID") String newBookId) {
        if (null == userId || StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return afentiQuizResultDao.queryByUserIdAndNewBookId(userId, newBookId);
    }

    @Override
    public List<AfentiQuizStat> loadAfentiQuizStatByUserId(@CacheParameter("UID") Long userId) {
        if (null == userId) return Collections.emptyList();
        return afentiQuizStatDao.queryByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> loadPurchaseInfos(StudentDetail student) {
        return afentiOperationalInfoService.loadUserPurchaseInfos(student);
    }

//    @Override
//    public List<AfentiProductInfo> loadAllAfentiProductsByModifyPrice(StudentDetail user) {
//        if (user == null || user.getClazz() == null) return Collections.emptyList();
//
//        List<AfentiProductInfo> productInfos = loadAllAfentiProductsIncludeOffline()
//                .stream()
//                .filter(p -> StringUtils.equals(p.getStatus(), "ONLINE"))
//                .collect(Collectors.toList());
//
//        StudentGrayFunctionManager sgfm = grayFunctionManagerClient.getStudentGrayFunctionManager();
//        for (AfentiProductInfo afentiProductInfo : productInfos) {
//            if (afentiProductInfo.getProductServiceType() == OrderProductServiceType.AfentiMath) {
//                // 升价灰度，上海阿分题数学全部调价，深圳学校尾号单号的阿分题数学价格调整
//                if (sgfm.isWebGrayFunctionAvailable(user, "AfentiMathRaisePrice", "Task34312") ||
//                        sgfm.isWebGrayFunctionAvailable(user, "AfentiMathRaisePrice", "Task38295")) {
//                    switch (afentiProductInfo.getPeriod()) {
//                        case 15:
//                            afentiProductInfo.setPrice(new BigDecimal(19));
//                            afentiProductInfo.setOrignalPrice(new BigDecimal(23));
//                            break;
//                        case 30:
//                            afentiProductInfo.setPrice(new BigDecimal(36));
//                            afentiProductInfo.setOrignalPrice(new BigDecimal(45));
//                            break;
//                        case 90:
//                            afentiProductInfo.setPrice(new BigDecimal(99));
//                            afentiProductInfo.setOrignalPrice(new BigDecimal(135));
//                            break;
//                        case 365:
//                            afentiProductInfo.setPrice(new BigDecimal(369));
//                            afentiProductInfo.setOrignalPrice(new BigDecimal(548));
//                            break;
//                        default:
//                            break;
//                    }
//                }
//                // 升价灰度，重庆学校尾号单号的阿分题数学价格调整
//                if (sgfm.isWebGrayFunctionAvailable(user, "AfentiMathRaisePrice", "Task38296")) {
//                    switch (afentiProductInfo.getPeriod()) {
//                        case 15:
//                            afentiProductInfo.setPrice(new BigDecimal(19));
//                            afentiProductInfo.setOrignalPrice(new BigDecimal(23));
//                            break;
//                        case 30:
//                            afentiProductInfo.setPrice(new BigDecimal(36));
//                            afentiProductInfo.setOrignalPrice(new BigDecimal(45));
//                            break;
//                        case 90:
//                            afentiProductInfo.setPrice(new BigDecimal(80));
//                            afentiProductInfo.setOrignalPrice(new BigDecimal(135));
//                            break;
//                        case 365:
//                            afentiProductInfo.setPrice(new BigDecimal(300));
//                            afentiProductInfo.setOrignalPrice(new BigDecimal(548));
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            }
//        }
//        return productInfos;
//    }


    @Override
    public Map<String, List<Video>> loadQuestionVideosByQuestionIds(Collection<String> ids) {
        // 大数据提供的接口获取数据
        Map<String, List<Video>> results = new HashMap<>();
        try {
            Map<String, List<String>> videoMaps = afentiWrongQuestionServiceClient.getAfentiWrongQuestionService().loadVideoByQuestionIds((List<String>) ids);
            if (MapUtils.isNotEmpty(videoMaps)) {
                for (Map.Entry<String, List<String>> entry : videoMaps.entrySet()) {
                    String qid = entry.getKey();
                    List<String> videoIds = entry.getValue();
                    if (StringUtils.isNotBlank(qid) && CollectionUtils.isNotEmpty(videoIds)) {
                        // 获取视频
                        Map<String, Video> videoMap = videoLoaderClient.loadVideoIncludeDisabled(videoIds);
                        if (MapUtils.isNotEmpty(videoMap)) {
                            List<Video> videoList = videoMap.values().stream().filter(video -> !video.isDeleted()).collect(Collectors.toList());
                            if (CollectionUtils.isNotEmpty(videoList)) {
                                // 去重复根据URL
                                List<Video> videos = new ArrayList<>();
                                for (Video video : videoList) {
                                    Video v = videos.stream().filter(vi -> StringUtils.equals(vi.getVideoUrl(), video.getVideoUrl()))
                                            .findFirst().orElse(null);
                                    if (v != null) {
                                        continue;
                                    }
                                    videos.add(video);
                                }
                                results.put(qid, videos);
                            }
                        }
                    }
                }
            }
            return results;
        } catch (Exception e) {
            logger.error("AfentiLoader loadQuestionVideosByQuestionIds error", e);
            return results;
        }
    }

    @Override
    public Map<String,Video> loadPreparationVideosByBookId(String bookId) {
        List<String> videoIds = AfentiPreparationVideoUtils.getVideosByBookId(bookId);

        if(CollectionUtils.isNotEmpty(videoIds)) {
            return videoLoaderClient.loadVideoByDocIds(videoIds);
        }

        return Collections.emptyMap();
    }
}
