/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.afenti.consumer;

import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.afenti.api.AfentiLoader;
import com.voxlearning.utopia.service.afenti.api.entity.*;
import com.voxlearning.utopia.service.question.api.entity.Video;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.afenti.api.constant.UtopiaAfentiConstants.AVAILABLE_SUBJECT;

public class AfentiLoaderClient implements AfentiLoader {

    @ImportService(interfaceClass = AfentiLoader.class) private AfentiLoader afentiLoader;

    public List<WrongQuestionLibrary> loadWrongQuestionLibraryByUserIdAndSubject(Long userId, Subject subject) {
        if (userId == null || !AVAILABLE_SUBJECT.contains(subject)) return Collections.emptyList();
        return afentiLoader.loadWrongQuestionLibraryByUserIdAndSubject(userId, subject);
    }

    public UserAfentiStats loadUserAfentiStats(Long userId) {
        return null == userId ? null : afentiLoader.loadUserAfentiStats(userId);
    }

    public List<AfentiLearningPlanPushExamHistory> loadAfentiLearningPlanPushExamHistoryByUserId(Long userId) {
        if (null == userId) return Collections.emptyList();
        return afentiLoader.loadAfentiLearningPlanPushExamHistoryByUserId(userId);
    }

    public List<AfentiLearningPlanPushExamHistory> loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(Long userId, String newBookId) {
        if (null == userId || StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return afentiLoader.loadAfentiLearningPlanPushExamHistoryByUserIdAndNewBookId(userId, newBookId);
    }

    public List<AfentiLearningPlanUnitRankManager> loadAfentiLearningPlanUnitRankManagerByNewBookId(String newBookId) {
        if (StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return afentiLoader.loadAfentiLearningPlanUnitRankManagerByNewBookId(newBookId);
    }

    @Override
    public List<String> loadBookIdsWithUnitRankManagerForPreparation() {
        return afentiLoader.loadBookIdsWithUnitRankManagerForPreparation();
    }

    public List<AfentiLearningPlanUserBookRef> loadAfentiLearningPlanUserBookRefByUserIdAndSubject(Long userId, Subject subject) {
        if (userId == null || !AVAILABLE_SUBJECT.contains(subject)) return Collections.emptyList();
        return afentiLoader.loadAfentiLearningPlanUserBookRefByUserIdAndSubject(userId, subject);
    }

    public AfentiLearningPlanUserFootprint loadAfentiLearningPlanUserFootprintByUserIdAndSubject(Long userId, Subject subject) {
        if (userId == null || !AVAILABLE_SUBJECT.contains(subject)) return null;
        return afentiLoader.loadAfentiLearningPlanUserFootprintByUserIdAndSubject(userId, subject);
    }

    public Map<Long, AfentiLearningPlanUserFootprint> loadAfentiLearningPlanUserFootprintByUserIdsAndSubject(Collection<Long> userIds, Subject subject) {
        if (CollectionUtils.isEmpty(userIds) || !AVAILABLE_SUBJECT.contains(subject)) return Collections.emptyMap();
        return afentiLoader.loadAfentiLearningPlanUserFootprintByUserIdsAndSubject(userIds, subject);
    }

    public List<AfentiLearningPlanUserRankStat> loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(Long userId, String newBookId) {
        if (null == userId || StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return afentiLoader.loadAfentiLearningPlanUserRankStatByUserIdAndNewBookId(userId, newBookId);
    }

    public int loadUserTotalStar(Long userId, Subject subject) {
        if (userId == null || !AVAILABLE_SUBJECT.contains(subject)) return 0;
        return afentiLoader.loadUserTotalStar(userId, subject);
    }

//    public List<AfentiProductInfo> loadAllAfentiProductsIncludeOffline() {
//        return afentiLoader.loadAllAfentiProductsIncludeOffline();
//    }
//
//    public List<AfentiProductInfo> loadAllAfentiProducts() {
//        return loadAllAfentiProductsIncludeOffline().stream()
//                .filter(p -> com.voxlearning.alps.util.StringUtils.equals(p.getStatus(), "ONLINE"))
//                .collect(Collectors.toList());
//    }

    /**
     * 灰度地区修改价格
     */
//    public List<AfentiProductInfo> loadAllAfentiProductsByModifyPrice(StudentDetail user) {
//        if (user == null || user.getClazz() == null) return Collections.emptyList();
//        int grade = user.getClazzLevelAsInteger();
//
//        List<AfentiProductInfo> productInfos = getAvalibleProductInfosForClassLevel(grade)
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

//    public List<AfentiProductInfo> getAvalibleProductInfos() {
//        return getAvalibleProductInfosForClassLevel(null);
//    }
//
//    public List<AfentiProductInfo> getAvalibleProductInfosForClassLevel(Integer level) {
//        List<AfentiProductInfo> productInfos = loadAllAfentiProducts();
//
//        // FIXME 悟空识字只开放一二年级 by Wyc 2016-05-06
//        if (null == level || level > 2) {
//            productInfos = productInfos.stream().filter(p -> !p.getType().equals(OrderProductServiceType.WukongShizi.name())).collect(Collectors.toList());
//        }
//
//        // FIXME  悟空拼音只开放一二年级 by Wyc 2016-05-09
//        if (null == level || level > 2) {
//            productInfos = productInfos.stream().filter(p -> !p.getType().equals(OrderProductServiceType.WukongPinyin.name())).collect(Collectors.toList());
//        }
//
//        return productInfos;
//    }
    @Override
    public List<AfentiQuizResult> loadAfentiQuizResultByUserIdAndNewBookId(Long userId, String newBookId) {
        if (null == userId || StringUtils.isBlank(newBookId)) return Collections.emptyList();
        return afentiLoader.loadAfentiQuizResultByUserIdAndNewBookId(userId, newBookId);
    }

    @Override
    public List<AfentiQuizStat> loadAfentiQuizStatByUserId(@CacheParameter("UID") Long userId) {
        if (null == userId) return Collections.emptyList();
        return afentiLoader.loadAfentiQuizStatByUserId(userId);
    }

    // // TODO: 2017/1/9 此方法底层是读取缓存的内容，暂时先放这里
    @Override
    public List<Map<String, Object>> loadPurchaseInfos(StudentDetail student) {
        if (student == null) return Collections.emptyList();
        return afentiLoader.loadPurchaseInfos(student);
    }

    public Map<String, List<Video>> loadQuestionVideosByQuestionIds(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return afentiLoader.loadQuestionVideosByQuestionIds(ids);
    }

    @Override
    public Map<String,Video> loadPreparationVideosByBookId(String bookId) {
        return afentiLoader.loadPreparationVideosByBookId(bookId);
    }
}
