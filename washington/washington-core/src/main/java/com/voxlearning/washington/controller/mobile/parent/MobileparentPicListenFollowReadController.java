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

package com.voxlearning.washington.controller.mobile.parent;

import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.question.api.entity.PicListen;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.constant.FollowReadLikeRangeType;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadCollection;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadRangeWrapper;
import com.voxlearning.utopia.service.vendor.api.entity.FollowReadSentenceResult;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2017-03-13 下午4:37
 **/
@Controller
@RequestMapping(value = "/parentMobile/selfstudy/piclisten/followread")
@Slf4j
public class MobileparentPicListenFollowReadController extends AbstractMobileParentSelfStudyController {

    @Inject private RaikouSystem raikouSystem;

    @RequestMapping(value = "/range.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage rangeList() {
        User user = currentParent();
        if (user == null)
            return noLoginResult;

        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return MapMessage.errorMessage("学生id?");

        String period = getRequestString("period");
        if (!FollowReadLikeRangeType.validatePeriod(period))
            return MapMessage.errorMessage("error period");
        String dimension = getRequestString("dimension");
        if (!FollowReadLikeRangeType.validateDimension(dimension))
            return MapMessage.errorMessage("error dimension");

        FollowReadLikeRangeType followReadLikeRangeType = FollowReadLikeRangeType.paresFromPeriodDimension(period, dimension);
        if (followReadLikeRangeType == null)
            return MapMessage.errorMessage("error type");

        Integer currentPage = getRequestInt("currentPage", 1);
        Pageable page = new PageRequest(currentPage-1, 20);

        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        if (studentDetail == null)
            return MapMessage.errorMessage("error student");

        if (studentDetail.getClazz() == null && followReadLikeRangeType.isNeedClazz())
            return MapMessage.successMessage().add("show_no_clazz_warn", true);
        Integer cityCode = studentDetail.getCityCode();
        ExRegion regionFromBuffer = raikouSystem.loadRegion(cityCode);
        String cityName = "";
        if (regionFromBuffer != null)
            cityName = regionFromBuffer.getCityName();
        Page<FollowReadRangeWrapper> rangeWrapperPage = parentSelfStudyService.loadLikeRangeList(studentDetail, followReadLikeRangeType, page);
        rangeWrapperPage.forEach(t -> t.setAvatar(getUserAvatarImgUrl(t.getAvatar())));
        if (CollectionUtils.isEmpty(rangeWrapperPage.getContent()))
            return MapMessage.successMessage().add("show_no_clazz_warn", false).add("range_list", new ArrayList<>())
                    .add("total_count", 0).add("currentPage", currentPage)
                    .add("totalPage", 0).add("city_name", cityName);

        return MapMessage.successMessage().add("show_no_clazz_warn", false).add("range_list", rangeWrapperPage.getContent())
                .add("total_count", rangeWrapperPage.getTotalElements()).add("currentPage", currentPage)
                .add("totalPage", rangeWrapperPage.getTotalPages()).add("city_name", cityName);

    }



    @RequestMapping(value = "/share_content.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage shareContent() {
        String collectionId = getRequestString("content_id");
        if (StringUtils.isBlank(collectionId))
            return MapMessage.errorMessage("该分享内容不存!");

        FollowReadCollection collection = parentSelfStudyService.loadFollowReadCollection(collectionId);
        if (collection == null)
            return MapMessage.errorMessage("该分享内容不存在或者已过期!");

        List<String> resultIdList = collection.getResultIdList();
        if (CollectionUtils.isEmpty(resultIdList))
            return MapMessage.errorMessage("该分享内容不存在或者已过期!");

        Map<String, FollowReadSentenceResult> readSentenceResultMap = parentSelfStudyService.loadFollowReadSentenceResults(resultIdList);
        if (MapUtils.isEmpty(readSentenceResultMap))
            return MapMessage.errorMessage("该分享内容不存在或者已过期!");

        String unitId = collection.getUnitId();
        NewBookCatalog unitNode = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unitNode == null)
            return MapMessage.errorMessage("该分享内容不存!");
        NewBookCatalogAncestor bookCatalogAncestor = unitNode.getAncestors().stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(null);
        if (bookCatalogAncestor == null)
            return MapMessage.errorMessage("该分享内容不存!");

        AlpsFuture<Long> likeCountFuture = parentSelfStudyService.loadFollowReadCollectionLikeCount(collectionId);
        Map<String, List<FollowReadSentenceResult>> sentenceResultMap = readSentenceResultMap.values().stream()
                .collect(Collectors.groupingBy(FollowReadSentenceResult::getPicListenId));

        Set<String> picListenIdSet = sentenceResultMap.keySet();
        Map<String, PicListen> picListenMap = picListenLoaderClient.loadPicListensIncludeDisabled(picListenIdSet);
        List<PicListen> sortedPicListenList = picListenMap.values().stream()
                .sorted(Comparator.comparingInt(PicListen::getRank)).collect(Collectors.toList());

        List<Map<String, Object>> contentMapList = new ArrayList<>(picListenMap.size());
        for (PicListen picListen : sortedPicListenList) {
            String picListenId = picListen.getId();
            List<FollowReadSentenceResult> sentenceResultList = sentenceResultMap.get(picListenId);
            if (CollectionUtils.isEmpty(sentenceResultList))
                continue;
            Map<String, Object> contentMap = new LinkedHashMap<>();
            contentMap.put("pic_listen_id", picListenId);
            contentMap.put("img_url", picListen.getImgUrl());
            List<Map<String, Object>> sentenceMapList = new ArrayList<>(sentenceResultList.size());
            sentenceResultList.forEach(t -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("sentence_id", t.getSentenceId());
                map.put("audio_url", t.getAudioUrl());
                sentenceMapList.add(map);
            });
            contentMap.put("sentence_list", sentenceMapList);

            contentMapList.add(contentMap);
        }

        return MapMessage.successMessage().add("like_count", likeCountFuture.getUninterruptibly())
                .add("book_id", bookCatalogAncestor.getId())
                .add("content_id", collectionId).add("content_list", contentMapList);

    }


    @RequestMapping(value = "/like_share.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage like() {
        String contentId = getRequestString("content_id");
        if (StringUtils.isBlank(contentId))
            return MapMessage.errorMessage("error contentID");

        try {
            AtomicLockManager.getInstance().wrapAtomic(parentSelfStudyService)
                    .keys(contentId).proxy().someoneLikeCollection(contentId);
        }catch (DuplicatedOperationException e){
            return MapMessage.errorMessage("您点击太快了,请稍候重试!");
        }

        return MapMessage.successMessage();
    }


    @RequestMapping(value = "/my_collection_list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage collectionList() {
        User parent = currentParent();
        if (parent == null || !parent.isParent())
            return noLoginResult;
        Long studentId = getRequestLong("sid");
        if (studentId == 0)
            return MapMessage.successMessage().add("list", new ArrayList<>());
        List<FollowReadCollection> readCollections = parentSelfStudyService.loadStudentFollowReadCollections(studentId);
        if (CollectionUtils.isEmpty(readCollections))
            return MapMessage.successMessage().add("list", new ArrayList<>());
        Set<String> collectionIds = new HashSet<>();
        List<String> unitIdList = new ArrayList<>();
        for (FollowReadCollection readCollection : readCollections) {
            collectionIds.add(readCollection.getId());
            unitIdList.add(readCollection.getUnitId());
        }

        Map<String, AlpsFuture<Long>> likeCountFutureMap = new HashMap<>();
        for (String collectionId : collectionIds) {
            AlpsFuture<Long> likeCountFuture = parentSelfStudyService.loadFollowReadCollectionLikeCount(collectionId);
            likeCountFutureMap.put(collectionId, likeCountFuture);
        }

        Map<String, NewBookCatalog> unitNodeMap = newContentLoaderClient.loadBookCatalogByCatalogIds(unitIdList);
        Map<String, UnitWrapper> unitWrapperMap = new HashMap<>();
        unitNodeMap.values().forEach(t -> {
            NewBookCatalogAncestor bookNode = t.getAncestors().stream().filter(f -> BookCatalogType.BOOK.name().equals(f.getNodeType())).findFirst().orElse(null);
            if (bookNode != null){
                UnitWrapper unitWrapper = new UnitWrapper();
                unitWrapper.setBookId(bookNode.getId());
                unitWrapper.setUnitId(t.getId());
                unitWrapper.setUnitName(t.getName());
                unitWrapperMap.put(t.getId(), unitWrapper);
            }

        });
        List<String> bookIdList = unitWrapperMap.values().stream().map(UnitWrapper::getBookId).collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);

        List<Map<String, Object>> collectionMapList = new ArrayList<>(readCollections.size());
        readCollections.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        for (FollowReadCollection readCollection : readCollections) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("content_id", readCollection.getId());
            UnitWrapper unitWrapper = unitWrapperMap.get(readCollection.getUnitId());
            if (unitWrapper == null)
                continue;
            String bookId = unitWrapper.getBookId();
            if (StringUtils.isBlank(bookId))
                continue;
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile == null)
                continue;
            map.put("book_name", newBookProfile.getShortName());
            map.put("unit_name", unitWrapper.getUnitName());
            AlpsFuture<Long> likeCountFuture = likeCountFutureMap.get(readCollection.getId());
            if (likeCountFuture == null)
                continue;
            map.put("like_count", likeCountFuture.getUninterruptibly());
            map.put("date", DateUtils.dateToString(readCollection.getCreateTime(), DateUtils.FORMAT_SQL_DATE));
            collectionMapList.add(map);
        }
        return MapMessage.successMessage().add("list", collectionMapList);
    }

    @Getter
    @Setter
    private class UnitWrapper{
        private String unitId;
        private String unitName;
        private String bookId;
        private String bookName;
    }

}
