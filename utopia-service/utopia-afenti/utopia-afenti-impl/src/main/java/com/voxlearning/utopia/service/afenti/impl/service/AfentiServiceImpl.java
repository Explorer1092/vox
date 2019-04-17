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

import com.voxlearning.alps.annotation.meta.BookStatus;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.AfentiService;
import com.voxlearning.utopia.service.afenti.api.constant.*;
import com.voxlearning.utopia.service.afenti.api.data.AfentiBook;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUnitRankManager;
import com.voxlearning.utopia.service.afenti.api.entity.AfentiLearningPlanUserBookRef;
import com.voxlearning.utopia.service.afenti.impl.queue.AfentiQueueProducer;
import com.voxlearning.utopia.service.afenti.impl.service.internal.AfentiOperationalInfoService;
import com.voxlearning.utopia.service.afenti.impl.util.AfentiUtils;
import com.voxlearning.utopia.service.afenti.impl.util.FetchRecommendedBook;
import com.voxlearning.utopia.service.afenti.impl.util.GenAfentiRankUtils;
import com.voxlearning.utopia.service.afenti.impl.util.UtopiaAfentiSpringBean;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.api.client.IFeeCourseLoaderClient;
import com.voxlearning.utopia.service.question.api.client.ITestMethodLoaderClient;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.question.consumer.SolutionMethodLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.data.RankType.BASE;
import static com.voxlearning.utopia.data.RankType.SUMMARIZE;
import static com.voxlearning.utopia.service.content.api.constant.BookCatalogType.SECTION;
import static com.voxlearning.utopia.service.content.api.constant.BookCatalogType.UNIT;

@Named
@ExposeService(interfaceClass = AfentiService.class)
public class AfentiServiceImpl extends UtopiaAfentiSpringBean implements AfentiService {

    @Inject private AfentiQueueProducer afentiQueueProducer;

    @Inject private AfentiLoaderImpl afentiLoader;
    @Inject private FetchRecommendedBook fetchRecommendedBook;
    @Inject private AfentiOperationalInfoService afentiOperationalInfoService;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private IFeeCourseLoaderClient iFeeCourseLoaderClient;
    @Inject private AsyncAfentiCacheServiceImpl asyncAfentiCacheService;
    @Inject private ITestMethodLoaderClient iTestMethodLoaderClient;
    @Inject private SolutionMethodLoaderClient solutionMethodLoaderClient;

    private static String afentiVideoDefImg = "https://oss-image.17zuoye.com/wonderland/2018/02/12/20180212154936796160.png";

    @Override
    public void completeGuide(Long studentId, final String name) {
        if (studentId == null || !AfentiGuide.isValid(name)) return;
        AfentiGuide guide = AfentiGuide.valueOf(name);
        userAfentiGuidePersistence.completeGuide(studentId, guide);
    }

    @Override
    public MapMessage getAfentiLastestOrderStatus(Long userId, Subject subject) {
        StudentDetail student = studentLoaderClient.loadStudentDetail(userId);
        if (student == null) return MapMessage.errorMessage("用户不能为空");
        if (student.isInPaymentBlackListRegion()) return MapMessage.successMessage().set("isDisplayBuyMsg", false);

        OrderProductServiceType productType = AfentiUtils.getOrderProductServiceType(subject);
        if (productType == null) return MapMessage.errorMessage();
        List<UserActivatedProduct> products = userOrderLoaderClient.loadUserActivatedProductList(userId);
        products = products.stream().filter(p -> OrderProductServiceType.safeParse(p.getProductServiceType()) == productType)
                .collect(Collectors.toList());

        String buyContent;
        String buyBtnText;
        UseAppStatus useAppStatus;

        if (CollectionUtils.isEmpty(products)) {
            buyContent = MessageFormat.format("{0}在阿分题{1}的3节免费试用课已结束，马上开通让孩子继续学习。",
                    student.fetchRealnameIfBlankId(), subject.getValue());
            buyBtnText = "开通学习";
            useAppStatus = UseAppStatus.NotBuy;
        } else {
            UserActivatedProduct product = products.stream()
                    .filter(o -> o.getServiceEndTime() != null)
                    .filter(o -> o.getServiceEndTime().after(new Date()))
                    .sorted((o1, o2) -> Long.compare(o2.getCreateDatetime().getTime(), o1.getCreateDatetime().getTime()))
                    .findFirst().orElse(null);
            if (null == product) {
                buyContent = MessageFormat.format("{0}的阿分题{1}课程已经过期，马上续费让孩子继续学习。",
                        student.fetchRealnameIfBlankId(), subject.getValue());
                buyBtnText = "续费学习";
                useAppStatus = UseAppStatus.Expired;
            } else {
                long remaindDay = Math.max(DateUtils.dayDiff(product.getServiceEndTime(), new Date()), 1);
                if (remaindDay <= 7) {
                    buyContent = MessageFormat.format("{0}的阿分题{1}还有{2}天就要到期了，马上续费让孩子继续学习。",
                            student.fetchRealnameIfBlankId(), subject.getValue(), remaindDay);
                    buyBtnText = "续费学习";
                    useAppStatus = UseAppStatus.SoonExpire;
                } else {
                    buyContent = MessageFormat.format("{0}的阿分题{1}课程还有{2}天到期。",
                            student.fetchRealnameIfBlankId(), subject.getValue(), remaindDay);
                    buyBtnText = "续费学习";
                    useAppStatus = UseAppStatus.Using;
                }
            }
        }

        return MapMessage.successMessage().set("useAppStatus", useAppStatus).set("buyContent", buyContent)
                .set("buyBtnText", buyBtnText).set("isDisplayBuyMsg", true);
    }

    @Override
    public boolean hasValidAfentiOrder(Long userId, Subject subject) {
        if (userId == null) return false;
        OrderProductServiceType type = AfentiUtils.getOrderProductServiceType(subject);
        UserActivatedProduct product = userOrderLoaderClient.loadUserActivatedProductList(userId).stream()
                .filter(t -> OrderProductServiceType.safeParse(t.getProductServiceType()) == type)
                .findFirst()
                .orElse(null);

        if (product == null) return false;
        Date endTime = product.getServiceEndTime() == null ? new Date(0) : product.getServiceEndTime();
        return endTime.getTime() >= System.currentTimeMillis();
    }

    @Override
    public MapMessage fetchAfentiBook(Long userId, Subject subject, AfentiLearningType type) {
        if (null == userId || !AfentiUtils.isSubjectAvailable(subject)) return null;

        NewBookProfile oldBook = null;
        // 获取正在使用中的教材
        AfentiLearningPlanUserBookRef ref = afentiLearningPlanUserBookRefPersistence.findByUserIdAndSubject(userId, subject)
                .stream().filter(r -> Boolean.TRUE.equals(r.getActive())).filter(r -> r.getType() == type).findFirst().orElse(null);
        boolean changeFlag = false;
        if (ref != null) {
            oldBook = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singletonList(ref.getNewBookId())).values().stream().findFirst().orElse(null);
            if (type != AfentiLearningType.castle) {
                AfentiBook afentiBook = new AfentiBook();
                afentiBook.createTime = new Date(ref.fetchCreateTimestamp());
                afentiBook.userId = userId;
                afentiBook.active = ref.getActive() != null && ref.getActive();
                afentiBook.book = oldBook;
                return MapMessage.successMessage().add("book", afentiBook).add("changeFlag", changeFlag);
            }

            if (oldBook != null && !oldBook.isDeletedTrue() && BookStatus.ONLINE.name().equals(oldBook.getStatus())) {
                // 语文和英语不是最新版 直接切
                if ((subject == Subject.CHINESE || subject == Subject.ENGLISH) &&
                        (GenAfentiRankUtils.chineseInvalidBookIds.contains(oldBook.getId()) || oldBook.getLatestVersion() != 1)) {
                    afentiLearningPlanUserBookRefPersistence.inactivate(userId, subject, ref.getNewBookId(), type);
                    changeFlag = true;
                } else {
                    AfentiBook afentiBook = new AfentiBook();
                    afentiBook.createTime = new Date(ref.fetchCreateTimestamp());
                    afentiBook.userId = userId;
                    afentiBook.active = ref.getActive() != null && ref.getActive();
                    afentiBook.book = oldBook;
                    return MapMessage.successMessage().add("book", afentiBook).add("changeFlag", changeFlag);
                }

            } else {
                afentiLearningPlanUserBookRefPersistence.inactivate(userId, subject, ref.getNewBookId(), type);
                changeFlag = true;
            }
        }

        // 初始化教材
        NewBookProfile init = null;
        if (AfentiLearningType.castle == type && subject == Subject.ENGLISH && oldBook != null) { //英语的先去旧书的中找这个系列的书
            init = fetchRecommendedBook.fetchRecommendedBookByOldBook(oldBook);
        }

        if (init == null) {
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(userId);
            if (AfentiLearningType.castle == type) {
                init = fetchRecommendedBook.fetchRecommendedBookForSystemClazz(subject, clazz);
            } else {
                init = fetchRecommendedBook.fetchRecommendedBookForSystemClazz(subject, clazz, type);
            }
        }

        if (init != null) {
            // 将原来正在使用的教材变成未使用状态
            afentiLearningPlanUserBookRefPersistence.inactivate(userId, subject, type);
            // 将当前教材变成使用中状态
            if (!afentiLearningPlanUserBookRefPersistence.activate(userId, subject, init.getId(), type)) {
                afentiLearningPlanUserBookRefPersistence.persist(AfentiLearningPlanUserBookRef
                        .newInstance(userId, true, init.getId(), subject, type));
            }
            AfentiBook afentiBook = new AfentiBook();
            afentiBook.book = init;
            afentiBook.createTime = new Date();
            afentiBook.userId = userId;
            afentiBook.active = true;
            return MapMessage.successMessage().add("book", afentiBook).add("changeFlag", changeFlag);
        }
        return MapMessage.errorMessage();
    }

    // 获取换书历史
    @Override
    public List<AfentiBook> fetchAfentiBooks(Long userId, Subject subject, AfentiLearningType type) {
        if (null == userId || !AfentiUtils.isSubjectAvailable(subject)) return Collections.emptyList();

        // 获取所有换书记录并排序
        List<AfentiLearningPlanUserBookRef> refs = afentiLearningPlanUserBookRefPersistence
                .findByUserIdAndSubject(userId, subject).stream().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(refs)) {
            refs = refs.stream().filter(r -> r.getType() == type).collect(Collectors.toList());
        }
        Collections.sort(refs, new AfentiLearningPlanUserBookRef.UPDATETIME_DESC_ACTIVE_PRIOR());
        Set<String> bookIds = refs.stream().map(AfentiLearningPlanUserBookRef::getNewBookId).collect(Collectors.toSet());

        // 获取教材
        Map<String, NewBookProfile> books = newContentLoaderClient.loadBooks(bookIds);

        // 返回值
        List<AfentiBook> result = new LinkedList<>();
        for (AfentiLearningPlanUserBookRef ref : refs) {
            NewBookProfile book = books.get(ref.getNewBookId());
            // 如果教材不存在或者已经下线，如果这本书还在使用中状态，将其状态改变
            if (book == null || !book.isOnline()) {
                if (Boolean.TRUE.equals(ref.getActive())) {
                    afentiLearningPlanUserBookRefPersistence.inactivate(userId, subject, ref.getNewBookId());
                }
                continue;
            }
            if (subject == Subject.CHINESE && book.getLatestVersion() != 1) {
                continue;
            }
            String bookName = StringUtils.defaultString(book.getName());
            if (UtopiaAfentiConstants.AFENTI_BOOK_BLACK_LIST.contains(book.getId())) {
                bookName = StringUtils.replace(bookName, "外研版-新标准", "阿分题教材");
                bookName = StringUtils.replace(bookName, "新概念英语1(培训用)", "阿分题教材");
            }
            book.setName(bookName);
            AfentiBook afentiBook = new AfentiBook();
            afentiBook.createTime = new Date(ref.fetchCreateTimestamp());
            afentiBook.userId = userId;
            afentiBook.active = ref.getActive() != null && ref.getActive();
            afentiBook.book = book;
            result.add(afentiBook);
        }
        return result;
    }

    @Override
    public MapMessage generateAfentiRank(Collection<String> bookIds, Subject subject) {
        if (CollectionUtils.isEmpty(bookIds)) return MapMessage.successMessage();
        if (!AfentiUtils.isSubjectAvailable(subject)) return MapMessage.errorMessage();

        for (String bookId : bookIds) {
            // 如果这本教材已经生成了阿分题关卡，就不再生成了
            List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader.loadAfentiLearningPlanUnitRankManagerByNewBookId(bookId).stream()
                    .filter(e -> e.getType() == AfentiLearningType.castle).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(ranks)) continue;

            // 获取教材的单元id以及单元内的知识点个数
            Map<String, Integer> ukpcs = newKnowledgePointLoaderClient.loadUnitIdAndKnowledgePointIdCountByBookId(bookId);

            // 去掉知识点个数不足的单元，英语为3，数学为1
            int limit = subject == Subject.ENGLISH ? 3 : 1;
            Iterator<Map.Entry<String, Integer>> it = ukpcs.entrySet().iterator();
            while (it.hasNext()) if (it.next().getValue() < limit) it.remove();
            if (MapUtils.isEmpty(ukpcs)) continue;

            int unitRank = 1;
            for (String unitId : ukpcs.keySet()) {
                // 计算生成关卡数量，英语3个知识点一个关卡，数学1个知识点一个关卡，如需要向上取整，在加上一个总结关卡，总数不超过15
                int kpc = ukpcs.get(unitId);
                int count;
                switch (subject) {
                    case ENGLISH: {
                        count = Math.min(15, new BigDecimal(kpc).divide(new BigDecimal(3), 0, BigDecimal.ROUND_CEILING).intValue() + 1);
                        break;
                    }
                    case MATH: {
                        count = Math.min(15, kpc + 1);
                        break;
                    }
                    default:
                        count = 0;
                }
                for (int i = 1; i <= count; i++) {
                    AfentiLearningPlanUnitRankManager rank = AfentiLearningPlanUnitRankManager.newInstance();
                    rank.setNewBookId(bookId);
                    rank.setNewUnitId(unitId);
                    rank.setRank(i);
                    rank.setUnitRank(unitRank);
                    rank.setRankType(i == count ? SUMMARIZE.toString() : BASE.toString());
                    rank.setSubject(subject);
                    rank.setRuntimeMode(RuntimeMode.current().getLevel());
                    rank.setType(AfentiLearningType.castle);
                    ranks.add(rank);
                }
                unitRank++;
            }
            afentiLearningPlanUnitRankManagerPersistence.persist(ranks);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage generateAfentiRankForMath(Collection<String> bookIds) {
        if (CollectionUtils.isEmpty(bookIds)) return MapMessage.successMessage();

        Map<String, List<NewBookCatalog>> bookId_units_map = newContentLoaderClient.loadChildren(bookIds, UNIT);
        for (String bookId : bookIds) {
            // 如果这本教材已经生成了阿分提关卡，就不再生成了
            List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader.loadAfentiLearningPlanUnitRankManagerByNewBookId(AfentiUtils.getNewBookId(bookId));
            if (CollectionUtils.isNotEmpty(ranks)) continue;

            // 获取教材的单元
            List<NewBookCatalog> units = bookId_units_map.get(bookId).stream()
                    .filter(u -> !u.isDeletedTrue())
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(units)) continue;
            Collections.sort(units, ((o1, o2) -> Integer.compare(o1.getRank(), o2.getRank())));
            List<String> unitIds = units.stream().map(NewBookCatalog::getId).collect(Collectors.toList());

            // 获取单元下的section
            Map<String, List<NewBookCatalog>> unitId_section_map = newContentLoaderClient.loadChildren(unitIds, SECTION);
            int unitRank = 1;
            for (NewBookCatalog unit : units) {
                int rankCount = 0;
                List<NewBookCatalog> sections = unitId_section_map.get(unit.getId());
                if (CollectionUtils.isEmpty(sections)) continue;
                // 根据section下的知识点数 计算关卡
                for (NewBookCatalog section : sections) {
                    Integer npCount = newKnowledgePointLoaderClient.loadKnowledgePointWithFeatureCountBySectionId(section.getId());
                    int sectionRankCount = 0;
                    if (npCount == null || npCount <= 0) {
                        // 知识点为空 判断是否有考法解法 如果有生成一个关卡 没有不生成关卡
                        Map<String, List<TestMethod>> testMap = iTestMethodLoaderClient.loadTestMethodByBookCatalogIds(Collections.singleton(section.getId()));
                        if (MapUtils.isNotEmpty(testMap) && CollectionUtils.isNotEmpty(testMap.get(section.getId()))) {
                            sectionRankCount = 1;
                        } else {
                            Map<String, List<SolutionMethod>> solutionMap = solutionMethodLoaderClient.loadSolutionMethodByBookCatalogIds(Collections.singleton(section.getId()));
                            if (MapUtils.isNotEmpty(solutionMap) && CollectionUtils.isNotEmpty(solutionMap.get(section.getId()))) {
                                sectionRankCount = 1;
                            }
                        }
                    } else {
                        // 根据知识点数量判断关卡的数量  BK_10200001565806这本教材因为知识点数量过少 单独处理
                        if (StringUtils.equals(bookId, "BK_10200001565806")) {
                            if (npCount <= 4) {
                                sectionRankCount = 2 * npCount;
                            } else if (npCount > 4) {
                                sectionRankCount = 8;
                            }
                        } else {
                            if (npCount == 1) {
                                sectionRankCount = 2;
                            } else if (npCount > 1 && npCount <= 4) {
                                sectionRankCount = 4;
                            } else if (npCount > 4) {
                                sectionRankCount = 5;
                            }
                        }
                    }
                    if (sectionRankCount > 0) {
                        for (int i = rankCount; i < sectionRankCount + rankCount; i++) {
                            AfentiLearningPlanUnitRankManager rank = AfentiLearningPlanUnitRankManager.newInstance();
                            rank.setNewBookId(AfentiUtils.getNewBookId(bookId));
                            rank.setNewUnitId(unit.getId());
                            rank.setNewSectionId(section.getId());
                            rank.setRank(i + 1);
                            rank.setUnitRank(unitRank);
                            rank.setRankType(BASE.toString());
                            rank.setSubject(Subject.MATH);
                            rank.setRuntimeMode(RuntimeMode.current().getLevel());
                            rank.setType(AfentiLearningType.castle);
                            ranks.add(rank);
                        }
                        rankCount = rankCount + sectionRankCount;
                    }
                }
                unitRank++;
            }
            afentiLearningPlanUnitRankManagerPersistence.persist(ranks);
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage generateAfentiRankForChinese(Collection<String> bookIds) {
        if (CollectionUtils.isEmpty(bookIds)) return MapMessage.successMessage();

        Map<String, List<NewBookCatalog>> bookId_units_map = newContentLoaderClient.loadChildren(bookIds, UNIT);
        for (String bookId : bookIds) {
            if (GenAfentiRankUtils.chineseInvalidBookIds.contains(bookId)) {
                continue;
            }
            List<NewBookCatalog> units = bookId_units_map.get(bookId).stream()
                    .filter(u -> !u.isDeletedTrue())
                    .filter(u -> !GenAfentiRankUtils.chineseNoRankUnits.contains(u.getId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(units)) continue;
            Collections.sort(units, ((o1, o2) -> Integer.compare(o1.getRank(), o2.getRank())));
            List<String> unitIds = units.stream().map(NewBookCatalog::getId).collect(Collectors.toList());

            // 如果这本教材已经生成了阿分题关卡，就不再生成了
            List<AfentiLearningPlanUnitRankManager> ranks = afentiLoader.loadAfentiLearningPlanUnitRankManagerByNewBookId(AfentiUtils.getNewBookId(bookId));
            if (CollectionUtils.isNotEmpty(ranks)) continue;

            Map<String, List<NewBookCatalog>> unitId_section_map = newContentLoaderClient.loadChildren(unitIds, SECTION);
            int unitRank = 1;
            for (NewBookCatalog unit : units) {
                int rankCount = 0;
                List<NewBookCatalog> sections = unitId_section_map.get(unit.getId());
                if (CollectionUtils.isEmpty(sections)) continue;
                for (NewBookCatalog section : sections) {
                    if (GenAfentiRankUtils.chineseNoRankSections.contains(section.getId())) {
                        continue;
                    }
                    // 每个课时4个关卡
                    int sectionCount = 4;
                    for (int i = rankCount; i < rankCount + sectionCount; i++) {
                        AfentiLearningPlanUnitRankManager rank = AfentiLearningPlanUnitRankManager.newInstance();
                        rank.setNewBookId(AfentiUtils.getNewBookId(bookId));
                        rank.setNewUnitId(unit.getId());
                        rank.setNewSectionId(section.getId());
                        rank.setRank(i + 1);
                        rank.setUnitRank(unitRank);
                        rank.setRankType(BASE.toString());
                        rank.setSubject(Subject.CHINESE);
                        rank.setRuntimeMode(RuntimeMode.current().getLevel());
                        rank.setType(AfentiLearningType.castle);
                        ranks.add(rank);
                    }
                    rankCount = rankCount + sectionCount;
                }
                // 复习关卡
                AfentiLearningPlanUnitRankManager rank = AfentiLearningPlanUnitRankManager.newInstance();
                rank.setNewBookId(AfentiUtils.getNewBookId(bookId));
                rank.setNewUnitId(unit.getId());
                rank.setNewSectionId("-1");
                rank.setRank(rankCount + 1);
                rank.setUnitRank(unitRank);
                rank.setRankType(SUMMARIZE.toString());
                rank.setSubject(Subject.CHINESE);
                rank.setRuntimeMode(RuntimeMode.current().getLevel());
                rank.setType(AfentiLearningType.castle);
                ranks.add(rank);
                unitRank++;
            }
            afentiLearningPlanUnitRankManagerPersistence.persist(ranks);
        }
        return MapMessage.successMessage();
    }

    @Override
    public boolean addUserPurchaseInfo(StudentDetail studentDetail, PurchaseType purchaseType, Date createDate) {
        return afentiOperationalInfoService.addUserPurchaseInfo(studentDetail, purchaseType, createDate);
    }

    @Override
    public boolean addUserRewardInfo(StudentDetail studentDetail, Integer integral) {
        if (integral > 0) {
            return afentiOperationalInfoService.addUserRewardInfo(studentDetail, integral);
        }
        return false;
    }

    @Override
    public MapMessage wrongQuestionPlusIndex(Long studentId, Subject subject, Integer clazzLevel, Integer termType, String bookSeries) {
        if (studentId == null || subject == null || clazzLevel == null || termType == null || StringUtils.isBlank(bookSeries))
            return MapMessage.errorMessage("参数传递错误");

        // 获取课程ID,查询课程详细信息
        List<FeeCourse> feeCourses = iFeeCourseLoaderClient.loadFeeCoursesByConditions(subject.getId(), clazzLevel, bookSeries, termType);
        if (CollectionUtils.isEmpty(feeCourses))
            return MapMessage.errorMessage("找不到对应视频课程");
        FeeCourse feeCourse = feeCourses.get(0);


        // 获取相应课程的第一节免费课时
        List<FeeCourseTopic> feeCourseTopics = iFeeCourseLoaderClient.loadFeeCourseTopicsByCourseId(feeCourse.getId());
        if (CollectionUtils.isEmpty(feeCourseTopics))
            return MapMessage.errorMessage("找不到对应视频课程");

        FeeCourseTopic feeCourseTopic = feeCourseTopics.get(0);
        if (feeCourseTopic == null || CollectionUtils.isEmpty(feeCourseTopic.getLessons()))
            return MapMessage.errorMessage("找不到对应视频课程");

        FeeCourseTopic.FeeCourseLesson lesson = feeCourseTopic.getLessons().get(0);
        Map<String, FeeCourseContent> feeCourseContentMap = iFeeCourseLoaderClient.loadFeeCourseContentsIncludeDisabled(Collections.singleton(lesson.getId()));

        if (feeCourseContentMap == null || !feeCourseContentMap.containsKey(lesson.getId()))
            return MapMessage.errorMessage("找不到对应视频课程");

        MapMessage result = MapMessage.successMessage();

        // 免费课时详细信息
        FeeCourseContent feeCourseContent = feeCourseContentMap.get(lesson.getId());

        result.add("freeVideoId", feeCourseContent.getId());
        result.add("freeVideoName", feeCourseContent.getName());
        result.add("freeVideoThumbUrl", feeCourseContent.getVideoThumbUrl());
        result.add("freeVideoUrl", feeCourseContent.getVideoUrl());

        return result;
    }

    @Override
    public MapMessage getCurrentCourseVideoList(Long studentId, Subject subject, Integer clazzLevel, Integer termType, String bookSeries) {
        if (studentId == null || subject == null || clazzLevel == null || termType == null || StringUtils.isBlank(bookSeries))
            return MapMessage.errorMessage("参数传递错误");

        // 获取课程ID,查询课程详细信息
        List<FeeCourse> feeCourses = iFeeCourseLoaderClient.loadFeeCoursesByConditions(subject.getId(), clazzLevel, bookSeries, termType);
        if (CollectionUtils.isEmpty(feeCourses))
            return MapMessage.errorMessage("找不到对应视频课程");
        FeeCourse feeCourse = feeCourses.get(0);

        List<FeeCourseTopic> feeCourseTopics = iFeeCourseLoaderClient.loadFeeCourseTopicsByCourseId(feeCourse.getId());
        if (CollectionUtils.isEmpty(feeCourseTopics))
            return MapMessage.errorMessage("找不到对应视频课程");

        // 视频课程基本信息
        MapMessage result = MapMessage.successMessage();
        result.add("courseInfo", new MapMessage()
                .set("id", feeCourse.getId())
                .set("clazzLevel", feeCourse.getClazzLevel())
                .set("termType", feeCourse.getTermType())
                .set("subject", subject)
                .set("imgThumbUrl", feeCourse.getImgUrl())
                .set("name", feeCourse.getName())
                .set("status", feeCourse.getStatus())
                .set("lessonNum", feeCourse.getLessonNum())
                .set("buyerCount", asyncAfentiCacheService.getAfentiCourseBuyerCountManager().getCurrentBuyerCount()));

        // 课时列表信息整理(加入免费课时判断字段)
        List<MapMessage> feeCourseTopicListMap = new ArrayList<>();
        Boolean isFirstVideo = true;        // 每个课程第一课时免费
        for (FeeCourseTopic feeCourseTopic : feeCourseTopics) {
            MapMessage feeCourseTopicMap = new MapMessage();
            feeCourseTopicMap.set("id", feeCourseTopic.getId());
            feeCourseTopicMap.set("name", feeCourseTopic.getName());
            List<MapMessage> lessonsMap = new ArrayList<>();

            for (FeeCourseTopic.FeeCourseLesson lesson : feeCourseTopic.getLessons()) {
                MapMessage lessonMap = new MapMessage();
                lessonMap.set("id", lesson.getId());
                lessonMap.set("name", lesson.getName());
                if (StringUtils.isBlank(lesson.getImgUrl())) {
                    lessonMap.set("imgUrl", afentiVideoDefImg);
                    lessonMap.set("isOpened", false);
                } else {
                    lessonMap.set("imgUrl", lesson.getImgUrl());
                    lessonMap.set("isOpened", true);
                }
                if (isFirstVideo) {
                    lessonMap.set("isFree", isFirstVideo);
                    isFirstVideo = false;
                } else
                    lessonMap.set("isFree", isFirstVideo);
                lessonsMap.add(lessonMap);
            }
            feeCourseTopicMap.set("lessons", lessonsMap);
            feeCourseTopicListMap.add(feeCourseTopicMap);
        }

        result.add("catalog", feeCourseTopicListMap);
        return result;
    }

    @Override
    public MapMessage getCurrentLessonVideoDetail(String lessonId, Boolean hasOpened) {
        Map<String, FeeCourseContent> feeCourseContentMap = iFeeCourseLoaderClient.loadFeeCourseContentsIncludeDisabled(Collections.singleton(lessonId));
        if (feeCourseContentMap == null || feeCourseContentMap.get(lessonId) == null)
            return MapMessage.errorMessage("找不到对应视频课时");
        MapMessage result = MapMessage.successMessage();
        FeeCourseContent feeCourseContent = feeCourseContentMap.get(lessonId);

        // 如果该用户没有开通服务且该视频不是免费视频时退出
        if (!hasOpened && feeCourseContent.getRank() != 1)
            return MapMessage.errorMessage("没有访问该视频的权限");

        result.add("id", feeCourseContent.getId());
        result.add("name", feeCourseContent.getName());
        result.add("nodeType", feeCourseContent.getNodeType());
        result.add("order", feeCourseContent.getRank());
        result.add("videoThumbUrl", feeCourseContent.getVideoThumbUrl());
        result.add("videoUrl", feeCourseContent.getVideoUrl());
        return result;
    }

    @Override
    public Boolean isAddedVideoViewRecord(Long studentId, String lessonId) {
        if (studentId == null || StringUtils.isBlank(lessonId))
            return true;

        return asyncAfentiCacheService.getAfentiVideoCourseViewRecordCacheManager().isAddedViewRecord(studentId, lessonId);
    }

    @Override
    public void addVideoViewRecord(Long studentId, String lessonId) {
        if (studentId == null || StringUtils.isBlank(lessonId))
            return;

        asyncAfentiCacheService.getAfentiVideoCourseViewRecordCacheManager().addViewRecord(studentId, lessonId);
    }

    @Override
    public void sendMessage(Message message) {
        if (message != null) {
            afentiQueueProducer.getProducer().produce(message);
        }
    }

    @Override
    public void sendLoginMessage(Message message) {
        if (message != null) {
            afentiQueueProducer.getLoginProducer().produce(message);
        }
    }
}
