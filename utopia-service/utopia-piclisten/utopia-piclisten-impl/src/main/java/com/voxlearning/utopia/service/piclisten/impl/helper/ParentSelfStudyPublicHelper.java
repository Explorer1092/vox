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

package com.voxlearning.utopia.service.piclisten.impl.helper;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.calendar.DateRangePrecision;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.api.entity.UserSelfStudyBookRef;
import com.voxlearning.utopia.service.content.consumer.ClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.UserBookLoaderClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.impl.loader.TextBookManagementLoaderImpl;
import com.voxlearning.utopia.service.piclisten.support.PiclistenBookImgUtils;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.UserAuthQueryServiceClient;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookShelf;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.api.mapper.TextBookMapper;
import com.voxlearning.utopia.service.vendor.cache.VendorCache;
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
    private NewClazzBookLoaderClient newClazzBookLoaderClient;

    @Inject
    private TextBookManagementLoaderImpl textBookManagementLoader;

    @Inject
    private UserBookLoaderClient userBookLoaderClient;

    @Inject
    private ClazzBookLoaderClient clazzBookLoaderClient;

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject
    private UserAuthQueryServiceClient userAuthQueryServiceClient;

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    private ParentSelfStudyService parentSelfStudyService;


    private Boolean picListenBookShow(NewBookProfile newBookProfile, Boolean isPreview, String sys) {
        return textBookManagementLoader.picListenBookShow(newBookProfile.getId(), isPreview, sys);
    }


    /**
     * 获取这个用户购买的所有点读教材的到期时间
     *
     * @param parentId
     * @return
     */
    public Map<String, DayRange> parentBuyBookPicListenLastDayMap(Long parentId) {
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
            if (userActivatedProduct.getServiceEndTime().before(new Date()))
                return;
            map.put(orderProductItem.getAppItemId(), DayRange.newInstance(userActivatedProduct.getServiceEndTime().getTime(), DateRangePrecision.MILLISECOND));
        });

        return map;
    }


    @Deprecated
    public Set<String> offlineBookIdSet() {
        List<PageBlockContent> selfStudyAdConfigPageContentList = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("picListenOffline");
        if (CollectionUtils.isEmpty(selfStudyAdConfigPageContentList))
            return Collections.emptySet();
        PageBlockContent configPageBlockContent = selfStudyAdConfigPageContentList.stream().filter(p ->
                "offlineConfig".equals(p.getBlockName())
        ).findFirst().orElse(null);
        String configContent = configPageBlockContent == null ? "" : configPageBlockContent.getContent();

        Map<String, Object> configMap = JsonUtils.convertJsonObjectToMap(configContent);
        if (MapUtils.isEmpty(configMap))
            return Collections.emptySet();
        Object bookIdListObj = configMap.get("bookIdList");
        if (bookIdListObj == null || !(bookIdListObj instanceof List))
            return Collections.emptySet();
        List<String> bookIdList = (List<String>) bookIdListObj;
        if (CollectionUtils.isEmpty(bookIdList))
            return Collections.emptySet();
        return new HashSet<>(bookIdList);
    }

    private static final Map<Integer, String> englishDefaultTermBookIdMap = new HashMap<>();
    private static final Map<Integer, String> chineseDefaultTermBookIdMap = new HashMap<>();

    static {
        englishDefaultTermBookIdMap.put(1, "BK_10300000265057");
        englishDefaultTermBookIdMap.put(2, "BK_10300000264607");
        chineseDefaultTermBookIdMap.put(1, "BK_10100002547303");
        chineseDefaultTermBookIdMap.put(2, "BK_10100002540831");
    }

    public Set<String> picListenDefaultShelfBooks(Long parentId, String sys, String version) {
        Objects.requireNonNull(parentId);
        Map<String, DayRange> bookId2PayEndDateMap = parentBuyBookPicListenLastDayMap(parentId);
        Set<String> allBookIdSet = new HashSet<>(bookId2PayEndDateMap.keySet());

        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        if (CollectionUtils.isEmpty(studentParentRefs)) {
            Integer nowTermType = nowTermType();
            allBookIdSet.add(englishDefaultTermBookIdMap.get(nowTermType));
            allBookIdSet.add(chineseDefaultTermBookIdMap.get(nowTermType));
        } else {
            List<Long> studentIdList = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toList());
            Map<Long, StudentDetail> studentDetails = studentLoaderClient.loadStudentDetails(studentIdList);
            studentDetails.values().forEach(t -> allBookIdSet.addAll(studentHomeworkBookIdSet(t, sys)));
        }
        Boolean parentAuth = isParentAuth(parentId);

        return allBookIdSet.stream().filter(t -> textBookManagementLoader.picListenShow(t, sys, parentAuth)).collect(Collectors.toSet());
    }

    private static List<String> defaultEnglishBooKId = new ArrayList<>();
    private static List<String> defaultChineseBooKId = new ArrayList<>();

    static {
        defaultEnglishBooKId.add("BK_10300000587874");
        defaultEnglishBooKId.add("BK_10300000586129");
    }


    private static List<String> EXCLUDE_PUBLISHERS;

    static {
        String[] array = {"沪教版", "北京版", "外研版"};
        EXCLUDE_PUBLISHERS = Arrays.asList(array);

    }


    public List<String> getExcludePublishers() {

        return EXCLUDE_PUBLISHERS;
    }


    private List<String> distinctBook(List<String> bookIdList, List<String> existBookIds, Set<String> purchasedBookIdSet) {
        // Filter already exist book
        bookIdList.removeAll(existBookIds);

        // Remove purchases books
        bookIdList.removeAll(purchasedBookIdSet);

        if (bookIdList.size() > 1) {
            bookIdList = bookIdList.subList(0, 1);
        }

        return bookIdList;
    }

    /**
     * Wiki:  <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=37413618"> #37413618 3.1.2 </a>
     */
    public List<Map<String, Object>> recommendPicListenBook(StudentDetail studentDetail, Long parentId, String sys, String cdnBaseUrl) {
        Boolean parentAuth = isParentAuth(parentId);

        // 书架
        List<PicListenBookShelf> picListenBookShelfs = parentSelfStudyService.loadParentPicListenBookShelf(parentId);
        List<String> existBookIds = picListenBookShelfs.stream().map(PicListenBookShelf::getBookId).collect(Collectors.toList());
        List<TextBookManagement> selfBooks = new ArrayList<>(textBookManagementLoader.getTextBookByIds(existBookIds).values());

        // 购买过的
        Map<Integer, List<TextBookManagement>> purchaseBooks = getParentBuyBookPicListenBooks(parentId);
        Set<String> purchasedBookIdSet = new HashSet<>();
        purchaseBooks.forEach((subject, book) -> {
            purchasedBookIdSet.addAll(book.stream().map(TextBookManagement::getBookId).collect(Collectors.toSet()));
        });

        Map<String, DayRange> buyLastDayMap = parentBuyBookPicListenLastDayMap(parentId);

        List<String> enBookIdList = loadStudentRecommendSubjectBook(selfBooks, purchaseBooks, studentDetail, sys, parentAuth, Subject.ENGLISH); // English
        // English recommend
        List<String> bookIdList = new ArrayList<>(distinctBook(enBookIdList, existBookIds, purchasedBookIdSet));
        List<String> cnBookIdList = loadStudentRecommendSubjectBook(selfBooks, purchaseBooks, studentDetail, sys, parentAuth, Subject.CHINESE); // Chinese
        // Chinese recommend
        bookIdList.addAll(distinctBook(cnBookIdList, existBookIds, purchasedBookIdSet));


        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList.stream().distinct().collect(Collectors.toList()));
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (String bookId : bookIdList) {
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile == null) {
                continue;
            }
            Boolean bookNeedPay = textBookManagementLoader.picListenBookNeedPay(newBookProfile);
            mapList.add(bookMap(newBookProfile, bookNeedPay, buyLastDayMap.get(bookId) != null, buyLastDayMap.get(bookId)));
        }

        return mapList;
    }


    private List<String> loadStudentRecommendSubjectBook(List<TextBookManagement> selfBooks, Map<Integer, List<TextBookManagement>> purchaseBooks, StudentDetail studentDetail, String sys, Boolean parentAuth, Subject subject) {

        List<String> bookIdList = new ArrayList<>();

        // 当前学期
        Term currentTerm = SchoolYear.newInstance().currentTerm();

        Integer clazzLevel = getClazzLevelForRecBook(studentDetail);

        List<NewClazzBookRef> clazzBookRefList = loadStudentHomewordBookRefList(studentDetail.getId(), subject);
        List<NewClazzBookRef> bookRefs = clazzBookRefList.stream().filter(t -> subject.name().equals(t.getSubject())).collect(Collectors.toList());
        NewClazzBookRef lastBookRef = getLastUseOne(bookRefs);
        if (lastBookRef != null) {
            String bookId = lastBookRef.getBookId();
            bookIdList.add(bookId);

        } else {

            String seriesId;

            // English
            if (subject == Subject.ENGLISH) {


                List<TextBookManagement> purchBooks = purchaseBooks.get(subject.getId()) != null ? purchaseBooks.get(subject.getId()) : Collections.emptyList();
                // purchase book
                if (purchBooks.size() > 0) {
                    List<String> filterBookIds = getRelatedBookIds(purchBooks, subject, clazzLevel, currentTerm);
                    bookIdList.addAll(filterBookIds);
                } else {
                    // not purchase book
                    List<TextBookManagement> filterSelfBooks = selfBooks.stream().filter(b -> b.getSubjectId() != null && b.getSubjectId().equals(subject.getId())).collect(Collectors.toList());
                    if (filterSelfBooks.size() > 0) {

                        List<String> filterBookIds = getRelatedBookIds(filterSelfBooks, subject, clazzLevel, currentTerm);
                        bookIdList.addAll(filterBookIds);

                    } else {
                        // no piclistenbook
                        List<NewBookProfile> books;
                        // BKC_10300031638488( 一二年级)  BKC_10300008060767 (三年级以上) 人教PEP点读
                        if (clazzLevel < 3) {
                            seriesId = "BKC_10300031638488";
                            books = newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesId(subject, ClazzLevel.parse(clazzLevel), currentTerm, seriesId);
                        } else {
                            seriesId = "BKC_10300008060767";
                            books = newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesId(subject, ClazzLevel.parse(clazzLevel), currentTerm, seriesId);
                        }

                        if (books.size() > 0) {
                            bookIdList.addAll(books.stream().map(NewBookProfile::getId).collect(Collectors.toList()));
                        }

                    }

                }
            }
            // Chinese
            else if (subject == Subject.CHINESE) {
                // BKC_10100225556726 部编版
                seriesId = "BKC_10100225556726";
                List<NewBookProfile> books = newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesId(subject, ClazzLevel.parse(clazzLevel), currentTerm, seriesId);
                if (books.size() > 0) {
                    bookIdList.addAll(books.stream().map(NewBookProfile::getId).collect(Collectors.toList()));
                } else {


                    List<TextBookManagement> filterSelfBooks = selfBooks.stream().filter(b ->
                            b.getSubjectId() != null && b.getSubjectId().equals(subject.getId())
                    ).collect(Collectors.toList());
                    if (filterSelfBooks.size() > 0) {

                        List<String> filterBookIds = getRelatedBookIds(filterSelfBooks, subject, clazzLevel, currentTerm);
                        bookIdList.addAll(filterBookIds);

                    } else {
                        // BKC_10100000002119 人教点读
                        seriesId = "BKC_10100000002119";
                        books = newContentLoaderClient.loadBooksByClassLevelAndTermAndSeriesId(subject, ClazzLevel.parse(clazzLevel), currentTerm, seriesId);
                        if (books.size() > 0) {
                            bookIdList.addAll(books.stream().map(NewBookProfile::getId).collect(Collectors.toList()));
                        }
                    }
                }


            }


        }

        List<String> list = new ArrayList<>();

        // Exclude special publishers book
        List<String> excludePubs = getExcludePublishers();

        Map<String, TextBookManagement> bookManagementMap = textBookManagementLoader.getTextBookByIds(bookIdList);

        bookManagementMap.forEach((bookId, textBook) -> {
            if (!excludePubs.contains(textBook.getShortPublisherName())) {
                // and
                if (textBook.picListenSysSupport(sys) || textBook.walkManSysSupport(sys)) {
                    list.add(bookId);
                }
            }

        });

        return list;
    }

    private List<String> getRelatedBookIds(List<TextBookManagement> books, Subject subject, Integer clazzLevel, Term currentTerm) {

        books = books.stream().sorted(Comparator.comparing(TextBookManagement::getUpdateTime).reversed()).collect(Collectors.toList());

        TextBookManagement lastPurchBook = books.get(0);
        String publishName = lastPurchBook.getShortPublisherName();


        List<TextBookManagement> allBooks = textBookManagementLoader.getTextBookManagementList();

        List<TextBookManagement> filterBooks = allBooks.stream().filter(b ->
                and(subject.getId(), b.getSubjectId()) &&
                        and(clazzLevel, b.getClazzLevel()) &&
                        and(currentTerm.getKey(), b.getTermType()) &&
                        and(publishName, b.getShortPublisherName())
        ).collect(Collectors.toList());

        return filterBooks.stream().map(TextBookManagement::getBookId).collect(Collectors.toList());
    }


    private boolean and(Object o1, Object o2) {

        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1 instanceof Number) {
            return o1 == o2;
        }

        if (o1 instanceof CharSequence) {
            return o1.equals(o2);
        }

        return o1.equals(o2);
    }

    public Set<String> getPurchasedBookIds(Long parentId) {
        // 购买过的
        Map<Integer, List<TextBookManagement>> purchaseBooks = getParentBuyBookPicListenBooks(parentId);
        Set<String> purchasedBookIdSet = new HashSet<>();
        purchaseBooks.forEach((subject, book) -> {
            purchasedBookIdSet.addAll(book.stream().map(TextBookManagement::getBookId).collect(Collectors.toSet()));
        });
        return purchasedBookIdSet;
    }

    private Map<Integer, List<TextBookManagement>> getParentBuyBookPicListenBooks(Long parentId) {
        List<UserActivatedProduct> userActivatedProducts = userOrderLoaderClient.loadUserActivatedProductList(parentId);
        userActivatedProducts = userActivatedProducts.stream().filter(t -> OrderProductServiceType.PicListenBook == OrderProductServiceType.safeParse(t.getProductServiceType())).collect(Collectors.toList());


        List<String> productItemIds = userActivatedProducts.stream().sorted((o1, o2) -> o2.getUpdateDatetime().compareTo(o1.getUpdateDatetime()))
                .map(UserActivatedProduct::getProductItemId).collect(Collectors.toList());
        Map<String, OrderProductItem> orderProductItemMap = userOrderLoaderClient.loadOrderProductItems(productItemIds);
        if (orderProductItemMap.size() > 0) {
            List<String> bookIds = orderProductItemMap.values().stream().map(OrderProductItem::getAppItemId).collect(Collectors.toList());
            Map<String, TextBookManagement> textBooks = textBookManagementLoader.getTextBookByIds(bookIds);

            if (textBooks.size() > 0) {

                List<TextBookManagement> list = new ArrayList<>(textBooks.values());
                Map<Integer, List<TextBookManagement>> result = new HashMap<>();
                for (TextBookManagement t : list) {
                    if (result.get(t.getSubjectId()) == null) {
                        List<TextBookManagement> tmp = new ArrayList<>();
                        tmp.add(t);
                        result.put(t.getSubjectId(), tmp);
                    } else {
                        result.get(t.getSubjectId()).add(t);
                    }
                }
                return result;
            }

        }
        return Collections.emptyMap();
    }

    private Integer getClazzLevelForRecBook(StudentDetail studentDetail) {
        if (studentDetail.isJuniorStudent()
                || (studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()))
            return 7;
        if (studentDetail.isInfantStudent())
            return 1;
        if (studentDetail.getClazz() == null)
            return 3;
        return studentDetail.getClazzLevelAsInteger();
    }

    private List<NewClazzBookRef> loadStudentHomewordBookRefList(Long studentId, Subject... subjects) {
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(studentId, false);
        List<Subject> subjectsList = Arrays.asList(subjects);
        List<Long> groupIds = groupMappers.stream()
                .filter(t -> subjectsList.contains(t.getSubject()))
                .map(GroupMapper::getId).collect(Collectors.toList());
        return newClazzBookLoaderClient.loadGroupBookRefs(groupIds).toList();
    }


    public List<String> defaultBookId(Integer clazzLevel, Subject subject) {
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
        if (subject == Subject.CHINESE) {
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


    private Map<String, Object> bookMap(NewBookProfile bookProfile, Boolean bookNeedPay, Boolean isPurchased, DayRange lastDayRange) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", bookProfile.getShortName());
        map.put("book_name", bookProfile.getShortName());
        map.put("cover_url", PiclistenBookImgUtils.getCompressBookImg(bookProfile.getImgUrl()));
        map.put("is_purchased", isPurchased);
        map.put("book_need_pay", bookNeedPay);
        map.put("subject", Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        map.put("clazz_level", bookProfile.getClazzLevel());
        map.put("clazz_level_name", ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        map.put("book_id", bookProfile.getId());
        map.put("publisher", bookProfile.getPublisher());
        map.put("short_publisher", bookProfile.getShortPublisher());
        map.put("term", Term.of(bookProfile.getTermType()).name());

        String bookStatus = "free";
        if (bookNeedPay) {
            if (lastDayRange == null || lastDayRange.getEndDate().before(new Date())) {
                bookStatus = "not_purchased";
            } else {
                bookStatus = "purchased";
            }
        }
        map.put("status", bookStatus);
        return map;
    }


    private String getNextClazzLevelAndTermBookId(String bookId) {
        TextBookManagement textBook = textBookManagementLoader.getTextBook(bookId);
        if (textBook == null)
            return null;
        Subject subject = Subject.fromSubjectId(textBook.getSubjectId());
        TextBookMapper.ClazzAndTerm currentClazzAndTerm = TextBookMapper.ClazzAndTerm.newInstance(textBook.getClazzLevel(), textBook.getTermType());
        TextBookMapper.ClazzAndTerm nextClazzAndTerm = currentClazzAndTerm.next();
        if (nextClazzAndTerm == null)
            return null;
        List<TextBookManagement> nextClazzTermSameClazzLevelBooks
                = textBookManagementLoader.getTextBookManagementBySubjectClazzLevel(subject, nextClazzAndTerm.getClazzLevel());
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
            NewBookProfile bookProfile = samePublisherBookProfiles.stream().filter(t -> currentBookProfile.getSeriesId().equals(t.getSeriesId())).findFirst().orElse(null);
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
                .filter(t -> Subject.ENGLISH == t.getSubject() || Subject.CHINESE == t.getSubject())
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
                            newBookProfile.getSeriesId().equals(t.getSeriesId())
                                    && nowTerm.intValue() == t.getTermType() && "ONLINE".equals(t.getStatus())
                                    && picListenBookShow(t, false, sys)).findFirst().orElse(null);
                    if (term2Book == null) {
                        term2Book = namiBookList.stream().filter(t ->
                                newBookProfile.getSeriesId().equals(t.getSeriesId())
                                        && nowTerm.intValue() == t.getTermType()
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
     *
     * @param parentId
     * @return
     */
    public Boolean isParentAuth(Long parentId) {
        String key = CacheKeyGenerator.generateCacheKey("parentAuth", new String[]{"pid"}, new Object[]{parentId});
        CacheObject<Object> cacheObject = VendorCache.getVendorPersistenceCache().get(key);
        if (cacheObject == null || cacheObject.getValue() == null) {
            Boolean isParentAuth = innerIsParentAuth(parentId);
            VendorCache.getVendorPersistenceCache().set(key, 3600, isParentAuth);
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

        for (Long sid : studentIdList) {
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
                GroupMapper targetSubjectGroup = groups.stream().filter(g -> selfStudyType.getSubject() == g.getSubject()).findFirst().orElse(null);
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
                    hasContent = textBookManagementLoader.textReadBookShow(newBookProfile.getId(), sys);
                    break;
                case WALKMAN_ENGLISH:
                    hasContent = textBookManagementLoader.walkManBookShow(newBookProfile.getId(), sys);
                    break;
                default:
                    break;
            }
        }

        if (!hasContent)
            newBookProfile = newContentLoaderClient.loadBook(selfStudyType.getDefaultBookId());

        return newBookProfile;
    }
}
