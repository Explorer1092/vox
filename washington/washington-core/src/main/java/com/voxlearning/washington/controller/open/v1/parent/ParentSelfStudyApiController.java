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

package com.voxlearning.washington.controller.open.v1.parent;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DayRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.action.client.ActionServiceClient;
import com.voxlearning.utopia.service.content.api.constant.BookCatalogType;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.vendor.api.MySelfStudyService;
import com.voxlearning.utopia.service.vendor.api.entity.TextBookManagement;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorCacheServiceClient;
import com.voxlearning.utopia.temp.GrindEarActivity;
import com.voxlearning.washington.controller.open.AbstractSelfStudyApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.controller.open.v1.content.AbstractContentLoaderWrapper;
import com.voxlearning.washington.controller.open.v1.content.ContentApiConstants;
import com.voxlearning.washington.controller.open.v1.content.ContentLoaderWrapperFactory;
import com.voxlearning.washington.mapper.SelfStudyAdInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.Subject.ENGLISH;
import static com.voxlearning.washington.controller.open.ApiConstants.*;
import static com.voxlearning.washington.controller.open.ApiConstants.RES_BOOK_ID;
import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * Created by jiangpeng on 16/6/30.
 * <p>
 * 把学生端的 自学接口 复制过来了.....
 */

@Controller
@RequestMapping(value = "/v1/parent")
@Slf4j
public class ParentSelfStudyApiController extends AbstractSelfStudyApiController {

    @Inject private AsyncVendorCacheServiceClient asyncVendorCacheServiceClient;

    @Inject private ActionServiceClient actionServiceClient;

    @ImportService(interfaceClass = MySelfStudyService.class)
    private MySelfStudyService mySelfStudyService;

    @Inject
    private ContentLoaderWrapperFactory contentLoaderWrapperFactory;


    private static List<String> filterLesson = Arrays.asList("语感练习", "听力练习", "语法练习");


    private String shareBookUrl = "{0}/view/mobile/parent/walkman/home.vpage?book_id={1}&app_version={2}";
    private String shareUnitUrl = "{0}/view/mobile/parent/walkman/list.vpage?book_id={1}&unit_id={2}&app_version={3}";

    private String shareBookListUrl = "{0}/view/mobile/parent/learning_tool/teach_list?self_study_type={1}&clazz_level={2}&subject={3}&app_version={4}";
    private String shareBookDetailUrl = "{0}/view/mobile/parent/learning_tool/teach_catalog?self_study_type={1}&book_id={2}&clazz_level={3}&subject={4}&app_version={5}";

    private String shareBookDetailUrlForPicLisnte = "{0}/view/wx/parent/reading/unit?book_id={1}";

    /**
     * 随声听 在课本页和单元页分享配置
     * 用到新的页面
     *
     * @return
     */
    @RequestMapping(value = "/selfstudy/share.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage shareConfig() {
        MapMessage resultMap = new MapMessage();

        try {
            if (StringUtils.isBlank(getRequestString(REQ_UNIT_ID))) {
                validateRequired(REQ_BOOK_ID, "book_id");
                validateRequest(REQ_BOOK_ID);
            } else if (StringUtils.isNotBlank(getRequestString(REQ_UNIT_ID))) {
                validateRequired(REQ_BOOK_ID, "book_id");
                validateRequired(REQ_UNIT_ID, "unit_id");
                validateRequest(REQ_BOOK_ID, REQ_UNIT_ID);
            } else {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
                return resultMap;
            }
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
        String bookId = getRequestString(REQ_BOOK_ID);
        String unitId = getRequestString(REQ_UNIT_ID);
        NewBookProfile bookProfile = newContentLoaderClient.loadBooks(Collections.singleton(bookId)).get(bookId);
        Integer clazzLevel = 3;
        Subject subject = Subject.ENGLISH;
        if (bookProfile != null) {
            clazzLevel = bookProfile.getClazzLevel();
            subject = Subject.fromSubjectId(bookProfile.getSubjectId());
        }
//        点读机上了之后,原来随声听的分享也到新的h5,不过还没上,先不跳,还跳到老的.
        //点读机和语文朗读终于特么上了。。。可以分享到新的了。。。
        String url = MessageFormat.format(shareBookDetailUrl, fetchMainsiteUrlByCurrentSchema(), SelfStudyType.WALKMAN_ENGLISH.name(), bookId, clazzLevel, SelfStudyType.WALKMAN_ENGLISH.getSubject().name());

//        String url;
//        if(StringUtils.isNotBlank(unitId)){
//            url = MessageFormat.format(shareUnitUrl,fetchMainsiteUrlByCurrentSchema(),bookId,unitId);
//        }else
//            url = MessageFormat.format(shareBookUrl,fetchMainsiteUrlByCurrentSchema(),bookId);

        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_SHARE_TITLE, "我在一起学上找到了好资源—小学英语随身听");
        resultMap.add(RES_SHARE_CONTENT, "一起学，小学生成长教育的爸妈帮手");
        resultMap.add(RES_SHARE_URL, url);

        return resultMap;
    }


    /**
     * 新版的 点读,课文朗读 在课本页和单元页分享配置
     * 必 传入 分享对象 BOOK_LIST  BOOK_DETAIL UNIT_DETAIL
     * 必 传入自学类型  SelfStudyType
     * 选 传入 clazz_level,book_id,unit_id
     */
    @RequestMapping(value = "/selfstudy/share_v2.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage shareConfigV2() {
        MapMessage resultMap = new MapMessage();

        String shareTarget = getRequestString(REQ_SHARE_TARGET);
        try {
            validateRequired(REQ_SELF_STUDY_TYPE, "自学类型");
            validateRequired(REQ_SHARE_TARGET, "分享对象");
            validateEnum(REQ_SELF_STUDY_TYPE, "自学类型", SelfStudyType.WALKMAN_ENGLISH.name(),
                    SelfStudyType.PICLISTEN_ENGLISH.name(), SelfStudyType.TEXTREAD_CHINESE.name());
            validateEnum(REQ_SHARE_TARGET, "分享对象", "BOOK_LIST", "BOOK_DETAIL", "UNIT_DETAIL", "LESSON_DETAIL");
            switch (shareTarget) {
                case "BOOK_LIST":
                    validateRequiredNumber(REQ_CLAZZ_LEVEL, "年级");
                    validateEnum(REQ_CLAZZ_LEVEL, "年级", "1", "2", "3", "4", "5", "6");
                    validateRequest(REQ_SELF_STUDY_TYPE, REQ_SHARE_TARGET, REQ_CLAZZ_LEVEL);
                    break;
                case "BOOK_DETAIL":
                    validateRequired(REQ_BOOK_ID, "教材id");
                    validateRequest(REQ_SELF_STUDY_TYPE, REQ_SHARE_TARGET, REQ_BOOK_ID);
                    break;
                case "UNIT_DETAIL":
                    validateRequired(REQ_BOOK_ID, "教材id");
                    validateRequired(REQ_UNIT_ID, "单元id");
                    validateRequest(REQ_SELF_STUDY_TYPE, REQ_SHARE_TARGET, REQ_BOOK_ID, REQ_UNIT_ID);
                    break;
                case "LESSON_DETAIL":
                    validateRequired(REQ_BOOK_ID, "教材id");
                    validateRequired(RES_LESSON_ID, "课程id");
                    validateRequest(REQ_SELF_STUDY_TYPE, REQ_SHARE_TARGET, REQ_BOOK_ID, RES_LESSON_ID);
                    break;
                default:
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_BAD_REQUEST_MSG);
                    return resultMap;
            }
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
        String content = "一起学，小学生成长教育的爸妈帮手";

        String title;
        SelfStudyType selfStudyType = SelfStudyType.of(getRequestString(REQ_SELF_STUDY_TYPE));
        if (SelfStudyType.PICLISTEN_ENGLISH == selfStudyType) {
            title = "小学英语课本点读，尽在一起学";
        } else if (SelfStudyType.WALKMAN_ENGLISH == selfStudyType) {
            title = "我在一起学上找到了好资源—小学英语随身听";
        } else if (SelfStudyType.TEXTREAD_CHINESE == selfStudyType) {
            title = "小学语文同步电子课文，口袋里的语文课本";
        } else
            title = "";


        String url;
        switch (shareTarget) {
            case "BOOK_LIST":
                if (selfStudyType != SelfStudyType.PICLISTEN_ENGLISH) {
                    Integer clazzLevel = getRequestInt(REQ_CLAZZ_LEVEL);
                    url = MessageFormat.format(shareBookListUrl, fetchMainsiteUrlByCurrentSchema(), selfStudyType.name(), clazzLevel, selfStudyType.getSubject().name(), version);
                    break;
                }
            case "BOOK_DETAIL":
            case "UNIT_DETAIL":
            case "LESSON_DETAIL":
                if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {
                    if (shareTarget.equals("BOOK_LIST")) {
                        url = MessageFormat.format(shareBookDetailUrlForPicLisnte, fetchMainsiteUrlByCurrentSchema(), SelfStudyType.PICLISTEN_ENGLISH.getDefaultBookId());
                        break;
                    } else {
                        String bookId = getRequestString(REQ_BOOK_ID);
                        url = MessageFormat.format(shareBookDetailUrlForPicLisnte, fetchMainsiteUrlByCurrentSchema(), bookId);
                        break;
                    }
                } else {
                    String bookId = getRequestString(REQ_BOOK_ID);
                    Integer clazzl = 3;
                    NewBookProfile book = newContentLoaderClient.loadBooks(Collections.singleton(bookId)).get(bookId);
                    if (book != null)
                        clazzl = book.getClazzLevel();
                    url = MessageFormat.format(shareBookDetailUrl, fetchMainsiteUrlByCurrentSchema(), selfStudyType.name(), bookId, clazzl, selfStudyType.getSubject().name(), version);
                }
                break;
            default:
                url = "";
        }


        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        resultMap.add(RES_SHARE_TITLE, title);
        resultMap.add(RES_SHARE_CONTENT, content);
        resultMap.add(RES_SHARE_URL, url);
        return resultMap;
    }


    /**
     * 随声听专用
     * 返回这个学生的课本信息
     */
    @RequestMapping(value = "/selfloadbook.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage selfloadbook() {
        MapMessage resultMap = new MapMessage();

        String requestBookId = getRequestString(REQ_BOOK_ID);
        try {
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name());
            if (StringUtils.isNotBlank(requestBookId)) {
                validateRequest(REQ_SUBJECT, REQ_STUDENT_ID, REQ_BOOK_ID);
            } else
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
        NewBookProfile newBookProfile = null;
        if (StringUtils.isBlank(requestBookId)) {
            if (studentId != 0) {
                if (!checkStudentParentRef(studentId, parentId)) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                    return resultMap;
                }

                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
                if (studentDetail == null) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
                    return resultMap;
                }
                if (studentDetail.isJuniorStudent()) {
                    resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                    resultMap.add(RES_MESSAGE, RES_RESULT_CLAZZ_LEVEL_ERROR);
                    return resultMap;
                }

                newBookProfile = parentSelfStudyPublicHelper.loadDefaultSelfStudyBook(studentDetail, SelfStudyType.WALKMAN_ENGLISH, false, sys);
            } else {
                String bookId = asyncPiclistenCacheServiceClient.getAsyncPiclistenCacheService()
                        .CParentSelfStudyBookCacheManager_getParentSelfStudyBook(parentId, SelfStudyType.WALKMAN_ENGLISH)
                        .take();
                if (bookId != null) {
                    newBookProfile = newContentLoaderClient.loadBook(bookId);
                }
                if (newBookProfile == null)
                    newBookProfile = newContentLoaderClient.loadBook(SelfStudyType.WALKMAN_ENGLISH.getDefaultBookId());

            }
        } else {
            newBookProfile = newContentLoaderClient.loadBook(requestBookId);
        }
        if (newBookProfile == null) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_RESULT_BOOK_ERROR);
            return resultMap;
        }

        NewBookCatalog newBookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(newBookProfile.getSeriesId());
        Map<String, Object> bookMap = convert2BookMapForOld(newBookProfile, newBookCatalog);

        Map<String, List<NewBookCatalog>> bookId2ModuleMap = newContentLoaderClient
                .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.MODULE);
        List<NewBookCatalog> moduleList = bookId2ModuleMap == null ? new ArrayList<>() : bookId2ModuleMap.get(newBookProfile.getId());
        Boolean unitGroupFlag = !CollectionUtils.isEmpty(moduleList);

        //随身听付费
        Boolean walkManNeedPay = textBookManagementLoaderClient.walkManNeedPay(requestBookId);
        Map<String, DayRange> dayRangeMap = picListenCommonService.parentBuyWalkManLastDayMap(parentId, false);
        Boolean walkManHasPurchase = dayRangeMap.get(requestBookId) != null ? new Date().before(dayRangeMap.get(requestBookId).getEndDate()) : Boolean.FALSE;
        bookMap.put(RES_GROUP_FLAG, unitGroupFlag);
        if (walkManNeedPay) {
            bookMap.put(RES_PURCHASE_TEXT, "您即将进入随身听收费内容，如需体验完整内容，请购买该教材。");
            bookMap.put(RES_PURCHASE_URL, payWalkManProdcutDetailPage(requestBookId));
        }
        if (!unitGroupFlag) {
            List<Map> unitMapList = new LinkedList<>();
            Map<String, List<NewBookCatalog>> bookId2UnitListMap = newContentLoaderClient
                    .loadChildren(Collections.singleton(newBookProfile.getId()), BookCatalogType.UNIT);
            List<NewBookCatalog> unitList = bookId2UnitListMap == null ? new ArrayList<>() : bookId2UnitListMap.get(newBookProfile.getId());
            if (!CollectionUtils.isEmpty(unitList)) {
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitMap = new LinkedHashMap<>();
                    unitMap.put(RES_UNIT_ID, unit.getId());
                    unitMap.put(RES_RANK, unit.getRank());
                    unitMap.put(RES_UNIT_CNAME, unit.getName());
                    unitMap.put(RES_UNIT_ENAME, unit.getAlias());
                    unitMap.put(RES_PAY_STATUS, unitStatus(walkManNeedPay, walkManHasPurchase, unitList.indexOf(unit) == 0));
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

            for (NewBookCatalog module : moduleList) {
                Map<String, Object> groupInfo = new LinkedHashMap<>();

                String moduleId = module.getId();
                List<NewBookCatalog> unitList = moduleId2UnitListMap.get(moduleId);
                if (unitList == null)
                    unitList = new ArrayList<>();

                List<Map<String, Object>> groupUnitList = new LinkedList<>();
                for (NewBookCatalog unit : unitList) {
                    Map<String, Object> unitGroupInfo = new LinkedHashMap<>();
                    addIntoMap(unitGroupInfo, RES_UNIT_ID, unit.getId());
                    addIntoMap(unitGroupInfo, RES_UNIT_CNAME, unit.getName());
                    addIntoMap(unitGroupInfo, RES_UNIT_ENAME, unit.getAlias());
                    addIntoMap(unitGroupInfo, RES_RANK, unit.getRank());
                    addIntoMap(unitGroupInfo, RES_PAY_STATUS, unitStatus(walkManNeedPay, walkManHasPurchase, unitList.indexOf(unit) == 0 && moduleList.indexOf(module) == 0));
                    groupUnitList.add(unitGroupInfo);
                }

                groupInfo.put(RES_GROUP_CNAME, module.getName());
                groupInfo.put(RES_GROUP_ENAME, module.getAlias());
                groupInfo.put(RES_GROUP_INFO_LIST, groupUnitList);
                groupInfo.put(RES_PAY_STATUS, unitStatus(walkManNeedPay, walkManHasPurchase, moduleList.indexOf(module) == 0));
                groupList.add(groupInfo);

            }

            bookMap.put(RES_UNIT_LIST, null);
            bookMap.put(RES_GROUP_LIST, groupList);
        }

        List<SelfStudyAdInfo> selfStudyAdInfoList = loadSelfStudyAdConfigListByPosition(SelfStudyAdPosition.WALKMAN_UNIT_LIST_BANNER, newBookProfile);
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

        Boolean needPay = false;
        if (MapUtils.isNotEmpty(newBookProfile.getExtras())) { //need_upgrade
            needPay = textBookManagementLoaderClient.walkManNeedPay(newBookProfile.getId());
        }


        return resultMap.add(RES_RESULT, RES_RESULT_SUCCESS).add(RES_USER_BOOK, bookMap).add(RES_BANNERS, bannerMapList).add(RES_NEED_UPGRADE, needUpgrade(needPay, getRequestString(REQ_APP_NATIVE_VERSION)));

    }


    protected Map<String, Object> convert2BookMapForOld(NewBookProfile bookProfile, NewBookCatalog newBookCatalog) {
        Map<String, Object> map = new LinkedHashMap<>();
        addIntoMap(map, ContentApiConstants.RES_BOOK_ID, bookProfile.getId());
        addIntoMap(map, RES_BOOK_CNAME, bookProfile.getName());
        addIntoMap(map, RES_BOOK_ENAME, bookProfile.getAlias());
        addIntoMap(map, RES_SUBJECT, Subject.fromSubjectId(bookProfile.getSubjectId()).name());
        addIntoMap(map, RES_CLAZZ_LEVEL, bookProfile.getClazzLevel());
        addIntoMap(map, RES_CLAZZ_LEVEL_NAME, ClazzLevel.parse(bookProfile.getClazzLevel()).getDescription());
        addIntoMap(map, RES_BOOK_TERM, bookProfile.getTermType());
        addIntoMap(map, RES_BOOK_COVER_URL, StringUtils.isBlank(bookProfile.getImgUrl()) ? "" : getCdnBaseUrlStaticSharedWithSep() + bookProfile.getImgUrl());

        if (newBookCatalog != null) {
            BookPress bookPress = BookPress.getBySubjectAndPress(Subject.fromSubjectId(bookProfile.getSubjectId()), newBookCatalog.getName());
            if (bookPress != null) {
                addIntoMap(map, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
                addIntoMap(map, RES_BOOK_COLOR, bookPress.getColor());
                addIntoMap(map, RES_BOOK_IMAGE, MessageFormat.format(bookImgUrlPrefix, bookPress.getColor()));
            }
        }
        return map;
    }


    /**
     * 随声听专用
     * 返回这个学生的课本信息
     */
    @RequestMapping(value = "/selfloadbook_old.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public MapMessage selfloadbookOld() {
        MapMessage resultMap = new MapMessage();

        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name());
            validateRequiredNumber(REQ_STUDENT_ID, " 学生id");
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

        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long parentId = getCurrentParentId();

//        Boolean isRightRef = studentIsParentChildren(parentId, studentId);
//        if (!isRightRef) {
//            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//            resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
//            return resultMap;
//        }

        StudentDetail student = studentLoaderClient.loadStudentDetail(studentId);

        Subject subject = Subject.valueOf(getRequestString(REQ_SUBJECT));
        List<Map<String, Object>> books = businessStudentServiceClient.getStudentSelfStudyDefaultBooks(student);

        // KEY名转换
        List<Map<String, Object>> retBooks = new ArrayList<>();
        for (Map<String, Object> item : books) {
            Map<String, Object> bookInfo = new LinkedHashMap<>();
            if (item.get("bookSubject") == subject) {
                AbstractContentLoaderWrapper loaderWrapper = contentLoaderWrapperFactory.getContentLoaderWrapper(subject.name().toLowerCase());

                Book book = (Book) loaderWrapper.loadBook((Long) item.get("bookId"));
                List<Unit> unitList = loaderWrapper.loadBookUnitList((Long) item.get("bookId"));

                bookInfo.put(RES_BOOK_SUBJECT, item.get("bookSubject"));
                bookInfo.put(RES_BOOK_ID, item.get("bookId"));
                bookInfo.put(RES_BOOK_IS_PICLISTEN, item.get("isPicListen"));
                bookInfo.put(RES_BOOK_CNAME, book.getCname());
                bookInfo.put(RES_BOOK_ENAME, book.getEname());
                bookInfo.put(RES_BOOK_CLASS_LEVEL, item.get("classLevel"));
                bookInfo.put(RES_BOOK_COLOR, item.get("color"));
                bookInfo.put(RES_BOOK_VIEW_CONTENT, item.get("viewContent"));

                if (book.getBookStructure() == 3) {
                    List<Map> unitMapList = new LinkedList<>();
                    for (Unit unit : unitList) {
                        Map<String, Object> unitMap = new LinkedHashMap<>();
                        unitMap.put(RES_UNIT_ID, unit.getId());
                        unitMap.put(RES_RANK, unit.getRank());
                        unitMap.put(RES_UNIT_CNAME, unit.getCname());
                        unitMap.put(RES_UNIT_ENAME, unit.getEname());
                        unitMapList.add(unitMap);
                    }
                    bookInfo.put(RES_UNIT_LIST, unitMapList);
                    bookInfo.put(RES_GROUP_FLAG, false);
                    bookInfo.put(RES_GROUP_LIST, null);
                } else {
                    bookInfo.put(RES_UNIT_LIST, null);
                    bookInfo.put(RES_GROUP_FLAG, true);
                    List<Map> unitGroupList = new LinkedList<>();
                    Map<String, Object> groupInfo = new LinkedHashMap<>();
                    List<Map> groupInfoList = new LinkedList<>();
                    for (Unit unit : unitList) {

                        Map<String, Object> unitGroupInfo = new LinkedHashMap<>();
                        addIntoMap(unitGroupInfo, RES_UNIT_ID, unit.getId());
                        addIntoMap(unitGroupInfo, RES_UNIT_CNAME, unit.getCname());
                        addIntoMap(unitGroupInfo, RES_UNIT_ENAME, unit.getEname());
                        addIntoMap(unitGroupInfo, RES_RANK, unit.getRank());
                        addIntoMap(unitGroupInfo, RES_GROUP_CNAME, unit.getGroupCname());
                        addIntoMap(unitGroupInfo, RES_GROUP_ENAME, unit.getGroupEname());

                        if (groupInfo.get(RES_GROUP_CNAME) == null) {
                            groupInfo.put(RES_GROUP_CNAME, unit.getGroupCname());
                            groupInfo.put(RES_GROUP_ENAME, unit.getGroupEname());
                            groupInfoList.add(unitGroupInfo);
                            groupInfo.put(RES_GROUP_INFO_LIST, groupInfoList);
                        } else {

                            if (groupInfo.get(RES_GROUP_CNAME).equals(unit.getGroupCname())) {
                                groupInfoList.add(unitGroupInfo);
                                groupInfo.put(RES_GROUP_INFO_LIST, groupInfoList);
                            } else {
                                unitGroupList.add(groupInfo);
                                groupInfo = new LinkedHashMap<>();
                                groupInfoList = new LinkedList<>();
                                groupInfo.put(RES_GROUP_CNAME, unit.getGroupCname());
                                groupInfo.put(RES_GROUP_ENAME, unit.getGroupEname());
                                groupInfoList.add(unitGroupInfo);
                                groupInfo.put(RES_GROUP_INFO_LIST, groupInfoList);
                            }
                        }
                    }
                    if (!groupInfo.isEmpty()) {
                        unitGroupList.add(groupInfo);
                    }
                    bookInfo.put(RES_GROUP_LIST, unitGroupList);
                }

                retBooks.add(bookInfo);
            }
        }

        if (CollectionUtils.isEmpty(retBooks)) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, RES_STUDENT_CLAZZ_ERROR_MSG);
        } else {
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            resultMap.add(RES_USER_BOOK, retBooks.get(0));
        }
        return resultMap;
    }

    /**
     * 随声听专用
     * 返回课本的单元信息
     */
    @RequestMapping(value = "/selfloadurl.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage getUnitInfo() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SUBJECT, "科目");
            validateEnum(REQ_SUBJECT, "科目", ENGLISH.name());
            validateRequired(REQ_UNIT_ID, "单元ID");
            validateRequest(REQ_STUDENT_ID, REQ_SUBJECT, REQ_UNIT_ID);
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
        String unitId = getRequestString(REQ_UNIT_ID);
        Long studentId = getRequestLong(REQ_STUDENT_ID);
        Long parentId = getCurrentParentId();

        if (studentId != 0) {
//            Boolean isRightRef = studentIsParentChildren(parentId, studentId);
//            if (!isRightRef) {
//                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
//                resultMap.add(RES_MESSAGE, RES_RESULT_WRONG_STUDENT_USER_ID_MSG);
//                return resultMap;
//            }
        }

        String ver = getRequestString(REQ_APP_NATIVE_VERSION);

        if (subject == Subject.ENGLISH) {

            NewBookCatalog unit = newContentLoaderClient.loadBookCatalogByCatalogId(unitId);
            if (unit == null || !BookCatalogType.UNIT.name().equals(unit.getNodeType())) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
                return resultMap;
            }

            Map<String, List<NewBookCatalog>> unti2LessonListMap = newContentLoaderClient.loadChildren(Collections.singleton(unitId), BookCatalogType.LESSON);
            if (MapUtils.isEmpty(unti2LessonListMap)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
                return resultMap;
            }

            List<NewBookCatalog> lessonList = unti2LessonListMap.get(unitId);
            if (CollectionUtils.isEmpty(lessonList)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
                return resultMap;
            }
            Set<String> lessonIds = lessonList.stream().map(NewBookCatalog::getId).collect(Collectors.toSet());

            Map<String, List<Sentence>> lesson2SentenceListMap = newEnglishContentLoaderClient.loadEnglishLessonSentences(lessonIds);

            if (MapUtils.isEmpty(lesson2SentenceListMap)) {
                resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
                resultMap.add(RES_MESSAGE, RES_RESULT_UNIT_ERROR_MSG);
                return resultMap;
            }
            Map<String, Object> unitInfo = new LinkedHashMap<>();
            List<Map<String, Object>> lessonInfo = new LinkedList<>();

            for (NewBookCatalog lesson : lessonList) {
                List<Map> sentenceInfo = new LinkedList<>();

                if (!lesson2SentenceListMap.containsKey(lesson.getId())) {
                    continue;
                }
                List<Sentence> sentencesList = lesson2SentenceListMap.get(lesson.getId());
                for (Sentence sentence : sentencesList) {
                    if (StringUtils.isNotBlank(sentence.getWaveUri())) {
                        Map<String, Object> sentenceInfoMap = new LinkedHashMap<>();
                        sentenceInfoMap.put(RES_SENTENCE_ID, sentence.getId());
                        sentenceInfoMap.put(RES_SENTENCE_CNTEXT, sentence.getCnText());
                        sentenceInfoMap.put(RES_SENTENCE_ENTEXT, sentence.getEnText());
                        String url = getCdnBaseUrlStaticSharedWithSep() + sentence.getWaveUri();
                        sentenceInfoMap.put(RES_SENTENCE_WAVE_URI, url);
                        sentenceInfo.add(sentenceInfoMap);
                    }
                }
                if (sentenceInfo.size() > 0) {
                    if (containsNeedFilterLessons(lesson.getName())) {
                        continue;
                    }
                    Map<String, Object> lessonInfoMap = new LinkedHashMap<>();
                    lessonInfoMap.put(RES_LESSON_CNAME, lesson.getName());
                    lessonInfoMap.put(RES_LESSON_ENAME, lesson.getAlias());
                    lessonInfoMap.put(RES_LESSON_ID, lesson.getId());
                    lessonInfoMap.put(RES_SENTENCE_LIST, sentenceInfo);

                    lessonInfo.add(lessonInfoMap);
                }

            }

            unitInfo.put(RES_UNIT_CNAME, unit.getName());
            unitInfo.put(RES_UNIT_ENAME, unit.getAlias());
            unitInfo.put(RES_GROUP_CNAME, "xxx1232xxx");
            unitInfo.put(RES_GROUP_ENAME, "xxx13213xxxxE");
            unitInfo.put(RES_RANK, unit.getRank());
            unitInfo.put(RES_LESSON_LIST, lessonInfo);

            resultMap.add(RES_UNIT, unitInfo);
            resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
            return resultMap;
        } else {
            resultMap.add(RES_RESULT, "Subject is wrong.");
            return resultMap;
        }
    }


    /**
     * 返回教材列表 ,按照出版社分组
     * 支持3个自学工具调用
     */
    @RequestMapping(value = "/selfstudy/book/list.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage bookList() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_SELF_STUDY_TYPE, "自学类型");
            validateEnum(REQ_SELF_STUDY_TYPE, "自学类型", SelfStudyType.WALKMAN_ENGLISH.name(), SelfStudyType.PICLISTEN_ENGLISH.name()
                    , SelfStudyType.TEXTREAD_CHINESE.name());
            validateRequiredNumber(REQ_CLAZZ_LEVEL, "年级");
            if (StringUtils.isNotBlank(getRequestString("is_preview"))) {
                validateRequest(REQ_CLAZZ_LEVEL, REQ_SELF_STUDY_TYPE, "is_preview");
            } else
                validateRequest(REQ_CLAZZ_LEVEL, REQ_SELF_STUDY_TYPE);
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
        ClazzLevel level = ClazzLevel.parse(getRequestInt(REQ_CLAZZ_LEVEL));
        SelfStudyType selfStudyType = SelfStudyType.of(getRequestString(REQ_SELF_STUDY_TYPE));
        List<TextBookManagement> textBookManagements = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(selfStudyType.getSubject(), level.getLevel());
        if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) { //点读的话,再去纳米盒子的教材, 还要取语文点读教材。。。
            if (CollectionUtils.isEmpty(textBookManagements))
                textBookManagements = new ArrayList<>();
            List<TextBookManagement> textBookManagements1 = textBookManagementLoaderClient.getTextBookManagementBySubjectClazzLevel(Subject.CHINESE, level.getLevel());
            if (CollectionUtils.isNotEmpty(textBookManagements1)) {
                textBookManagements.addAll(textBookManagements1);
            }
        }
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        if (CollectionUtils.isEmpty(textBookManagements))
            return resultMap.add(RES_BOOK_LIST, new ArrayList<>());

        //如果是需要load出预览点读教材,则需要判断灰度用户,只有灰度内用户方可load预览教材
        Boolean isPreview = false;
        if (SelfStudyType.PICLISTEN_ENGLISH == selfStudyType) {
            isPreview = getRequestBool("is_preview");
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
        }
        Boolean parentIsAuth = false;

        // http://project.17zuoye.net/redmine/issues/41499 认证家长上线指定教材
        if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {
            parentIsAuth = picListenCommonService.userIsAuthForPicListen(getApiRequestUser());
        }

        final Boolean finalIsPreview = isPreview;
        String sys = getRequestString(REQ_SYS);
        //过滤出支持点读的教材
        //有可能要的是需要点读预览功能的教材
        Boolean finalParentIsAuth = parentIsAuth;
        Set<String> bookIds = textBookManagements.stream().map(TextBookManagement::getBookId).collect(Collectors.toSet());
        Map<String, NewBookProfile> bookProfileMap = newContentLoaderClient.loadBooks(bookIds);
        List<NewBookProfile> newBookProfileList = new ArrayList<>(bookProfileMap.values());


        newBookProfileList = newBookProfileList.stream().filter(t -> {
            Map<String, Object> extraMap = t.getExtras();
            if (MapUtils.isEmpty(extraMap))
                return false;
            switch (selfStudyType) {
                case PICLISTEN_ENGLISH:
                    Boolean picListenBookShow = textBookManagementLoaderClient.picListenBookShow(t.getId(), finalIsPreview, sys);
                    if (!picListenBookShow) {
                        return finalParentIsAuth && textBookManagementLoaderClient.picListenBookAuthOnline(t.getId());
                    } else
                        return true;
                case TEXTREAD_CHINESE:
                    return textBookManagementLoaderClient.textReadBookShow(t.getId(), sys);
                case WALKMAN_ENGLISH:
                    return textBookManagementLoaderClient.walkManBookShow(t.getId(), sys);
                default:
                    break;
            }
            return false;
        }).collect(Collectors.toList());


        Map<String, DayRange> bookId2PayEndDateMap = picListenCommonService.parentBuyBookPicListenLastDayMap(getCurrentParentId(), false);

        List<String> seriesIdList = new ArrayList<>();
        Map<Integer, String> publisherOrderMap = new HashMap<>();
        Map<String, List<NewBookProfile>> publisher2BookListMap = new HashMap<>();
        for (NewBookProfile newBookProfile : newBookProfileList) {
            String publisherName = !StringUtils.isBlank(newBookProfile.getShortPublisher()) ? newBookProfile.getShortPublisher() : "其他";
            seriesIdList.add(newBookProfile.getSeriesId());
            int publisherRank = SafeConverter.toInt(newBookProfile.getPublisherRank(), 9999);
            publisherOrderMap.put(publisherRank, publisherName);
            List<NewBookProfile> newBookProfiles = publisher2BookListMap.get(publisherName);
            if (newBookProfiles == null)
                newBookProfiles = new ArrayList<>();
            newBookProfiles.add(newBookProfile);
            publisher2BookListMap.put(publisherName, newBookProfiles);
        }

        List<Integer> sortOrderKey = publisherOrderMap.keySet().stream().sorted(Integer::compareTo).collect(Collectors.toList());
        Map<String, NewBookCatalog> stringNewBookCatalogMap = newContentLoaderClient.loadBookCatalogByCatalogIds(seriesIdList);

        List<Map<String, Object>> publisherResultMap = new ArrayList<>();
        for (Integer order : sortOrderKey) {
            String publishName = publisherOrderMap.get(order);
            if (StringUtils.isBlank(publishName)) {
                continue;
            }
            Map<String, Object> publisherMap = new LinkedHashMap<>();
            List<NewBookProfile> newBookProfiles = publisher2BookListMap.get(publishName);
            if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {
                if (publishName.equals("人教版"))
                    publishName = publishName + "（英语+语文）";
            }
            publisherMap.put(RES_PUBLISHER_NAME, publishName);
            List<Map<String, Object>> bookMapList = new ArrayList<>();
            for (NewBookProfile bookProfile : newBookProfiles) {
                NewBookCatalog newBookCatalog = stringNewBookCatalogMap.get(bookProfile.getSeriesId());
                bookMapList.add(convert2BookMap(bookProfile, newBookCatalog, selfStudyType == SelfStudyType.PICLISTEN_ENGLISH, bookId2PayEndDateMap.get(bookProfile.getId())));
            }
            publisherMap.put(RES_BOOK_LIST, bookMapList);
            publisherResultMap.add(publisherMap);
        }


        resultMap.add(RES_BOOK_MAP, publisherResultMap);
        return resultMap;
    }

    private void logEvent(Long studentId, SelfStudyType selfStudyType, Long parentId) {
        com.voxlearning.alps.spi.bootstrap.LogCollector.info("self_study_event_log",
                MiscUtils.map(
                        "studentId", studentId == null ? "" : studentId,
                        "sst", selfStudyType == null ? "" : selfStudyType.name(),
                        "parentId", parentId == null ? "" : parentId,
                        "env", RuntimeMode.getCurrentStage(),
                        "time", com.voxlearning.alps.calendar.DateUtils.dateToString(new Date()),
                        "sys", getRequestString(REQ_SYS),
                        "native_version", getRequestString(REQ_APP_NATIVE_VERSION)
                ));
    }

    @RequestMapping(value = "/selfstudy/event.vpage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public MapMessage clickAction() {
        MapMessage resultMap = new MapMessage();
        String multipleJson = getRequestString(REQ_MULTIPLE);
        try {
            if (StringUtils.isNotBlank(multipleJson)) {
                validateRequest(REQ_MULTIPLE);
            } else {
                validateRequired(REQ_STUDENT_ID, "学生id");
                validateRequiredNumber(REQ_STUDENT_ID);
                validateRequired(REQ_SELF_STUDY_TYPE, "自学类型");
                validateEnum(REQ_SELF_STUDY_TYPE, "自学类型", SelfStudyType.WALKMAN_ENGLISH.name(), SelfStudyType.PICLISTEN_ENGLISH.name(), SelfStudyType.TEXTREAD_CHINESE.name());
                if (StringUtils.isNotBlank(getRequestString(REQ_PROGRESS_ID))) {
                    validateRequest(REQ_STUDENT_ID, REQ_SELF_STUDY_TYPE, REQ_PROGRESS_ID);
                } else
                    validateRequest(REQ_STUDENT_ID, REQ_SELF_STUDY_TYPE);
            }

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
        if (StringUtils.isNotBlank(multipleJson)) {
            List<SelfStudyEvent> events = fromJson(multipleJson);
            events.sort(Comparator.comparing(SelfStudyEvent::getDateTime));
            events.forEach(t -> {
                t.setParentId(parentId);
                dealEvent(t);
            });
        } else {
            SelfStudyEvent event = new SelfStudyEvent();
            event.setStudentId(getRequestLong(REQ_STUDENT_ID));
            event.setDateTime(new Date());
            event.setSelfStudyType(SelfStudyType.valueOf(getRequestString(REQ_SELF_STUDY_TYPE)));
            event.setProgressId(getRequestString(REQ_PROGRESS_ID));
            event.setParentId(parentId);
            dealEvent(event);
        }

        return successMessage();
    }

    private void dealEvent(SelfStudyEvent selfStudyEvent) {
        if (selfStudyEvent == null || selfStudyEvent.getSelfStudyType() == SelfStudyType.UNKNOWN || !DayRange.current().contains(selfStudyEvent.getDateTime()))
            return;
        SelfStudyType selfStudyType = selfStudyEvent.getSelfStudyType();
        Long studentId = selfStudyEvent.getStudentId();
        logEvent(studentId, selfStudyType, getCurrentParentId());
        switch (selfStudyType) {
            case WALKMAN_ENGLISH:
                actionServiceClient.startSelfStudyEnglishWalkman(studentId);
                break;
            case PICLISTEN_ENGLISH:
                actionServiceClient.clickSelfStudyEnglishPicListen(studentId);
                break;
            case TEXTREAD_CHINESE:
                actionServiceClient.saveSelfStudyChineseTextRead(studentId);
                break;
            default:
                break;
        }
        String progressId = selfStudyEvent.getProgressId();
        if (StringUtils.isNotBlank(progressId)) {
            String progress = generateProgress(progressId, selfStudyType);
            if (StringUtils.isNotBlank(progress)) {
                mySelfStudyService.updateSelfStudyProgress(studentId, selfStudyType, progress);
            }
        }
        Date dateTime = selfStudyEvent.getDateTime();
        //点读磨耳朵活动 start
        if (selfStudyType == SelfStudyType.PICLISTEN_ENGLISH) {

            if (GrindEarActivity.isInActivityPeriod(dateTime)) {
                //当前时间在上午9点05之前
                if (GrindEarActivity.beforeDeadLineTime(dateTime)) {
                    grindEarService.pushTodayRecord(studentId, dateTime);
                }
            }
        }
        //点读磨耳朵活动 end
    }


    private static List<SelfStudyEvent> fromJson(String json) {
        List<Map> maps = JsonUtils.fromJsonToList(json, Map.class);
        List<SelfStudyEvent> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(maps)) {
            for (Map map : maps) {
                Date date = new Date(SafeConverter.toLong(map.get("time")));
                if (!DayRange.current().contains(date))
                    continue;
                SelfStudyEvent event = new SelfStudyEvent();
                event.setDateTime(date);
                Long sid = SafeConverter.toLong(map.get("sid"));
                event.setStudentId(sid);

                String studyType = SafeConverter.toString(map.get("self_study_type"));
                event.setSelfStudyType(SelfStudyType.valueOf(studyType));

                event.setProgressId(SafeConverter.toString(map.get("progress_id")));

                list.add(event);
            }
        }
        return list;
    }

    @Data
    public static class SelfStudyEvent {
        private Long parentId;
        private Long studentId;
        private SelfStudyType selfStudyType;
        private String progressId;
        private Date dateTime;
    }


    private String generateProgress(String progressId, SelfStudyType selfStudyType) {
        if (SelfStudyType.TEXTREAD_CHINESE == selfStudyType) {
            NewBookCatalog lesson = newContentLoaderClient.loadBookCatalogByCatalogId(progressId);
            if (lesson == null || !lesson.getNodeType().equals(BookCatalogType.LESSON.name()))
                return null;
            NewBookCatalogAncestor bookNode = lesson.getAncestors().stream().filter(t -> StringUtils.equals(t.getNodeType(), BookCatalogType.BOOK.name())).findFirst().orElse(null);
            if (bookNode == null)
                return null;
            NewBookProfile newBookProfile = newContentLoaderClient.loadBook(bookNode.getId());
            if (newBookProfile == null)
                return null;
            return newBookProfile.getName() + " " + lesson.getName();
        } else {
            NewBookCatalog bookCatalog = newContentLoaderClient.loadBookCatalogByCatalogId(progressId);
            if (bookCatalog == null)
                return null;

            if (bookCatalog.getNodeType().equals(BookCatalogType.UNIT.name()) || bookCatalog.getNodeType().equals(BookCatalogType.LESSON.name())) {
                return bookCatalog.getName();
            } else if (bookCatalog.getNodeType().equals(BookCatalogType.BOOK.name())) {
                return bookCatalog.getName();
            } else
                return null;
        }
    }

    /**
     * 过滤几种（根据filterLesson定义）lesson
     */
    private boolean containsNeedFilterLessons(String lessonName) {
        if (StringUtils.isBlank(lessonName)) {
            return false;
        }
        for (String name : filterLesson) {
            if (lessonName.contains(name)) {
                return true;
            }
        }
        return false;
    }

}
