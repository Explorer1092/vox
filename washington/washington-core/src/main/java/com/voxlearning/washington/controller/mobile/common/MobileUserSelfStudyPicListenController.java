package com.voxlearning.washington.controller.mobile.common;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookShelf;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.washington.controller.mobile.AbstractMobileSelfStudyController;
import com.voxlearning.utopia.mapper.PicListenShelfBookMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-03-01 下午8:14
 **/
@Controller
@RequestMapping(value = "/userMobile/selfstudy/piclisten")
@Slf4j
public class MobileUserSelfStudyPicListenController extends AbstractMobileSelfStudyController {


    /**
     * 书架教材列表
     * @return
     */
    @RequestMapping(value = "/book_shelf.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookShelf() {

        User user = currentParent();
        if (user == null)
            return MapMessage.errorMessage().setErrorCode("920"); //去下载页面。。。。
        String sys = getRequestString("sys");

        List<String> allBookIdList = new ArrayList<>();

        List<PicListenBookShelf> picListenBookShelfs = parentSelfStudyService.loadParentPicListenBookShelf(user.getId());
        if (CollectionUtils.isEmpty(picListenBookShelfs)){ //当用户第一次进入书架一本书都没有的时候,初始化一些书放入书架
            Long allBookCount = parentSelfStudyService.parentPicListenBookShelfCountIncludeDisabled(user.getId());
            if (allBookCount == 0) { //说明用户第一次进入书架一本书都没有
                Set<String> defaultBookIdSet = parentSelfStudyPublicHelper.picListenDefaultShelfBooks(user, sys, "");
                parentSelfStudyService.initParentPicListenBookShelfBooks(user.getId(), defaultBookIdSet);
                allBookIdList.addAll(defaultBookIdSet);
            }
        }else
            allBookIdList = picListenBookShelfs.stream().map(PicListenBookShelf::getBookId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(allBookIdList)){
            return MapMessage.successMessage().add("book_list", new ArrayList<>());
        }

        Map<String, PicListenBookShelf> shelfBookMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(picListenBookShelfs)) {
            for (PicListenBookShelf picListenBookShelf : picListenBookShelfs) {
                shelfBookMap.put(picListenBookShelf.getBookId(), picListenBookShelf);
            }
        }

        Map<String, DayRange> buyLastDayMap = picListenCommonService.parentBuyBookPicListenLastDayMap(user.getId(), false);

        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(allBookIdList);


        List<PicListenShelfBookMapper> mapperList = new ArrayList<>();
        for (String bookId : allBookIdList) {
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile == null)
                continue;
            PicListenBookShelf picListenBookShelf = shelfBookMap.get(bookId);
            Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(newBookProfile);
            Boolean isPayed = buyLastDayMap.get(bookId) != null;
            Date createTime = picListenBookShelf == null ? new Date() : picListenBookShelf.getCreateTime();
            mapperList.add(new PicListenShelfBookMapper(bookId, bookNeedPay, isPayed, createTime));
        }
        mapperList.sort(new PicListenShelfBookMapper.ShelfBookComparator(""));

//        allBookIdList.sort((o1, o2) -> {
//            DayRange b1 = buyLastDayMap.get(o1);
//            DayRange b2 = buyLastDayMap.get(o2);
//            int ob1 = b1 == null ? 1 : -1;
//            int ob2 = b2 == null ? 1 : -1;
//            int compareBuy = Integer.compare(ob1, ob2);
//            if (compareBuy != 0)
//                return compareBuy;
//
//            NewBookProfile book1 = bookProfileMap.get(o1);
//            NewBookProfile book2 = bookProfileMap.get(o2);
//            Integer of1;
//            if (book1 != null && !parentSelfStudyPublicHelper.picListenBookNeedPay(book1, false))
//                of1 = -1;
//            else
//                of1 = 1;
//            Integer of2;
//            if (book2 != null && !parentSelfStudyPublicHelper.picListenBookNeedPay(book2, false))
//                of2 = -1;
//            else
//                of2 = 1;
//            int compareFree = Integer.compare(of1, of2);
//            if (compareFree != 0)
//                return compareFree;
//            PicListenBookShelf p1 = shelfBookMap.get(o1);
//            PicListenBookShelf p2 = shelfBookMap.get(o2);
//            Date date1 = p1 == null ? new Date() : p1.getCreateTime();
//            Date date2 = p2 == null ? new Date() : p2.getCreateTime();
//            return date2.compareTo(date1);
//        });
        List<Map<String, Object>> bookMapList = new ArrayList<>();

        for (PicListenShelfBookMapper mapper : mapperList) {
            NewBookProfile newBookProfile = bookProfileMap.get(mapper.getBookId());
            bookMapList.add(convert2BookMap(newBookProfile, true, buyLastDayMap.get(newBookProfile.getId()), false));
        }

        return MapMessage.successMessage().add("book_list", bookMapList);
    }


    @RequestMapping(value = "/book_shelf/add.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addBook2Shelf() {
        User user = currentParent();
        if (user == null)
            return noLoginResult;
        String bookId = getRequestString("book_id");
        if (StringUtils.isBlank(bookId))
            return MapMessage.errorMessage("请选择一本教材!");

        try {
            return AtomicLockManager.instance().wrapAtomic(parentSelfStudyService).keyPrefix("addBook").keys(user.getId())
                    .proxy().addBook2PicListenShelf(user.getId(), bookId);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }

    @RequestMapping(value = "/book_shelf/delete.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteBook2Shelf() {
        User user = currentParent();
        if (user == null)
            return noLoginResult;
        String bookId = getRequestString("book_id");
        if (StringUtils.isBlank(bookId))
            return MapMessage.errorMessage("请选择一本教材!");
        try {
            return AtomicLockManager.instance().wrapAtomic(parentSelfStudyService).keyPrefix("deleteBook").keys(user.getId())
                    .proxy().deleteBookFromPicListenShelf(user.getId(), bookId);
        } catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
    }


    @RequestMapping(value = "/book/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookList() {
        User user = currentParent();
        if (user == null)
            return MapMessage.errorMessage().setErrorCode("920"); //去下载页面。。。。
        int clazzLevel = getRequestInt("clazz_level", 3);
        String sys = getRequestString("sys");
        if (StringUtils.isBlank(sys))
            return MapMessage.errorMessage("客户端版本错误");

        ClazzLevel level = ClazzLevel.parse(clazzLevel);
//        List<NewBookProfile> newBookProfileList = newContentLoaderClient.loadBooksByClassLevelWithSortByRegionCode(Subject.ENGLISH, 0, level);
//        if (CollectionUtils.isEmpty(newBookProfileList))
//            newBookProfileList = new ArrayList<>();
//        List<NewBookProfile> namiBookList = newContentLoaderClient.loadNamiBookBySubject(Subject.ENGLISH, level);
//        if (CollectionUtils.isNotEmpty(namiBookList)) {
//            newBookProfileList.addAll(namiBookList);
//        }
//        List<NewBookProfile> chineseNamiBookList = newContentLoaderClient.loadNamiBookBySubject(Subject.CHINESE, level);
//        if (CollectionUtils.isNotEmpty(chineseNamiBookList)) {
//            newBookProfileList.addAll(chineseNamiBookList);
//        }

        List<TextBookManagement> englishTextBookList = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(Subject.ENGLISH, level.getLevel());
        List<TextBookManagement> chineseTextBookList = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(Subject.CHINESE, level.getLevel());
        List<TextBookManagement> allTextBookList = new ArrayList<>(englishTextBookList);
        allTextBookList.addAll(chineseTextBookList);

        List<String> allBookId = allTextBookList.stream().map(TextBookManagement::getBookId).collect(Collectors.toList());

        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(allBookId);

        List<NewBookProfile> newBookProfileList = new ArrayList<>(bookProfileMap.values());

        Boolean finalParentIsAuth = picListenCommonService.userIsAuthForPicListen(user);

        List<PicListenBookShelf> picListenBookShelfs = parentSelfStudyService.loadParentPicListenBookShelf(user.getId());
        Set<String> alreadyAddedBookIdSet = picListenBookShelfs.stream().map(PicListenBookShelf::getBookId).collect(Collectors.toSet());

        newBookProfileList = newBookProfileList.stream().filter(t -> {
            Boolean picListenBookShow = textBookManagementLoaderClient.picListenBookShow(t.getId(), false, sys);
            if (!picListenBookShow){
                return finalParentIsAuth && textBookManagementLoaderClient.picListenBookAuthOnline(t.getId());
            }else
                return true;
        }).collect(Collectors.toList());

        Map<String, DayRange> bookId2PayEndDateMap = picListenCommonService.parentBuyBookPicListenLastDayMap(user.getId(), false);


        Map<PublisherRankMapper, List<NewBookProfile>> publisherRankMapperListHashMap = new HashMap<>();
        for (NewBookProfile newBookProfile : newBookProfileList) {
            String publisherName = !StringUtils.isBlank(newBookProfile.getShortPublisher()) ? newBookProfile.getShortPublisher() : "其他";
            int publisherRank = SafeConverter.toInt(newBookProfile.getPublisherRank(), 9999);

            PublisherRankMapper rankMapper = new PublisherRankMapper(publisherRank, publisherName);
            List<NewBookProfile> newBookProfiles = publisherRankMapperListHashMap.get(rankMapper);
            if (newBookProfiles == null)
                newBookProfiles = new ArrayList<>();
            newBookProfiles.add(newBookProfile);
            publisherRankMapperListHashMap.put(rankMapper, newBookProfiles);
        }

        List<PublisherRankMapper> publisherRankMapperList = publisherRankMapperListHashMap.keySet().stream().sorted(PublisherRankMapper::compareTo).collect(Collectors.toList());

        List<Map<String, Object>> publisherMapList = new ArrayList<>();
        for (PublisherRankMapper publisherRankMapper : publisherRankMapperList) {
            List<NewBookProfile> newBookProfiles = publisherRankMapperListHashMap.get(publisherRankMapper);
            if (CollectionUtils.isEmpty(newBookProfiles))
                continue;
            Map<String, Object> publisherMap = new LinkedHashMap<>();
            String publisherName = publisherRankMapper.getPublisherName();
            if (publisherName.equals("人教版"))
                publisherName = publisherName + "（英语+语文）";
            publisherMap.put("publisher_name", publisherName);
            List<Map<String, Object>> bookMapList = new ArrayList<>();
            for (NewBookProfile bookProfile : newBookProfiles) {
                Map<String, Object> bookMap = convert2BookMap(bookProfile, true, bookId2PayEndDateMap.get(bookProfile.getId()), false, alreadyAddedBookIdSet);
                bookMapList.add(bookMap);
            }
            publisherMap.put("book_list", bookMapList);
            publisherMapList.add(publisherMap);
        }

        return MapMessage.successMessage().add("clazz_level", level.getLevel()).add("book_list", publisherMapList);
    }

    @RequestMapping(value = "/book/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookDetail() {
        User user = currentParent();

        String bookId = getRequestString("book_id");
        if (StringUtils.isBlank(bookId))
            return MapMessage.errorMessage("请选择一本教材!");

        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
        if (newBookProfile == null)
            return MapMessage.errorMessage("教材不存在");
        Map<String, DayRange> bookId2PayEndDateMap = user == null ? Collections.emptyMap() : picListenCommonService.parentBuyBookPicListenLastDayMap(user.getId(), false);
        DayRange dayRange = bookId2PayEndDateMap.get(bookId);
        Map<String, Object> bookMap = convert2BookMap(newBookProfile, true, dayRange, false);
        Map<String, Object> map = addUnitInfo2BookMap(bookMap, newBookProfile);
        return MapMessage.successMessage().add("book_detail", map);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class PublisherRankMapper implements Comparable {
        private Integer rank;
        private String publisherName;

        @Override
        public int hashCode() {
            if (publisherName == null)
                return 0;
            return publisherName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if ( !(obj instanceof PublisherRankMapper))
                return false;
            PublisherRankMapper mapper = (PublisherRankMapper) obj;
            if (this == mapper)
                return true;
            if (this.publisherName == null || mapper.getPublisherName() == null)
                return false;
            if (this.publisherName.equals(mapper.getPublisherName()))
                return true;
            return false;
        }


        @Override
        public int compareTo(Object o) {
            if (!(o instanceof PublisherRankMapper))
                return -1;
            if (this.rank == null)
                return 1;
            PublisherRankMapper mapper = (PublisherRankMapper) o;
            return this.rank.compareTo(mapper.getRank());
        }
    }






}
