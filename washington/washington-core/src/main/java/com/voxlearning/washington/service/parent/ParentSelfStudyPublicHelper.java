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

package com.voxlearning.washington.service.parent;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.consumer.ClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.UserBookLoaderClient;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.PicListenCommonService;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.utopia.service.question.consumer.PicListenLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 家长通-h5接口和原生接口公用方法
 * 咳咳 学生端他么也来用了。。。类名不改了。。
 * fixme 不知道是否合理,先这么干了
 *
 * @author jiangpeng
 * @since 2017-02-09 下午6:10
 **/
@Named
public class ParentSelfStudyPublicHelper {

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    public static final String FIELD_PIC_LISTEN_SDK_BOOK_ID = "pic_listen_sdk_book_id";

    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @Inject
    private PicListenLoaderClient picListenLoaderClient;

    @Inject
    private NewContentLoaderClient newContentLoaderClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject
    private DeprecatedGroupLoaderClient groupLoaderClient;

    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;

    @Inject
    private NewClazzBookLoaderClient newClazzBookLoaderClient;

    @Inject
    private TextBookManagementLoaderClient textBookManagementLoaderClient;


    @Inject
    private UserBookLoaderClient userBookLoaderClient;

    @Inject
    private ClazzBookLoaderClient clazzBookLoaderClient;

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject private UserAuthQueryServiceClient userAuthQueryServiceClient;

    @ImportService(interfaceClass = PicListenCommonService.class)
    private PicListenCommonService picListenCommonService;

    /**
     * 任务是否完成
     * 此方法没有验证本次作业是否有task
     *
     * @param homeworkId
     * @param studentId
     * @return
     */
    public Boolean taskIsFinish(String homeworkId, Long studentId) {
        Integer taskStatus = asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                .HomeworkReportPicListenTaskCacheManager_getTaskStatus(homeworkId, studentId)
                .take();
        return taskStatus != null && taskStatus == 1;
    }


    /**
     * maps的结构为
     *
     * @param maps    [
     *                {
     *                "sentence_id":123123123,
     *                "unit_id":"ldflsdfisldfkjd"
     *                },
     *                {
     *                "sentence_id":123123123,
     *                "unit_id":"ldflsdfisldfkjd"
     *                },
     *                {
     *                "sentence_id":123123123,
     *                "unit_id":"ldflsdfisldfkjd"
     *                }
     *                ]
     * @param percent 需要的百分比 目前要求的是75%  ,就传75
     * @return
     */
    public Boolean showTask(List<Map> maps, Integer percent) {

        long sentenceCount = maps.stream().map(t -> SafeConverter.toLong(t.get("sentence_id"))).distinct().count();
        if (sentenceCount == 0)
            return false;
        LinkedHashMap<Long, String> sentence2PicListenIdMap = getSentence2PicListenIdMap(maps);
        return sentence2PicListenIdMap.size() / sentenceCount * 100 >= percent;
    }


    /**
     * @param maps [
     *             {
     *             "sentence_id":123123123,
     *             "unit_id":"ldflsdfisldfkjd"
     *             },
     *             {
     *             "sentence_id":123123123,
     *             "unit_id":"ldflsdfisldfkjd"
     *             },
     *             {
     *             "sentence_id":123123123,
     *             "unit_id":"ldflsdfisldfkjd"
     *             }
     *             ]
     * @return
     */
    public LinkedHashMap<Long, String> getSentence2PicListenIdMap(List<Map> maps) {
        String currentUnitId = "";
        LinkedHashMap<Long, Set<String>> sentence2UnitSetMap = new LinkedHashMap<>();
        List<Long> sentenceIdList = new ArrayList<>();
        for (Map map : maps) {
            long sentenceId = SafeConverter.toLong(map.get("sentence_id"));
            String unitId = SafeConverter.toString(map.get("unit_id"));
            if (StringUtils.isBlank(unitId) || sentenceId == 0)
                continue;
            if (StringUtils.isBlank(currentUnitId))
                currentUnitId = unitId;
            Set<String> unitIdSet = sentence2UnitSetMap.get(sentenceId);
            if (unitIdSet == null)
                unitIdSet = new HashSet<>();
            unitIdSet.add(unitId);
            sentence2UnitSetMap.put(sentenceId, unitIdSet);
            sentenceIdList.add(sentenceId);
        }

        Map<Long, List<Map<String, String>>> sentence2PicMapListMap = picListenLoaderClient.loadUnitAndPicListenIds(sentenceIdList);
        LinkedHashMap<Long, String> sentence2PicListenIdMap = new LinkedHashMap<>();
        for (Long sentenceId : sentenceIdList) {
            List<Map<String, String>> picMapList = sentence2PicMapListMap.get(sentenceId);
            if (CollectionUtils.isEmpty(picMapList))
                continue;
            Set<String> rightUnitIdSet = sentence2UnitSetMap.get(sentenceId);
            if (CollectionUtils.isEmpty(rightUnitIdSet))
                continue;
            for (Map<String, String> picMap : picMapList) {
                String unitId = SafeConverter.toString(picMap.get("unitId"));
                String picListenId = SafeConverter.toString(picMap.get("picListenId"));
                if (rightUnitIdSet.contains(unitId))
                    sentence2PicListenIdMap.put(sentenceId, picListenId);
            }
        }
        return sentence2PicListenIdMap;
    }

    /**
     * @param maps [
     *             {
     *             "sentence_id":123123123,
     *             "unit_id":"ldflsdfisldfkjd"
     *             },
     *             {
     *             "sentence_id":123123123,
     *             "unit_id":"ldflsdfisldfkjd"
     *             },
     *             {
     *             "sentence_id":123123123,
     *             "unit_id":"ldflsdfisldfkjd"
     *             }
     *             ]
     * @return
     */
    public NewBookProfile getPicListenBookByOneUnit(List<Map> maps) {
        String unitId = maps.stream().findAny().map(t -> SafeConverter.toString(t.get("unit_id"))).orElse(null);
        if (unitId == null)
            return null;
        NewBookProfile bookProfile = null;
        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unit != null) {
            List<NewBookCatalogAncestor> ancestors = unit.getAncestors();
            NewBookCatalogAncestor bookAncestor = ancestors.stream().filter(t -> t.getNodeType().equals(BookCatalogType.BOOK.name())).findFirst().orElse(null);
            if (bookAncestor != null)
                bookProfile = newContentLoaderClient.loadBook(bookAncestor.getId());
        }
        return bookProfile;
    }


    private Boolean picListenBookShow(NewBookProfile newBookProfile, Boolean isPreview, String sys) {
        return textBookManagementLoaderClient.picListenBookShow(newBookProfile.getId(), isPreview, sys);
    }

    private static final Map<Integer, String> englishDefaultTermBookIdMap = new HashMap<>();
    private static final Map<Integer, String> chineseDefaultTermBookIdMap = new HashMap<>();

    static {
        englishDefaultTermBookIdMap.put(1, "BK_10300000265057");
        englishDefaultTermBookIdMap.put(2, "BK_10300000264607");
        chineseDefaultTermBookIdMap.put(1, "BK_10100002547303");
        chineseDefaultTermBookIdMap.put(2, "BK_10100002540831");
    }

    public Set<String> picListenDefaultShelfBooks(User parent, String sys, String version) {
        Objects.requireNonNull(parent);
        Map<String, DayRange> bookId2PayEndDateMap = picListenCommonService.parentBuyBookPicListenLastDayMap(parent.getId(), false);
        Set<String> allBookIdSet = new HashSet<>(bookId2PayEndDateMap.keySet());

        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
        if (CollectionUtils.isEmpty(studentParentRefs)) {
            Integer nowTermType = nowTermType();
            allBookIdSet.add(englishDefaultTermBookIdMap.get(nowTermType));
            allBookIdSet.add(chineseDefaultTermBookIdMap.get(nowTermType));
        } else {
            List<Long> studentIdList = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
            Map<Long, StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIdList);
            studentDetails.values().forEach(t -> allBookIdSet.addAll(studentHomeworkBookIdSet(t, sys)));
        }
        Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(parent);

        return allBookIdSet.stream().filter(t -> textBookManagementLoaderClient.picListenShow(t, sys, parentAuth)).collect(Collectors.toSet());
    }

    private static List<String> defaultEnglishBooKId = new ArrayList<>();
    private static List<String> defaultChineseBooKId = new ArrayList<>();

    static {
        defaultEnglishBooKId.add("BK_10300000587874");
        defaultEnglishBooKId.add("BK_10300000586129");
    }

    /**
     * @param parentId
     * @return
     */
    public Integer parentRecomandBook(Long parentId, List<Map<String, Object>> resultList) {
        Long studentId = 0L;
        Integer clazzLevel = null;
        List<String> bookIdList = new ArrayList<>();
        StudentParentRef studentParentRef = parentLoaderClient.loadParentStudentRefs(parentId).stream().findAny().orElse(null);
        if (studentParentRef == null) {
            bookIdList.addAll(defaultEnglishBooKId);
            bookIdList.addAll(defaultChineseBooKId);
        } else {
            studentId = studentParentRef.getStudentId();
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            clazzLevel = studentDetail == null ? null : studentDetail.getClazzLevelAsInteger();
            List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(studentId, false);
            List<Long> groupIds = groupMappers.stream()
                    .filter(t -> t.getSubject() == Subject.ENGLISH || t.getSubject() == Subject.CHINESE)
                    .map(GroupMapper::getId).collect(Collectors.toList());
            List<NewClazzBookRef> clazzBookRefList = newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList();
            //英语教材
            List<NewClazzBookRef> englishBookRefs = clazzBookRefList.stream().filter(t -> Subject.ENGLISH.name().equals(t.getSubject())).collect(Collectors.toList());
            NewClazzBookRef lastEnglishBookRef = getLastUseOne(englishBookRefs);
            if (lastEnglishBookRef != null) {
                String bookId = lastEnglishBookRef.getBookId();
                TextBookManagement textBook = textBookManagementLoaderClient.getTextBook(bookId);
                if (textBook != null) {
//                    if ("外研版".equals(textBook.getShortPublisherName()) || "人教版".equals(textBook.getShortPublisherName())) {
                    if ("外研版".equals(textBook.getShortPublisherName()) && textBook.picListenBookNeedPay()) {
                        //取当前教材,以及当前学期下一个学期的教材
                        bookIdList.add(textBook.getBookId());
                        String nextClazzTermBookId = getNextClazzLevelAndTermBookId(textBook.getBookId());
                        if (nextClazzTermBookId != null)
                            bookIdList.add(nextClazzTermBookId);
                    } else {
                        bookIdList.addAll(defaultBookIdsByClazzLevel(Subject.ENGLISH, clazzLevel));
                    }
                } else
                    bookIdList.addAll(defaultBookIdsByClazzLevel(Subject.ENGLISH, clazzLevel));
            } else
                bookIdList.addAll(defaultBookIdsByClazzLevel(Subject.ENGLISH, clazzLevel));

//            // 语文教材
//            List<NewClazzBookRef> chineseBookRefs = clazzBookRefList.stream().filter(t -> Subject.CHINESE.name().equals(t.getSubject())).collect(Collectors.toList());
//            NewClazzBookRef lastChineseBookRef = getLastUseOne(chineseBookRefs);
//            if (lastChineseBookRef != null) {
//                String bookId = lastChineseBookRef.getBookId();
//                TextBookManagement textBook = textBookManagementLoaderClient.getTextBook(bookId);
//                if (textBook != null) {
//                    if ("人教版".equals(textBook.getShortPublisherName())) {
//                        //取当前教材,以及当前学期下一个学期的教材
//                        bookIdList.add(textBook.getBookId());
//                        String nextClazzTermBookId = getNextClazzLevelAndTermBookId(textBook.getBookId());
//                        if (nextClazzTermBookId != null)
//                            bookIdList.add(nextClazzTermBookId);
//                    } else{
//                        bookIdList.addAll(defaultBookIdsByClazzLevel(Subject.CHINESE, clazzLevel));
//                    }
//                } else
//                    bookIdList.addAll(defaultBookIdsByClazzLevel(Subject.CHINESE, clazzLevel));
//            } else
//                bookIdList.addAll(defaultBookIdsByClazzLevel(Subject.CHINESE, clazzLevel));
        }

        Set<String> purchasedBookIdSet = picListenCommonService.parentBuyBookPicListenLastDayMap(parentId, false).keySet();
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);
        Long finalStudentId = studentId;
        resultList.addAll(bookProfileMap.values().stream().map(t -> bookMap(t, purchasedBookIdSet.contains(t.getId()), finalStudentId)).collect(Collectors.toList()));
        return clazzLevel;
    }

    public List<Map<String, Object>> bookListMapForActivity(Long parentId, List<String> bookIdList) {
        Set<String> purchasedBookIdSet = picListenCommonService.parentBuyBookPicListenLastDayMap(parentId, false).keySet();
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);
        return bookProfileMap.values().stream().map(t -> bookMap(t, purchasedBookIdSet.contains(t.getId()), 0L)).collect(Collectors.toList());
    }

    //FIXME 代码重复稍后做修改
    public List<Map<String, Object>> recommendPicListenBook(StudentDetail studentDetail, User parent, String sys) {
        Boolean parentAuth = picListenCommonService.userIsAuthForPicListen(parent);
        List<String> bookIdList = new ArrayList<>();
        List<NewClazzBookRef> clazzBookRefList = loadStudentHomewordBookRefList(studentDetail.getId(), Subject.CHINESE, Subject.ENGLISH);
        //英语教材
        bookIdList.addAll(getStudentDefaultSubjectBook(studentDetail, sys, parentAuth, clazzBookRefList, Subject.ENGLISH));

        // 语文教材
        bookIdList.addAll(getStudentDefaultSubjectBook(studentDetail, sys, parentAuth, clazzBookRefList, Subject.CHINESE));

        Set<String> purchasedBookIdSet = picListenCommonService.parentBuyBookPicListenLastDayMap(parent.getId(), false).keySet();
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList.stream().distinct().collect(Collectors.toList()));
        List<Map<String, Object>> mapList = new ArrayList<>();
        bookIdList.forEach( t -> {
            NewBookProfile newBookProfile = bookProfileMap.get(t);
            if (newBookProfile == null)
                return;
            mapList.add(bookMap(newBookProfile, purchasedBookIdSet.contains(t), studentDetail.getId()));
        });
        return mapList;
    }


    public List<String> getStudentDefaultSubjectBook(StudentDetail studentDetail, String sys, Boolean parentAuth, List<NewClazzBookRef> clazzBookRefList, Subject subject){
        List<String> bookIdList = new ArrayList<>();
        Integer clazzLevel = getClazzLevelForRecBook(studentDetail);

        if (clazzBookRefList == null) {
            clazzBookRefList = loadStudentHomewordBookRefList(studentDetail.getId(), subject);
        }
        Term currentTerm = SchoolYear.newInstance().currentTerm();
        List<NewClazzBookRef> englishBookRefs = clazzBookRefList.stream().filter(t -> subject.name().equals(t.getSubject())).collect(Collectors.toList());
        NewClazzBookRef lastEnglishBookRef = getLastUseOne(englishBookRefs);
        if (lastEnglishBookRef != null) {
            String bookId = lastEnglishBookRef.getBookId();
            if (textBookManagementLoaderClient.picListenShow(bookId, sys, parentAuth)){   //当前教材支持点读机
                NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
                if (newBookProfile != null) {
                    List<String> towBookId = new ArrayList<>(2);
                    String nextClazzTermBookId = getNextClazzLevelAndTermBookId(bookId);
                    if (newBookProfile.getTermType().equals(currentTerm.getKey())) {
                        //取当前教材,以及当前学期下一个学期的教材
                        towBookId.add(bookId);
                        if (StringUtils.isNotBlank(nextClazzTermBookId))
                            towBookId.add(nextClazzTermBookId);
                    } else {
                        if (StringUtils.isBlank(nextClazzTermBookId)){
                            towBookId.add(bookId);
                        }else {
                            String nextNextTermBookId = getNextClazzLevelAndTermBookId(nextClazzTermBookId);
                            if (StringUtils.isNotBlank(nextNextTermBookId)) {
                                towBookId.add(nextClazzTermBookId);
                                towBookId.add(nextNextTermBookId);
                            }else
                                towBookId.add(bookId);
                        }

                    }
                    towBookId.forEach(t -> {
                        if (t != null && textBookManagementLoaderClient.picListenShow(t, sys, parentAuth))
                            bookIdList.add(t);
                    });
                }

            } else
                bookIdList.addAll(defaultBookId(clazzLevel, subject));
        } else
            bookIdList.addAll(defaultBookId(clazzLevel, subject));
        if (CollectionUtils.isEmpty(bookIdList))
            bookIdList.addAll(defaultBookId(clazzLevel, subject));
        return bookIdList;
    }

    private Integer getClazzLevelForRecBook(StudentDetail studentDetail) {
        if (studentDetail.isJuniorStudent()
                || (studentDetail.getClazz()!= null && studentDetail.getClazz().isTerminalClazz()))
            return 7;
        if (studentDetail.isInfantStudent())
            return 1;
        if (studentDetail.getClazz() == null)
            return 3;
        return studentDetail.getClazzLevelAsInteger();
    }

    private List<NewClazzBookRef> loadStudentHomewordBookRefList(Long studentId, Subject ... subjects){
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(studentId, false);
        List<Subject> subjectsList = Arrays.asList(subjects);
        List<Long> groupIds = groupMappers.stream()
                .filter(t -> subjectsList.contains(t.getSubject()))
                .map(GroupMapper::getId).collect(Collectors.toList());
        return newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList();
    }


    public List<String> defaultBookId(Integer clazzLevel, Subject subject){
        if (clazzLevel == null)
            clazzLevel = 3;
        if (subject == Subject.ENGLISH) {
            switch (clazzLevel) {
                case 0:
                case 1:
                    return Arrays.asList("BK_10300001722068", "BK_10300001807246");
                case 2:
                    return Arrays.asList("BK_10300001724304", "BK_10300001808902");
                case 3:
                    return Arrays.asList("BK_10300000265057", "BK_10300000264607");
                case 4:
                    return Arrays.asList("BK_10300000266810", "BK_10300000267732");
                case 5:
                    return Arrays.asList("BK_10300000263225", "BK_10300000261609");
                case 6:
                case 7:
                    return Arrays.asList("BK_10300000262593", "BK_10300000260149");
                default:
                    return Collections.emptyList();
            }
        }
        if (subject == Subject.CHINESE){
            switch (clazzLevel) {
                case 0:
                case 1:
                    return Arrays.asList("BK_10100002551703", "BK_10100001675679");
                case 2:
                    return Arrays.asList("BK_10100000004683", "BK_10100000003482");
                case 3:
                    return Arrays.asList("BK_10100000013407", "BK_10100000008693");
                case 4:
                    return Arrays.asList("BK_10100000005225", "BK_10100000006594");
                case 5:
                    return Arrays.asList("BK_10100000007851", "BK_10100000002989");
                case 6:
                case 7:
                    return Arrays.asList("BK_10100000011387", "BK_10100000012766");
                default:
                    return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private List<String> defaultBookIdsByClazzLevel(Subject subject, Integer clazzLevel) {
        if (clazzLevel == null || clazzLevel == 0) {
            if (subject == Subject.ENGLISH)
                return defaultEnglishBooKId;
            if (subject == Subject.CHINESE)
                return defaultChineseBooKId;
            return Collections.emptyList();
        }
        List<String> bookIdList = new ArrayList<>();
        List<TextBookManagement> subjectClazzLevelBooks = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(subject, clazzLevel);
        TextBookManagement currentTermRenjiaoBook = subjectClazzLevelBooks.stream().filter(t -> t.getShortPublisherName().equals("外研版")
                && SchoolYear.newInstance().currentTerm().getKey() == t.getTermType())
                .filter(TextBookManagement::picListenBookNeedPay)
                .findAny().orElse(null);
        if (currentTermRenjiaoBook == null)
            bookIdList.addAll(defaultChineseBooKId);
        else {
            bookIdList.add(currentTermRenjiaoBook.getBookId());
            String nextClazzLevelAndTermBookId = getNextClazzLevelAndTermBookId(currentTermRenjiaoBook.getBookId());
            if (nextClazzLevelAndTermBookId != null)
                bookIdList.add(nextClazzLevelAndTermBookId);
        }
        return bookIdList;
    }


    public Map<String, Object> bookMap(NewBookProfile bookProfile, Boolean isPurchased, Long studentId) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", bookProfile.getShortName());
        map.put("img", bookProfile.getImgUrl());
        map.put("isPurchased", isPurchased);
        map.put("book_need_pay",  textBookManagementLoaderClient.picListenBookNeedPay(bookProfile.getId()));
        map.put("subject", Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        map.put("url", payBookProductDetailPage(studentId, bookProfile.getId()));
        map.put("book_id", bookProfile.getId());
        return map;
    }

    String payBookProductDetailPage(Long studentId, String bookId) {
        TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookId);
        return "/parentMobile/ucenter/shoppinginfo.vpage?sid=" + studentId + "&productType=" + OrderProductServiceType.PicListenBook.name() + "&book_id=" + bookId +
                "&sdk=" + sdkInfo.getSdkType().name() + "&sdk_book_id=" + sdkInfo.getSdkBookIdV2();
    }

    private String getNextClazzLevelAndTermBookId(String bookId) {
        TextBookManagement textBook = textBookManagementLoaderClient.getTextBook(bookId);
        if (textBook == null)
            return null;
        Subject subject = Subject.fromSubjectId(textBook.getSubjectId());
        TextBookMapper.ClazzAndTerm currentClazzAndTerm = TextBookMapper.ClazzAndTerm.newInstance(textBook.getClazzLevel(), textBook.getTermType());
        TextBookMapper.ClazzAndTerm nextClazzAndTerm = currentClazzAndTerm.next();
        if (nextClazzAndTerm == null)
            return null;
        List<TextBookManagement> nextClazzTermSameClazzLevelBooks
                = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(subject, nextClazzAndTerm.getClazzLevel());
        if (CollectionUtils.isNotEmpty(nextClazzTermSameClazzLevelBooks)) {
            List<TextBookManagement> samePublisherBooks = nextClazzTermSameClazzLevelBooks.stream()
                    .filter(t -> t.getShortPublisherName().equals(textBook.getShortPublisherName())
                            && t.getTermType().equals(nextClazzAndTerm.getTermType()))
                    .collect(Collectors.toList());
            List<NewBookProfile> samePublisherBookProfiles
                    = newContentLoaderClient.loadBooks(samePublisherBooks.stream().map(TextBookManagement::getBookId)
                    .collect(Collectors.toList())).values().stream().collect(Collectors.toList());
            NewBookProfile currentBookProfile = newContentLoaderClient.loadBook(bookId);
            if (currentBookProfile == null)
                return null;
            NewBookProfile bookProfile = samePublisherBookProfiles.stream().filter(t -> t.getSeriesId().equals(currentBookProfile.getSeriesId())).findFirst().orElse(null);
            if (bookProfile != null)
                return bookProfile.getId();
        }
        return null;
    }

    /**
     * 获取到学生的乱七暴躁教材
     * http://project.17zuoye.net/redmine/issues/40079
     * 去掉了只要人教版和外研版的限制
     * 并且根据当前月份自动找学期
     *
     * @return
     */
    Set<String> studentHomeworkBookIdSet(StudentDetail studentDetail, String sys) {
        Integer nowTerm = nowTermType();
        Set<String> bookIdSet = new HashSet<>();
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(studentDetail.getId(), false);
        List<Long> groupIds = groupMappers.stream()
                .filter(t -> t.getSubject() == Subject.ENGLISH || t.getSubject() == Subject.CHINESE)
                .map(GroupMapper::getId).collect(Collectors.toList());
        List<NewClazzBookRef> newClazzBookRefs = newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList();
        //英语教材
        List<NewClazzBookRef> englishBookRefs = newClazzBookRefs.stream().filter(t -> Subject.ENGLISH.name().equals(t.getSubject())).collect(Collectors.toList());
        NewClazzBookRef englishBookRef = getLastUseOne(englishBookRefs);
        String currentEnglishBookId;
        if (englishBookRef == null) {
            Clazz clazz = studentDetail.getClazz();
            if (clazz != null) {
                currentEnglishBookId = newContentLoaderClient.initializeClazzBook(Subject.ENGLISH, clazz.getClazzLevel(), studentDetail.getStudentSchoolRegionCode());
            } else {
                currentEnglishBookId = null;
            }

        } else {
            currentEnglishBookId = englishBookRef.getBookId();
        }
        if (currentEnglishBookId != null) {
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(currentEnglishBookId);
            if (newBookProfile != null) {
                if (newBookProfile.getTermType() == nowTerm.intValue())
                    bookIdSet.add(newBookProfile.getId());
                else {
                    List<NewBookProfile> newBookProfileList = newContentLoaderClient.loadBooksByClassLevelWithSortByRegionCode(Subject.ENGLISH, 0, ClazzLevel.parse(newBookProfile.getClazzLevel()));
                    List<NewBookProfile> namiBookList = newContentLoaderClient.loadNamiBookBySubject(Subject.ENGLISH, ClazzLevel.parse(newBookProfile.getClazzLevel()));
                    NewBookProfile term2Book = newBookProfileList.stream().filter(t ->
                            t.getSeriesId().equals(newBookProfile.getSeriesId())
                                    && t.getTermType() == nowTerm.intValue() && t.getStatus().equals("ONLINE")
                                    && picListenBookShow(t, false, sys)).findFirst().orElse(null);
                    if (term2Book == null) {
                        term2Book = namiBookList.stream().filter(t ->
                                t.getSeriesId().equals(newBookProfile.getSeriesId())
                                        && t.getTermType() == nowTerm.intValue()
                                        && picListenBookShow(t, false, sys)).findFirst().orElse(null);
                    }
                    if (term2Book != null)
                        bookIdSet.add(term2Book.getId());
                }
            }

        }

        //语文教材
        List<NewClazzBookRef> chineseBookRefs = newClazzBookRefs.stream().filter(t -> Subject.CHINESE.name().equals(t.getSubject())).collect(Collectors.toList());
        NewClazzBookRef chineseBookRef = getLastUseOne(chineseBookRefs);
        String currentChineseBookId;
        if (chineseBookRef == null) {
            Clazz clazz = studentDetail.getClazz();
            if (clazz != null) {
                currentChineseBookId = newContentLoaderClient.initializeClazzBook(Subject.CHINESE, clazz.getClazzLevel(), studentDetail.getStudentSchoolRegionCode());
            } else {
                currentChineseBookId = null;
            }

        } else {
            currentChineseBookId = chineseBookRef.getBookId();
        }
        if (currentChineseBookId != null)
            bookIdSet.add(currentChineseBookId);
        return bookIdSet;
    }


    private Integer nowTermType() {
        return SchoolYear.newInstance().currentTerm().getKey();
    }

    NewClazzBookRef getLastUseOne(List<NewClazzBookRef> newClazzBookRefs) {
        return newClazzBookRefs.stream().sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime())).findFirst().orElse(null);
    }


    /**
     * 加了1小时的缓存
     * @param parentId
     * @return
     */
    public Boolean isParentAuth(Long parentId) {
        String key = CacheKeyGenerator.generateCacheKey("parentAuth",new String[]{"pid"},new Object[]{parentId});
        CacheObject<Object> cacheObject = VendorCache.getVendorPersistenceCache().get(key);
        if (cacheObject == null || cacheObject.getValue() == null){
            Boolean isParentAuth = innerIsParentAuth(parentId);
            VendorCache.getVendorPersistenceCache().set(key, 3600, isParentAuth);
            return isParentAuth;
        }else {
            return SafeConverter.toBoolean(cacheObject.getValue());
        }
    }

    private Boolean innerIsParentAuth(Long parentId){
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        if (CollectionUtils.isEmpty(studentParentRefs))
            return false;
        List<Long> studentIdList = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
        for (Long sid :studentIdList) {
            if (userAuthQueryServiceClient.isAuthedStudent(sid, SchoolLevel.JUNIOR)) {
                return true;
            }
        }

        return false;
    }

    public NewBookProfile loadDefaultSelfStudyBook(StudentDetail student,
                                                   SelfStudyType selfStudyType, Boolean isPreview) {
        return loadDefaultSelfStudyBook(student, selfStudyType, isPreview, "all");
    }


    public NewBookProfile loadDefaultSelfStudyBook(StudentDetail student,
                                                   SelfStudyType selfStudyType,
                                                   Boolean isPreview, String sys) {
        //throw new UnsupportedOperationException();
        if (student == null || selfStudyType == SelfStudyType.UNKNOWN || selfStudyType == null) {
            return null;
        }
        //如果孩子之前选过教材,用之前选的教材
        NewBookProfile newBookProfile = null;
        UserSelfStudyBookRef userSelfStudyBookRef = userBookLoaderClient.loadUserSelfStudyBookRef(student.getId(), selfStudyType.getSubject(), selfStudyType.name());
        if (userSelfStudyBookRef != null) {
            newBookProfile = newContentLoaderClient.loadBook(userSelfStudyBookRef.getBookId());
        }
        if (newBookProfile == null) {
            if (student.getClazz() != null) {
                //孩子有班级的话,就根据之前的作业神马的取教材...
                List<GroupMapper> groups = groupLoaderClient.loadStudentGroups(student.getId(), false);
                GroupMapper targetSubjectGroup = groups.stream().filter(g -> g.getSubject() == selfStudyType.getSubject()).findFirst().orElse(null);
                if (targetSubjectGroup != null) {
                    Book book = clazzBookLoaderClient.loadGroupLatestEnglishHomeworkBook(targetSubjectGroup.getId());
                    if (book != null) {
                        newBookProfile = newContentLoaderClient.loadNewBookProfileByOldId(selfStudyType.getSubject(), book.getId());
                    }
                }
            }
        }

        //如果取出来的默认教材并不支持对应的自学类型,则返回自学类型对应的默认教材.
        //注意随声听的不需要判断!!! 随声听都有内容. 2016-10-18 因为版权问题,随声听教材也需要支持过滤 #33595
        Boolean hasContent = false;
        if (newBookProfile != null) {
            Map<String, Object> extraMap = newBookProfile.getExtras();
            switch (selfStudyType) {
                case PICLISTEN_ENGLISH:
                    if ("all".equals(sys))
                        hasContent = picListenBookShow(newBookProfile, isPreview, "ios") || picListenBookShow(newBookProfile, isPreview, "android");
                    else
                        hasContent = picListenBookShow(newBookProfile, isPreview, sys);
                    break;
                case TEXTREAD_CHINESE:
                    hasContent = textBookManagementLoaderClient.textReadBookShow(newBookProfile.getId(), sys);
                    break;
                case WALKMAN_ENGLISH:
                    hasContent = textBookManagementLoaderClient.walkManBookShow(newBookProfile.getId(), sys);
                    break;
                default:
                    break;
            }
        }

        if (!hasContent)
            newBookProfile = newContentLoaderClient.loadBook(selfStudyType.getDefaultBookId());

        return newBookProfile;
    }


    //增值应用 selfstudytype -> dayrange 的方法

    //阿分题小 U 提高版或者普通阿分题购买了，就算阿分题已购买，sst 还用老阿分题返回
    public Map<SelfStudyType, DayRange> moneySSTLastDayMap(Long studentId, Boolean keepExpired) {
        List<String> keys = SelfStudyType.orderProductServiceTypes.stream().map(Enum::name).collect(Collectors.toList());
        Map<String, AppPayMapper> userAppPaidStatusMap = userOrderLoaderClient.getUserAppPaidStatus(keys, studentId, false);

        Map<SelfStudyType, AppPayMapper> map = new HashMap<>();

        userAppPaidStatusMap.forEach((appKey , payMapper) -> {
            OrderProductServiceType type = OrderProductServiceType.safeParse(appKey);
            AppPayMapper userActiveAppPayMapper = getUserActiveAppPayMapper(type, payMapper, studentId);
            if (userActiveAppPayMapper == null)
                return;
            map.put(SelfStudyType.fromOrderType(type), userActiveAppPayMapper);
        });

        Map<SelfStudyType, DayRange> mmp = new HashMap<>();
        map.forEach((k, v) -> {
            if (keepExpired){
                if (v.isActive() || v.isExpire()){
                    DayRange dayRange = DayRange.newInstance(v.getExpireTime().getTime());
                    mmp.put(k, dayRange);
                }
            }else {
                if (v.isActive()){
                    DayRange dayRange = DayRange.newInstance(v.getExpireTime().getTime());
                    mmp.put(k, dayRange);
                }
            }
        });
        return mmp;
    }

    private AppPayMapper getUserActiveAppPayMapper(OrderProductServiceType type, AppPayMapper oldAppPayMapper, Long studentId){
        OrderProductServiceType improvedType = old2ImprovedType(type);
        AppPayMapper improvedMapper = null;
        if (improvedType != null) {
            improvedMapper = userOrderLoaderClient.getUserAppPaidStatus(improvedType.name(), studentId);
        }

        AppPayMapper userAppMapper;
        if (improvedMapper == null){
            userAppMapper = oldAppPayMapper;
        }else {
            if (safeIsInActive(improvedMapper)){
                userAppMapper = improvedMapper;
            }else if (safeIsInActive(oldAppPayMapper)){
                userAppMapper = oldAppPayMapper;
            }else
                userAppMapper = null;
        }
        return userAppMapper;
    }

    private boolean safeIsInActive(AppPayMapper appPayMapper){
        return appPayMapper != null && appPayMapper.isActive();
    }

    private OrderProductServiceType old2ImprovedType(OrderProductServiceType appKey) {
        switch (appKey){
            case AfentiExam: return OrderProductServiceType.AfentiExamImproved;
            case AfentiChinese: return OrderProductServiceType.AfentiChineseImproved;
            case AfentiMath: return OrderProductServiceType.AfentiMathImproved;
            default: return null;
        }
    }
}
