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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.mapper.PicListenShelfBookMapper;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalogAncestor;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.question.api.entity.PicListen;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Student;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookPayInfo;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenBookShelf;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenCollectData;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.utopia.temp.GrindEarActivity;
import com.voxlearning.washington.controller.open.AbstractSelfStudyApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.mapper.SelfStudyAdInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.CHINESE;
import static com.voxlearning.alps.annotation.meta.Subject.ENGLISH;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.RES_TITLE;

/**
 * @author jiangpeng
 * @since 16/7/10.
 */
@Controller
@RequestMapping(value = "/v1/parent/selfstudy/piclisten")
@Slf4j
public class ParentSelfStudyPicListenApiController extends AbstractSelfStudyApiController {

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @Inject
    private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @Inject
    private ActionServiceClient actionServiceClient;

    private static final String FEED_BACK_URL = "/view/mobile/parent/send_question?dest_id=9610&qs_type=question_readingmachine&origin=点读机";
    private static final String ADD_BOOK_URL = "/view/mobile/parent/choice_book/index.vpage?useNewCore=wk&rel=sj";

    /**
     * 查询要传给外研社的手机号
     *
     */
    @RequestMapping(value = "/mobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage mobile() {
        try {
            User user = getApiRequestUser();
            if (user == null)
                return failMessage(RES_RESULT_NEED_RELOGIN_CODE, RES_RESULT_RELOGIN);
            if (user.isParent()) {
                MapMessage message = parentSelfStudyService.getFltrpMobile(user.getId());
                if (message.isSuccess()) {
                    return successMessage().add("mobile", message.get("mobile"));
                } else {
                    return failMessage(message.getInfo());
                }
            }else if (user.isStudent()){
                MapMessage mapMessage = parentServiceClient.loadOrRegisteDefaultParentUserByStudentId(user.getId());
                if (mapMessage.isSuccess()){
                    Object parentObj = mapMessage.get("parent");
                    if (parentObj instanceof User){
                        User parent = User.class.cast(parentObj);
                        MapMessage message = parentSelfStudyService.getFltrpMobile(parent.getId());
                        if (message.isSuccess()) {
                            return successMessage().add("mobile", message.get("mobile"));
                        } else {
                            return failMessage(message.getInfo());
                        }
                    }
                }else
                    return failMessage(mapMessage.getInfo());
            }
            return failMessage("unKnow error");
        } catch (Exception ex) {
            logger.error("get fltrp mobile fail,uid:{},msg:{}", getCurrentParentId(), ex.getMessage(), ex);
            return failMessage("系统异常");
        }
    }


    /**
     * 为点读报告收集数据
     */
    @RequestMapping(value = "/data/collect.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage dataCollect() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_DATA, "数据");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_DATA, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String data = getRequestString(REQ_DATA);
        Long studentId = getRequestStudentId();
        if (studentId == 0)
            return successMessage();

        List<PicListenCollectData.SentenceResult> sentenceResultList =
                JsonUtils.fromJsonToList(data, PicListenCollectData.SentenceResult.class);
        if (CollectionUtils.isEmpty(sentenceResultList))
            return failMessage(RES_RESULT_DATA_ERROR_MSG);

        PicListenCollectData picListenCollectData = new PicListenCollectData(studentId, DayRange.current());
        picListenCollectData.setSentenceResultList(sentenceResultList);
        parentSelfStudyService.processPicListenCollectData(picListenCollectData.union());
        return successMessage();
    }

    /**
     * 点读机书架
     */
    @RequestMapping(value = "/book/shelf.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookShelf() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequest();
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long parentId = getCurrentParentId();
        if (parentId == null)
            return noUserResult;
        String sys = getRequestString(REQ_SYS);
        List<String> bookIdList = new ArrayList<>();
        List<PicListenBookShelf> picListenBookShelfs = parentSelfStudyService.loadParentPicListenBookShelf(parentId);
        if (CollectionUtils.isEmpty(picListenBookShelfs)){ //当用户第一次进入书架一本书都没有的时候,初始化一些书放入书架
            Long allBookCount = parentSelfStudyService.parentPicListenBookShelfCountIncludeDisabled(parentId);
            if (allBookCount == 0) { //说明用户第一次进入书架一本书都没有
                Set<String> defaultBookIdSet = parentSelfStudyPublicHelper.picListenDefaultShelfBooks(getCurrentParent(), sys, getRequestString(REQ_APP_NATIVE_VERSION));
                parentSelfStudyService.initParentPicListenBookShelfBooks(parentId, defaultBookIdSet);
                bookIdList.addAll(defaultBookIdSet);
            }
        }else
            bookIdList = picListenBookShelfs.stream().map(PicListenBookShelf::getBookId).collect(Collectors.toList());

        return bookShelfListMap(bookIdList, picListenBookShelfs, parentId, getRequestString(REQ_APP_NATIVE_VERSION));

    }
    /**
     * 点读机书架 添加教材
     */
    @RequestMapping(value = "/book/shelf/add_book.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage addBook() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "教材id");
            validateRequest(REQ_BOOK_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long parentId = getCurrentParentId();
        if (parentId == null)
            return noUserResult;
        String bookId = getRequestString(REQ_BOOK_ID);
        try {
            MapMessage mapMessage = AtomicLockManager.instance().wrapAtomic(parentSelfStudyService).keyPrefix("addBook").keys(parentId)
                    .proxy().addBook2PicListenShelf(parentId, bookId);
            if (mapMessage.isSuccess())
                return successMessage();
            else
                return failMessage(mapMessage.getInfo());
        } catch (DuplicatedOperationException ex) {
            return failMessage("您点击太快了，请重试");
        }
    }

    /**
     * 点读机书架 删除教材
     */
    @RequestMapping(value = "/book/shelf/delete_book.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage deleteBook() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "教材id");
            validateRequest(REQ_BOOK_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long parentId = getCurrentParentId();
        if (parentId == null)
            return noUserResult;
        String bookId = getRequestString(REQ_BOOK_ID);
        try {
            MapMessage mapMessage = AtomicLockManager.instance().wrapAtomic(parentSelfStudyService).keyPrefix("deleteBook").keys(parentId)
                    .proxy().deleteBookFromPicListenShelf(parentId, bookId);
            if (mapMessage.isSuccess())
                return successMessage();
            else
                return failMessage(mapMessage.getInfo());
        } catch (DuplicatedOperationException ex) {
            return failMessage("您点击太快了，请重试");
        }
    }

    /**
     * 点读机书架 壳初始化教材
     * 一个用户第一次进入会调用一次,把本地已存在的教材,上报上来。wo
     */
    @RequestMapping(value = "/book/shelf/init.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage initBook() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_IDS, "教材id");
            validateRequest(REQ_BOOK_IDS);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long parentId = getCurrentParentId();
        if (parentId == null)
            return noUserResult;

        String bookIds = getRequestString(REQ_BOOK_IDS);
        List<String> nativeBookIdList = JsonUtils.fromJsonToList(bookIds, String.class);
        List<PicListenBookShelf> picListenBookShelfs;
        try {
            picListenBookShelfs = AtomicLockManager.getInstance().wrapAtomic(parentSelfStudyService).keys(parentId).proxy()
                    .mergePicListenBookShelf(parentId, nativeBookIdList);
        }catch (DuplicatedOperationException ex) {
            return MapMessage.errorMessage("您点击太快了，请重试");
        }
        Map<String, PicListenBookShelf> shelfBookMap = new HashMap<>();
        for (PicListenBookShelf bookShelf : picListenBookShelfs) {
            shelfBookMap.put(bookShelf.getBookId(), bookShelf);
        }

        List<String> bookIdList = new ArrayList<>(shelfBookMap.keySet());
        return bookShelfListMap(bookIdList, picListenBookShelfs, parentId, getRequestString(REQ_APP_NATIVE_VERSION));
    }

    private MapMessage bookShelfListMap(List<String> bookIdList, List<PicListenBookShelf> picListenBookShelfs, Long parentId, String version){

        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);

        List<String> seriesIdList = bookProfileMap.values().stream().map(NewBookProfile::getSeriesId).collect(Collectors.toList());

        Map<String, DayRange> bookId2PayEndDateMap = picListenCommonService.parentBuyBookPicListenLastDayMap(getCurrentParentId(), false);
        Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdList);
        List<Map<String, Object>> bookMapList = new ArrayList<>();

        Map<String, PicListenBookShelf> shelfBookMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(picListenBookShelfs)) {
            for (PicListenBookShelf picListenBookShelf : picListenBookShelfs) {
                shelfBookMap.put(picListenBookShelf.getBookId(), picListenBookShelf);
            }
        }

        List<PicListenShelfBookMapper> mapperList = new ArrayList<>();
        for (String bookId : bookIdList) {
            NewBookProfile newBookProfile = bookProfileMap.get(bookId);
            if (newBookProfile == null)
                continue;
            PicListenBookShelf picListenBookShelf = shelfBookMap.get(bookId);
            Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(newBookProfile);
            Boolean isPayed = bookId2PayEndDateMap.get(bookId) != null;
            Date createTime = picListenBookShelf == null ? new Date() : picListenBookShelf.getCreateTime();
            mapperList.add(new PicListenShelfBookMapper(bookId, bookNeedPay, isPayed, createTime));
        }
        mapperList.sort(new PicListenShelfBookMapper.ShelfBookComparator(version));
        mapperList.forEach(mapper -> {
            NewBookProfile bookProfile = bookProfileMap.get(mapper.getBookId());
            if (bookProfile == null)
                return;
            bookMapList.add(convert2BookMap(bookProfile, stringNewBookCatalogMap.get(bookProfile.getSeriesId()), true, bookId2PayEndDateMap.get(bookProfile.getId())));
        });

        return successMessage().add(RES_BOOK_LIST, bookMapList).add(RES_BANNERS, picListenAdMapList(null, SelfStudyAdPosition.PIC_LISTEN_BOOK_SHELF_BANNER))
                .add(RES_PURCHASE_TEXT, "该教材为收费教材，您还未购买。请尝试免费体验，或前往商品详情页面购买后使用。")
                .add(RES_FEEDBACK_URL, fetchMainsiteUrlByCurrentSchema() + FEED_BACK_URL). add(RES_EXTRA_ENTRY, extraEntry(parentId))
                .add(RES_ADDBOOK_URL, fetchMainsiteUrlByCurrentSchema() + ADD_BOOK_URL);
    }

    private Map<String, Object> extraEntry(Long parentId){
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(RES_MY_RECORD_URL, fetchMainsiteUrlByCurrentSchema() + "/view/wx/parent/reading/records");
        //点读报告,灰度入口
        List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
        if (CollectionUtils.isNotEmpty(studentParentRefs)){
            Set<Long> studentIdSet = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIdSet);
            Boolean hit = false;
            for (StudentDetail studentDetail : studentDetailMap.values()) {
                hit= grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "jztpiclisen", "reportentry");
                if (hit)
                    break;
            }
            if (hit)
                map.put(RES_PICLISTEN_REPORT_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/read_report/index.vpage");

            //排行版入口
            map.put(RES_RANGE_URL, fetchMainsiteUrlByCurrentSchema() + "/view/mobile/parent/read_report/rank.vpage");
        }
        return map;
    }

    ///////////////////////////////////////////////////
    /**
     * 点读专用
     * 根据年级学科返回课本列表
     */
    @RequestMapping(value = "/book/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name());
            validateRequiredNumber(REQ_CLAZZ_LEVEL, "年级");
            if (StringUtils.isNotBlank(getRequestString("is_preview"))) {
                validateRequest(REQ_SUBJECT, REQ_CLAZZ_LEVEL, "is_preview");
            } else
                validateRequest(REQ_SUBJECT, REQ_CLAZZ_LEVEL);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Subject subject = Subject.valueOf(getRequestString(REQ_SUBJECT));
        ClazzLevel level = ClazzLevel.parse(getRequestInt(REQ_CLAZZ_LEVEL));
        String sys = getRequestString(REQ_SYS);
        List<NewBookProfile> newBookProfileList = newContentLoaderClient.loadBooksByClassLevelWithSortByRegionCode(subject, 0, level);

        List<NewBookProfile> namiBookList = newContentLoaderClient.loadNamiBookBySubject(subject, level);
        if (CollectionUtils.isNotEmpty(namiBookList)) {
            if (CollectionUtils.isEmpty(newBookProfileList))
                newBookProfileList = new ArrayList<>();
            newBookProfileList.addAll(namiBookList);
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        if (CollectionUtils.isEmpty(newBookProfileList))
            return resultMap.add(RES_BOOK_LIST, new ArrayList<>());

        //如果是需要load出预览点读教材,则需要判断灰度用户,只有灰度内用户方可load预览教材
        Boolean isPreview = getRequestBool("is_preview");
        if (isPreview) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(getCurrentParentId());
            Set<Long> studentIdSet = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIdSet);
            Boolean hitGrey = false;
            for (StudentDetail studentDetail : studentDetailMap.values()) {
                Boolean singleHitGrey = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "picListenPreview", "whiteList");
                if (singleHitGrey) {
                    hitGrey = true;
                    break;
                }
            }
            if (!hitGrey)
                isPreview = false;
        }
        final Boolean finalIsPreview = isPreview;
        //过滤出支持点读的教材
        //有可能要的是需要点读预览功能的教材
        newBookProfileList = newBookProfileList.stream().filter(t ->
           textBookManagementLoaderClient.picListenBookShow(t.getId(), finalIsPreview, sys)
        ).collect(Collectors.toList());
        List<Map<String, Object>> bookMapList = new ArrayList<>();
        List<String> seriesIdList = newBookProfileList.stream().map(NewBookProfile::getSeriesId).collect(Collectors.toList());
        Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdList);
        for (NewBookProfile bookProfile : newBookProfileList) {
            NewBookCatalog newBookCatalog = stringNewBookCatalogMap.get(bookProfile.getSeriesId());
            bookMapList.add(convert2BookMap(bookProfile, newBookCatalog, false, null));
        }
        resultMap.add(RES_BOOK_LIST, bookMapList);
        return resultMap;
    }


    /**
     * 英语点读机-获取教材信息
     */
    @RequestMapping(value = "/book/infos.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookInfos() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_BOOK_IDS, "教材");
            validateRequest(REQ_STUDENT_ID, REQ_BOOK_IDS);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String bookIdStr = getRequestString(REQ_BOOK_IDS);

        List<String> bookIdList = JsonUtils.fromJsonToList(bookIdStr, String.class);
        if (bookIdList == null)
            bookIdList = new ArrayList<>();

        bookIdList = bookIdList.stream().distinct().collect(Collectors.toList());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIdList);

        List<String> seriesIdList = bookProfileMap.values().stream().map(NewBookProfile::getSeriesId).collect(Collectors.toList());
        long sid = getRequestStudentId();
        final StudentDetail studentDetail = sid == 0L ? null : studentLoaderClient.loadStudentDetail(sid);
        Map<String, PicListenBookPayInfo> bookId2PayEndDateMap = picListenCommonService.userBuyBookPicListenLastDayMap(getApiRequestUser(), false);
        Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdList);
        List<Map<String, Object>> bookMapList = new ArrayList<>();
        bookIdList.forEach(bookId -> {
            NewBookProfile bookProfile = bookProfileMap.get(bookId);
            if (bookProfile == null)
                return;
            PicListenBookPayInfo picListenBookPayInfo = bookId2PayEndDateMap.get(bookProfile.getId());
            DayRange dayRange = picListenBookPayInfo == null ? null : picListenBookPayInfo.getDayRange();
            bookMapList.add(convert2BookMap(bookProfile, stringNewBookCatalogMap.get(bookProfile.getSeriesId()), dayRange, studentDetail));
        });

        return successMessage().add(RES_BOOK_LIST, bookMapList).add(RES_BANNERS, picListenAdMapList(null, SelfStudyAdPosition.PIC_LISTEN_BOOK_SHELF_BANNER))
                .add(RES_PURCHASE_TEXT, "该教材为收费教材，您还未购买。请尝试免费体验，或前往商品详情页面购买后使用。")
                .add(RES_FEEDBACK_URL, fetchMainsiteUrlByCurrentSchema() + FEED_BACK_URL);
    }

    /**
     * 英语点读机-书架为空时获取一本默认教材
     */
    @RequestMapping(value = "/default/book.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage defaultBookInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long parentId = getCurrentParentId();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String sys = getRequestString(REQ_SYS);

        Map<String, DayRange> bookId2PayEndDateMap = picListenCommonService.parentBuyBookPicListenLastDayMap(parentId, false);

        Set<String> allBookIdSet = new HashSet<>(bookId2PayEndDateMap.keySet());

//        NewBookProfile newBookProfile = getDefaultPicListenBook(parentId, studentId, sys);
//        if (newBookProfile != null)
//            allBookIdSet.add(newBookProfile.getId());

        allBookIdSet.addAll(parentSelfStudyPublicHelper.picListenDefaultShelfBooks(getCurrentParent(), sys, getRequestString(REQ_APP_NATIVE_VERSION)));


        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(allBookIdSet);
        List<String> seriesIdList = bookProfileMap.values().stream().map(NewBookProfile::getSeriesId).collect(Collectors.toList());
        Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdList);
        List<Map<String, Object>> bookMapList = new ArrayList<>();
        allBookIdSet.forEach(bookId -> {
            NewBookProfile bookProfile = bookProfileMap.get(bookId);
            if (bookProfile == null)
                return;
            bookMapList.add(convert2BookMap(bookProfile, stringNewBookCatalogMap.get(bookProfile.getSeriesId()), true, bookId2PayEndDateMap.get(bookProfile.getId())));
        });


        return successMessage().add(RES_BOOK_LIST, bookMapList)
                .add(RES_BANNERS, picListenAdMapList(null, SelfStudyAdPosition.PIC_LISTEN_BOOK_SHELF_BANNER))
                .add(RES_PURCHASE_TEXT, "该教材为收费教材，您还未购买。请尝试免费体验，或前往商品详情页面购买后使用。")
                .add(RES_FEEDBACK_URL, fetchMainsiteUrlByCurrentSchema() + FEED_BACK_URL);

    }


    /**
     * 点读专用
     * 返回学生的默认教材详情
     */
    @RequestMapping(value = "/book/detail.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookDetail() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name());
            validateRequest(REQ_SUBJECT, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        Long parentId = getCurrentParentId();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String sys = getRequestString(REQ_SYS);

        NewBookProfile newBookProfile = getDefaultPicListenBook(getCurrentParent(), studentId, sys);

        if (newBookProfile == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BOOK_ERROR);
            return resultMap;
        }


        //兼容安卓系统的问题,点击进入就算使用了
        if ("android".equalsIgnoreCase(getRequestString(REQ_SYS))) {
            //磨耳朵活动
            if (GrindEarActivity.isInActivityPeriod()) {
                //当前时间在上午9点05之前
                if (GrindEarActivity.beforeDeadLineTime()) {
                    Date current = new Date();
                    grindEarService.pushTodayRecord(studentId, current);
                }
            }
            //成长值
            if (studentId != 0)
                actionServiceClient.clickSelfStudyEnglishPicListen(studentId);

        }
        Boolean needPay = textBookManagementLoaderClient.picListenBookNeedPay(newBookProfile);

        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_USER_BOOK, generateBookDetailMapForPicListen(newBookProfile, needPay, true)).add(RES_BANNERS, picListenAdMapList(newBookProfile, SelfStudyAdPosition.PIC_LISTEN_UNIT_LIST_BANNER)).add(RES_NEED_UPGRADE, needUpgrade(needPay, getRequestString(REQ_APP_NATIVE_VERSION)));
    }

    private List<Map<String, Object>> picListenAdMapList(NewBookProfile newBookProfile, SelfStudyAdPosition selfStudyAdPosition) {
        List<SelfStudyAdInfo> selfStudyAdInfoList = loadSelfStudyAdConfigListByPosition(selfStudyAdPosition, newBookProfile);
        List<Map<String, Object>> bannerMapList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(selfStudyAdInfoList)) {
            selfStudyAdInfoList.forEach(ad -> {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(RES_USER_IMG_URL, ad.getImgUrl());
                if (StringUtils.isNotBlank(ad.getJumpUrl()))
                    map.put(RES_JUMP_URL, ad.getJumpUrl());
                bannerMapList.add(map);
            });
        }
        return bannerMapList;
    }

    private NewBookProfile getDefaultPicListenBook(User parent, Long studentId, String sys) {
        NewBookProfile newBookProfile = null;
        Boolean isPreview = false;
        if (RuntimeMode.current() == Mode.STAGING) {
            List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parent.getId());
            Set<Long> studentIdSet = studentParentRefs.stream().map(StudentParentRef::getStudentId).collect(Collectors.toSet());
            Map<Long, StudentDetail> studentDetailMap = studentLoaderClient.loadStudentDetails(studentIdSet);
            Boolean hitGrey = false;
            for (StudentDetail studentDetail : studentDetailMap.values()) {
                Boolean singleHitGrey = grayFunctionManagerClient.getStudentGrayFunctionManager().isWebGrayFunctionAvailable(studentDetail, "picListenPreview", "whiteList");
                if (singleHitGrey) {
                    hitGrey = true;
                    break;
                }
            }
            if (hitGrey)
                isPreview = true;
        }
        if (studentId != 0) {
            if (!checkStudentParentRef(studentId, parent.getId())) {
//                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//                resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                return null;
            }

            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
            if (studentDetail == null) {
//                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//                resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                return null;
            }

            if (studentDetail.isJuniorStudent()) {
//                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//                resultMap.add(RES_MESSAGE, RES_RESULT_CLAZZ_LEVEL_ERROR);
                return null;
            }
            newBookProfile = parentSelfStudyPublicHelper.loadDefaultSelfStudyBook(studentDetail, SelfStudyType.PICLISTEN_ENGLISH, isPreview, sys);
        } else {
            String bookId = asyncPiclistenCacheServiceClient.getAsyncPiclistenCacheService()
                    .CParentSelfStudyBookCacheManager_getParentSelfStudyBook(parent.getId(), SelfStudyType.PICLISTEN_ENGLISH)
                    .take();
            if (bookId != null) {
                newBookProfile = newContentLoaderClient.loadBooks(Collections.singleton(bookId)).get(bookId);
            }
            if (newBookProfile == null)
                newBookProfile = newContentLoaderClient.loadBooks(Collections.singleton(SelfStudyType.PICLISTEN_ENGLISH.getDefaultBookId())).get(SelfStudyType.PICLISTEN_ENGLISH.getDefaultBookId());
        }
        if (newBookProfile == null) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_BOOK_ERROR);
            return null;
        }
        //灰度下线一批教材。。。。
//        Boolean hitOfflineGrey = parentHitOfflineBookGrey(parentId);
        Boolean isParentAuth = picListenCommonService.userIsAuthForPicListen(parent);
        if (!isParentAuth) {
            if (textBookManagementLoaderClient.picListenBookAuthOnline(newBookProfile.getId())) {
                newBookProfile = newContentLoaderClient.loadBook(SelfStudyType.PICLISTEN_ENGLISH.getDefaultBookId());
            }
        }
        return newBookProfile;
    }


    /**
     * 指定教材的信息
     */
    @RequestMapping(value = "/pay/book/detail.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookDetailForPay() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequired(REQ_BOOK_ID, "教材id");
            validateRequest(REQ_BOOK_ID, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String requestBookId = getRequestString(REQ_BOOK_ID);
        String sys = getClientSys();

        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(requestBookId);
        if (newBookProfile == null)
            return failMessage(RES_RESULT_BOOK_UPGRADING);

        Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(newBookProfile);

        Map<String, Object> bookMap = generateBookDetailMapForPicListen(newBookProfile, bookNeedPay, true);

        //兼容安卓系统的问题,点击进入就算使用了
        if ("android".equalsIgnoreCase(sys)) {
            Long studentId = getRequestLong(REQ_STUDENT_ID);
            if (studentId != 0) {
                //磨耳朵活动
                if (GrindEarActivity.isInActivityPeriod()) {
                    if (GrindEarActivity.beforeDeadLineTime()) {
                        Date current = new Date();
                        grindEarService.pushTodayRecord(studentId, current);
                    }
                }
                //成长值
                actionServiceClient.clickSelfStudyEnglishPicListen(studentId);

            }
        }

        return successMessage().add(RES_USER_BOOK, bookMap).add(RES_NEED_UPGRADE, needUpgrade(bookNeedPay, getClientVersion()))
                .add(RES_PURCHASE_TEXT, "您即将进入点读机收费内容，如需体验完整内容，请购买该点读教材。")
                .add(RES_BANNERS, picListenAdMapList(newBookProfile, SelfStudyAdPosition.PIC_LISTEN_UNIT_LIST_BANNER));
    }




    private Map<String, Object> generateBookDetailMapForPicListen(NewBookProfile newBookProfile, Boolean bookNeedPay, Boolean withUnit) {
        Boolean userPayedBook;
        DayRange lastDayRange = null;
        if (bookNeedPay) {
            PicListenBookPayInfo picListenBookPayInfo = picListenCommonService.userBuyBookPicListenLastDayMap(getApiRequestUser(), false).get(newBookProfile.getId());
            lastDayRange = picListenBookPayInfo != null ? picListenBookPayInfo.getDayRange() : null;
            userPayedBook = lastDayRange != null && lastDayRange.getEndDate().after(new Date());
        } else
            userPayedBook = true; //免费的教材就当作是已付费了



        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String, Object> bookMap = convert2BookMap(newBookProfile, newBookCatalog, true, lastDayRange);
        if (withUnit) {
            addBookStructure(bookMap, bookNeedPay, userPayedBook, newBookProfile);
        }
        return bookMap;
    }

    /**
     * 语文教材和英语教材的结构不同,但返回的给壳的结构都是unitGroup->unit
     * 英语教材取的结构是 module(对应返回unitGroup)->unit(对应返回unit)
     * 语文教材取的结构是 unit(对应返回unitGroup)->lesson(对应返回unit)
     * 注意
     * 1. 取zip包,两个学科方法貌似不一样
     * 2. 取点读内容的方法也不一样。。。
     * 3. 语文是 生词表 英语是单词表。。。
     * @param bookMap
     * @param bookNeedPay
     * @param userPayedBook
     * @param newBookProfile
     */
    private void addBookStructure(Map<String, Object> bookMap, Boolean bookNeedPay, Boolean userPayedBook, NewBookProfile newBookProfile){

        boolean isChinese = newBookProfile.getSubjectId() == Subject.CHINESE.getId();
        BookCatalogType unitGroupNode = isChinese ? BookCatalogType.UNIT : BookCatalogType.MODULE;
        BookCatalogType unitNode = isChinese ? BookCatalogType.LESSON : BookCatalogType.UNIT;

        String bookId = newBookProfile.getId();
        List<NewBookCatalog> moduleList = newContentLoaderClient.loadChildrenSingle(bookId, unitGroupNode);
        Boolean unitGroupFlag = !CollectionUtils.isEmpty(moduleList);

        List<NewBookCatalog> allUnitList = newContentLoaderClient.loadChildrenSingle(bookId, unitNode);
        List<String> unitIdList = allUnitList.stream().map(NewBookCatalog::getId).collect(Collectors.toList());
        Map<String, List<PicListen>> unitId2PicListMap;
        if (CollectionUtils.isNotEmpty(unitIdList)) {
            unitId2PicListMap = isChinese ? questionLoaderClient.loadPicListenByNewLessonIds(unitIdList) : questionLoaderClient.loadPicListenByNewUnitIds(unitIdList);
        }else {
            unitId2PicListMap = new HashMap<>();
        }


        bookMap.put(RES_GROUP_FLAG, unitGroupFlag);
        if (!unitGroupFlag) {
            List<Map> unitMapList = new LinkedList<>();

            if (!CollectionUtils.isEmpty(allUnitList)) {
                int i = 0;
                for (NewBookCatalog unit : allUnitList) {
                    List<PicListen> picListens = unitId2PicListMap.get(unit.getId());
                    if (CollectionUtils.isEmpty(picListens))
                        continue;
                    unitMapList.add(picUnitMap(unit, unitStatus(bookNeedPay, userPayedBook, i == 0)));
                    i++;
                }

                bookMap.put(RES_UNIT_LIST, unitMapList);
                bookMap.put(RES_GROUP_LIST, null);
            }

        } else {
            List<Map> groupList = new LinkedList<>();
            Set<String> moduleIdList = moduleList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());
            Map<String, List<NewBookCatalog>> moduleId2UnitListMap = newContentLoaderClient
                    .loadChildren(moduleIdList, unitNode);
            if (moduleId2UnitListMap == null) {
                moduleId2UnitListMap = new LinkedHashMap<>();
            }
            int i = 0;
            for (NewBookCatalog module : moduleList) {
                Map<String, Object> groupInfo = new LinkedHashMap<>();

                String moduleId = module.getId();

                List<NewBookCatalog> unitList = moduleId2UnitListMap.get(moduleId);
                if (unitList == null)
                    unitList = new ArrayList<>();

                List<Map<String, Object>> groupUnitList = new LinkedList<>();
                int j = 0;
                for (NewBookCatalog unit : unitList) {
                    List<PicListen> picListens = unitId2PicListMap.get(unit.getId());
                    if (CollectionUtils.isEmpty(picListens))
                        continue;
                    groupUnitList.add(picUnitMap(unit, unitStatus(bookNeedPay, userPayedBook, i == 0 && j == 0)));
                    j++;
                }
                if (CollectionUtils.isEmpty(groupUnitList))
                    continue;
                groupInfo.put(RES_GROUP_CNAME, module.getName());
                groupInfo.put(RES_MODULE_GROUP_ID, module.getId());
                groupInfo.put(RES_GROUP_INFO_LIST, groupUnitList);
//                groupInfo.put(RES_PAY_STATUS, unitStatus(picListenBookNeedPay, userPayedBook, i==0));
                groupList.add(groupInfo);
                i++;

            }

            bookMap.put(RES_UNIT_LIST, null);
            bookMap.put(RES_GROUP_LIST, groupList);
        }
        bookMap.put(RES_HAS_WORD_LIST, textBookManagementLoaderClient.englishWordListShow(bookId));
        bookMap.put(RES_HAS_CHINESE_WORD_LIST, textBookManagementLoaderClient.chineseWordListShow(bookId));
    }

    private Map<String, Object> picUnitMap(NewBookCatalog unit, String unitStatus) {
        Map<String, Object> unitMap = new LinkedHashMap<>();
        addIntoMap(unitMap, RES_UNIT_ID, unit.getId());
        addIntoMap(unitMap, RES_UNIT_CNAME, unit.getName());
        addIntoMap(unitMap, RES_RANK, unit.getRank());
        addIntoMap(unitMap, RES_PAY_STATUS, unitStatus);
        return unitMap;
    }

    /**
     * 更新学生的点读默认教材
     */
    @RequestMapping(value = "/book/update.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage updateBook() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name());
            validateRequired(REQ_BOOK_ID, "教材id");
            validateRequest(REQ_SUBJECT, REQ_STUDENT_ID, REQ_BOOK_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        Long parentId = getCurrentParentId();
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        String bookId = getRequestString(REQ_BOOK_ID);
        SelfStudyType selfStudyType = SelfStudyType.PICLISTEN_ENGLISH;
        if (studentId != 0) {
            if (!checkStudentParentRef(studentId, parentId)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                return resultMap;
            }
        } else {  //如果传的学生id为0,说明没有孩子,这个时候把选的教材记到家长上
            asyncPiclistenCacheServiceClient.getAsyncPiclistenCacheService()
                    .CParentSelfStudyBookCacheManager_setParentSeflStudyBook(parentId, selfStudyType, bookId)
                    .awaitUninterruptibly();
            return successMessage();
        }

        Student student = studentLoaderClient.loadStudent(getRequestLong(REQ_STUDENT_ID));

        Subject subject = Subject.valueOf(getRequestString(REQ_SUBJECT));

        contentServiceClient.setUserSelfStudyDefaultBook(student, subject, selfStudyType.name(), bookId);
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);

    }


    /**
     * 点读专用
     * 返回学生的默认教材详情
     */
    @RequestMapping(value = "/unit/detail.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage unitPicListen() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name(), CHINESE.name());
            validateRequired(REQ_UNIT_ID, "单元id");
            validateRequest(REQ_SUBJECT, REQ_UNIT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String unitId = getRequestString(REQ_UNIT_ID);

        NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
        if (unit == null || (!BookCatalogType.UNIT.name().equals(unit.getNodeType()) && !BookCatalogType.LESSON.name().equals(unit.getNodeType()))) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
            return resultMap;
        }

//        Map<String, List<NewBookCatalog>> unti2LessonListMap = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON);
//        if(MapUtils.isEmpty(unti2LessonListMap)){
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
//            return resultMap;
//        }
//
//        List<NewBookCatalog> lessonList = unti2LessonListMap.get(unitId);
//        Set<String> lessonIds = lessonList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());
//
//        Map<String, List<PicListen>> stringListMap = questionLoaderClient.loadPicListenByNewLessonIds(lessonIds);

        //是否支持跟读
        NewBookCatalogAncestor bookCatalogAncestor = unit.getAncestors().stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(null);
        if (bookCatalogAncestor == null)
            return failMessage(RES_RESULT_BOOK_ERROR);

        List<PicListen> allPicListen ;
        if (unit.getNodeType().equals(BookCatalogType.LESSON.name()))
               allPicListen = questionLoaderClient.loadPicListenByNewLessonId(unitId);
        else
            allPicListen = questionLoaderClient.loadPicListenByNewUnitId(unitId);

        Map<String, Object> unitInfoMap = new LinkedHashMap<>();

        addIntoMap(unitInfoMap, RES_UNIT_ID, unit.getId());
        addIntoMap(unitInfoMap, RES_UNIT_CNAME, unit.getName());
        addIntoMap(unitInfoMap, RES_TOTAL_INDEX, allPicListen.size());

        List<Map<String, Object>> picListenMapList = new ArrayList<>();

        int index = 1;
        for (PicListen t : allPicListen) {
            Map<String, Object> picListenMap = new LinkedHashMap<>();
            addIntoMap(picListenMap, RES_PICLISTEN_ID, t.getId());
            addIntoMap(picListenMap, RES_PIC_URL, t.getImgUrl());
            addIntoMap(picListenMap, RES_PIC_FILENAME, t.getImgFilename());
            addIntoMap(picListenMap, RES_INDEX, index);
            addIntoMap(picListenMap, RES_BLOCK_DATA, t.getBlocksData() == null ? new ArrayList<>() : t.getBlocksData());
            picListenMapList.add(picListenMap);
            index++;
        }

        addIntoMap(unitInfoMap, RES_PICLISTEN_LIST, picListenMapList);

        List<NewBookCatalogAncestor> ancestors = unit.getAncestors();
        NewBookCatalogAncestor book = ancestors.stream().filter(t -> BookCatalogType.BOOK.name().equals(t.getNodeType())).findFirst().orElse(null);
        if (book != null) {
            List<SelfStudyAdInfo> selfStudyAdInfoList = loadSelfStudyAdConfigListByPosition(SelfStudyAdPosition.PIC_LISTEN_PLAYER_AD,
                    newContentLoaderClient.loadBook(book.getId()));
            if (!CollectionUtils.isEmpty(selfStudyAdInfoList)) {
                Map<String, Object> adMap = new LinkedHashMap<>();
                SelfStudyAdInfo selfStudyAdInfo = selfStudyAdInfoList.get(0);
                adMap.put(RES_USER_IMG_URL, selfStudyAdInfo.getImgUrl());
                adMap.put(RES_JUMP_URL, selfStudyAdInfo.getJumpUrl());
                adMap.put(RES_CONTENT, selfStudyAdInfo.getContent());
                resultMap.add(RES_BE_INFO, adMap);
            }
        }

        addIntoMap(unitInfoMap, RES_FOLLOW_READ_SUPPORT, textBookManagementLoaderClient.followReadBookSupport(bookCatalogAncestor.getId()));
        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_UNIT_INFO, unitInfoMap);
    }

    @RequestMapping(value = "/get_by_sentence_unit.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage sentencePicListenContent() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_DATA, "请求数据");
            validateRequest(REQ_DATA);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String jsonArray = getRequestString(REQ_DATA);
        List<Map> maps = JsonUtils.fromJsonToList(jsonArray, Map.class);

        NewBookProfile bookProfile = parentSelfStudyPublicHelper.getPicListenBookByOneUnit(maps);
        if (bookProfile == null || !textBookManagementLoaderClient.picListenBookShow(bookProfile.getId(), false, getRequestString(REQ_SYS)))
            return failMessage(RES_RESULT_BOOK_ERROR);


        LinkedHashMap<Long, String> sentence2PicListenIdMap = parentSelfStudyPublicHelper.getSentence2PicListenIdMap(maps);

        Map<String, PicListen> picListenMap = picListenLoaderClient.loadPicListensIncludeDisabled(sentence2PicListenIdMap.values());

        List<Map<String, Object>> picListenMapList = new ArrayList<>();

        List<Long> taskSentenceIdList = new ArrayList<>();
        int index = 1;
        Set<String> picListenIdSet = new HashSet<>();
        for (Map.Entry<Long, String> entry : sentence2PicListenIdMap.entrySet()) {
            String picListenId = entry.getValue();
            Long sentenceId = entry.getKey();
            PicListen picListen = picListenMap.get(picListenId);
            if (picListen == null) {
                continue;
            }
            taskSentenceIdList.add(sentenceId);
            if (picListenIdSet.contains(picListenId))
                continue;
            Map<String, Object> picListenResultMap = new LinkedHashMap<>();
            addIntoMap(picListenResultMap, RES_PICLISTEN_ID, picListen.getId());
            addIntoMap(picListenResultMap, RES_PIC_URL, picListen.getImgUrl());
            addIntoMap(picListenResultMap, RES_INDEX, index);
            addIntoMap(picListenResultMap, RES_BLOCK_DATA, picListen.getBlocksData() == null ? new ArrayList<>() : picListen.getBlocksData());
            picListenMapList.add(picListenResultMap);
            index++;
            picListenIdSet.add(picListenId);
        }

        Boolean bookNeedPay = textBookManagementLoaderClient.picListenBookNeedPay(bookProfile);
        if (bookNeedPay) {
            Map<String, DayRange> userBuyBookLastDayMap = picListenCommonService.parentBuyBookPicListenLastDayMap(getCurrentParentId(), false);
            DayRange dayRange = userBuyBookLastDayMap.get(bookProfile.getId());
            if (dayRange != null && dayRange.getEndDate().after(new Date()))
                bookNeedPay = false;
        }


        //是否需要sdk,以及对应的sdk的教材id
        TextBookManagement.SdkInfo sdkInfo = textBookManagementLoaderClient.picListenSdkInfo(bookProfile.getId());
        String sdkBookId = sdkInfo.getSdkBookIdV2(getClientVersion());
        String sdk = sdkInfo.getSdkType().name();
        return successMessage().add(RES_PICLISTEN_LIST, picListenMapList).add(RES_TITLE, "点读课文").add(RES_NEED_PAY, bookNeedPay)
                .add(RES_PURCHASE_TEXT, "您即将进入点读机收费内容，如需体验完整内容，请购买该点读教材。")
                .add(RES_PURCHASE_URL, payBookProductDetailPage(bookProfile.getId(), sdk, sdkBookId))
                .add(RES_TASK_SENTENCE_ID_LIST, taskSentenceIdList);
    }


    @RequestMapping(value = "/red_task/finish.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage finishTask() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_PIC_HOMEWORK_ID, "作业id");
            validateRequired(REQ_STUDENT_ID, "学生id");
            validateRequest(REQ_PIC_HOMEWORK_ID, REQ_STUDENT_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }

        String homeworkId = getRequestString(REQ_PIC_HOMEWORK_ID);
        asyncVendorCacheServiceClient.getAsyncVendorCacheService()
                .HomeworkReportPicListenTaskCacheManager_finishTask(homeworkId, getRequestLong(REQ_STUDENT_ID))
                .awaitUninterruptibly();
        return successMessage();
    }


    /**
     * 点读专用
     * 返回指定教材的单词表
     */
    @RequestMapping(value = "/book/word/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookWordList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_BOOK_ID, "教材id");
            validateRequest(REQ_BOOK_ID);
        } catch (IllegalArgumentException e) {
            if (e instanceof IllegalVendorUserException) {
                resultMap.add(RES_RESULT, ((IllegalVendorUserException) e).getCode());
                resultMap.add(RES_MESSAGE, e.getMessage());
                return resultMap;
            }
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        Boolean longTitle = VersionUtil.compareVersion(version, "1.7.5.0") >= 0;
        String bookId = getRequestString(REQ_BOOK_ID);
        String sys = getRequestString(REQ_SYS);
        NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookId);
        if (newBookProfile == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BOOK_ERROR);
            return resultMap;
        }

        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String, Object> bookMap = convert2BookMap(newBookProfile, newBookCatalog, false, null);

        Map<String, List<NewBookCatalog>> bookId2ModuleMap = newContentLoaderClient
                .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.MODULE);
        List<NewBookCatalog> moduleList = bookId2ModuleMap == null ? new ArrayList<>() : bookId2ModuleMap.get(newBookProfile.getId());
        Boolean unitGroupFlag = !CollectionUtils.isEmpty(moduleList);

        Map<String, List<NewBookCatalog>> bookId2UnitListMap = newContentLoaderClient
                .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.UNIT);
        List<NewBookCatalog> unitList = bookId2UnitListMap == null ? new ArrayList<>() : bookId2UnitListMap.get(newBookProfile.getId());
        //TODO 对这个接口做个缓存调用吧
        Map<String, List<WordStock>> unit2WordStockListMap = newEnglishContentLoaderClient.loadWordStockByUnitIds(unitList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet()));
        //如果 家长通 ios 并 unitGroupFlag = false 并且 版本小玉2.0.6.4,不返回这个字段
        if (isParentReqeust() && (unitGroupFlag || !clientIsIos() || VersionUtil.compareVersion(getClientVersion(), "2.0.6.4") > 0) ) {
            bookMap.put(RES_GROUP_FLAG, unitGroupFlag);
        }
        Map<String, Object> extras = newBookProfile.getExtras();
        String pronunciation;
        if (extras == null)
            pronunciation = null;
        else
            pronunciation = SafeConverter.toString(extras.get("pronunciation"));

        if (!unitGroupFlag) {
            List<Map> unitMapList = new LinkedList<>();

            if (!CollectionUtils.isEmpty(unitList)) {
                for (NewBookCatalog unit : unitList) {
                    List<WordStock> wordStockList = unit2WordStockListMap.get(unit.getId());
                    if (CollectionUtils.isEmpty(wordStockList))
                        continue;
                    Map<String, Object> unitMap = new LinkedHashMap<>();
                    unitMap.put(RES_UNIT_ID, unit.getId());
                    unitMap.put(RES_TITLE, longTitle ? unit.getName() : "Unit " + unit.getRank());
                    unitMap.put(RES_WORD_LIST, toWordListMap(wordStockList, pronunciation));
                    unitMapList.add(unitMap);
                }

                bookMap.put(RES_UNIT_LIST, unitMapList);
                bookMap.put(RES_GROUP_LIST, null);
            }

        } else {
            List<Map> groupList = new LinkedList<>();
            Set<String> moduleIdList = moduleList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());
            Map<String, List<NewBookCatalog>> moduleId2UnitListMap = newContentLoaderClient
                    .loadChildren(moduleIdList, BookCatalogType.UNIT);
            if (moduleId2UnitListMap == null) {
                moduleId2UnitListMap = new LinkedHashMap<>();
            }

            int i = 1;
            for (NewBookCatalog module : moduleList) {
                Map<String, Object> groupInfo = new LinkedHashMap<>();

                String moduleId = module.getId();

                List<NewBookCatalog> mUnitList = moduleId2UnitListMap.get(moduleId);
                if (mUnitList == null)
                    mUnitList = new ArrayList<>();

                List<Map<String, Object>> groupUnitList = new LinkedList<>();
                int j = 1;
                for (NewBookCatalog unit : mUnitList) {
                    List<WordStock> wordStockList = unit2WordStockListMap.get(unit.getId());
                    if (CollectionUtils.isEmpty(wordStockList))
                        continue;
                    Map<String, Object> unitGroupInfo = new LinkedHashMap<>();
                    addIntoMap(unitGroupInfo, RES_TITLE, longTitle ? unit.getName() : "Lesson " + j);
                    addIntoMap(unitGroupInfo, RES_UNIT_ID, unit.getId());
                    addIntoMap(unitGroupInfo, RES_WORD_LIST, toWordListMap(wordStockList, pronunciation));
                    groupUnitList.add(unitGroupInfo);
                    j++;
                }
                if (CollectionUtils.isEmpty(groupUnitList))
                    continue;
                groupInfo.put(RES_TITLE, longTitle ? module.getName() : "Unit " + i);
                groupInfo.put(RES_MODULE_GROUP_ID, module.getId());
                groupInfo.put(RES_GROUP_INFO_LIST, groupUnitList);
                groupList.add(groupInfo);
                i++;

            }

            bookMap.put(RES_UNIT_LIST, null);
            if (groupList.isEmpty() && "ios".equalsIgnoreCase(sys)) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put(RES_TITLE, "");
                map.put(RES_MODULE_GROUP_ID, "");
                Map<String, Object> groupInfoMap = new LinkedHashMap<>();
                groupInfoMap.put(RES_UNIT_ID, "");
                groupInfoMap.put(RES_TITLE, "");
                List<Map<String, Object>> wordList = new ArrayList<>();
                Map<String, Object> wordMap = new LinkedHashMap<>();
                wordList.add(wordMap);
                groupInfoMap.put(RES_WORD_LIST, wordList);

                List<Map<String, Object>> groupInfoList = new ArrayList<>();
                groupInfoList.add(groupInfoMap);
                map.put(RES_GROUP_INFO_LIST, groupInfoList);
                groupList.add(map);
            }

            bookMap.put(RES_GROUP_LIST, groupList);

        }
//        if (!hasContent)
//            return resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE).add(RES_MESSAGE, RES_RESULT_NO_WORD);
        List<SelfStudyAdInfo> selfStudyAdInfoList = loadSelfStudyAdConfigListByPosition(SelfStudyAdPosition.PIC_LISTEN_WORD_LIST_AD, newBookProfile);
        if (!CollectionUtils.isEmpty(selfStudyAdInfoList)) {
            Map<String, Object> adMap = new LinkedHashMap<>();
            SelfStudyAdInfo selfStudyAdInfo = selfStudyAdInfoList.get(0);
            adMap.put(RES_USER_IMG_URL, selfStudyAdInfo.getImgUrl());
            adMap.put(RES_JUMP_URL, selfStudyAdInfo.getJumpUrl());
            adMap.put(RES_CONTENT, selfStudyAdInfo.getContent());
            resultMap.add(RES_BE_INFO, adMap);
        }

        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_USER_BOOK, bookMap);
    }

    private List<Map<String, Object>> toWordListMap(List<WordStock> wordStocks, String pronunciation) {
        List<Map<String, Object>> list = new ArrayList<>();
        Set<String> wordKeySet = new HashSet<>();
        wordStocks.forEach(wordStock -> {
            String wordEnText = wordStock.getEnText();
            String key = wordEnText + "_" + wordStock.getMultiMeaning();
            if (wordKeySet.contains(key))
                return;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(RES_WORD, wordEnText);
            map.put(RES_TRANSLATION, wordStock.getCnText());
            if (pronunciation != null && "美音".equals(pronunciation)) {
                map.put(RES_PHONETIC, wordStock.getPronounceUS());
                if (StringUtils.isNoneBlank(wordStock.getAudioUS()))
                    map.put(RES_AUDIO, getCdnBaseUrlStaticSharedWithSep() + wordStock.getAudioUS());
            } else {
                map.put(RES_PHONETIC, wordStock.getPronounceUK());
                if (StringUtils.isNoneBlank(wordStock.getAudioUK()))
                    map.put(RES_AUDIO, getCdnBaseUrlStaticSharedWithSep() + wordStock.getAudioUK());
            }
            list.add(map);
            wordKeySet.add(key);
        });

        return list;
    }


}
